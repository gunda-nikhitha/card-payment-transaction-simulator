# 🧪 Testing & Quality Assurance Guide — CPTS

This document outlines automated and manual verification procedures for the **Card Payment Transaction Simulator**.

---

## 🤖 1. Automated Test Suites

### Backend Tests (JUnit 5 + MockMvc + Spring Data JPA + H2)
The backend test suite executes 17 automated unit and integration tests with an isolated in-memory H2 database:

```bash
cd backend
# Using Maven Wrapper:
.\mvnw.cmd test
# Or with system Maven:
mvn test
```

#### What Is Tested:
1. **`SimulationEngineTest` (6 tests)**:
   - `shouldGenerateValidTransactionReference`: Ensures reference conforms to `^TXN-\d{4}-[A-Z0-9]{8}$`.
   - `shouldDeclineCardEndingIn0000`: Validates forced DECLINE for card suffix `0000`.
   - `shouldTimeoutCardEndingIn9999`: Validates forced TIMEOUT for card suffix `9999`.
   - `shouldApproveCardEndingIn1111`: Validates forced APPROVAL for card suffix `1111`.
   - `shouldDeclineAmountExceedingLimit`: Validates decline when amount > `1,000,000.00`.
   - `shouldDeclineAmountEndingIn99`: Validates decline trigger for `.99` rule.
2. **`JwtTokenProviderTest` (3 tests)**:
   - `shouldGenerateAndExtractClaims`: Verifies HS512 cryptographic signing and claims extraction.
   - `shouldRejectInvalidToken`: Verifies tampered token rejection.
   - `shouldRejectExpiredToken`: Verifies expired token rejection.
3. **`AuthControllerIntegrationTest` (4 tests)**:
   - `shouldRegisterNewUser`: Tests `POST /api/auth/register` (201 Created).
   - `shouldRejectDuplicateEmail`: Tests email conflict detection (409 Conflict).
   - `shouldLoginSuccessfully`: Tests `POST /api/auth/login` (200 OK + JWT).
   - `shouldDenyMeWithoutToken`: Tests unauthorized protection on `/api/auth/me` (401 Unauthorized).
4. **`TransactionServiceTest` (4 tests)**:
   - `shouldSimulateAndPersistTransaction`: Tests lifecycle transitions and MySQL persistence.
   - `shouldEnforceIdempotency`: Tests duplicate request with same key returns cached result without duplicate entry.
   - `shouldPreventCrossUserAccess`: Enforces that User A cannot read User B's transaction details.
   - `shouldRejectUnsupportedCurrency`: Validates rejection of currencies not in allowed whitelist.

---

### Frontend Unit Tests & Build Validation
1. **Frontend Unit Tests (Vitest)**:
   ```bash
   cd frontend
   npm test
   ```
   *Executes unit tests verifying API client authentication headers, 401 token clearing, and 204 response handling.*

2. **Frontend Production Build**:
   ```bash
   cd frontend
   npm run build
   ```
   *Compiles Vite bundle, verifies React JSX, and generates production distribution.*

---

## 🎯 2. Deterministic Simulation Triggers

You can test every edge case in the UI or via curl using these deterministic inputs:

| Trigger Scenario | Input Values | Expected Result | Reason Code |
| :--- | :--- | :--- | :--- |
| **Forced Approval** | Card: `**** 1111` | `APPROVED` | `TEST_APPROVED` |
| **Forced Decline** | Card: `**** 0000` | `DECLINED` | `TEST_DECLINE` |
| **Forced Timeout** | Card: `**** 9999` | `TIMEOUT` | `TEST_TIMEOUT` |
| **Trigger Rule .99**| Amount: `49.99` | `DECLINED` | `AMOUNT_TRIGGER_DECLINE` |
| **Exceed Limit** | Amount: `1500000.00` | `DECLINED` | `AMOUNT_EXCEEDED` |
| **Probabilistic** | Any other card suffix | 75% Approve, 20% Decline, 5% Timeout | `SIM_APPROVED` / `SIM_DECLINED` / `SIM_TIMEOUT` |

---

## 📋 3. Manual End-to-End Verification Checklist

| # | Feature Under Test | Steps to Verify | Expected Result |
| :- | :--- | :--- | :--- |
| 1 | **User Registration** | Go to Register page, enter name, email, password | Account created, JWT stored, redirected to Dashboard |
| 2 | **Duplicate Email Prevention** | Try registering with the same email again | Red error alert: `"User already exists with email: ..."` |
| 3 | **Login & JWT Storage** | Log out, log in with registered credentials | JWT stored in localStorage, session restored |
| 4 | **Simulate Payment** | Navigate to Simulate tab, click "Card 1111", click Process | Live stepper cycles 1 ➡️ 2 ➡️ 3, card displays `APPROVED` |
| 5 | **Simulate Decline** | Click "Card 0000", click Process | Card displays `DECLINED` with reason `TEST_DECLINE` |
| 6 | **Simulate Timeout** | Click "Card 9999", click Process | Card displays `TIMEOUT` with reason `TEST_TIMEOUT` |
| 7 | **Idempotency Protection** | Process a transaction without changing Idempotency Key | Returns identical existing transaction; no duplicate created |
| 8 | **History Ledger & Filters**| Go to History tab, filter by status `APPROVED` | Only approved transactions displayed in table |
| 9 | **CSV Export** | In History tab, click "Export to CSV" | Downloads `.csv` file with matching filtered records |
| 10 | **Inspect Modal** | Click the eye icon on any transaction row | Modal pops up with lifecycle breakdown and copy button |
| 11 | **Dashboard Analytics** | Go to Dashboard | KPI cards update, donut chart displays status distribution |
| 12 | **AI Assistant (RAG)** | Go to AI Tutor, ask: *"What is Authorization vs Settlement?"* | Returns educational explanation citing MySQL documents |
| 13 | **Admin Governance** | Log in as `admin@cpts.local`, visit Admin tab | Global metrics, user list, and toggle buttons visible |
| 14 | **Profile & Password** | Visit Account Settings, change password | Password updated; old password rejected on next login |
