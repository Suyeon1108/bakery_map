function createMap(containerId, lat, lng, level) {
    var container = document.getElementById(containerId);

    var mapOption = {
        center: new kakao.maps.LatLng(lat, lng),
        level: level
    };

    return new kakao.maps.Map(container, mapOption);
}

function addZoomControl(map) {
    var zoomControl = new kakao.maps.ZoomControl();
    map.addControl(zoomControl, kakao.maps.ControlPosition.RIGHT);
}

function createMarker(map, lat, lng, image) {
    var markerOption = {
        map: map,
        position: new kakao.maps.LatLng(lat, lng)
    };

    if (image) {
        markerOption.image = image;
    }

    return new kakao.maps.Marker(markerOption);
}

function createInfoWindow(content) {
    return new kakao.maps.InfoWindow({
        content: content
    });
}

function moveMap(map, lat, lng, level) {
    var position = new kakao.maps.LatLng(lat, lng);
    map.setCenter(position);

    if (typeof level === "number") {
        map.setLevel(level);
    }
}

function createBounds() {
    return new kakao.maps.LatLngBounds();
}

function extendBounds(bounds, lat, lng) {
    bounds.extend(new kakao.maps.LatLng(lat, lng));
}

function setMapBounds(map, bounds) {
    map.setBounds(bounds);
}