//TODO wechsele später auf Polygone damit wir die Objekte drehen können, wir übernehmen also die Rect Logik und wandeln sie um in Polygone

//TODO Safeguard für Delete All Button
let drawControl = null;
let drawnItems = null;

    /**
     * Init Draw Mode: legt FeatureGroup an, fügt DrawControl hinzu und registriert Events
     */
    function initDrawMode(map) {
        if (!map) return;

        // FeatureGroup für alle Rechtecke
        drawnItems = new L.FeatureGroup();
        map.addLayer(drawnItems);

        // Draw Control konfigurieren
        drawControl = new L.Control.Draw({
            draw: {
                rectangle: true,
                polygon: false,
                polyline: false,
                marker: false,
                circle: false,
                circlemarker: false
            },
            edit: {
                featureGroup: drawnItems, // alle Layer, die editierbar/entfernbar sind
                edit: true,
                remove: true
            }
        });

        // Draw Control ist initial deaktiviert – über Button aktivierbar
        // map.addControl(drawControl); → nur enableDrawMode()

    // Event: Neues Rechteck erstellt
    map.on(L.Draw.Event.CREATED, async function(event) {
        const layer = event.layer;

        //Sofort Layer hinzufügen, damit es klickbar ist
        drawnItems.addLayer(layer);

        //gebe user ein dem rechteck hinzugefügt wird
        let assignedUsername = prompt("Bitte geben Sie den Nutzernamen ein, dem dieses Rechteck zugeordnet werden soll:")
        if(!assignedUsername || assignedUsername.trim() === "") {
            alert("Rechteck kann nicht erstellt werden, kein Nutzer gegeben!");
            return;
        }
        assignedUsername = assignedUsername.trim();

        //gebe dem Rechteck eine farbe
        let selectedColor= await selectColor();

        if (!selectedColor) {
            drawnItems.removeLayer(layer);
            return;
        }

        layer.setStyle({
            color: selectedColor,
            fillColor: selectedColor,
            fillOpacity: 0.5
        });

        //Direkt Klick-Event hinzufügen
        layer.on('click', () => {
            if (APP_STATE.mode === "normal") {
                openSideBar(sidebar, SIDEBAR_STATE.RECT, layer._territoryId);
            }
        });

        //ermittele user-id
        let assignedUserId;
        try {
            assignedUserId = await getUserId(assignedUsername);
        } catch (err) {
            console.error("Kein User mit dem Namen " + assignedUsername + " gefunden!", err);
            drawnItems.removeLayer(layer);
            return;
        }

        const bounds = layer.getBounds();
        const rect = {
            minLat: bounds.getSouth(),
            minLon: bounds.getWest(),
            maxLat: bounds.getNorth(),
            maxLon: bounds.getEast(),
            assignedUserId,
            color: selectedColor
        };

        try {
            const saved = await postRect(rect);
            const id = saved.id;
            layer._territoryId = id;
            drawnItems.addLayer(layer);
            APP_STATE.activeTerritories.set(id, layer);
            console.log("Rect saved:", saved);

            location.reload();
        } catch (err) {
            drawnItems.removeLayer(layer);
            console.error(err);
        }
    });

    // Event: Rechtecke bearbeitet
    map.on(L.Draw.Event.EDITED, async function(event) {
        event.layers.eachLayer(async layer => {
            const id = layer._territoryId;
            const bounds = layer.getBounds();

            let selectedColor= await selectColor();
            if (!selectedColor) {
                console.log("Farbwahl unterbrochen, Update konnte nicht ausgeführt werden!");
                return;
            }

            layer.setStyle({
                color: selectedColor,
                fillColor: selectedColor,
                fillOpacity: 0.5
            });

            const rect = {
                minLat: bounds.getSouth(),
                minLon: bounds.getWest(),
                maxLat: bounds.getNorth(),
                maxLon: bounds.getEast(),
                assignedUserId: layer._assignedUserId || null,
                color: selectedColor
            };

            try {
                await updateRect(id, rect);
                console.log("Rect updated:", id);
            } catch (err) {
                console.error(err);
            }
        });
    });

    // Event: Rechtecke gelöscht
    map.on(L.Draw.Event.DELETED, async function(event) {
        event.layers.eachLayer(async layer => {
            const id = layer._territoryId;

            try {
                await deleteRect(id);
                APP_STATE.activeTerritories.delete(id);
                console.log("Deleted territory:", id);
            } catch (err) {
                console.error(err);
            }
        });
    });
}

/**
 * Draw Mode aktivieren: zeigt Draw Toolbar
 */
function enableDrawMode(map) {
    if (!drawControl || !map) return;
    map.addControl(drawControl);
}

/**
 * Draw Mode deaktivieren: Toolbar ausblenden
 */
function disableDrawMode(map) {
    if (!drawControl || !map) return;
    map.removeControl(drawControl);
}

/**
 * Optional: ein spezifisches Rechteck entfernen
 */
function removeTerritory(id) {
    const layer = APP_STATE.activeTerritories.get(id);
    if (!layer) return;

    drawnItems.removeLayer(layer);
    APP_STATE.activeTerritories.delete(id);
}

//select color from pop up bar
function selectColor(currentColor = '#3388ff') {
    return new Promise(resolve => {
        const popup = document.getElementById("color-popup");
        const select = document.getElementById("color-select");
        const button = document.getElementById("color-confirm");
        const cancelBtn = document.getElementById("color-cancel");

        select.value = currentColor;

        popup.style.display = "block";

        button.onclick = () => {
            const color = select.value;
            popup.style.display = "none";
            resolve(color);
        };
        cancelBtn.addEventListener("click", () => {
            popup.style.display = "none";
            resolve(null);
        });
    });
}

/**
 * Lade Rects aus dem BackEnd in unsere Map nach Start
 */
async function loadRects(sidebar) {
    const rects = await getRects();
    rects.forEach(rect => {
        const assignedUserId = rect.assignedUserId || null;
        const bounds = [
            [rect.minLat, rect.minLon],
            [rect.maxLat, rect.maxLon]
        ];

        const layerColor = rect.color || '#3388ff';

        const layer = L.rectangle(bounds, {
            color: layerColor,
            fillColor: layerColor,
            fillOpacity: 0.5,
            weight: 2
        });
        layer._territoryId = rect.id;
        layer._assignedUserId = assignedUserId;

        //RECT CLICK EVENT
        layer.on('click', () => {
            if (APP_STATE.mode === "normal") {
                openSideBar(sidebar, SIDEBAR_STATE.RECT, rect.id);
            }
        });

        drawnItems.addLayer(layer);
        APP_STATE.activeTerritories.set(rect.id, layer);
    });
    await showPoisInRects();
}