# 🚀 START HERE — Card Payment Transaction Simulator

Welcome! This is an educational, production-style full-stack application demonstrating how card payment authorizations move through modern financial software architectures.

---

## ⚡ 60-Second Quick Start (Docker)

If you have Docker Desktop installed, run everything with one single command:

```bash
docker compose up --build
```

- **Frontend Application**: [http://localhost](http://localhost) (or [http://localhost:3000](http://localhost:3000))
- **Backend REST API**: [http://localhost:8080](http://localhost:8080)
- **Interactive Swagger Docs**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Health Check**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## 💻 Local Development Setup (Without Docker)

### Prerequisites
- **Java 21** (`java -version`)
- **Node.js 20+** (`node -v`)
- **MySQL 8.x** running on `localhost:3306` with database `cpts_db` (or see `backend/src/main/resources/application.yml`)

### Step 1: Start Backend (Spring Boot)
```bash
cd backend
# Using Maven Wrapper (no pre-installed Maven required):
.\mvnw.cmd spring-boot:run
# Or with system Maven:
mvn spring-boot:run
```
*Flyway will automatically execute database migrations `V1` through `V5`, creating tables and seeding educational RAG knowledge.*

### Step 2: Start Frontend (React + Vite)
In a new terminal window:
```bash
cd frontend
npm install
npm run dev
```
Open **[http://localhost:5173](http://localhost:5173)** in your browser.

---

## 🔑 Demo Login Credentials

The application provides quick 1-click credential filling on the login page, or you can register your own account:

| Role | Email | Password | Permissions |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin@cpts.local` | `Admin@12345` | Global Audit Ledger, User Governance, System Analytics |
| **Student / User** | `student@cpts.local` | `Student@12345` | Simulation Terminal, Personal Ledger, RAG AI Tutor |

*(If registering fresh accounts, any new registered user has standard `USER` access and can be promoted to `ADMIN` via the database or admin portal).*

---

## 🧪 Try These Simulation Triggers

Navigate to the **Simulate** tab and test our deterministic triggers:

| Scenario | Input Parameter | Expected Outcome | What It Teaches |
| :--- | :--- | :--- | :--- |
| **Forced Approval** | Card ending in `1111` | `APPROVED` | Issuing bank authorization granted |
| **Forced Decline** | Card ending in `0000` | `DECLINED` | Decline code handling & merchant messaging |
| **Forced Timeout** | Card ending in `9999` | `TIMEOUT` | Network latency & reconciliation handling |
| **Rule Trigger** | Amount ending in `.99` (e.g. `49.99`) | `DECLINED` | Risk engine rules & behavioral triggers |
| **Limit Exceeded** | Amount > `1,000,000.00` | `DECLINED` | Issuer credit/velocity limits |
| **Idempotency** | Submit same Idempotency Key twice | Same Result Cached | Network retry protection (prevents double charge) |

---

## 🤖 Ask the AI Payment Assistant

Click on **AI Tutor** and try questions like:
- *"What is the difference between Authorization and Settlement?"*
- *"Why do transactions fail with TIMEOUT?"*
- *"How does an idempotency key protect cardholders?"*
- *"What is PCI DSS and why are card numbers masked?"*

*The AI tutor retrieves relevant knowledge documents directly from MySQL via RAG and works completely offline without requiring an external paid API key.*

---

## 📦 Sharing With a Friend

To share this project:
1. Create a clean ZIP archive of this directory (ensure `node_modules` and `target` are omitted).
2. Send the ZIP file via WhatsApp or email.
3. Your friend can extract it and follow this guide or [DEPLOYMENT.md](file:///c:/JFS/CARD_PAYMENT_TRANSACTION_SIMULATOR/DEPLOYMENT.md) for 1-click cloud deployment on Render!
