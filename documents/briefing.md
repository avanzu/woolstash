# Projektbriefing: Wool Stash Companion

## 1. Projektidee

**Wool Stash Companion** ist eine Android-App zur persönlichen Verwaltung eines Woll- und Faserbestands. Die App richtet sich nicht an buchhalterische Lagerverwaltung, sondern versteht sich als freundlicher Begleiter: Sie soll beim Erinnern, Wiederfinden, Stöbern, Sortieren und groben Planen helfen.

Im Mittelpunkt steht der persönliche Wollvorrat: gesponnene Wolle / Garn sowie spinnbare Fasern. Die App soll nützlich bleiben, auch wenn Daten unvollständig, geschätzt oder subjektiv sind. Regelmäßige, sorgfältige Datenpflege darf keine Voraussetzung für den Nutzen der App sein.

## 2. Produktframing

Der rote Faden ist **Companion statt Inventory Control**.

Die App soll nicht kontrollieren, ob der Bestand exakt gepflegt ist. Sie soll vielmehr helfen, Antworten auf Fragen zu finden wie:

* Was habe ich eigentlich noch?
* Wo liegt es?
* Welche Farben, Materialien oder Mengen habe ich?
* Wofür war das gedacht?
* Welche Garne oder Fasern passen zu einer Idee?
* Welche Dinge möchte ich wiederfinden, bevor ich Neues kaufe?

Die App ist kein Woll-ERP, keine Verbrauchsbuchhaltung und kein System, das Pflegefehler problematisiert.

## 3. Zielplattform und Technologie

Die App wird zunächst als native Android-App entwickelt.

Geplante technische Richtung:

* Kotlin
* Jetpack Compose
* Room / SQLite für strukturierte lokale Daten
* app-interner Dateispeicher für Bilder
* Activity Result APIs für Fotoaufnahme und Galerieimport
* später optional CameraX, falls eine eigene Kamera-UI nötig wird

Die Entscheidung für native Android-Entwicklung beruht darauf, dass Offlinefähigkeit, lokale Bilder, Backup/Restore, Geräte-APIs und Android-Speicherverhalten Kernaspekte der App sind.

## 4. MVP-Ziele

Das MVP soll bewusst klein und pflegeleicht bleiben.

Kernfunktionen:

* Inventareinträge anlegen, bearbeiten und anzeigen
* Produkttypen: Garn und Spinnfaser
* gemeinsame Basisdaten erfassen
* produktspezifische Attribute erfassen
* Fotos hinzufügen
* Tags vergeben
* Liste anzeigen
* Liste sortieren und filtern
* einfache Suche
* lokaler Offlinebetrieb
* Backup/Restore als manuelle Geräteübertragung

Nicht im MVP:

* bidirektionaler Echtzeit-Sync
* Verbrauchsbuchungen
* Transaktionshistorie
* automatische Restmengenberechnung
* komplexe Projektplanung
* automatische Verbrauchsschätzung anhand abweichender Nadelstärke
* echte Git-Integration
* Cloud-Konto oder Pflicht-Backend

## 5. Datenmodell: Grundstruktur

Das Datenmodell unterscheidet drei Ebenen:

### 5.1 Shared Kernel

Der Shared Kernel enthält fachlich bekannte Attribute, die für mehrere Produkttypen relevant sind.

Beispiele:

* ID
* Produkttyp
* Name / Bezeichnung
* Farbe
* Material / Faserzusammensetzung
* Gewicht
* Lagerort
* Status
* Fotos
* Tags
* Notizen
* Erstellungs- und Änderungszeitpunkte

Diese Attribute sind erstklassig. Die App kann sie sortieren, filtern, suchen, validieren und stabil exportieren.

### 5.2 Produktspezifische Attribute

Produktspezifische Attribute sind ebenfalls fachlich erstklassig, gelten aber nur für bestimmte Produkttypen.

Für Garn können relevant sein:

* Lauflänge
* Lauflängenbasis, z. B. gesamt, pro Knäuel, pro 100 g
* Garnstärke
* Anzahl Knäuel / Stränge
* empfohlene Nadelstärke
* Maschenprobe optional
* Farbpartie
* Zwirnung
* Drallrichtung optional

Für Spinnfaser können relevant sein:

* Faserform, z. B. Kammzug, Vlies, Batt, Rolag, Rohwolle
* Vorbereitung, z. B. gewaschen, ungewaschen, kardiert, gekämmt, gefärbt, natur
* Rasse / Herkunft optional
* Stapellänge optional
* Feinheit / Micron optional
* Spinnziel optional

Garn und Spinnfaser werden im MVP als getrennte Produkttypen behandelt, auch wenn sie physikalisch aus denselben Fasern bestehen können. Ein Garn kann später eine Herkunftsbeziehung zu einer oder mehreren Spinnfasern haben, ist aber fachlich ein eigenes Inventarobjekt.

### 5.3 User-defined Attributes

Nutzerdefinierte Attribute erlauben eine dynamische Erweiterung des Datenmodells.

Sie bestehen aus:

* Name
* Datentyp
* optionaler Einheit
* Wert
* optionalem Produkttyp-Scope

Im MVP haben diese Attribute keine tiefere fachliche Semantik. Die App kann sie speichern, anzeigen, bearbeiten, exportieren und importieren. Sie werden aber nicht für spezialisierte Berechnungen oder intelligente Fachlogik verwendet.

Wenn sich später zeigt, dass bestimmte nutzerdefinierte Attribute regelmäßig und fachlich zentral verwendet werden, können sie in einer späteren Version in das explizite Kernmodell überführt werden.

## 6. Tags und Kategorisierung

Die App verwendet Tags als flexible, flache Kategorisierung. Es gibt keine verpflichtende Taxonomie und keinen festen Kategorienbaum.

Ein Inventareintrag kann mehrere Tags haben.

Beispiele:

* #socken
* #handgefärbt
* #pflanzengefärbt
* #spinnen
* #natur
* #rest
* #projektidee
* #geschenk
* #luxus
* #verplant

Tags dienen der losen Gruppierung, Wiederauffindbarkeit und Inspiration. Die Listenansicht soll nach Tags filterbar sein.

Produkttypen wie Garn und Spinnfaser sind keine Tags, sondern echte fachliche Typen, weil daran Attribute und UI-Verhalten hängen.

## 7. Mengen, Gewicht und Pflegeaufwand

Die App soll keine vollständige Bestandsführung erzwingen.

Mengen- und Gewichtsangaben dienen der Orientierung. Sie müssen nicht jederzeit exakt aktuell sein.

Bewusst nicht vorgesehen im MVP:

* Verbrauchsbuchungen
* Pflicht zur Aktualisierung nach jedem Projekt
* automatische Warnungen wegen inkonsistenter Mengen
* Restmengenlogik
* Lagerbewegungen

Die App soll nützlich bleiben, auch wenn ein Gewicht geschätzt, veraltet oder unbekannt ist.

## 8. Einheiten und Konvertierungen

Die App soll relevante textile und allgemeine Einheiten unterstützen.

Vorgesehen:

* metrische und imperiale Gewichtseinheiten
* Gramm, Kilogramm, Unzen, Pfund
* Meter und Yard
* Lauflängenangaben wie Meter pro 100 g oder Yard pro Unze
* Nadelstärken in metrischen Größen und gängigen Vergleichssystemen

Interne Werte sollten möglichst normalisiert gespeichert werden, z. B. Gewicht in Gramm und Länge in Metern. Die Anzeige kann je nach Präferenz umgerechnet werden.

Automatische Verbrauchsschätzungen anhand abweichender Nadelstärke werden im MVP bewusst nicht umgesetzt. Nadelstärke und Maschenprobe können erfasst werden, aber die App leitet daraus zunächst keine komplexen Prognosen ab.

## 9. Fotos und Offlinefähigkeit

Fotos sind ein zentraler Bestandteil der App, weil sie Wiedererkennung, Farbeindruck, Etiketten und Inspiration unterstützen.

Die App soll nicht dauerhaft auf externe URLs oder Galerie-URIs angewiesen sein. Beim Fotografieren oder Importieren übernimmt die App eine eigene lokale Bildversion in app-internen Speicher.

Standardverhalten:

* Bild wird aufgenommen oder aus der Galerie gewählt
* App erzeugt eine optimierte Display-Version
* App erzeugt ein Thumbnail
* Originalgröße wird standardmäßig nicht dauerhaft gespeichert

Optional:

* Originalbild behalten, wenn die Nutzerin dies pro Bild ausdrücklich auswählt

Die Entscheidung gegen standardmäßige Originalspeicherung reduziert Speicherverbrauch. Für Woll- und Faserfotos ist meist Farb- und Wiedererkennungsinformation wichtiger als maximale Auflösung. Bei Werkstücken, Etiketten oder Detailaufnahmen kann das Original optional relevant sein.

Die Datenbank speichert Foto-Metadaten, Zuordnung zum Inventareintrag, lokale Referenz, Bildgröße, Dateigröße, Hash, Sortierung, Caption und Sync-/Backup-Status. Die Bilddaten selbst liegen im app-internen persistenten Dateispeicher.

## 10. Backup, Restore und manuelle Geräteübertragung

Bidirektionaler Sync ist im MVP nicht vorgesehen.

Das primäre Nutzungsszenario ist:

* Smartphone als Hauptgerät zur Datenpflege
* Tablet als gelegentliches Anzeige- oder Neben-Gerät
* Übertragung über vollständige Sicherungsdatei

Die App soll eine vollständige Backup-Datei erzeugen können. Diese enthält:

* strukturierte Daten
* Tags
* dynamische Attribute
* Foto-Metadaten
* lokale Bilddateien
* Manifest mit Schema-Version, App-Version, Erstellungszeitpunkt und Statistik

Restore ersetzt im MVP den lokalen Bestand auf dem Zielgerät. Vor dem Restore kann die App optional automatisch eine Sicherung des bisherigen lokalen Stands erstellen.

Eine spätere Importvorschau oder Staging-Funktion ist als Option denkbar.

## 11. Git-artige Staging-Idee als spätere Prüfoption

Eine spätere Synchronisations- oder Importlogik könnte sich konzeptionell an Git orientieren.

Gedankliches Modell:

* main = aktueller lokaler Bestand
* stage/import = importierter Backup-Stand
* Import öffnen = staged Bestand als Vorschau prüfen
* Import übernehmen = staged Bestand wird neuer main
* Import verwerfen = staged Bestand wird gelöscht

Diese Idee ist nicht Teil des MVP. Vor Umsetzung müsste separat bewertet werden:

* tatsächliche Git-Integration vs. eigenes Staging-Modell
* Komplexität auf Android
* Umgang mit Bilddateien
* Backup-Größe und Historienwachstum
* Merge-Bedarf
* Bedienbarkeit für Nicht-Technikerinnen
* Fehler- und Recovery-Verhalten

## 12. UX- und Produktprinzipien

Wichtige Produktregeln:

* Die App soll freundlich und unterstützend wirken.
* Unvollständige Daten sind erlaubt.
* Schätzungen sind erlaubt.
* Notizen und Tags sind gleichwertige Werkzeuge neben strukturierten Feldern.
* Kein Feature darf regelmäßige, sorgfältige Pflege erzwingen.
* Features, die Schuldgefühl oder Pflegefrust erzeugen, gehören nicht ins MVP.
* Features, die Erinnern, Wiederfinden, Stöbern oder Inspiration erleichtern, passen zum Companion-Gedanken.

## 13. Erste technische Meilensteine

Geplanter Einstieg:

1. Android-Studio-Projekt mit Kotlin und Compose initialisieren
2. statische Inventarliste mit Beispielobjekten anzeigen
3. erstes Domainmodell in Kotlin formulieren
4. einfache Detailansicht ergänzen
5. lokale Persistenz mit Room einführen
6. Tags und Filter ergänzen
7. Fotoaufnahme und Galerieimport als Spike testen
8. optimierte lokale Bildspeicherung implementieren
9. Backup/Restore als ZIP-basierten Snapshot entwerfen

## 14. Offene Fragen

Noch zu klären:

* Exakte Minimalversion von Android
* konkrete Felder für Garn im MVP
* konkrete Felder für Spinnfaser im MVP
* Statusmodell, z. B. aktiv / archiviert
* ob Werkstücke oder Projekte vollständig ausgeklammert bleiben oder nur als Tags/Notizen erscheinen
* konkrete Backup-Dateiendung und Manifeststruktur
* ob User-defined Attributes im MVP bereits UI-seitig enthalten sind oder erst kurz nach dem MVP folgen
