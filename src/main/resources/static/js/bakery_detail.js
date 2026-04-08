function renderBakeryDetail(bakery) {
    if (!bakery) {
        alert("선택된 빵집 정보가 없습니다.");
        location.href = "cont1_0.html";
        return;
    }

    var nameEl = document.getElementById("bakeryName");
    var addressEl = document.getElementById("bakeryAddress");
    var esgEl = document.getElementById("bakeryEsg");
    var udEl = document.getElementById("bakeryUd");
    var barrierLabel = document.getElementById("barrierLabel");
    var barrierIcon = document.getElementById("barrierIcon");
    var barrierIconI = document.getElementById("barrierIconI");

    if (nameEl) nameEl.textContent = bakery.name;
    if (addressEl) addressEl.textContent = bakery.address;
    if (esgEl) esgEl.textContent = bakery.esg;
    if (udEl) udEl.textContent = bakery.ud;

    if (barrierLabel && barrierIcon && barrierIconI) {
        if (bakery.barrierFree) {
            barrierLabel.textContent = "장애인우대";
            barrierIcon.style.background = "#59b65a";
            barrierIconI.className = "fa-solid fa-wheelchair";
        } else {
            barrierLabel.textContent = "장애인비우대";
            barrierIcon.style.background = "#e74c3c";
            barrierIconI.className = "fa-solid fa-xmark";
        }
    }
}

function renderBakeryDetailMap(containerId, bakery) {
    if (!bakery) return null;

    var map = createMap(containerId, bakery.lat, bakery.lng, 3);
    addZoomControl(map);
    createMarker(map, bakery.lat, bakery.lng);

    return map;
}