window.addEventListener("online", () => {
    updateStatus(true);
});

window.addEventListener("offline", () => {
    updateStatus(false);
});

function updateStatus(isOnline) {
    const el = document.getElementById("connection-status");
    if (el) {
        el.innerText = isOnline ? "Conectado a Internet" : "Modo Offline";
        el.style.color = isOnline ? "#03dac6" : "#cf6679";
    }
}

// Check initial state
updateStatus(navigator.onLine);
