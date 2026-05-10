# Architektur

## Systemübersicht

Die Anwendung besteht aus drei Hauptkomponenten:

* **Frontend** (Vanilla JavaScript)
* **Backend** (Spring Boot REST API in Java und JDBC)
* **Datenbank** (SQLite)

### Architekturdiagramm

```
[ Browser / Frontend ]
          ↓ HTTP (REST, JSON)
[ Spring Boot Backend ]
          ↓ Java
[ Datenbank ]
```

---

## Komponenten

### Frontend

* Implementiert in Vanilla JavaScript
* Verantwortlich für:

    * UI Rendering (Karte, Tabellen, Sidebar)
    * User Interaktionen
    * API Requests an das Backend
* State Management über `APP_STATE`

---

### Backend

* Spring Boot REST API
* Verantwortlich für:

    * Business Logic
    * Authentifizierung (Session-basiert)
    * Autorisierung (User / Admin)
    * Datenvalidierung
    * Zugriff auf die Datenbank

Schichtenstruktur:

```
Karte mit UI und User Eingabe → Controller → Service ( → Mapper für DTOs ) → Repository → Database
```

---

### Datenbank

* Speicherung von:

    * Nutzern
    * Verträgen
    * Gebieten (Rectangles)
    * Orten (POIs)

* Zugriff erfolgt über Raw JDBC

---

## Datenfluss

### Beispiel: Vertragsstatus ändern

1. User klickt im Frontend auf Status
2. Frontend sendet:

   ```
   PUT /contracts/{id}/status
   ```
3. Backend:

    * prüft Session
    * prüft Admin-Rechte
    * updated Contract
4. Datenbank speichert Änderung
5. Frontend aktualisiert UI

---

## Authentifizierung & Autorisierung

* Session-basierte Authentifizierung (`HttpSession`)
* Benutzer wird im Backend gehalten (`SessionUser`)
* Rollen:

    * `USER`
    * `ADMIN`

### Zugriffskontrolle

* Backend entscheidet über Berechtigungen
* Frontend dient nur zur Darstellung

---

## Designentscheidungen

### Warum Session statt JWT?

* Einfacher zu implementieren
* Ausreichend für internes Tool

### Warum Vanilla JS?

* volle Kontrolle
* kein Framework-Overhead

### Warum REST API?

* klare Trennung von Frontend und Backend
* erweiterbar (z. B. Mobile App möglich)

---
