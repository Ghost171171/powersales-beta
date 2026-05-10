# Powersales Vertrieb Web Applikation

## Übersicht

Diese Web-Applikation dient zur Verwaltung und Optimierung von Vertriebsprozessen.
Zentraler Bestandteil ist eine interaktive Karte, über die Nutzer Gebiete, Orte und Verträge verwalten können.

Ziel des Systems:

* Automatisierung und Strukturierung des Workflows für Mitarbeiter
* Transparenz und Kontrolle für Administratoren über alle Vertriebsaktivitäten

---

## Features

### Allgemein

* Kartenbasierte Darstellung von Gebieten (Rectangles)
* Anzeige und Verwaltung von Orten (POIs)
* Vertragsverwaltung mit Statussystem
* Rollenbasiertes Zugriffssystem (User / Admin)

---

## Nutzerrollen

### User

Ein normaler Nutzer kann:

* Eigene Profildaten einsehen
* Zugewiesene Gebiete auf der Karte sehen
* Orte innerhalb dieser Gebiete anzeigen
* Mit Orten interagieren:

    * Vertragsdaten erfassen
    * Status setzen (z. B. besucht / nicht erreicht)
    * Notizen hinzufügen
* Eigene Verträge einsehen
* Orte über die Suchfunktion finden

---

### Admin

Ein Administrator besitzt alle User-Rechte sowie zusätzliche Funktionen:

* Zugriff auf **alle**:

    * Gebiete
    * Orte
    * Verträge
* Verwaltung von Gebieten:

    * erstellen
    * bearbeiten
    * löschen
* Vertragsstatus verwalten (z. B. erfolgreich / fehlgeschlagen / in Bearbeitung)
* Zuweisung und Kontrolle von Arbeitsbereichen

---

## Workflow

1. Nutzer loggt sich über den Login-Bereich ein
2. Nach dem Login wird die Karte geladen
3. Nutzer sieht ihm zugewiesene Gebiete
4. Innerhalb eines Gebiets:

    * Orte werden angezeigt
    * Interaktion mit einzelnen Orten möglich
5. Vertragsdaten können erfasst und gespeichert werden
6. Admins können zusätzlich:

    * Status validieren
    * globale Übersicht einsehen
    * Daten verwalten

---

## Quickstart

### Backend starten

```bash
java -jar BergAlgSim.jar
```

### Frontend starten

* `index.html` im Browser öffnen

---

## Hinweise

* Authentifizierung erfolgt über Sessions
* Rollen (Admin/User) werden serverseitig geprüft
* Frontend dient ausschließlich als UI — sicherheitsrelevante Logik liegt im Backend

---


