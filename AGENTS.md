# AGENTS.md

## Projekt

Dieses Repository enthält **TooManyRecipeViewers (TMRV)**, eine clientseitige
Kompatibilitätsschicht für JEI-Plugins unter EMI.

Unterstützte Zielvarianten:

- Minecraft 1.21.1 mit NeoForge
- Minecraft 1.20.1 mit LexForge

Die Stonecutter- und `jei-api`-Struktur ist Bestandteil des Projekts und darf
nicht ohne ausdrücklichen Auftrag entfernt oder auf eine Zielversion reduziert
werden.

## Arbeitsbereich und Grenzen

- Änderungen dieses Auftrags betreffen ausschließlich dieses Repository.
- `E:\Minecraft\ModDev\BuildUpExp\TDEconemy` bleibt unverändert und dient nur
  als Strukturreferenz.
- Vor Änderungen `rtk git status --short` und relevante Diffs prüfen.
- Vorhandene fremde Änderungen niemals überschreiben, zurücksetzen oder
  mitspeichern.
- Minecraft, Server, Instanz oder GUI nicht automatisch starten.
- Keine JAR in eine Instanz kopieren und nichts veröffentlichen, sofern das
  nicht ausdrücklich beauftragt wurde.

## Shell und Git

- Jeder Shellbefehl beginnt mit `rtk`.
- Programme ohne eigenen RTK-Unterbefehl laufen über `rtk proxy`.
- Python ausschließlich über `rtk proxy python ...` oder `rtk proxy py ...`.
- Shellbefehle immer als eine physische Zeile ausführen.
- Kein `git push`.
- Kein Commit, sofern der Benutzer ihn nicht ausdrücklich beauftragt.

## Versionen und Build

- `gradle.properties` ist die Quelle für die gemeinsame Mod-Version,
  Metadaten, Abhängigkeiten und Plugin-Versionen.
- `versions/*/gradle.properties` enthält die Minecraft-, Loader-, Java-,
  Mapping-, EMI- und JEI-Werte der jeweiligen Zielvariante.
- Build- und Testabhängigkeiten dürfen keine unkommentierten, versteckten
  Versionswerte im Gradle-Script duplizieren.
- Die Mod-Version, Ressourcen-Metadaten, JAR-Namen und Publishing-Werte müssen
  aus derselben Property-Version entstehen.
- Eine reine Regel-, Dokumentations- oder Strukturänderung erhält keinen
  automatischen Versionssprung.
- Bei ausgeliefertem Mod-Verhalten müssen Changelog, Version und Artefakt vor
  einer Auslieferung zusammen geprüft werden.

## Code

- TMRV bleibt client-only; keine serverseitige Funktionalität ergänzen.
- JEI-/EMI-Kompatibilität und beide Loader-Zweige müssen erhalten bleiben.
- Offizielle `mezz.jei...`-Paketnamen im gebündelten JEI-API-Stubs nicht umbenennen.
- Mixin-Klassen und Stonecutter-Kompatibilitätsblöcke nur mit gleicher
  Verhaltenserwartung verschieben oder umbenennen.
- Refactorings ändern keine öffentliche Kompatibilität, Reihenfolge,
  Fehlerbehandlung oder Laufzeitlogik ohne separaten Auftrag.

## Nachweis

- Nach Änderungen `rtk git diff --check` ausführen.
- Beide Zielvarianten bauen und die erzeugten JARs sowie ihre Metadaten prüfen.
- Ein erfolgreicher Build ist kein Nachweis für funktionierendes Ingame- oder
  Serververhalten; solche Tests müssen ausdrücklich separat durchgeführt
  werden.
