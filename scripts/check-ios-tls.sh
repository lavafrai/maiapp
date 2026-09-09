#!/bin/bash
# Run the Kotlin client in a simulator app so that ATS uses the production Info.plist.
set -euo pipefail
cd "$(dirname "$0")/.."
./gradlew :shared:linkDebugTestIosSimulatorArm64 --console=plain
mkdir -p build
work_dir="$(mktemp -d "$PWD/build/tls-ios.XXXXXX")"
bundle_id="ru.lavafrai.maiapp.tlscheck"
simulator="${MAI_TLS_SIMULATOR:-booted}"
cleanup() {
    xcrun simctl uninstall "$simulator" "$bundle_id" >/dev/null 2>&1 || true
    rm -rf "$work_dir"
}
trap cleanup EXIT
mkdir -p "$work_dir/TLSCheck.app"
cp shared/build/bin/iosSimulatorArm64/debugTest/test.kexe "$work_dir/TLSCheck.app/TLSCheck"
python3 - "$work_dir/TLSCheck.app/Info.plist" <<'PYTHON'
import plistlib
import sys
from pathlib import Path
info = plistlib.loads(Path("iosApp/iosApp/Info.plist").read_bytes())
info.update(CFBundleIdentifier="ru.lavafrai.maiapp.tlscheck", CFBundleExecutable="TLSCheck",
            CFBundleName="TLSCheck", CFBundlePackageType="APPL", CFBundleVersion="1",
            CFBundleShortVersionString="1.0", MinimumOSVersion="15.6")
Path(sys.argv[1]).write_bytes(plistlib.dumps(info))
PYTHON
xcrun simctl install "$simulator" "$work_dir/TLSCheck.app"
SIMCTL_CHILD_MAI_TLS_LIVE_TESTS=1 xcrun simctl launch --console "$simulator" "$bundle_id" | tee "$work_dir/result.log"
# simctl itself can return success even when the test process fails.
rg -q '\[  PASSED  \]' "$work_dir/result.log"
if rg -q '\[  FAILED  \]|Live TLS check skipped' "$work_dir/result.log"; then
    exit 1
fi
