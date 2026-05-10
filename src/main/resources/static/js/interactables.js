function initButtons(map) {
    if (APP_STATE.currentUser?.role === "ADMIN") {
        // Füge das Control der Karte hinzu
        const toggleAppStateBtn = L.Control.extend({
            onAdd: function (map) {
                const btn = L.DomUtil.create('button', 'toggle-app-state-btn');
                btn.innerHTML = 'Wechsel App State';
                btn.style.padding = '8px 12px';
                btn.style.backgroundColor = 'white';
                btn.style.cursor = 'pointer';
                btn.style.border = '1px solid #ccc';
                btn.style.borderRadius = '4px';

                // verhindern, dass Klick-Events die Map-Interaktion blockieren
                L.DomEvent.disableClickPropagation(btn);

                //toggle between draw and normal mode
                btn.addEventListener('click', () => {
                    if (APP_STATE.mode === "draw") {
                        APP_STATE.mode = "normal";
                        disableDrawMode(map);
                        alert("App changed to normal")
                    } else {
                        APP_STATE.mode = "draw";
                        enableDrawMode(map);
                        alert("App changed to draw");
                    }
                    persistAppState();
                });
                return btn;
            }
        });

        map.addControl(new toggleAppStateBtn({position: 'topright'}));
    }
}

function initSearchbar(map) {
    const searchControl = L.Control.extend({
        onAdd: function () {
            const searchBar = L.DomUtil.create('div', 'leaflet-search-control');

            searchBar.innerHTML = `
                <input 
                    type="text"
                    id="location-search"
                    placeholder="Search location ..."
                    style="
                        padding: 6px;
                        border: 1px solid #ccc;
                        border-radius: 4px;
                    "
                />
            `;

            L.DomEvent.disableClickPropagation(searchBar);

            setTimeout(() => {
                const input = searchBar.querySelector("#location-search");
                input.addEventListener("keydown", (e) => {
                    if (e.key === "Enter") {
                        const value = input.value.trim();
                        if (value.length > 0 && value.length < 50) {
                            searchPOIByName(map, value);
                        }
                    }
                });

            });
            return searchBar;
        }
    });
    map.addControl(new searchControl({position: 'topright'}));
}

async function searchPOIByName(map, address) {
    try {
        const data = await getPOICoordinate(address);

        if (!data) return;

        const {lat, lon} = data;

        map.flyTo({lat, lon}, 18);
    } catch (error) {
        console.error(error);
        alert("Adresse nicht gefunden!")
    }
}

