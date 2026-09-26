#!/usr/bin/env python3

import json
import re
import sys
from pathlib import Path


def main() -> None:
    if len(sys.argv) != 4:
        raise SystemExit("usage: prepare-web-distribution.py DIST_DIR RELEASE_ID CHANNEL")

    dist = Path(sys.argv[1])
    release_id = sys.argv[2]
    channel = sys.argv[3]
    if not re.fullmatch(r"[A-Za-z0-9._-]+", release_id):
        raise SystemExit("release ID may contain only letters, digits, dots, dashes, and underscores")
    if channel not in {"rolling", "release"}:
        raise SystemExit("channel must be rolling or release")
    if not dist.is_dir():
        raise SystemExit(f"web distribution does not exist: {dist}")

    required = {
        "index.html",
        "composeApp.js",
        "manifest.json",
        "icon-192.webp",
        "icon-512.png",
    }
    missing = required - {path.name for path in dist.iterdir() if path.is_file()}
    if missing:
        raise SystemExit(f"web distribution is missing: {', '.join(sorted(missing))}")

    manifest_path = dist / "manifest.json"
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    if manifest.get("start_url") != "./" or manifest.get("scope") != "./":
        raise SystemExit("web manifest must use relative start_url and scope")
    index_path = dist / "index.html"
    index = index_path.read_text(encoding="utf-8")
    release_title = "<title>Приложение МАИ</title>"
    rolling_title = "<title>Приложение МАИ — Rolling</title>"
    current_titles = [title for title in (release_title, rolling_title) if title in index]
    if len(current_titles) != 1 or index.count(current_titles[0]) != 1:
        raise SystemExit("web index does not contain the expected title")
    if 'href="manifest.json"' not in index or 'register("./sw.js")' not in index:
        raise SystemExit("web index does not link the manifest and service worker")

    manifest["name"] = "Приложение МАИ (Rolling)" if channel == "rolling" else "Приложение МАИ"
    manifest["short_name"] = "МАИ Rolling" if channel == "rolling" else "МАИ"
    target_title = rolling_title if channel == "rolling" else release_title
    index = index.replace(current_titles[0], target_title)
    manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    index_path.write_text(index, encoding="utf-8")

    assets = sorted(
        "./" + path.relative_to(dist).as_posix()
        for path in dist.rglob("*")
        if path.is_file()
        and path.name != "sw.js"
        and not path.name.endswith((".map", ".LICENSE.txt"))
    )
    precache = json.dumps(["./", *assets], ensure_ascii=False)
    cache_name = json.dumps(f"maiapp-web-{release_id}")

    worker = f"""const CACHE_NAME = {cache_name};
const APP_ROOT = new URL('./', self.location).href;
const PRECACHE = {precache};

self.addEventListener('install', (event) => {{
    event.waitUntil(caches.open(CACHE_NAME).then((cache) => cache.addAll(PRECACHE)));
}});

self.addEventListener('activate', (event) => {{
    event.waitUntil((async () => {{
        const names = await caches.keys();
        await Promise.all(names.filter((name) => name.startsWith('maiapp-web-') && name !== CACHE_NAME)
            .map((name) => caches.delete(name)));
        await self.clients.claim();
    }})());
}});

self.addEventListener('fetch', (event) => {{
    const request = event.request;
    if (request.method !== 'GET' || !request.url.startsWith(APP_ROOT)) return;

    event.respondWith((async () => {{
        const cache = await caches.open(CACHE_NAME);
        const cached = await cache.match(request);
        if (cached) return cached;

        try {{
            const response = await fetch(request);
            if (response.ok) await cache.put(request, response.clone());
            return response;
        }} catch (error) {{
            if (request.mode === 'navigate') {{
                const shell = await cache.match('./index.html');
                if (shell) return shell;
            }}
            throw error;
        }}
    }})());
}});
"""

    (dist / "sw.js").write_text(worker, encoding="utf-8")


if __name__ == "__main__":
    main()
