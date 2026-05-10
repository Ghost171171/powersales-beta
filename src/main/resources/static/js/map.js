//set all necessary parameters for map
function initMap() {
    //set bounds for map, berlin and brandenburg
    const southWest = L.latLng(52.29, 12.8);
    const northEast = L.latLng(52.83, 14.1);
    const softBounds = L.latLngBounds(southWest, northEast);

    //define the map, set bounds, zoom etc.
    let map = L.map('map', {
        center: [52.52, 13.405],
        zoom: 11,
        zoomSnap: 0.5,
        zoomDelta: 0.5,
        minZoom: 11,
        maxZoom: 19,
        maxBoundsViscosity: 1.0
    });
    map.setMaxBounds(softBounds);
    map.options.maxBoundsViscosity = 0.7;

    //add tile layer to map
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; OpenStreetMap contributors'
    }).addTo(map);

    return map;
}

function storeMapState(map) {
    if(!map) return;
    const saveState = () => {
        const center = map.getCenter();
        const zoom = map.getZoom();
        sessionStorage.setItem("mapState", JSON.stringify({
            lat: center.lat,
            lng: center.lng,
            zoom
        }));
    };
    map.on('moveend', saveState);
    map.on('zoomend', saveState);
}

function restoreMapState(map) {
    const saved = sessionStorage.getItem("mapState");
    if (saved) {
        const {lat, lng, zoom} = JSON.parse(saved);
        map.setView([lat, lng], zoom);
    }
}