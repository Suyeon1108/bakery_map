console.log("bakery_search.js loaded");

let allBakeries = [];
let nearbyBakeries = [];
let currentBakeries = [];
let markers = [];
//let map;
let userPosition = null;

document.addEventListener("DOMContentLoaded", function () {
  initMap();
  loadBakeries();
  setupSearch();
});

function initMap() {
  const mapContainer = document.getElementById("map");

  const mapOption = {
    center: new kakao.maps.LatLng(36.3504, 127.3845),
    level: 5
  };

  map = new kakao.maps.Map(mapContainer, mapOption);
}

async function loadBakeries() {
  try {
    const response = await fetch("http://3.38.97.68:8080/bakery/list");
    allBakeries = await response.json();

    getUserLocation();
  } catch (error) {
    console.error("빵집 데이터 불러오기 실패:", error);
    alert("빵집 정보를 불러오지 못했습니다.");
  }
}

function getUserLocation() {
  if (!navigator.geolocation) {
    setDefaultLocation();
    return;
  }

  navigator.geolocation.getCurrentPosition(
    function (position) {
      userPosition = {
        lat: position.coords.latitude,
        lng: position.coords.longitude
      };

      makeNearbyBakeryList();
    },
    function () {
      setDefaultLocation();
    }
  );
}

function setDefaultLocation() {
  userPosition = {
    lat: 36.3504,
    lng: 127.3845
  };

  makeNearbyBakeryList();
}

function makeNearbyBakeryList() {
  nearbyBakeries = allBakeries
    .filter(function (bakery) {
      return bakery.lat && bakery.lng;
    })
    .map(function (bakery) {
      return {
        ...bakery,
        distance: getDistance(
          userPosition.lat,
          userPosition.lng,
          bakery.lat,
          bakery.lng
        )
      };
    })
    .sort(function (a, b) {
      return a.distance - b.distance;
    })
    .slice(0, 20);

  currentBakeries = nearbyBakeries;

  renderBakeries(currentBakeries);
}

function setupSearch() {
  const searchInput = document.getElementById("bakerySearchInput");
  const searchBtn = document.getElementById("bakerySearchBtn");
  const resetBtn = document.getElementById("bakeryResetBtn");

  searchBtn.addEventListener("click", function () {
    searchBakery();
  });

  searchInput.addEventListener("keydown", function (event) {
    if (event.key === "Enter") {
      searchBakery();
    }
  });

  resetBtn.addEventListener("click", function () {
    searchInput.value = "";
    currentBakeries = nearbyBakeries;
    renderBakeries(currentBakeries);
  });
}

function searchBakery() {
  const keyword = document
    .getElementById("bakerySearchInput")
    .value
    .trim()
    .toLowerCase();

  if (keyword === "") {
    currentBakeries = nearbyBakeries;
    renderBakeries(currentBakeries);
    return;
  }

  currentBakeries = allBakeries.filter(function (bakery) {
    const name = bakery.name ? bakery.name.toLowerCase() : "";
    const address = bakery.address ? bakery.address.toLowerCase() : "";

    return name.includes(keyword) || address.includes(keyword);
  });

  renderBakeries(currentBakeries);
}

function renderBakeries(bakeries) {
  renderBakeryList(bakeries);
  renderBakeryMarkers(bakeries);
}

function renderBakeryList(bakeries) {
  const listContainer = document.getElementById("bakeryList");
  listContainer.innerHTML = "";

  if (bakeries.length === 0) {
    listContainer.innerHTML = `
      <div class="empty-message">
        검색 결과가 없습니다.
      </div>
    `;
    return;
  }

  bakeries.forEach(function (bakery, index) {
    const item = document.createElement("div");
    item.className = "bakery-item";

    const distanceText = bakery.distance
      ? bakery.distance.toFixed(2) + "km"
      : "거리 정보 없음";

    item.innerHTML = `
    <div class="bakery_top">
        <div>
            <div class="bakery_name">${bakery.name}</div>
            <div class="bakery_address">${bakery.address}</div>
        </div>
        <button class="detail_link_btn" data-id="${bakery.id}">상세보기</button>
    </div>
    `;
      

    const detailBtn = item.querySelector(".detail_link_btn");

    detailBtn.addEventListener("click", function (event) {
        event.stopPropagation();

        const bakeryId = this.dataset.id;

        location.href = `/cont1_1.html?id=${bakeryId}`;
    });
      
      
      
      
      

    item.addEventListener("click", function () {
      moveToBakery(bakery);
    });

    listContainer.appendChild(item);
  });
}


function renderBakeryMarkers(bakeries) {
  clearMarkers();

  bakeries.forEach(function (bakery) {
    const position = new kakao.maps.LatLng(bakery.lat, bakery.lng);

    const marker = new kakao.maps.Marker({
      map: map,
      position: position
    });

    const infoWindow = new kakao.maps.InfoWindow({
      content: `
        <div style="padding:8px;font-size:13px;">
          <strong>${bakery.name}</strong><br>
          ${bakery.address}
        </div>
      `
    });

    kakao.maps.event.addListener(marker, "click", function () {
      infoWindow.open(map, marker);
    });

    markers.push(marker);
  });

  if (bakeries.length > 0) {
    map.setCenter(new kakao.maps.LatLng(bakeries[0].lat, bakeries[0].lng));
  }
}

function clearMarkers() {
  markers.forEach(function (marker) {
    marker.setMap(null);
  });

  markers = [];
}

function moveToBakery(bakery) {
  const position = new kakao.maps.LatLng(bakery.lat, bakery.lng);
  map.setCenter(position);
  map.setLevel(3);
}

function getDistance(lat1, lng1, lat2, lng2) {
  const R = 6371;

  const dLat = toRad(lat2 - lat1);
  const dLng = toRad(lng2 - lng1);

  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(lat1)) *
      Math.cos(toRad(lat2)) *
      Math.sin(dLng / 2) *
      Math.sin(dLng / 2);

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return R * c;
}

function toRad(value) {
  return value * Math.PI / 180;
}
