#!/bin/bash
# shellcheck disable=SC1091,SC2154 # bsenv.sh of the image sets home_vagrant and fdroidserver
# Builds the committed HEAD of the repository at $1 the way F-Droid does and runs its checks, so a break of the
# F-Droid build (e.g. a proprietary dependency in the foss flavor) shows up before a release. The build mirrors
# the "fdroid build" job of https://gitlab.com/fdroid/fdroiddata/-/blob/master/.gitlab-ci.yml.
#
# Runs as root in registry.gitlab.com/fdroid/fdroidserver:buildserver-trixie; uncommitted changes are ignored.
# Locally (the image is amd64 only, so on Apple Silicon it's emulated and several times slower):
#   docker run --rm --platform linux/amd64 -v "$PWD":/src:ro \
#       registry.gitlab.com/fdroid/fdroidserver:buildserver-trixie bash /src/scripts/fdroid-check.sh /src
set -euo pipefail

APP_ID=ru.lavafrai.maiapp
SRC=$(realpath "${1:-.}")
git_src() { git -c safe.directory='*' -C "$SRC" "$@"; }

source /etc/profile.d/bsenv.sh
export PATH="$fdroidserver:$PATH" PYTHONPATH="$fdroidserver:$fdroidserver/examples" PYTHONUNBUFFERED=true

commit=$(git_src rev-parse HEAD)
properties=$(git_src show HEAD:gradle.properties)
version=$(sed -n 's/^maiapp\.version=//p' <<< "$properties")
version_code=$(sed -n 's/^maiapp\.versionCode=//p' <<< "$properties")
echo "Checking $APP_ID $version ($version_code) at $commit"

echo "::group::Set up fdroidserver"
apt-get update -q
apt-get install -qy sudo openjdk-21-jdk-headless >/dev/null
update-alternatives --set java /usr/lib/jvm/java-21-openjdk-amd64/bin/java
git clone -q --depth=1 https://gitlab.com/fdroid/fdroidserver.git "$fdroidserver"
git -C "$fdroidserver" log -1 --format='fdroidserver %h %ci'
echo "::endgroup::"

echo "::group::Fastlane metadata"
git_src archive HEAD fastlane | tar -x -C /tmp
python3 - "$version_code" <<'EOF'
import pathlib, sys

# Limits of https://f-droid.org/docs/All_About_Descriptions_Graphics_and_Screenshots/
limits = {"title.txt": 50, "short_description.txt": 80, "full_description.txt": 4000}
errors = []
for locale in sorted(pathlib.Path("/tmp/fastlane/metadata/android").iterdir()):
    files = [(locale / name, limit) for name, limit in limits.items()]
    files += [(changelog, 500) for changelog in (locale / "changelogs").glob("*.txt")]
    for path, limit in files:
        if path.exists() and len(text := path.read_text().strip()) > limit:
            errors.append(f"{path.relative_to('/tmp')} has {len(text)} characters, the limit is {limit}")
    if not (locale / "changelogs" / f"{sys.argv[1]}.txt").exists():
        print(f"::warning::{locale.name} has no changelogs/{sys.argv[1]}.txt, F-Droid will show the version without one")
for error in errors:
    print(f"::error::{error}")
sys.exit(1 if errors else 0)
EOF
echo "::endgroup::"

echo "::group::fdroid lint"
upstream=/tmp/fdroiddata-upstream
git clone -q --depth=1 --filter=blob:none --sparse https://gitlab.com/fdroid/fdroiddata.git "$upstream"
git -C "$upstream" sparse-checkout set config
mkdir -p "$upstream/metadata"
git_src show "HEAD:fdroid/$APP_ID.yml" > "$upstream/metadata/$APP_ID.yml"
cd "$upstream"
fdroid lint "$APP_ID"
cp "metadata/$APP_ID.yml" /tmp/recipe.yml
fdroid rewritemeta "$APP_ID"
diff -u /tmp/recipe.yml "metadata/$APP_ID.yml" ||
    { echo "::error::fdroid/$APP_ID.yml isn't formatted the way fdroid rewritemeta does it"; exit 1; }
echo "::endgroup::"

echo "::group::fdroid build"
work=/tmp/fdroiddata
mkdir -p "$work/tmp" "$home_vagrant/metadata" "$home_vagrant/.android" "$home_vagrant/.gradle"
sdkmanager "platform-tools" "build-tools;31.0.0" >/dev/null
git -C "$home_vagrant/gradlew-fdroid" pull -q
# F-Droid clones the repository from GitHub; here it's a copy of the checkout, owned by vagrant like a fresh clone
cp -a "$SRC" /srv/app
# The recipe has a single build: it's switched to this commit and to the copy
git_src show "HEAD:fdroid/$APP_ID.yml" | sed \
    -e "s|^Repo: .*|Repo: /srv/app|" \
    -e "s|^    versionName: .*|    versionName: $version|" \
    -e "s|^    versionCode: .*|    versionCode: $version_code|" \
    -e "s|^    commit: .*|    commit: $commit|" \
    > "$home_vagrant/metadata/$APP_ID.yml"
ln -s "$work/tmp" "$home_vagrant/tmp"
chown -R vagrant /srv/app "$home_vagrant" "$work"
cd "$home_vagrant"
# Like in the F-Droid CI: as vagrant with sudo, which `fdroid build --on-server` uninstalls afterwards
(unset CI; sudo --preserve-env --user vagrant env PATH="$PATH" PYTHONPATH="$PYTHONPATH" PYTHONUNBUFFERED=true \
    HOME="$home_vagrant" GRADLE_USER_HOME="$home_vagrant/.gradle" \
    fdroid build --verbose --test --refresh-scanner --on-server --no-tarball "$APP_ID:$version_code")
echo "::endgroup::"

echo "::group::fdroid scanner on the APK"
# Without --exit-code the scanner reports proprietary classes but still exits with 0
fdroid scanner --exit-code --refresh "$work/tmp/${APP_ID}_$version_code.apk"
echo "::endgroup::"

echo "F-Droid check passed: $APP_ID $version ($version_code) at $commit"
