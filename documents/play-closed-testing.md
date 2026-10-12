# Wool Stash als geschlossenen Google-Play-Test veröffentlichen

Stand: 12. Oktober 2026

Ziel ist ein dauerhaft geschlossener Test: Nur freigeschaltete Google-Konten können beitreten und installieren. Die App ist nicht über die Play-Store-Suche auffindbar. Nach dem Beitritt erfolgen Installationen und Updates über Google Play.

## 1. Vor dem Upload

- Einen verifizierten Google-Play-Entwickleraccount verwenden.
- Den vorhandenen dauerhaften Wool-Stash-Release-Schlüssel und seine Backups bereithalten.
- `de.avanzu.woolstash` in der Play Console anlegen beziehungsweise registrieren.
- Falls die Play Console wegen der bisherigen Verteilung außerhalb von Google Play einen Besitznachweis verlangt, die dort erzeugte Verifikationsdatei mit dem bestehenden Release-Schlüssel signiert hochladen.
- Die Datenschutzerklärung aus `documents/privacy-policy.md` öffentlich unter der im App- und Store-Eintrag verwendeten URL bereitstellen.
- Mindestens zwei aktuelle Smartphone-Screenshots und ein 512 × 512 px großes Play-Store-Symbol vorbereiten.

## 2. Signing ohne Update-Bruch

Wool Stash wurde bereits als signiertes APK außerhalb von Google Play verteilt. Deshalb muss bei der erstmaligen Einrichtung von Play App Signing **eine Kopie des bestehenden App-Signaturschlüssels an Google übertragen** werden. Nicht den von Google angebotenen neuen App-Signaturschlüssel erzeugen lassen. Andernfalls kann die Play-Version vorhandene GitHub-/APK-Installationen nicht aktualisieren.

In der Play Console bei der Einrichtung von Play App Signing:

1. **App-Signaturschlüssel ändern** auswählen.
2. Die Option zum Bereitstellen des eigenen App-Signaturschlüssels wählen.
3. Den aktuellen Anweisungen der Play Console folgen und den bestehenden Schlüssel mit dem von Google bereitgestellten PEPK-Werkzeug verschlüsselt übertragen.
4. Für spätere Uploads möglichst einen separaten Upload-Schlüssel erzeugen und dessen Zertifikat registrieren.
5. Den App-Signatur-Fingerabdruck der Play Console mit dem Zertifikat des bisher veröffentlichten APK vergleichen.

Der mit `apksigner` geprüfte SHA-256-Fingerabdruck des Signaturzertifikats von `WoolStash-1.2.apk` lautet:

```text
02:33:D0:97:34:9D:F3:C1:F1:30:D4:88:A1:53:81:75:45:34:15:F8:D0:84:F6:ED:8E:6D:B6:FE:46:A0:D5:73
```

Die von Google Play angezeigte **App-Signatur** muss diesem Fingerabdruck entsprechen. Der Fingerabdruck eines später getrennten Upload-Schlüssels darf und soll davon abweichen.

Private Schlüssel, Keystores und Passwörter niemals in das Repository oder in Release-Anhänge aufnehmen.

## 3. App in der Play Console einrichten

1. In der Play Console **App erstellen** wählen.
2. App-Name `Wool Stash`, Standardsprache Deutsch, Typ App und Preis Kostenlos festlegen.
3. Paketname `de.avanzu.woolstash` verwenden.
4. Den Haupt-Store-Eintrag mit den Texten aus `documents/play-store-listing.md`, dem App-Symbol und mindestens zwei Screenshots vervollständigen.
5. Die Entwicklerkontaktadresse hinterlegen.
6. Unter **Richtlinien und Programme > App-Inhalte** alle verlangten Erklärungen abschließen, insbesondere Datenschutz, Anzeigen, App-Zugriff, Zielgruppe, Altersfreigabe und Datensicherheit.
7. Unter **Datenschutzerklärung** die öffentliche URL eintragen.
8. Länder/Regionen und Geräteverfügbarkeit kontrollieren.

## 4. Geschlossenen Test konfigurieren

1. **Testen und veröffentlichen > Tests > Geschlossener Test** öffnen.
2. Den vorhandenen Track verwalten oder einen Track wie `family-and-friends` anlegen.
3. Im Tab **Tester** entweder eine E-Mail-Liste oder eine private Google Group auswählen.
4. Nur die Google-Konten eintragen, mit denen die eingeladenen Personen den Play Store verwenden.
5. Eine Feedback-E-Mail oder Feedback-URL angeben und speichern.

Für eine kleine, stabile Gruppe ist eine E-Mail-Liste am einfachsten. Eine Google Group ist praktischer, wenn Personen häufiger hinzukommen oder entfernt werden sollen.

## 5. Release erstellen

1. In Android Studio **Build > Generate Signed Bundle / APK > Android App Bundle** öffnen.
2. Das Bundle mit dem korrekten bestehenden Schlüssel beziehungsweise dem registrierten Upload-Schlüssel signieren.
3. Kontrollieren: Paketname `de.avanzu.woolstash`, `versionCode` 4 und `versionName` 1.3.
4. Im Closed-Test-Track **Neues Release erstellen** wählen und das signierte `.aab` hochladen.
5. Die Versionshinweise aus `documents/play-store-listing.md` eintragen.
6. Warnungen prüfen, Release speichern und zur Überprüfung senden.
7. Nach Freigabe den Closed Test ausrollen.

Der erste Review kann länger dauern. Der Beitrittslink erscheint erst, wenn die App veröffentlicht und nicht mehr im Status Entwurf oder Veröffentlichung ausstehend ist.

## 6. Tester einladen

1. Im Tab **Tester** den Opt-in-Link kopieren.
2. Den Link nur an die freigeschalteten Personen senden.
3. Jede Person öffnet den Link mit dem freigeschalteten Google-Konto, tritt dem Test bei und installiert Wool Stash über den Play Store.
4. Falls bereits die bisherige signierte APK installiert ist, sollte der Play Store bei identischem App-Signaturschlüssel ein Update anbieten. Vorher vorsichtshalber ein Wool-Stash-Backup erstellen.

Closed-Test-Versionen können nicht öffentlich bewertet werden. Feedback läuft privat über Google Play oder den im Test hinterlegten Kontakt.

## 7. Spätere Updates

- Für jedes Update den `versionCode` erhöhen.
- Ein signiertes Android App Bundle mit demselben registrierten Upload-Schlüssel hochladen.
- Das Release im selben Closed-Test-Track ausrollen.
- Tester erhalten das Update wie andere Play-Store-Updates, bei aktivierten automatischen Updates in der Regel automatisch.

## Noch von Hand erforderlich

- Play-Console-Zugang und gegebenenfalls Identitäts-/Kontoverifizierung
- Besitznachweis für den bereits außerhalb von Play verwendeten Paketnamen
- Zugriff auf den bestehenden privaten Release-Schlüssel
- öffentlich erreichbare Datenschutzerklärung
- Entwicklerkontaktadresse
- E-Mail-Adressen oder Google Group der Tester
- Screenshots aus einem Gerät oder Emulator
- Upload, Erklärungen und verbindliche Veröffentlichung in der Play Console

## Offizielle Referenzen

- [Geschlossene Tests einrichten](https://support.google.com/googleplay/android-developer/answer/9845334)
- [Release vorbereiten und ausrollen](https://support.google.com/googleplay/android-developer/answer/9859348)
- [Play App Signing verwenden](https://support.google.com/googleplay/android-developer/answer/9842756)
- [Paketnamen in der Play Console registrieren](https://support.google.com/googleplay/android-developer/answer/16984799)
- [Informationen für den Bereich Datensicherheit](https://support.google.com/googleplay/android-developer/answer/10787469)
- [Vorschau-Assets für den Store-Eintrag](https://support.google.com/googleplay/android-developer/answer/9866151)
