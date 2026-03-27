# 🍞 대전 빵지순례 ESG 지도 플랫폼

## 📌 프로젝트 소개
대전 지역 베이커리를 기반으로  
ESG(친환경) + UD(유니버설 디자인) 정보를 제공하는 지도 서비스

---

## 🌐 서버 주소
http://43.201.14.82:8080

---

## 📡 API 명세

### 1️⃣ GET /bakery
테스트용 API

#### 요청
GET /bakery

#### 응답
API연결성공!


---

### 2️⃣ POST /bakery/save
크롤링 데이터 전송 API

#### 요청
POST /bakery/save

#### Body (JSON)
```json
{
  "name": "성심당",
  "lat": 36.3504,
  "lng": 127.3845,
  "esg": 4.8,
  "ud": 4.5
}

응답 --> OK

