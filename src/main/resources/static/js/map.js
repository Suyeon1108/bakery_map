// map.js

function createBaseMap(containerId, lat, lng, level = 3) {
    return new kakao.maps.Map(
        document.getElementById(containerId),
        {
            center: new kakao.maps.LatLng(lat, lng),
            level: level
        }
    );
}

function addBasicMarker(map, lat, lng, imageSrc = null) {
    const position = new kakao.maps.LatLng(lat, lng);

    let markerOption = {
        map: map,
        position: position
    };

    if (imageSrc) {
        const imageSize = new kakao.maps.Size(42, 56);
        const imageOption = { offset: new kakao.maps.Point(21, 56) };

        markerOption.image = new kakao.maps.MarkerImage(
            imageSrc,
            imageSize,
            imageOption
        );
    }

    return new kakao.maps.Marker(markerOption);
}

function drawLine(map, fromLat, fromLng, toLat, toLng, color = "#1b9cff") {
    return new kakao.maps.Polyline({
        map: map,
        path: [
            new kakao.maps.LatLng(fromLat, fromLng),
            new kakao.maps.LatLng(toLat, toLng)
        ],
        strokeWeight: 5,
        strokeColor: color,
        strokeOpacity: 0.95
    });
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