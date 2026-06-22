# Spring Boot Backend for Propridict – Land Price Predictor

Build a full Spring Boot REST API backend to replace the current client-side-only data and logic with a proper server-side architecture.

## Background

The current app ([index.html](file:///c:/propredict/index.html), [app.js](file:///c:/propredict/app.js)) has all data (property rates, bank offers, stamp duty rules) and prediction logic hardcoded in JavaScript. The goal is to move this to a Spring Boot backend with:

- REST API endpoints
- JPA entities with H2 database (dev-ready, switchable to MySQL/Postgres)
- Server-side prediction engine
- Contact form persistence
- Clean separation of concerns

## User Review Required

> [!IMPORTANT]
> The plan uses **H2 in-memory database** for development simplicity. Data is seeded on startup via `data.sql`. This can be swapped for MySQL/PostgreSQL later with a simple config change. Is this acceptable, or would you prefer MySQL from the start?

> [!IMPORTANT]
> The frontend will be updated to make `fetch()` calls to the Spring Boot API instead of using hardcoded data. The existing HTML/CSS structure stays unchanged — only the JavaScript data-fetching and form-submission logic changes.

## Open Questions

1. **Authentication**: Should we add Spring Security with JWT for the contact/consultation endpoints, or keep it public for now?
2. **Database**: H2 in-memory (dev) vs MySQL/PostgreSQL (prod-ready) — which do you prefer to start with?
3. **Deployment**: Do you want a Docker setup included?

---

## Proposed Changes

### Spring Boot Project Structure

```
c:\propredict\backend\
├── pom.xml
├── src/main/java/com/propridict/
│   ├── PropridictApplication.java
│   ├── config/
│   │   └── WebConfig.java                  (CORS config)
│   ├── model/
│   │   ├── PropertyRate.java               (JPA entity)
│   │   ├── BankOffer.java                  (JPA entity)
│   │   ├── StampDutyRule.java              (JPA entity)
│   │   └── ContactRequest.java            (JPA entity)
│   ├── dto/
│   │   ├── PredictionRequest.java
│   │   ├── PredictionResponse.java
│   │   ├── EMIRequest.java
│   │   ├── EMIResponse.java
│   │   ├── DutyRequest.java
│   │   ├── DutyResponse.java
│   │   └── ContactDTO.java
│   ├── repository/
│   │   ├── PropertyRateRepository.java
│   │   ├── BankOfferRepository.java
│   │   ├── StampDutyRuleRepository.java
│   │   └── ContactRequestRepository.java
│   ├── service/
│   │   ├── PredictionService.java          (CAGR + premium logic)
│   │   ├── EMIService.java                 (EMI calculations)
│   │   ├── StampDutyService.java           (Duty calculations)
│   │   ├── BankOfferService.java           (Offer filtering/sorting)
│   │   └── ContactService.java            (Contact form persistence)
│   └── controller/
│       ├── PropertyController.java         (Cities, landmarks, categories)
│       ├── PredictionController.java       (Price prediction endpoint)
│       ├── BankOfferController.java        (Bank offers endpoint)
│       ├── StampDutyController.java        (Duty calculation endpoint)
│       └── ContactController.java          (Contact form submission)
├── src/main/resources/
│   ├── application.properties
│   ├── data.sql                            (Seed data)
│   └── schema.sql                          (Table definitions)
└── src/test/java/com/propridict/
    └── PropridictApplicationTests.java
```

---

### 1. Core Configuration

#### [NEW] [pom.xml](file:///c:/propredict/backend/pom.xml)
- Spring Boot 3.2.x parent
- Dependencies: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `h2`, `lombok`, `spring-boot-starter-validation`
- Java 17 compiler target

#### [NEW] [application.properties](file:///c:/propredict/backend/src/main/resources/application.properties)
- H2 console enabled at `/h2-console`
- Server port: `8080`
- JPA ddl-auto: `none` (using schema.sql)
- CORS allowed origins: `http://localhost:*`, `http://127.0.0.1:*`

#### [NEW] [WebConfig.java](file:///c:/propredict/backend/src/main/java/com/propridict/config/WebConfig.java)
- Global CORS configuration allowing the frontend to call the API
- Allows GET, POST methods from any localhost origin

---

### 2. JPA Entities & Repositories

#### [NEW] [PropertyRate.java](file:///c:/propredict/backend/src/main/java/com/propridict/model/PropertyRate.java)
Maps the `DATA_SET` array: `id`, `city`, `landmark`, `category`, `rate2018`, `rate2024`, `lat`, `lng`

#### [NEW] [BankOffer.java](file:///c:/propredict/backend/src/main/java/com/propridict/model/BankOffer.java)
Maps `BANK_OFFERS`: `id`, `bank`, `product`, `customer`, `type`, `rateMin`, `rateMax`, `procPct`, `procMin`, `procMax`, `maxTenure`, `link`, `lastUpdated`

#### [NEW] [StampDutyRule.java](file:///c:/propredict/backend/src/main/java/com/propridict/model/StampDutyRule.java)
Maps `STAMP_RULES`: `id`, `state`, `stampBase`, `stampFemale`, `stampJoint`, `registrationPct`

#### [NEW] [ContactRequest.java](file:///c:/propredict/backend/src/main/java/com/propridict/model/ContactRequest.java)
Persists consultation form submissions: `id`, `name`, `email`, `phone`, `purpose`, `preferredDate`, `preferredTime`, `message`, `createdAt`

#### [NEW] Repositories
Standard `JpaRepository` interfaces with custom query methods:
- `PropertyRateRepository`: `findDistinctCities()`, `findByCity()`, `findByCityAndLandmark()`, `findByCityAndLandmarkAndCategory()`
- `BankOfferRepository`: `findByType()`, `findByTypeAndCustomer()`
- `StampDutyRuleRepository`: `findByState()`
- `ContactRequestRepository`: basic CRUD

---

### 3. DTOs (Request/Response Objects)

#### [NEW] [PredictionRequest.java](file:///c:/propredict/backend/src/main/java/com/propridict/dto/PredictionRequest.java)
```java
String city, landmark, category;
int size, year;
double roadWidth, frontage;
boolean cornerPlot;
```

#### [NEW] [PredictionResponse.java](file:///c:/propredict/backend/src/main/java/com/propridict/dto/PredictionResponse.java)
```java
double baseRate, adjustedRate, totalPriceINR, totalPriceUSD;
double premiumFactor, premiumPct, cagr;
List<Integer> years;      // for graph x-axis
List<Double> baseRates;    // for graph y-axis
List<Double> adjustedRates; // for graph y-axis
double lat, lng;           // map coords
```

#### [NEW] Other DTOs
- `EMIRequest/Response`: interest rate, tenure, down payment → EMI, total payment, total interest
- `DutyRequest/Response`: state, owner type, property value → stamp duty, registration, total fees
- `ContactDTO`: form fields with validation annotations

---

### 4. Service Layer

#### [NEW] [PredictionService.java](file:///c:/propredict/backend/src/main/java/com/propridict/service/PredictionService.java)
- Port of `predictRate()`, `buildSeries()`, `computePremiumFactor()` from [app.js](file:///c:/propredict/app.js#L111-L152) to Java
- CAGR-based rate extrapolation with decay factors (>2035: 0.98×, >2040: 0.95×)
- Premium factors: road width, frontage, corner plot bonuses

#### [NEW] [EMIService.java](file:///c:/propredict/backend/src/main/java/com/propridict/service/EMIService.java)
- Port of EMI formula: `P × r × (1+r)^n / ((1+r)^n - 1)`
- Processing fee calculation

#### [NEW] [StampDutyService.java](file:///c:/propredict/backend/src/main/java/com/propridict/service/StampDutyService.java)
- Stamp duty + registration fee computation per state/owner type

#### [NEW] [BankOfferService.java](file:///c:/propredict/backend/src/main/java/com/propridict/service/BankOfferService.java)
- Filter by loan type & customer type
- Calculate EMI per bank offer for a given loan amount
- Sort by EMI, rate, or processing fee

#### [NEW] [ContactService.java](file:///c:/propredict/backend/src/main/java/com/propridict/service/ContactService.java)
- Persist contact form submissions to DB
- Return success/failure status

---

### 5. REST Controllers

| Endpoint | Method | Description |
|---|---|---|
| `/api/properties/cities` | GET | List all distinct cities |
| `/api/properties/landmarks?city=` | GET | Landmarks for a city |
| `/api/properties/categories?city=&landmark=` | GET | Categories for city+landmark |
| `/api/predict` | POST | Run price prediction |
| `/api/emi/calculate` | POST | Calculate EMI |
| `/api/offers?type=&customer=&sort=&loanAmount=&tenure=` | GET | Bank offers filtered/sorted |
| `/api/duty/calculate` | POST | Stamp duty calculation |
| `/api/duty/states` | GET | Available states |
| `/api/contact` | POST | Submit consultation request |

---

### 6. Database Seeding

#### [NEW] [schema.sql](file:///c:/propredict/backend/src/main/resources/schema.sql)
Table definitions for all 4 entities.

#### [NEW] [data.sql](file:///c:/propredict/backend/src/main/resources/data.sql)
Insert all 30 property rate records, 8 bank offers, and 6 stamp duty rules from the existing JS data.

---

### 7. Frontend Integration

#### [MODIFY] [index.html](file:///c:/propredict/index.html)
- Remove inline `<script>` block containing hardcoded `DATA_SET`, `BANK_OFFERS`, `STAMP_RULES`
- Keep the external `app.js` reference
- Add `<script>` configuration constant `const API_BASE = 'http://localhost:8080/api'`

#### [MODIFY] [app.js](file:///c:/propredict/app.js)
- Replace hardcoded `DATA_SET` / `BANK_OFFERS` with `fetch()` calls to Spring Boot API
- `initUI()` → fetch cities from `/api/properties/cities`
- City change → fetch landmarks from `/api/properties/landmarks?city=...`
- Landmark change → fetch categories from `/api/properties/categories?city=...&landmark=...`
- Form submit → `POST /api/predict` with form data, render returned prediction response
- Bank offers → `GET /api/offers?...`
- Stamp duty → `POST /api/duty/calculate`
- Contact form → `POST /api/contact` (real persistence instead of mailto fallback)
- Keep all UI rendering, Plotly chart, Leaflet map logic on frontend — only data-fetching changes

---

## Verification Plan

### Automated Tests
```bash
cd c:\propredict\backend
mvn clean test
```

### Manual Verification
1. Start the Spring Boot backend: `mvn spring-boot:run`
2. Open `http://localhost:8080/h2-console` to verify seed data
3. Test API endpoints with browser/curl:
   - `GET http://localhost:8080/api/properties/cities`
   - `POST http://localhost:8080/api/predict` with JSON body
4. Open the frontend and verify the full flow: city → landmark → category → predict → results + graph + EMI + bank offers + stamp duty
5. Submit a contact form and verify it persists in the H2 database
