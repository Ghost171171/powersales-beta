# API Dokumentation

## Allgemein

* Base URL: `/`
* Datenformat: `application/json`
* Authentifizierung: Session-basiert (Cookies)

---

## User /users

### POST /login

**Beschreibung:** Nutzer einloggen

**Request:**

```json
{
  "username": "Ben",
  "password": "1234"
}
```

**Response:**

```
200 no content
```


### POST /logout

**Beschreibung:** Nutzer loggt sich aus, App State und Session Storage wird zurückgesetzt

```
204 ok
```
### GET /find
**Beschreibung:** Bekomme die Nutzer ID über den Nutzernamen

**Request:** 

```
/users/find?username="hier kommt der Nutzername hin"
```

**Response:**

```
"Nuter ID"
```

---

## Contracts /contracts

### GET /all (für Admin)

**Beschreibung:** Alle Verträge abrufen (Admin) oder eigene (User)

**Response:**

```json
[
  {
    "id": "123",
    "userId": "(User Id über UUID)",
    "contractProv": 60,
    "contractTimeDate": "27.04.2026 23:08",
    "contractProcessStatus": "IN_PROCESS"
  }
]
```

---

### PUT /{id}/status

**Beschreibung:** Vertragsstatus ändern (nur Admin)

**Request:**

```json
"SUCCEEDED"
```

**Response:**

```
204 No Content
```

### POST /rawContractNotes 

**Beschreibung:** Erstelle einen Contract aus einer String Eingabe

**Request:**

```
```

**Response:**

```
```

---

## Rectangles

### GET /rects

**Beschreibung:**

* Admin: alle Rectangles
* User: nur eigene

---

### POST /rects

**Beschreibung:** Neues Gebiet erstellen

---

## POIs

### GET /rects/{id}/pois

**Beschreibung:** Alle Orte innerhalb eines Gebiets

---

## Fehlercodes

| Code | Bedeutung                |
| ---- | ------------------------ |
| 401  | Nicht eingeloggt         |
| 403  | Keine Berechtigung       |
| 404  | Ressource nicht gefunden |
| 500  | Serverfehler             |

---
