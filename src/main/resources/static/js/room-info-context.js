(function () {
    const container = document.getElementById('roomInfoContainer');
    if (container) {
        window.teamId = container.dataset.teamId ? Number(container.dataset.teamId) : null;
        window.buildingId = container.dataset.buildingId ? Number(container.dataset.buildingId) : null;
        window.roomId = container.dataset.roomId ? Number(container.dataset.roomId) : null;
    }
})();