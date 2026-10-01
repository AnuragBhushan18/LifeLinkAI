# LifeLink AI

Intelligent Emergency Response & Healthcare Coordination Platform.

## Current Progress

- Phase 1 â€” Foundation âœ…
- Phase 2 â€” Core Modules âœ…
- Phase 3 - Emergency Engine ✅
- Phase 4 â€” Real-Time System â³
- Phase 5 â€” Healthcare Resources â³
- Phase 6 â€” AI/LLM â³
- Phase 7 â€” Analytics â³
- Phase 8 â€” Production Readiness â³

## Tech Stack

- **Backend:** Java 21, Spring Boot 3, Spring Security, MongoDB, JWT Auth
- **Frontend:** React 18, Vite, TailwindCSS, Axios
- **Database:** MongoDB
- **Containerization:** Docker Compose (MongoDB)

## Architecture

LifeLink AI Phase 1 utilizes a layered architecture for the backend (Controller-Service-Repository) exposing REST APIs secured by stateless JWT authentication. The frontend is a React single-page application handling routing, token management, and providing a clean, responsive UI.

## Setup

### 1. MongoDB (Docker)

To run the required MongoDB instance:
```bash
docker-compose up -d
```
*Note: Make sure Docker is running on your machine.*

### Environment Variables

Copy `.env.example` to `.env` in the root and in the respective directories if needed. For Phase 1:
```bash
cp .env.example .env
```
Ensure you have `MONGODB_URI` and `JWT_SECRET` properly configured in `.env`.

### 2. Backend

Navigate to the `backend/` folder and run:
```bash
cd backend
./mvnw spring-boot:run
```
The backend will start on `http://localhost:8080`.

### 3. Frontend

Navigate to the `frontend/` folder and run:
```bash
cd frontend
npm install
npm run dev
```
The frontend will start on `http://localhost:5173`.

## API

A Swagger/OpenAPI UI is available during development to explore the APIs. Once the backend is running, visit:
`http://localhost:8080/swagger-ui/index.html`

## Current Features

- MongoDB integration and Docker Compose ready
- JWT stateless authentication and BCrypt password hashing
- Role-based authorization foundation (PATIENT, AMBULANCE_DRIVER, HOSPITAL_STAFF, DOCTOR, ADMIN)
- Protected API endpoints and frontend routing
- Registration, Login, and secure User Profile retrieval
- Global error and validation handling
- Basic professional healthcare UI with Landing, Login, Register, and Dashboard pages
- Phase 2 Core Modules: Patient, Doctor, Hospital, Ambulance, Driver, and Admin CRUD operations
- Role-specific dynamic dashboards connected to REST APIs


### Phase 3 - Emergency Engine
- Emergency workflow implementation (deterministic rule-based decision logic)
- Severity engine to rank SOS
- Hospital scoring and recommendation (using estimated ETA, not live routing)
- Ambulance allocation and dispatch
- Emergency state machine validation
- Dashboards updated to display active emergencies and SOS button
- Note: No clinical diagnosis or live tracking API is used in this phase.
