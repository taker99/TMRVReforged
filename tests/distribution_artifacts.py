from pathlib import Path
from zipfile import ZipFile


root = Path(__file__).resolve().parents[1]
version = next(line.split("=", 1)[1].strip() for line in
               (root / "gradle.properties").read_text().splitlines()
               if line.startswith("mod_version ="))
assert (root / "CHANGELOG.md").read_text().startswith(f"## {version}\n")
required = {
    "LICENSE", "ATTRIBUTION.md", "DISTRIBUTION.md", "gradle.properties",
    "build.gradle.kts", "settings.gradle.kts", "stonecutter.gradle.kts",
    "gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.jar",
    "jei-api/build.gradle.kts", "versions/20.1-lexforge/gradle.properties",
    "versions/21.1-neoforge/gradle.properties",
}
for target, mc, metadata in (
    ("20.1-lexforge", "20.1", "META-INF/mods.toml"),
    ("21.1-neoforge", "21.1", "META-INF/neoforge.mods.toml"),
):
    base = root / "versions" / target / "build/libs"
    name = f"TMRVReforged-{version}+mc.{mc}"
    with ZipFile(base / f"{name}-source.zip") as archive:
        names = set(archive.namelist())
        assert required <= names, required - names
        assert not any(part in {"build", ".git", ".gradle", "run"}
                       for name in names for part in Path(name).parts)
        for source_path in names:
            if not source_path.endswith("/"):
                assert archive.read(source_path) == (root / source_path).read_bytes(), source_path
    for suffix in (".jar", "-sources.jar"):
        with ZipFile(base / f"{name}{suffix}") as archive:
            for notice in ("ATTRIBUTION.md", "DISTRIBUTION.md",
                           "LICENSE_toomanyrecipeviewers", "LICENSE_emi", "LICENSE_jei"):
                assert notice in archive.namelist(), notice
            if suffix == ".jar":
                text = archive.read(metadata).decode()
                assert 'license = "OSL-3.0"' in text
                assert f'version = "{version}+mc.{mc}"' in text
                assert 'displayName = "TMRVReforged"' in text
                assert 'https://github.com/taker99/TMRVReforged' in text
    print(f"PASS: {target} binary metadata, notices, and matching project source archive")
