// we need to add content to the contract tab, thus we first have to check whether the current user is registered
// (that means a user or admin in the database)
//
async function updateContractUI() {
    if (APP_STATE.currentUser) {
        const contractsList = await getContracts();
        showContracts(contractsList);
    } else {
        console.error('No user logged in.');
    }
}
// TODO ADD CONTRACT STATUS INTERACTION

const CONTRACT_PROCESS_CONVERSION = {
    "IN_PROGRESS": {
        color: 'grey'
    },
    "DENIED": {
        color: 'red'
    },
    "SUCCEEDED": {
        color: 'green'
    }
}

const CONTRACT_STATUS_ORDER = ["IN_PROGRESS", "DENIED", "SUCCEEDED"]
function showContracts(data) {
    const header = document.getElementById('contracts-header');
    const content = document.getElementById('contracts-content');

    header.replaceChildren();
    content.replaceChildren();

    const isAdmin = APP_STATE.currentUser.role === 'ADMIN';

    // --- HEADER ---
    const contrNrTh = document.createElement("th");
    contrNrTh.textContent = "Vertragsnr.";

    const provTh = document.createElement("th");
    provTh.textContent = "Provision";

    const dateTh = document.createElement("th");
    dateTh.textContent = "Datum";

    const statusTh = document.createElement("th");
    statusTh.textContent = "Status";

    if (isAdmin) {
        const nameTh = document.createElement("th");
        nameTh.textContent = "Name";

        header.append(nameTh, contrNrTh, provTh, dateTh, statusTh);

        // --- SORTIERUNG ---
        data.sort((a, b) => {
            const nameCmp = (a.userName || "").localeCompare(b.userName || "");
            if (nameCmp !== 0) return nameCmp;

            return new Date(b.contractTimeDate) - new Date(a.contractTimeDate);
        });

    } else {
        header.append(contrNrTh, provTh, dateTh, statusTh);
    }

    // --- HELPER ---
    function td(value) {
        const td = document.createElement("td");
        td.textContent = value ?? "-";
        return td;
    }

    let lastUser = null;

    // --- RENDER ---
    data.forEach(contract => {

        // 🔹 Gruppen-Header
        if (isAdmin && contract.userName !== lastUser) {
            const groupRow = document.createElement("tr");
            const groupCell = document.createElement("td");

            groupCell.colSpan = isAdmin ? 5 : 4; // Name + 3 Spalten
            groupCell.textContent = contract.userName;
            groupCell.className = "group-header";

            groupRow.append(groupCell);
            content.append(groupRow);

            lastUser = contract.userName;
        }

        // 🔹 Daten-Zeile
        const tr = document.createElement("tr");
        tr.dataset.id = contract.id;

        const statusTd = document.createElement("td");
        statusTd.append(renderStatus(contract));

        if (isAdmin) {
            tr.append(
                td(contract.userName),
                td(contract.id),
                td(contract.contractProv),
                td(contract.contractTimeDate),
                statusTd
            );
        } else {
            tr.append(
                td(contract.id),
                td(contract.contractProv),
                td(contract.contractTimeDate),
                statusTd
            );
        }

        content.append(tr);
    });

    content.addEventListener('click', async (event) => {
        const badge = event.target.closest(".status-badge");
        if (!badge || APP_STATE.currentUser.role !== "ADMIN") return;

        const row = badge.closest("tr");
        const contractId = row.dataset.id;

        const currentStatus = badge.dataset.status;
        const nextStatus = getNextStatus(currentStatus);

        const cfg = CONTRACT_PROCESS_CONVERSION[nextStatus];
        badge.className = `status-badge status-${cfg.color}`;
        badge.dataset.status = nextStatus;

        await updateContractStatus(contractId, nextStatus);
    });
}

function renderStatus(contract) {
    const cPS = CONTRACT_PROCESS_CONVERSION[contract.contractProcessStatus] || CONTRACT_PROCESS_CONVERSION.IN_PROCESS;

    const span = document.createElement("span");
    span.className = `status-badge status-${cPS.color}`;
    span.dataset.status = contract.contractProcessStatus;

    if (APP_STATE.currentUser.role === 'ADMIN') {
        span.style.cursor = "pointer";
    }

    return span;
}

function getNextStatus(currentStatus) {
    const idx = CONTRACT_STATUS_ORDER.indexOf(currentStatus);
    return CONTRACT_STATUS_ORDER[(idx+1) % CONTRACT_STATUS_ORDER.length];
}
