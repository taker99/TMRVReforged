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
- Codex darf niemals eine JAR im Modpack oder einer Minecraft-Instanz installieren, kopieren, ersetzen, umbenennen oder entfernen und kein Deployment-Skript ausführen. Der Benutzer erledigt die Installation selbst, auch wenn Build oder JAR-Übergabe beauftragt sind. Lokale Builds im Quellrepository sind erlaubt.
- Nichts veröffentlichen, sofern das nicht ausdrücklich beauftragt wurde.

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

### Release-Regeln

- Vor Änderungen an ausgeliefertem Code, Ressourcen, Metadaten oder Build-Einstellungen `mod_version` und vorhandene Release-Nachweise prüfen; vor dem finalen Build oder Commit erneut prüfen. In diesem Repository gibt es derzeit keine `version_release.txt`. Sie darf nicht von Codex angelegt oder geändert werden; falls das Deployment-Tool sie künftig verwaltet, darf Codex sie nur lesen und niemals erstellen, ändern, löschen, wiederherstellen, stagen oder committen.
- Ist die aktuelle Mod-Version nachweislich bereits ins Modpack kopiert oder anderweitig verteilt worden, für jede weitere JAR-Änderung eine höhere passende SemVer-Version setzen, sofern der Benutzer einen Bump nicht ausdrücklich untersagt. Unterschiedliche JAR-Inhalte niemals unter derselben Version erneut deployen. Ohne belastbaren Release-Nachweis den Status klären, statt eine Version als unveröffentlicht anzunehmen.
- Eine vorbereitete, nachweislich noch nicht verteilte Version darf über mehrere Aufgaben und lokale Builds hinweg weitergeführt werden. Ein lokaler Build allein löst keinen weiteren Bump aus. Nach einem Modpack-Test mit deployter JAR braucht ein weiterer Fix eine neue Version.
- `CHANGELOG.md` konsequent in natürlichem Englisch führen. Jede ausgelieferte Funktion, Verhaltensänderung und Fehlerbehebung, auch ein kleiner Fix während des Feature-Tests, gehört unter die konkrete Mod-Version. Tatsächliche Stabilitäts- oder Performance-Verbesserungen dürfen auch mit einer zusammenfassenden Formulierung beschrieben werden. Reine Kommentaränderungen und interne Umstrukturierungen ohne spürbare Verbesserung gehören nicht hinein. Bereits verteilte Versionsabschnitte nicht nachträglich erweitern. Die oberste konkrete Versionsüberschrift muss `mod_version` entsprechen.
- Aktuelle Versionsbeispiele in der Dokumentation beim Bump anpassen. Vor Übergabe einer JAR bei beiden Zielvarianten Dateinamen und eingebettete Mod-Version gegen `mod_version` und die neueste Changelog-Überschrift prüfen. Die Ingame-Abnahme und den Modpack-Release entscheidet der Benutzer nach dem Test.

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
