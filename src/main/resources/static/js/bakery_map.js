var bakeryMarkers = [];
var bakeryInfoWindows = [];

function clearBakeryMarkers() {
    bakeryMarkers.forEach(function(marker) {
        marker.setMap(null);
    });

    bakeryMarkers = [];
    bakeryInfoWindows = [];
}

function closeAllBakeryInfoWindows() {
    bakeryInfoWindows.forEach(function(infoWindow) {
        infoWindow.close();
    });
}

function renderBakeryMarkers(map, bakeries) {
    clearBakeryMarkers();

    var bounds = createBounds();

    bakeries.forEach(function(bakery) {
        var marker = createMarker(map, bakery.lat, bakery.lng);

        var infoWindow = createInfoWindow(
            '<div style="padding:8px 10px; font-size:13px; min-width:160px;">' +
                '<strong style="display:block; margin-bottom:4px;">' + bakery.name + '</strong>' +
                '<span style="color:#666;">' + bakery.address + '</span>' +
            '</div>'
        );

        kakao.maps.event.addListener(marker, "click", function() {
            closeAllBakeryInfoWindows();
            infoWindow.open(map, marker);
            moveMap(map, bakery.lat, bakery.lng);
        });

        bakeryMarkers.push(marker);
        bakeryInfoWindows.push(infoWindow);
        extendBounds(bounds, bakery.lat, bakery.lng);
    });

    if (bakeries.length > 0) {
        setMapBounds(map, bounds);
    }
}

function renderBakeryList(listId, bakeries, map, detailPage) {
    var list = document.getElementById(listId);
    if (!list) return;

    list.innerHTML = "";

    var count = document.getElementById("bakeryCount");
    if (count) {
        count.textContent = bakeries.length;
    }

    bakeries.forEach(function(bakery, index) {
        var item = document.createElement("div");
        item.className = "bakery_item";

        item.innerHTML = `
            <div class="bakery_top">
                <div>
                    <div class="bakery_name">${bakery.name}</div>
                    <div class="bakery_address">${bakery.address}</div>
                </div>
                <button class="detail_link_btn" data-id="${bakery.id}">
                    상세보기
                </button>
            </div>
        `;

        item.onclick = function(e) {
            if (e.target.classList.contains("detail_link_btn")) {
                return;
            }

            moveMap(map, bakery.lat, bakery.lng, 2);
            closeAllBakeryInfoWindows();

            if (bakeryInfoWindows[index] && bakeryMarkers[index]) {
                bakeryInfoWindows[index].open(map, bakeryMarkers[index]);
            }

            document.querySelectorAll(".bakery_item").forEach(function(el) {
                el.classList.remove("active");
            });

            item.classList.add("active");
        };

        var detailBtn = item.querySelector(".detail_link_btn");
        detailBtn.onclick = function(e) {
            e.stopPropagation();
            setSelectedBakery(bakery);
            location.href = detailPage || "cont1_1.html";
        };

        list.appendChild(item);
    });
}

function showCurrentLocation(map, options) {
    if (!navigator.geolocation) return;

    navigator.geolocation.getCurrentPosition(function(position) {
        var lat = position.coords.latitude;
        var lng = position.coords.longitude;

        moveMap(map, lat, lng);

        var marker = createMarker(map, lat, lng);

        if (options && options.showInfoWindow) {
            var infoWindow = createInfoWindow(
                '<div style="padding:6px 10px; font-size:13px;">현재 위치</div>'
            );
            infoWindow.open(map, marker);
        }
    }, function() {
        console.log("위치 정보를 가져오지 못했습니다. 기본 위치로 표시합니다.");
    });
}