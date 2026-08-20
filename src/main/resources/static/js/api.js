const baseUrl = `http://${window.location.hostname}:8080`;
const UNAUTHORIZED_EVENT = "app:unauthorized";

//Central API wrapper
async function apiFetch(url, options = {}) {
    const resp = await fetch(baseUrl + url, {
        credentials: "include",
        mode: "cors",
        ...options
    });

    if (resp.status === 401) {
        window.dispatchEvent(new Event(UNAUTHORIZED_EVENT));
        throw new Error("Unauthorized");
    }

    if (!resp.ok) {
        const text = await resp.text();
        throw new Error(text || `HTTP ${resp.status}`);
    }

    return resp;
}

function handleUnauthorized() {
    APP_STATE.currentUser = null;
    sessionStorage.removeItem("user");
    sessionStorage.removeItem("appState");
}

//Load values from server, send get request, we will only receive POIs on right zoom and only receive POIs in bounds
async function getPOIs(bounds) {
    if (!APP_STATE.currentUser) return [];

    const url = `/pois/bounds?minLat=${bounds.minLat}&maxLat=${bounds.maxLat}&minLon=${bounds.minLon}&maxLon=${bounds.maxLon}`;
    const resp = await apiFetch(url);

    return resp.json();
}

//Send GET, Load a single Rect
async function getRect(id) {
    const resp = await apiFetch(`/rects/${id}`);
    return resp.json();
}

//Send GET, Load Rects from server
async function getRects() {
    const resp = await apiFetch("/rects");
    return resp.json();
}
//Send GET for POIs inside of rect
async function loadPoisInRect(id) {
    const resp = await apiFetch(`/rects/${id}/pois`);
    return resp.json();
}
//GET all pois inside a rect, show in sidebar (for now console)
async function showPoisInRects() {
    const rects = await getRects();

    for (const rect of rects) {
        const pois = await loadPoisInRect(rect.id);
        console.log(`Rects: ${rect.id} enthält`, pois);
    }
}
//GET coordinates of poi by name
async function getPOICoordinate(address) {
    if (!APP_STATE.currentUser) return null;

    const resp = await apiFetch(`/pois/search?query=${encodeURIComponent(address)}`);
    return resp.json();
}
//get user id by session
async function getUserId(username) {
    if (!APP_STATE.currentUser) return null;

    const resp = await apiFetch(`/users/find?username=${encodeURIComponent(username)}`, {
        method: 'GET',
    });
    return (await resp.text()).trim();
}
//get user contracts by id, if user is admin send all
async function getContracts() {

    let url = "/contracts";

    if (APP_STATE.currentUser.role === "ADMIN") {
        url += "/all";
    }

    const resp = await apiFetch(url);
    return resp.json();
}


//validate login
async function handleLogin(username, password) {
    try {
        const resp = await apiFetch("/users/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });

        APP_STATE.currentUser = await resp.json();
        sessionStorage.setItem("user", JSON.stringify(APP_STATE.currentUser));

        console.log("Logged in successfully!");
        updateProfileUI();
        persistAppState();
        location.reload();

    } catch (error) {
        console.log("Login error:", error.message);
    }
}
//handle logout
async function logout() {
    try {
        await apiFetch("/users/logout", {
            method: "POST"
        });

        APP_STATE.currentUser = null;
        sessionStorage.removeItem("user");
        sessionStorage.removeItem("appState");
        location.reload();

    } catch (error) {
        console.log("Logout error:", error.message);
    }
}


//Send POST-Request Rects from Server, add Rect to DB
async function postRect(rect) {
    const resp = await apiFetch("/rects", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(rect)
    });

    return resp.json();
}
//Send POST-Request to add new contract to db
async function postContract(rawContractNote) {
    await apiFetch(`/contracts/rawContractNotes`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ rawContractNotes: rawContractNote })
    });
    return true;
}

//Send PUT-Request to update POI Objects
function updatePOIStatus(poi) {
    return apiFetch(`/pois/${poi.id}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            "Accept": "application/json"
        },
        body: JSON.stringify({
            id: poi.id,
            visitStatus: poi.visitStatus || "UNVISITED",
            contractStatus: poi.contractStatus || "NONE",
            note: poi.note || ""
        })
    })
        .then(r => r.json())
        .then(updatedPOI => {
            console.log("POI updated:", updatedPOI);

            window.dispatchEvent(
                new CustomEvent("poiStatusChange", {
                    detail: updatedPOI
                })
            );

            return updatedPOI;
        });
}
//Send PUT-Request
async function updateRect(id, rect) {
    await apiFetch(`/rects/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(rect),
    });
}
//send put request for contract endpoint
async function updateContractStatus(id, status) {
    await apiFetch(`/contracts/${id}/status`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(status)
    });
}


//DELETE rects
async function deleteRect(id) {
    await apiFetch(`/rects/${id}`, {
        method: "DELETE"
    });
}

async function saveDateToRect(id, date) {
    await apiFetch(`/rects/${id}/updatedAt?updatedAt=${encodeURIComponent(date)}`, {
        method: "PUT"
    });
}


