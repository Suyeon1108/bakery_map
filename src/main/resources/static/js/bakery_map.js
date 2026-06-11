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
