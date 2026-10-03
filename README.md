# LifeLink AI

Intelligent Emergency Response & Healthcare Coordination Platform.

## Current Progress

- Phase 1 — Foundation ✅
- Phase 2 — Core Modules ✅
- Phase 3 — Emergency Engine ✅
- Phase 4 — Real-Time System ✅
- Phase 5 — Healthcare Resources ✅
- Phase 6 — AI/LLM ⏳
- Phase 7 — Analytics ⏳
- Phase 8 — Production Readiness ⏳

## Tech Stack

- **Backend:** Java 21, Spring Boot 3/4, Spring Security, Spring WebSocket & STOMP, MongoDB, JWT Auth
- **Frontend:** React, Vite, TailwindCSS, Axios, @stomp/stompjs, Lucide Icons
- **Database:** MongoDB
- **Containerization:** Docker Compose (MongoDB)

---

## Real-Time Architecture (Phase 4)

Phase 4 transforms LifeLink AI into an instant, synchronized real-time coordination platform connecting Patients, Ambulance Drivers, Hospitals, and System Administrators.

```
+-------------------------------------------------------------------------+
|                              LifeLink AI                                |
|                           Real-Time Core                                |
+--------------------+-------------------+--------------------+-----------+
                     |                   |                    |
        +------------v----+      +-------v---------+   +------v-------+
        |     PATIENT     |      |    AMBULANCE    |   |   HOSPITAL   |
        |  Live Radar Map | <--> |  Driver Console | <-> Incoming Queue|
        |  Real-time ETA  |      |  GPS Simulation |   | Real-time Tx |
        |  Live Timeline  |      |  State Actions  |   | Bed Counters |
        +-----------------+      +-----------------+   +--------------+
                     |                   |                    |
                     +-------------------v--------------------+
                                         |
                            +------------v------------+
                            | WebSocket / STOMP Broker |
                            |       /ws Endpoint      |
                            +-------------------------+
```

### 1. WebSocket Endpoint & Connection

- **Endpoint:** `/ws` (supports native WebSocket and SockJS fallback)
- **Message Broker:** In-memory STOMP broker enabled for `/topic`, `/queue`, application prefixes `/app`, user prefix `/user`.
- **Connection States:** `CONNECTED`, `CONNECTING`, `RECONNECTING`, `DISCONNECTED` with visible UI badges on all dashboards.

### 2. Secure WebSocket Authentication & Server-Side Authorization

WebSocket handshakes and STOMP message frames are protected using stateless JWT authentication:
- **Authentication:** The client supplies the JWT token via STOMP native headers (`Authorization: Bearer <token>` or `token: <token>`) and via query parameter `?token=<token>`.
- **Channel Interceptor (`WebSocketAuthChannelInterceptor`):**
  - **`CONNECT`:** Validates JWT signature, expiration, extracts user details, and sets `Principal`.
  - **`SUBSCRIBE`:** Server-side access enforcement (client-supplied identity is never trusted):
    - `PATIENT` can only subscribe to their own emergency (`/topic/emergency/{id}`) and patient topic (`/topic/patient/{id}`).
    - `AMBULANCE_DRIVER` can only subscribe to emergencies assigned to them (`/topic/emergency/{id}`) and their driver topic (`/topic/driver/{id}`).
    - `HOSPITAL_STAFF` can only subscribe to emergencies recommended to their facility (`/topic/emergency/{id}`) and hospital topic (`/topic/hospital/{id}`).
    - `ADMIN` can monitor all emergency topics (`/topic/admin/**`, `/topic/emergency/**`).
    - `/topic/user/{userId}/notifications` is strictly limited to the authenticated user.
  - **`SEND`:** Directly publishing to `/topic/**` or `/queue/**` from clients is strictly blocked. Clients can only publish to `/app/**`.

### 3. STOMP Destinations

| Type | Destination | Purpose | Authorization |
|------|-------------|---------|---------------|
| Client -> Server | `/app/ambulance/location` | Driver sends real-time GPS coordinates | Authenticated AMBULANCE_DRIVER |
| Client -> Server | `/app/emergency/status` | Driver/Hospital initiates state transition | Assigned Driver or Hospital Staff |
| Broadcast | `/topic/emergency/{id}` | Real-time status, ETA, location & timeline | Patient, Assigned Driver, Hospital, Admin |
| Broadcast | `/topic/patient/{patientId}` | Patient notifications and dispatch alerts | Patient owner or Admin |
| Broadcast | `/topic/driver/{driverId}` | Dispatch assignments and updates | Assigned Driver or Admin |
| Broadcast | `/topic/hospital/{hospitalId}` | Incoming ambulance & recommendation alerts | Hospital staff or Admin |
| Broadcast | `/topic/user/{userId}/notifications` | Private in-app notification events | Authenticated user |
| Broadcast | `/topic/admin/emergencies` | System-wide emergency monitoring | ADMIN only |

### 4. Real-Time Emergency Status Flow & State Machine

Every emergency status transition passes through the deterministic `EmergencyStateService` state machine before persistence in MongoDB and broadcasting:

`CREATED` -> `SEVERITY_ANALYZED` -> `AMBULANCE_SEARCHING` -> `AMBULANCE_ASSIGNED` -> `GOING_TO_PATIENT` -> `ARRIVED_AT_PICKUP` -> `PATIENT_PICKED_UP` -> `GOING_TO_HOSPITAL` -> `ARRIVED_AT_HOSPITAL` -> `ADMITTED` -> `TREATMENT_IN_PROGRESS` -> `COMPLETED` (or `CANCELLED`).

Upon each transition:
1. Validated against valid next states in `EmergencyStateService`.
2. Persisted to MongoDB (single source of truth).
3. Milestone appended to `EmergencyTimeline` with timestamp.
4. Real-time event published via `RealtimeEventPublisher` to `/topic/emergency/{id}` and role topics.
5. In-app notifications generated and delivered via WebSocket.

### 5. Real-Time Location Tracking & ETA

- **Driver Location Updates:** Drivers send GPS coordinates (latitude, longitude, timestamp) to `/app/ambulance/location` (or REST `POST /api/ambulances/location`).
- **Validation & Rate Limiting:** Latitudes strictly validated within [-90, 90], longitudes within [-180, 180]. Throttling prevents updates faster than 1-2 seconds from flooding MongoDB.
- **Dynamic ETA Recalculation:**
  - When en route to patient (`GOING_TO_PATIENT`): ETA is calculated to patient coordinates using Haversine formula and average speed.
  - When en route to hospital (`GOING_TO_HOSPITAL`): ETA is calculated to hospital coordinates.
  - Broadcasted immediately with clear estimated indicator: `~X mins (Estimated)`.
- **Live Tracking Map (Radar):** Responsive visual coordinate radar displaying patient pickup, destination hospital, live ambulance marker, and route lines without page refresh.

### 6. Simulated Location Mode (Demo GPS)

For development and demonstration without physical GPS hardware:
- Toggleable **"Demo / Simulated Location"** mode on the Driver Dashboard.
- Smoothly steps coordinates along the route every 3 seconds.
- Clearly marked with prominent visual badge `Demo / Simulated Location` to distinguish from actual GPS hardware.

### 7. In-App Notification Center

- Persistent notification model stored in MongoDB (`notifications` collection).
- Real-time notification badge on Navbar with live unread counter.
- Dropdown panel supporting mark-as-read and mark-all-as-read.
- Animated toast popups for incoming dispatch and hospital arrival alerts.

### 8. Connection Management & Missed Update Reconciliation

- Centralized `websocketService` handles connection lifecycle across all components (preventing redundant socket connections).
- Exponential backoff retry on disconnect.
- **Reconciliation:** When reconnected, clients re-subscribe to STOMP destinations and fetch the latest emergency state via REST to reconcile any missed updates during disconnects.

---

## REST APIs Added/Updated in Phase 4

### Notifications API
- `GET /api/notifications` — Retrieve notifications for current user (ordered by date desc).
- `GET /api/notifications/unread-count` — Retrieve unread notification count.
- `PATCH /api/notifications/{id}/read` — Mark notification as read.
- `PATCH /api/notifications/read-all` — Mark all user notifications as read.

### Ambulance & Location API
- `POST /api/ambulances/location` — Driver sends location update via REST (validates coordinates, checks assignment, updates MongoDB, recalculates ETA, broadcasts update).

### Emergency API (Enhanced)
- `GET /api/emergencies/{id}/timeline` — Retrieve milestone timeline for emergency.
- `PATCH /api/emergencies/{id}/status?status={status}` — Updates status through state machine, records timeline, updates ETA, emits notifications and broadcasts event.

---

## Setup & Running

### 1. MongoDB (Docker)
```bash
docker-compose up -d
```

### 2. Backend
```bash
cd backend
./mvnw spring-boot:run
```
The backend starts on `http://localhost:8080`.
Swagger UI available at `http://localhost:8080/swagger-ui/index.html`.

### 3. Frontend
```bash
cd frontend
npm install
npm run dev
```
The frontend starts on `http://localhost:5173`.

---

## How to Test the Real-Time Workflow

1. **Patient triggers SOS:**
   - Register/login as Patient (`Role: PATIENT`).
   - Click **ACTIVATE SOS** and submit symptoms (e.g., "difficulty breathing, chest pain").
   - Severity is evaluated (`CRITICAL`), hospital is recommended, and ambulance is assigned immediately.
   - Patient dashboard displays active emergency, live map, ETA, and emergency timeline.
2. **Driver logs in & navigates:**
   - In a second browser window/incognito tab, register/login as Driver (`Role: AMBULANCE_DRIVER`).
   - Driver dashboard shows active dispatch mission, patient location, and hospital destination.
   - Click **Start Demo GPS Run**: ambulance marker on Patient and Driver dashboards moves smoothly in real time without refreshing!
   - Click **1. Going to Patient** -> **2. Arrived at Pickup** -> **3. Patient Picked Up** -> **4. Going to Hospital** -> **5. Arrived at Hospital**.
3. **Hospital coordinates admission:**
   - In a third browser window, login as Hospital (`Role: HOSPITAL_STAFF`).
   - Hospital dashboard queue updates in real-time.
   - View incoming ambulance on the radar with live ETA.
   - Click **Admit** -> **Start Tx** -> **Complete**.
4. **Notifications & Timeline:**
   - Observe Navbar bell icon incrementing with unread badge in real-time.
   - Open dropdown to view milestone alerts ("Ambulance Dispatched", "Patient Boarded", "Ambulance Arrived at Hospital").
   - Check timeline updating step-by-step.

---

## Phase 5 — Healthcare Resources & Medical Records

Phase 5 adds the foundational layer for managing hospital resources and patient medical data.

- **Bed Management**: APIs and logic to manage hospital beds (`GENERAL`, `ICU`, `EMERGENCY`) and their states (`AVAILABLE`, `OCCUPIED`, `RESERVED`, `MAINTENANCE`). Includes real-time capacity tracking.
- **Medical Records**: Securely manages patient medical history, clinician notes, medical reports, and prescriptions. Strict authorization ensures patients can view their records while only authorized doctors can edit them.
- **Blood Bank**: Features to manage blood inventory across different blood groups and process emergency blood requests from hospitals. Validations prevent negative stock.
- **Pharmacy**: Complete medicine inventory system with low-stock thresholds and expiration date tracking.
- **Emergency Integration**: The emergency state machine now natively includes the `BED_RESERVED` state, connecting the ambulance arrival directly to resource allocation.
- **Dashboard Support**: Extensible role-based dashboards (`BloodBankDashboard`, `PharmacyDashboard`) fully integrated with the existing frontend.

_Note: The system relies strictly on explicit business rules and clinician input; no AI or LLM-based diagnosis has been implemented yet (scheduled for Phase 6)._
