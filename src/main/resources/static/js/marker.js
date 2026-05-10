//set all markers
function createMarkersByPOIs(pois, sidebar) {
    if (!Array.isArray(pois)) {
        console.error("POIs isn't an array!")
        return [];
    }
    return pois.map(poi => createMarkerByPOI(poi, sidebar));
}
//set one marker
function createMarkerByPOI(poi, sidebar) {
    //define
    const marker = L.marker([poi.lat, poi.lon], {
        icon: setMarkerColor(poi)
    });
    // add tooltip on hover to marker
    marker.bindTooltip(`${poi.street} ${poi.houseNumber}, ${poi.plz}, ${poi.location}`, {
        direction: "top",
        offset: [0, -10],
        opacity: 0.9
    });

    //open marker menu on click
    marker.on('click', function () {
        if (APP_STATE.mode === "normal") {
            openSideBar(sidebar, SIDEBAR_STATE.POI, poi);
        }
    });

    return marker;
}

//marker coloring table
const COLOR_TABLE = {
    "VISITED": {
        "SUCCESS": "#169f14",
        "FAILED": "#ff5733",
        "NONE" : "#95a5a6"
    },
    "NOT_REACHED" : {
        "SUCCESS": "#95a5a6",
        "FAILED": "#95a5a6",
        "NONE" : "#f1c40f"
    },
    "UNVISITED" : {
        "SUCCESS": "#95a5a6", //isn't possible TODO Fix logic
        "FAILED": "#95a5a6",
        "NONE" : "#3498db"
    }

}

//change marker color based on contract status and visit status, TODO add legend to UI
function  setMarkerColor(poi) {
    let statusColor;
    if (!COLOR_TABLE[poi.visitStatus] || !COLOR_TABLE[poi.visitStatus][poi.contractStatus]) {
        statusColor = "#3498db"; // Fallback: Standard-Blau
    } else {
        //set color by color table
        statusColor = COLOR_TABLE[poi.visitStatus][poi.contractStatus];
    }

    const markerHtmlStyles = `
        background-color: ${statusColor};
        width: 3rem;
        height: 3rem;
        display: block;
        left: -1.5rem;
        top: -1.5rem;
        position: relative;
        border-radius: 3rem 3rem 0;
        transform: rotate(45deg);
        border: 1px solid #FFFFFF`;

    return L.divIcon({
        className: "my-custom-pin",
        iconAnchor: [0, 24],
        labelAnchor: [-6, 0],
        popupAnchor: [0, -36],
        html: `<span style="${markerHtmlStyles}" />`
    });
}


