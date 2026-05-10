//SET SIDEBAR STATES
const SIDEBAR_STATE = {
    DEFAULT: "default", //display default msg
    POI: "poi", //display poi information, when interacting with poi
    RECT: "rect" //display all names of pois inside rect, when interacting with rect
};
let currentSidebarState = SIDEBAR_STATE.DEFAULT;
let lastRectId = null; //last rect id, cache for back button
let rectClickHandler = null;

//set sidebar
function initSidebar(map) {

    const sidebar = L.control.sidebar('sidebar').addTo(map);

    renderDefault();
    updateProfileUI();
    updateContractUI();

    sidebar.on('closing', function () {

        setTimeout(() => {
            lastRectId = null; //set last rect id to null as we don't need it anymore
            renderDefault();
        }, 350);

    });

    return sidebar;
}

//open sidebar menu
function openSideBar(sidebar, state, data = null) {
    currentSidebarState = state;

    //cache current rect id
    if (state === SIDEBAR_STATE.RECT) {
        lastRectId = data;
    }

    switch (state) {
        case SIDEBAR_STATE.DEFAULT:
            renderDefault();
            break;
        case SIDEBAR_STATE.POI:
            renderPOI(data);
            break;
        case SIDEBAR_STATE.RECT:
            renderRect(data, sidebar);
            break;
    }

    sidebar.open('home');
    persistAppState();
}

//RENDER DEFAULT CONTENT
function renderDefault() {
    //BASE TEXT
    const container = document.getElementById("home-content");
    container.replaceChildren();

    const h2 = document.createElement("h2");
    h2.textContent = "Guide zur Nutzung!";

    const p1 = document.createElement("p");
    p1.textContent = "Willkommen im Berlin Status Simulator! In diesem Guide lernst du alles über die Nutzung und Funktionen deines neuen Systems!";
    p1.classList.add("intro-text");

    const h3 = document.createElement("h3");
    h3.textContent = "Nutzung am Computer:";

    const p2 = document.createElement("p");
    p2.textContent =
        "Mit der Maus über einen Ort zu gehen zeigt den Straßennamen eines Ortes an. Dann auf den Ort zu klicken erzeugt ein erweitertes Menu. " +
        "In diesem Menü bekommt man genauere Adressinformationen, kann den Status des Ortes verändern und Notizen hinzufügen. " +
        "Wichtig zu beachten, Notizen werden nur genutzt, um Vertragsdaten einzutragen, dies geschieht im folgenden Format (Vertragstyp;Vertragsnummer). " +
        "Es gibt hier zwei Vertragstypen, 1 und 2. Vertragstyp 1 ist die kleine Provision, 2 ist die große. Die Vertragsnummer ist gegeben auf dem Vertragsdokument. " +
        "Um mehrere Verträge auf einem Ort zu erstellen, einfach zwischen den Vertragsinformationen Leerzeichen setzen. " +
        "Um nun die veränderten Statusinformationen und Notizen speichern, einfach auf speichern drücken und das System speichert die Informationen. "

    const section1 = document.createElement("section");
    section1.append(h3, p2);

    const h31 = document.createElement("h3");
    h31.textContent = "Zeichen Modus:";

    const p3 = document.createElement("p");
    p3.textContent = "Oben Rechts befindet sich ein Knopf, der vom Normalen/Betrachter Modus in den Zeichen Modus zu wechseln und zurück über denselben Knopf. " +
        "Wenn man auf den Knopf drückt, und in den Zeichen Modus wechselt, kann man per Knopfdruck auf die Quader-Taste, nun mit dem Cursor ein Viereck ziehen. " +
        "Dieser Quader kann dann über die weiteren Tasten bearbeitet werden, also in Größe verstellt und verschoben werden, oder gelöscht werden. "

    const section2 = document.createElement("section");
    section2.append(h31, p3);

    container.append(h2, p1, section1, section2);
}

//RENDER POI CONTENT
function renderPOI(data) {
    //we want to modify content of home
    const container = document.getElementById('home-content');
    container.replaceChildren();

    //GO-BACK BUTTON
    //initialize the button
    const backBtn = document.createElement("button");
    backBtn.id = "back-to-rect";
    backBtn.textContent = "Zurück";
    backBtn.style.marginBottom = "10px";

    //initialize listener
    if(lastRectId) {
        backBtn.addEventListener('click', (event) => {
            openSideBar(sidebar, SIDEBAR_STATE.RECT, lastRectId);
        });
    } else {
        //if no state disable
        backBtn.disabled = true;
    }

    //TITLE
    const title = document.createElement("h2");
    title.textContent = `${data.street} ${data.houseNumber}`;

    //BASIC INFORMATION
    //initialize plz
    const plz = document.createElement("p");
    const plzStrong = document.createElement("strong");
    plzStrong.textContent = "PLZ: ";
    plz.append(plzStrong, document.createTextNode(data.plz ?? ""));

    //initialize location
    const location = document.createElement("p");
    const locationStrong = document.createElement("strong");
    locationStrong.textContent = "Ort: "
    location.append(locationStrong, document.createTextNode(data.location ?? ""));

    //STATUS SYSTEM
    //action table
    const actions = {
        'VISITED' : {fn: saveVisitStatus, textId: 'visit-status-text'},
        'NOT_REACHED' : {fn: saveVisitStatus, textId: 'visit-status-text'},
        'SUCCESS' : {fn: saveContractStatus, textId: 'contract-status-text'},
        'FAILED' : {fn: saveContractStatus, textId: 'contract-status-text'}
    }
    //renaming table
    const STATUS_LABELS = {
        'VISITED' : 'Besucht',
        'NOT_REACHED' : 'Nicht erreicht',
        'UNVISITED' : 'Nicht besucht',
        'SUCCESS' : 'Erfolgreich',
        'FAILED' : 'Fehlgeschlagen',
        'NONE' : 'Nichts'
    }

    //VISIT STATUS
    const visitP = document.createElement("p");

    const visitLabel = document.createElement("strong");
    visitLabel.textContent = "Status: ";

    const visitSpan = document.createElement("span");
    visitSpan.id = "visit-status-text";
    visitSpan.textContent = STATUS_LABELS[data.visitStatus] ?? "";

    const btnVisited = document.createElement("button");
    btnVisited.textContent = "Besucht";
    btnVisited.dataset.status = "VISITED";

    const btnNotReached = document.createElement("button");
    btnNotReached.textContent = "Nicht erreicht";
    btnNotReached.dataset.status = "NOT_REACHED";

    visitP.append(visitLabel, visitSpan, btnVisited, btnNotReached);

    //CONTRACT STATUS
    const contractP =  document.createElement("p");

    const contractLabel = document.createElement("strong");
    contractLabel.textContent = "Vertrag: ";

    const contractSpan = document.createElement("span");
    contractSpan.id = "contract-status-text";
    contractSpan.textContent = STATUS_LABELS[data.contractStatus] ?? "";

    const btnSuccess = document.createElement("button");
    btnSuccess.textContent = "Erfolgreich";
    btnSuccess.dataset.status = "SUCCESS";

    const btnFailed = document.createElement("button");
    btnFailed.textContent = "Fehlgeschlagen";
    btnFailed.dataset.status = "FAILED";

    contractP.append(contractLabel, contractSpan, btnSuccess, btnFailed);

    //NOTE
    const noteLabel = document.createElement("p");
    const noteStrong = document.createElement("strong");
    noteStrong.textContent = "Note: ";
    noteLabel.append(noteStrong);

    const textArea = document.createElement("textarea");
    textArea.id = "poi-note";
    textArea.rows = 4;
    textArea.style.width = "100%";
    textArea.value = data.note ?? "";

    //SAVE BUTTON
    const saveBtn = document.createElement("button");
    saveBtn.id = "save-changes";
    saveBtn.textContent = "Speichern";

    saveBtn.addEventListener('click', () => {
        const noteVal = textArea.value;

        savePOINote(data);

        if (!postContract(noteVal)) {
            console.log("Data Notes are faulty!")
        }
    });

    // Finde alle Buttons im Container, die ein 'data-status' Attribut haben
    const btns = [btnVisited, btnNotReached, btnSuccess, btnFailed];

    btns.forEach(button => {
        button.addEventListener('click', (event) => {
            const status = button.dataset.status;
            const config = actions[status];

            if (!config) return;

            // Führe die Funktion (saveVisitStatus oder saveContractStatus) aus
            config.fn(data, status);

            // Update den Text in der UI
            const target = document.getElementById(config.textId);
            if (target) {
                target.textContent = STATUS_LABELS[status] ?? "";
            }
        });
    });

    container.append(
      backBtn,
      title,
      plz,
      location,
      visitP,
      contractP,
      noteLabel,
      textArea,
      saveBtn
    );
}

//RENDER RECTANGLE CONTENT
async function renderRect(rectId, sidebar) {
    const container = document.getElementById('home-content');
    container.replaceChildren();

    //Loading ..
    const loadingText = document.createElement("p");
    loadingText.textContent = "Lade Orte ...";
    container.append(loadingText);

    try {
        const poisInRect = await loadPoisInRect(rectId);
        sortPOIs(poisInRect);
        container.replaceChildren();

        if (!poisInRect.length) {
            const warnNoPois = document.createElement("p");
            warnNoPois.textContent = "Keine Orte in diesem Gebiet";
            container.append(warnNoPois);
            return;
        }

        //Title
        const title = document.createElement("h2");
        title.textContent = "Orte im Gebiet";

        //List
        const list = document.createElement("ul");
        list.className = "poi-list";

        poisInRect.forEach(p => {
            const li = document.createElement("li");
            li.dataset.poiId = p.id;
            li.textContent = `${p.street} ${p.houseNumber}`;
            list.append(li);
        });

        container.append(title, list);

        // alten Event Listener entfernen
        if (rectClickHandler) {
            container.removeEventListener("click", rectClickHandler);
        }

        rectClickHandler = (event) => {
            const item = event.target.closest("[data-poi-id]");
            if (!item) return;

            const poiId = item.dataset.poiId;
            const poi = poisInRect.find(p => p.id === poiId);

            if (poi) {
                openSideBar(sidebar, SIDEBAR_STATE.POI, poi);
            }
        }

        container.addEventListener("click", rectClickHandler);

    } catch (err) {
        console.error("Fehler beim Laden der POIs in Rectangle", err);
        container.replaceChildren();

        const error = document.createElement("p");
        error.textContent = "Fehler beim Laden der POI in Rectangle";

        container.append(error);
    }
}

//HELPER SORT POIS IN RECT
function sortPOIs(pois) {
    return pois.sort((a, b) => {
        // 1. Straße
        const streetCompare = a.street.localeCompare(b.street, 'de', {
            sensitivity: 'base'
        });

        if (streetCompare !== 0) return streetCompare;

        // 2. Hausnummer
        const aHN = parseHouseNumber(a.houseNumber ?? "");
        const bHN = parseHouseNumber(b.houseNumber ?? "");

        if (aHN.number !== bHN.number) {
            return aHN.number - bHN.number;
        }

        return aHN.suffix.localeCompare(bHN.suffix);
    });
}

//HELPER PARSE HOUSE NUMBER ATTRIBUTE
function parseHouseNumber(hn) {
    const match = hn.match(/^(\d+)([a-zA-Z]*)$/);

    if (!match) {
        return { number: Number.MAX_SAFE_INTEGER, suffix: "" };
    }

    return {
        number: parseInt(match[1], 10),
        suffix: match[2] || ""
    };
}

//RENDER USER INFORMATION TO PROFILE TAB
function renderProfile(user) {
    const container = document.getElementById('profile-content');
    container.replaceChildren();

    //HEADER
    const h3 = document.createElement("h3");
    h3.textContent = "Profil";

    //ID
    const idP = document.createElement("p");
    const idStrong = document.createElement("strong");
    idStrong.textContent = "ID: ";
    idP.append(idStrong, user.id ? user.id : "-");

    //NAME
    const nameP = document.createElement("p");
    const nameStrong = document.createElement("strong");
    nameStrong.textContent = "Name: ";
    nameP.append(nameStrong, user.username ? user.username : "-");

    //ROLLE
    const roleP = document.createElement("p");
    const roleStrong = document.createElement("strong");
    roleStrong.textContent = "Rolle: ";
    roleP.append(roleStrong, user.role ? user.role : "-");

    //BUTTON
    const btn = document.createElement("button");
    btn.textContent = "Logout"
    btn.id = "logoutBtn";

    container.append(h3, idP, nameP, roleP, btn);

    btn.addEventListener("click", logout);
}

//save notes
function savePOINote(poi) {
    const poiNoteField = document.getElementById('poi-note');
    if (!poiNoteField) return;

    poi.note = poiNoteField.value;

    updatePOIStatus(poi)
        .then(updatedPOI => {
            // SICHERES MERGE: Nur Felder übernehmen, die wirklich da sind
            // So verhinderst du, dass lat/lon gelöscht werden!
            Object.assign(poi, { ...poi, ...updatedPOI });

            // WICHTIG: POI im Array nach hinten verschieben (damit er nicht weggeschnitten wird)
            const index = APP_STATE.visiblePois.findIndex(p => p.id === poi.id);
            if (index !== -1) {
                APP_STATE.visiblePois.splice(index, 1); // An alter Stelle löschen
                APP_STATE.visiblePois.push(poi);       // Ganz nach hinten (als "frisch") setzen
            }

            renderPOI(poi);

            // NEU: Den geänderten State sofort in den Session Storage schreiben!
            persistAppState();
        })
        .catch(() => {
            alert("Notiz konnte nicht gespeichert werden!");
        });
    //reloade damit in unserer Tabelle die Contracts richtug abgebildet werden
    location.reload();
}

//save visit status after change
// Beispiel für saveVisitStatus (gilt für alle drei!)
    async function saveVisitStatus(poi, visitStatus) {
    const oldStatus = poi.visitStatus;
    poi.visitStatus = visitStatus;

    try {
        const updatedPOI = await updatePOIStatus(poi);

        // SICHERES MERGE: Nur Felder übernehmen, die wirklich da sind
        // So verhinderst du, dass lat/lon gelöscht werden!
        Object.assign(poi, { ...poi, ...updatedPOI });

        // WICHTIG: POI im Array nach hinten verschieben (damit er nicht weggeschnitten wird)
        const index = APP_STATE.visiblePois.findIndex(p => p.id === poi.id);
        if (index !== -1) {
            APP_STATE.visiblePois.splice(index, 1); // An alter Stelle löschen
            APP_STATE.visiblePois.push(poi);       // Ganz nach hinten (als "frisch") setzen
        }

        renderPOI(poi);
        persistAppState();

    } catch (error) {
        console.error("Speichern fehlgeschlagen:", error);
        poi.visitStatus = oldStatus;
        renderDefault();
        alert("Status konnte nicht gespeichert werden.");
    }
}
//save contract status after change
async function saveContractStatus(poi, contractStatus) {
    const oldStatus = poi.contractStatus;
    poi.contractStatus = contractStatus;

    try {
        const updatedPOI = await updatePOIStatus(poi);

        // SICHERES MERGE: Nur Felder übernehmen, die wirklich da sind
        // So verhinderst du, dass lat/lon gelöscht werden!
        Object.assign(poi, { ...poi, ...updatedPOI });

        // WICHTIG: POI im Array nach hinten verschieben (damit er nicht weggeschnitten wird)
        const index = APP_STATE.visiblePois.findIndex(p => p.id === poi.id);
        if (index !== -1) {
            APP_STATE.visiblePois.splice(index, 1); // An alter Stelle löschen
            APP_STATE.visiblePois.push(poi);       // Ganz nach hinten (als "frisch") setzen
        }

        renderPOI(poi);
        // NEU: Den geänderten State sofort in den Session Storage schreiben!
        persistAppState();

    } catch (error) {
        console.error("Speichern fehlgeschlagen:", error);
        poi.contractStatus = oldStatus;
        renderDefault();
        alert("Status konnte nicht gespeichert werden.");
    }
}
