//TODO Hübsche UI auf
//TODO Searchbar für POIs
//TODO Touch Compatability
//TODO Untermenü für Verträge, Tabelle contracts auslesen und visualisieren

//Berlin Bounds (52.3383, 13.0884), (52.6755, 13.7611)

document.addEventListener('DOMContentLoaded', function() {
    window.addEventListener("app:unauthorized", () => {
        console.warn("Unauthorized detected -> global cleanup");
        handleUnauthorized();
    });
    startApplication();
});

//APP_STATE
const APP_STATE = {
    mode: "normal",         //normal or draw
    activeTerritories: new Map,  //current bounds of drawn object
    territoryPois: [],      //current POIs in bounds of drawn object
    visiblePois: [],        //all currently loaded pois
    markerReg: new Map,
    sidebarState: SIDEBAR_STATE.DEFAULT,
    currentUser: null
}

//CITY MARKER ZOOM
const CITY_MARKER_ZOOM = 18;

function startApplication() {
    const savedUser = sessionStorage.getItem("user");
    if(savedUser) {
        APP_STATE.currentUser = JSON.parse(savedUser);
    }
    restoreAppState();
    initApp();
    updateProfileUI();
}

//initialize map
function initApp() {

    if(!APP_STATE.currentUser) {
        console.log("No user logged in.");
    }
    //INIT MAP AND SIDEBAR
    const map = initMap();
    //Event zum Wiederherstellen
    restoreMapState(map);
    //Event zum Speichern
    storeMapState(map);
    const sidebar = initSidebar(map);

    initPoiSystem(map, sidebar);
    initTerritorySystem(map, sidebar);
    applyAppState(map, sidebar);
}

//init all functionalities of the poi-marker system
function initPoiSystem(map, sidebar) {
    //SET PARAMETERS, LAYERS, BUTTONS
    initButtons(map);
    initSearchbar(map);
    const cityLayerMarker = L.layerGroup(); //saves all markers of the pois on city level
    let loadedBounds = null;
    let moveTimeout = null;

    restoreVisiblePOIs(map, sidebar, cityLayerMarker);
    //GLOBAL EVENTS
    //ZOOM
    map.on("zoomend", async () => {
        if(map.getZoom() < CITY_MARKER_ZOOM) {
            if(map.hasLayer(cityLayerMarker)) map.removeLayer(cityLayerMarker);
        } else {
            if(!map.hasLayer(cityLayerMarker)) cityLayerMarker.addTo(map);
        }
    });
    //MOVE
    map.on("moveend", async () => {
        if (map.getZoom() < CITY_MARKER_ZOOM) return;

        clearTimeout(moveTimeout);

        moveTimeout = setTimeout(async () => {
            //we add padding to the bounds, to reduce load on next moveend event
            const paddedBounds = map.getBounds().pad(0.25);

            loadedBounds = await loadPOIsForBounds(
                paddedBounds,
                loadedBounds,
                APP_STATE.markerReg,
                cityLayerMarker,
                sidebar
            );
        }, 120);
    });

    //INITIAL LOAD
    map.whenReady(() => {
        if (map.getZoom() >= CITY_MARKER_ZOOM) {
            map.fire("moveend");
        }
    });

    //STATUS UPDATE
    window.addEventListener("poiStatusChange", (event) => {
        const marker = APP_STATE.markerReg.get(event.detail.id);
        if (marker) marker.setIcon(setMarkerColor(event.detail));
    });
}
//init territory functionalities
async function initTerritorySystem(map, sidebar) {
    initDrawMode(map);
    await loadRects(sidebar);
}
//load all pois in bounds
async function loadPOIsForBounds(bounds, loadedBounds, markerRegistry, markerLayer, sidebar) {
    if (loadedBounds && loadedBounds.contains(bounds)) return loadedBounds;

    const pois = await getPOIs({
        minLat: bounds.getSouth(), maxLat: bounds.getNorth(),
        minLon: bounds.getWest(), maxLon: bounds.getEast()
    });

    // Wir erstellen ein Set aus Strings, um Typ-Fehler zu vermeiden
    const existingIds = new Set(APP_STATE.visiblePois.map(p => String(p.id)));
    const newPois = [];

    pois.forEach(poi => {
        // Nur wenn die ID (als String) noch nicht existiert, fügen wir den POI hinzu
        if (!existingIds.has(String(poi.id))) {
            newPois.push(poi);
        }
    });

    if (newPois.length > 0) {
        const markers = createMarkersByPOIs(newPois, sidebar);
        markers.forEach((marker, i) => {
            const poi = newPois[i];
            markerRegistry.set(poi.id, marker);
            markerLayer.addLayer(marker);
        });

        APP_STATE.visiblePois.push(...newPois);

        // Begrenzung, aber wir achten darauf, dass wir nicht zu viel wegschneiden
        if (APP_STATE.visiblePois.length > 2000) {
            APP_STATE.visiblePois = APP_STATE.visiblePois.slice(-2000);
        }
        persistAppState();
    }

    return bounds;
}

//save APP_STATE
function  persistAppState() {
    const state = {
        userId: APP_STATE.currentUser?.id,
        visiblePois: APP_STATE.visiblePois,
        mode: APP_STATE.mode,
        sidebarState: APP_STATE.sidebarState
    };
    sessionStorage.setItem("appState", JSON.stringify(state));
}
function restoreAppState() {
    const saved = sessionStorage.getItem("appState");
    if(!saved) return;

    const parsed = JSON.parse(saved);

    // Nur löschen, wenn wirklich ein ANDERER User eingeloggt ist,
    // nicht wenn der User-Status nur noch kurz lädt.
    if (APP_STATE.currentUser && parsed.userId && parsed.userId !== APP_STATE.currentUser.id) {
        console.warn("User mismatch -> clear storage");
        sessionStorage.removeItem("appState");
        return;
    }

    APP_STATE.visiblePois = parsed.visiblePois || [];
    APP_STATE.mode = parsed.mode || "normal";
    APP_STATE.sidebarState = parsed.sidebarState || SIDEBAR_STATE.DEFAULT;
}
function applyAppState(map, sidebar) {
    if (APP_STATE.mode === "draw") {
        enableDrawMode(map);
    } else {
        disableDrawMode(map);
    }
    //später mit anderen Daten erweitern
}

function restoreVisiblePOIs(map, sidebar, markerLayer) {
    const pois = APP_STATE.visiblePois;
    if (!pois || !pois.length) return;

    const markers = createMarkersByPOIs(pois, sidebar);

    markers.forEach((marker, i) => {
        const poi = pois[i];

        markerLayer.addLayer(marker);
        APP_STATE.markerReg.set(poi.id, marker);
    });

    if (map.getZoom() >= CITY_MARKER_ZOOM) {
        markerLayer.addTo(map);
    }
}

