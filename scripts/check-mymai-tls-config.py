#!/usr/bin/env python3
"""Check the duplicated native TLS settings and CA without a network or SDK."""
import base64
import hashlib
import plistlib
import re
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(__file__).resolve().parents[1]
policy = (root / "shared/src/commonMain/kotlin/MyMaiTls.kt").read_text()
hosts = set(re.findall(r'"([a-z.]+\.mai\.ru)"', policy))
assert hosts == {"esia.mai.ru", "my.mai.ru"}, "Unexpected custom-CA scope"

info = plistlib.loads((root / "iosApp/iosApp/Info.plist").read_bytes())
ats = info["NSAppTransportSecurity"]
assert set(ats) == {"NSExceptionDomains"}, "Do not disable ATS globally"
assert ats["NSExceptionDomains"] == {
    host: {"NSExceptionAllowsInsecureHTTPLoads": True} for host in hosts
}, "ATS exceptions must match the exact account hosts"

android = root / "shared/src/androidMain"
manifest = ET.parse(android / "AndroidManifest.xml").getroot()
assert manifest.find("application").get(
    "{http://schemas.android.com/apk/res/android}networkSecurityConfig"
) == "@xml/mymai_network_security_config"
config = ET.parse(android / "res/xml/mymai_network_security_config.xml").getroot()
assert [child.tag for child in config] == ["domain-config"]
domain_config = config[0]
assert domain_config.get("cleartextTrafficPermitted") == "false"
assert {domain.text for domain in domain_config.findall("domain")} == hosts
assert all(domain.get("includeSubdomains") == "false" for domain in domain_config.findall("domain"))
assert {cert.get("src") for cert in domain_config.findall("trust-anchors/certificates")} == {
    "system", "@raw/russian_trusted_root_ca"
}

pem = (root / "shared/src/commonMain/kotlin/RussianTrustedRootCA.kt").read_text().split('"""')[1]
assert (android / "res/raw/russian_trusted_root_ca.pem").read_text().strip() == pem.strip()
der = base64.b64decode("".join(line for line in pem.splitlines() if not line.startswith("-----")), validate=True)
print("TLS configuration OK: exact hosts, system CAs retained, matching bundled root")
print("Root SHA-256:", hashlib.sha256(der).hexdigest())
