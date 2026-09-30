# LifeLink AI

Intelligent Emergency Response & Healthcare Coordination Platform.

## Current Progress

- Phase 1 — Foundation ✅
- Phase 2 — Core Modules ✅
- Phase 3 — Emergency Engine ⏳
- Phase 4 — Real-Time System ⏳
- Phase 5 — Healthcare Resources ⏳
- Phase 6 — AI/LLM ⏳
- Phase 7 — Analytics ⏳
- Phase 8 — Production Readiness ⏳

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
