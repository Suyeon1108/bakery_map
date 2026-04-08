function setSelectedBakery(bakery) {
    localStorage.setItem("selectedBakery", JSON.stringify(bakery));
}

function getSelectedBakery() {
    var bakery = localStorage.getItem("selectedBakery");
    return bakery ? JSON.parse(bakery) : null;
}

function clearSelectedBakery() {
    localStorage.removeItem("selectedBakery");
}