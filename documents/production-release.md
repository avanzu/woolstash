# WoolStash über GitHub Releases veröffentlichen und installieren

Diese Anleitung gilt für die private Verteilung von WoolStash über GitHub Releases. Die Zielperson lädt ein signiertes APK mit dem Browser herunter und installiert es mit dem Android-Paketinstaller.

## Was auf dem Zielgerät geändert wird

Für die Installation wird vorübergehend nur **Aus dieser Quelle zulassen** für den verwendeten Browser eingeschaltet. Nach der Installation wird diese Berechtigung wieder ausgeschaltet.

Folgende Einstellungen sind **nicht** erforderlich und bleiben ausgeschaltet:

- Entwickleroptionen und USB-Debugging
- `OEM-Entsperrung`
- Entsperren des Bootloaders
- Root-Zugriff
- Deaktivieren von Google Play Protect

## Voraussetzungen

- Das Zielgerät verwendet mindestens Android 8.0.
- Das signierte Release-APK und dessen SHA-256-Prüfsumme liegen vor.
- Das APK wurde mit dem dauerhaften WoolStash-Release-Schlüssel signiert.
- Der Release-Schlüssel liegt außerhalb des Repositories und ist an mindestens zwei sicheren Orten gesichert.
- Das GitHub-Repository ist öffentlich oder die Zielperson besitzt Lesezugriff und ist im Browser bei GitHub angemeldet.

Bei einem öffentlichen Repository ist kein GitHub-Konto für den Download erforderlich. Bei einem privaten Repository muss die Zielperson bei GitHub angemeldet sein.

## Release vorbereiten und veröffentlichen

Das von Android Studio erzeugte APK liegt derzeit unter:

```text
app/release/app-release.apk
```

Vor der Veröffentlichung müssen Signatur, Paketname, Versionsnummer und SHA-256-Prüfsumme kontrolliert werden. Das APK anschließend passend zur sichtbaren Version benennen, beispielsweise `WoolStash-1.0.apk`.

Auf GitHub:

1. Im Repository **Releases** öffnen.
2. **Draft a new release** auswählen.
3. Für das erste Release den neuen Tag `v1.0.0` auf `main` anlegen.
4. Den Titel `WoolStash 1.0` vergeben.
5. Das signierte APK unter **Attach binaries** hochladen.
6. Android-Mindestversion und SHA-256-Prüfsumme in die Release Notes aufnehmen.
7. **Publish release** auswählen.

Keystore, Passwörter, `keystore.properties`, Backups und persönliche Bestandsdaten dürfen niemals committed oder an ein Release angehängt werden.

Die dauerhafte Seite der jeweils neuesten Version lautet:

```text
https://github.com/avanzu/woolstash/releases/latest
```

Dieser Link kann als QR-Code ausgedruckt oder als Lesezeichen auf den Zielgeräten gespeichert werden.

## Vorher installierte Debug-Version sichern

Eine Debug-Version und das Production-Release werden mit unterschiedlichen Schlüsseln signiert. Android kann das Production-Release daher nicht als Update über eine Debug-Version installieren.

Wenn auf dem Gerät bereits WoolStash mit erhaltenswerten Daten installiert ist:

1. In WoolStash das Menü öffnen.
2. Unter **Backup** auf **Backup erstellen** tippen.
3. Die erzeugte `.woolstash.zip`-Datei außerhalb des App-Speichers ablegen, beispielsweise im Download-Ordner.
4. Erst danach die bisherige Debug-Version deinstallieren.
5. Nach der Release-Installation das Backup in WoolStash mit **Backup wiederherstellen** einlesen, prüfen und übernehmen.

Ohne eine erhaltenswerte vorhandene Installation kann dieser Abschnitt übersprungen werden.

Aktuelle Debug-Builds verwenden den separaten Paketnamen `de.avanzu.woolstash.debug` und erscheinen als **Wool Stash (Debug)**. Sie können deshalb parallel zum Production-Release installiert werden. Die obigen Schritte betreffen nur ältere Debug-Versionen, die noch `de.avanzu.woolstash` verwendet haben.

## Erstinstallation über GitHub

### 1. APK herunterladen

1. Mit Chrome oder dem bevorzugten Browser `https://github.com/avanzu/woolstash/releases/latest` öffnen.
2. Im Abschnitt **Assets** auf `WoolStash-1.0.apk` tippen.
3. Eine allgemeine Warnung vor APK-Dateien nur bestätigen, wenn Domain, Dateiname und Release stimmen.
4. Nach dem Download in der Benachrichtigung oder Downloadliste auf die APK tippen.

### 2. Installation aus dem Browser erlauben

Android blockiert die Installation zunächst und bietet einen Link zu den Einstellungen an. Dort **Aus dieser Quelle zulassen** für genau den verwendeten Browser einschalten.

Falls Android nicht direkt zur Einstellung führt, gelten typischerweise diese Menüpfade:

- Standard-Android und Google Pixel: **Einstellungen › Apps › Spezieller App-Zugriff › Unbekannte Apps installieren › Chrome › Aus dieser Quelle zulassen**
- Samsung Galaxy: **Einstellungen › Sicherheit und Datenschutz › Weitere Sicherheitseinstellungen › Unbekannte Apps installieren › Chrome › Aus dieser Quelle zulassen**

Bei Firefox, Samsung Internet oder einem Dateimanager muss statt Chrome die tatsächlich verwendete App ausgewählt werden. Die Berechtigung gilt nur für diese Quell-App.

### 3. Installation abschließen

1. Mit der Zurück-Geste zum Paketinstaller zurückkehren.
2. **Installieren** auswählen.
3. Eine angebotene Play-Protect-Prüfung zulassen.
4. WoolStash öffnen und einen Eintrag sowie die Backupfunktion testen.
5. Anschließend **Aus dieser Quelle zulassen** über denselben Menüpfad wieder ausschalten.

## Spätere Updates über GitHub

1. In WoolStash vorsichtshalber ein Backup erstellen.
2. Die Seite `https://github.com/avanzu/woolstash/releases/latest` öffnen.
3. Das APK der neuen Version herunterladen.
4. Die Browser-Berechtigung vorübergehend wieder einschalten.
5. Das APK öffnen und **Aktualisieren** auswählen. WoolStash nicht deinstallieren.
6. Bestand und Fotos kontrollieren und die Browser-Berechtigung wieder ausschalten.

Android akzeptiert das Update nur mit demselben Paketnamen, demselben Release-Schlüssel und einem höheren `versionCode`.

## Limited Distribution und Android-Verifizierung

Für den weltweiten Rollout der Android-Entwicklerverifizierung ab 2027 sollte `de.avanzu.woolstash` über ein kostenloses Limited-Distribution-Konto registriert und jedes Zielgerät per Einladungslink oder QR-Code autorisiert werden. GitHub stellt nur den Download bereit; die Registrierung verhindert den erweiterten Installationsablauf für unregistrierte Entwickler.

## Alternative: Installation per ADB

Dieser Abschnitt ist für die geplante GitHub-Installation nicht erforderlich. Er dient nur als technische Rückfalloption und setzt vorübergehend Entwickleroptionen und USB-Debugging voraus.

### Einstellungen am Zielgerät

### 1. Entwickleroptionen sichtbar machen

Der genaue Menüpfad ist vom Hersteller abhängig:

- Google Pixel und viele Geräte mit Standard-Android: **Einstellungen › Über das Telefon › Build-Nummer**
- Samsung Galaxy: **Einstellungen › Telefoninfo › Softwareinformationen › Buildnummer**
- OnePlus: **Einstellungen › Über das Gerät/Telefon › Build-Nummer**

Dann:

1. Siebenmal nacheinander auf **Build-Nummer** beziehungsweise **Buildnummer** tippen.
2. Falls Android danach fragt, die Geräte-PIN oder das Passwort eingeben.
3. Warten, bis Android sinngemäß **Du bist jetzt Entwickler** anzeigt.

Wenn die Build-Nummer nicht auffindbar ist, in den Einstellungen nach `Build-Nummer`, `Buildnummer` oder `Entwickleroptionen` suchen.

### 2. USB-Debugging einschalten

Typische Menüpfade:

- Android 9 und neuer: **Einstellungen › System › Erweitert › Entwickleroptionen › USB-Debugging**
- Android 8: **Einstellungen › System › Entwickleroptionen › USB-Debugging**
- Samsung Galaxy: **Einstellungen › Entwickleroptionen › USB-Debugging**

Den Schalter **USB-Debugging** einschalten und die Sicherheitsabfrage bestätigen. Keine anderen Entwickleroptionen verändern. Insbesondere **OEM-Entsperrung** nicht aktivieren.

### 3. Rechner einmalig autorisieren

1. Das entsperrte Gerät mit dem USB-Datenkabel an den Installationsrechner anschließen.
2. Falls eine USB-Verwendungsart abgefragt wird, **Dateiübertragung/Android Auto** auswählen.
3. Auf dem Rechner ausführen:

   ```bash
   adb devices
   ```

4. Auf dem Gerät erscheint **USB-Debugging zulassen?** mit dem RSA-Fingerabdruck des Rechners. Nur wenn es der eigene Installationsrechner ist, **Von diesem Computer immer zulassen** aktivieren und mit **Zulassen** bestätigen.
5. `adb devices` erneut ausführen. Hinter der Seriennummer muss nun `device` stehen:

   ```text
   List of devices attached
   SERIENNUMMER    device
   ```

Steht dort `unauthorized`, ist die Abfrage auf dem entsperrten Gerät noch nicht bestätigt.

### APK mit ADB installieren

#### Erstinstallation

Vom Repository-Verzeichnis aus, mit dem tatsächlichen Namen des signierten APK:

```bash
adb install ./WoolStash-1.0.apk
```

Bei mehreren gleichzeitig angeschlossenen Geräten muss die in `adb devices` angezeigte Seriennummer angegeben werden:

```bash
adb -s SERIENNUMMER install ./WoolStash-1.0.apk
```

Die Installation war erfolgreich, wenn `adb` `Success` ausgibt. Danach WoolStash auf dem Gerät starten und mindestens Folgendes prüfen:

1. Die App startet ohne Fehlermeldung.
2. Ein Testeintrag kann angelegt, geöffnet und bearbeitet werden.
3. Ein Foto kann hinzugefügt und wieder angezeigt werden.
4. Ein Backup kann erstellt werden.

#### Spätere Updates

Ein Update muss mit demselben Release-Schlüssel signiert sein und einen höheren `versionCode` besitzen. Vor dem Update vorsichtshalber ein WoolStash-Backup erstellen. Die App nicht deinstallieren, sondern das neue APK darüber installieren:

```bash
adb install -r ./WoolStash-1.1.apk
```

`-r` ersetzt die installierte App und behält ihre lokalen Daten. Nach dem Update kontrollieren, ob Bestand und Fotos weiterhin vorhanden sind.

### ADB-Einstellungen nach der Installation zurücksetzen

Auf jedem Zielgerät:

1. Das USB-Kabel abziehen.
2. **Einstellungen › System › Entwickleroptionen** öffnen. Bei Samsung liegen die Entwickleroptionen direkt in der Hauptansicht der Einstellungen.
3. **USB-Debugging** ausschalten.
4. Falls für ein bestimmtes Gerät zusätzlich **Über USB installieren** eingeschaltet werden musste, auch diesen Schalter wieder ausschalten.
5. Optional **USB-Debugging-Autorisierungen widerrufen** auswählen, wenn das Gerät den Installationsrechner nicht dauerhaft als vertrauenswürdig speichern soll.
6. Optional den Hauptschalter der Entwickleroptionen ausschalten.

Das Ausschalten von USB-Debugging entfernt oder beeinträchtigt die installierte App nicht.

## ADB-Fehlerbehebung

| Anzeige oder Fehler | Bedeutung und Vorgehen |
| --- | --- |
| Kein Gerät unter `adb devices` | Gerät entsperren, Datenkabel prüfen, USB-Debugging kontrollieren und gegebenenfalls **Dateiübertragung** als USB-Modus auswählen. |
| `unauthorized` | Gerät entsperren und die Abfrage **USB-Debugging zulassen?** bestätigen. Falls sie nicht erscheint: Entwickleroptionen öffnen, **USB-Debugging-Autorisierungen widerrufen**, Kabel neu verbinden und erneut bestätigen. |
| `offline` | Kabel trennen, Gerät entsperren, erneut verbinden und `adb devices` wiederholen. |
| `more than one device/emulator` | Mit `adb -s SERIENNUMMER ...` das Zielgerät explizit auswählen. |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Die vorhandene App wurde mit einem anderen Schlüssel signiert. Beim einmaligen Wechsel von Debug zu Release: Backup erstellen, alte App deinstallieren, Release installieren, Backup wiederherstellen. Bei einem späteren Production-Update nicht deinstallieren, sondern den korrekten Release-Schlüssel verwenden. |
| `INSTALL_FAILED_VERSION_DOWNGRADE` | Das neue APK hat keinen höheren `versionCode`. Den Release-Build korrigieren; nicht mit der ADB-Option zum Erzwingen eines Downgrades umgehen. |
| `INSTALL_FAILED_USER_RESTRICTED` | Eine Bestätigung auf dem entsperrten Gerät fehlt oder eine Hersteller-/Geräteverwaltung blockiert USB-Installationen. Nur wenn vorhanden und tatsächlich erforderlich, in den Entwickleroptionen **Über USB installieren** vorübergehend einschalten. Bei Arbeitsprofilen oder verwalteten Geräten kann die Installation administrativ gesperrt sein. |

Play Protect soll nicht deaktiviert werden. Wenn Android eine Prüfung des APK anbietet, diese durchlaufen lassen.

## Gerätespezifisches Protokoll

Vor der Geschenkübergabe die tatsächlichen Bezeichnungen und Menüpfade für die GitHub-Installation auf jedem Zielgerät einmal prüfen und hier festhalten:

| Gerät | Hersteller/Modell | Android-Version | Verwendeter Browser | Pfad zu **Unbekannte Apps installieren** | Besonderheiten |
| --- | --- | --- | --- | --- | --- |
| 1 |  |  |  |  |  |
| 2 |  |  |  |  |  |
| 3 |  |  |  |  |  |

## Referenzen

- [GitHub: Releases und Binärdateien](https://docs.github.com/en/repositories/releasing-projects-on-github/about-releases)
- [Android: Alternative Verteilung und unbekannte Apps](https://developer.android.com/distribute/marketing-tools/alternative-distribution)
- [Android: Limited Distribution](https://developer.android.com/developer-verification/guides/limited-distribution)
- [Android: Entwickleroptionen und USB-Debugging konfigurieren](https://developer.android.com/studio/debug/dev-options)
- [Android: App per Kommandozeile auf einem Gerät installieren](https://developer.android.com/build/building-cmdline)
- [Android: Developer Verification FAQ](https://developer.android.com/developer-verification/guides/faq)
- [Android: Apps signieren](https://developer.android.com/studio/publish/app-signing)
