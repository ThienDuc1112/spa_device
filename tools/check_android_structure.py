"""Check the requested Android paths and enforce the new dependency boundaries."""
from pathlib import Path
import re

workspace = Path(__file__).resolve().parents[1]
root = workspace / "pda-android"
spec = (workspace / "docs/android-target-structure.txt").read_text(encoding="utf-8")
stack, missing = [], []
checked = 0
for line in spec.splitlines():
    match = re.search(r"[\u251c\u2514]\u2500\u2500\s+(.+)", line)
    if not match:
        continue
    depth = match.start() // 4
    name = match.group(1).strip().rstrip("/")
    stack = stack[:depth]
    target = root.joinpath(*stack, name)
    if not target.exists():
        missing.append(str(target.relative_to(workspace)))
    stack.append(name)
    checked += 1
assert not missing, "Missing requested paths:\n" + "\n".join(missing)

for source in root.rglob("*.java"):
    if "build" in source.relative_to(root).parts:
        continue
    text = source.read_text(encoding="utf-8")
    package = re.search(r"^package ([\w.]+);", text, re.MULTILINE)
    assert package, f"Missing package: {source}"
    assert source.parent.as_posix().endswith(package.group(1).replace(".", "/")), source
    if "app/src/main/java/com/company/pda/domain/" in source.as_posix():
        assert not re.search(r"import (android|androidx|retrofit2|com\.google|com\.company\.pda\.(data|di|presentation|infrastructure))", text), source
    if not source.is_relative_to(root / "app"):
        assert "import com.company.pda." not in text, source
    if source.is_relative_to(root / "scanner-api") or source.is_relative_to(root / "device-api"):
        assert not re.search(r"import (android|androidx)\.", text), source

print(f"PASS: {checked} requested paths exist; packages and module/domain boundaries are consistent.")
