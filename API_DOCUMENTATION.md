# 📡 REST API Documentation — Card Payment Transaction Simulator

The Card Payment Transaction Simulator provides a RESTful API compliant with OpenAPI 3.0 standards.

- **Base URL (Local)**: `http://localhost:8080`
- **Interactive Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON Spec**: `http://localhost:8080/api-docs`

---

## 🔒 Authentication

All endpoints under `/api/**` (except `/api/auth/register` and `/api/auth/login`) require a valid JWT token passed in the `Authorization` header:

```http
Authorization: Bearer <your_jwt_token>
```

Tokens are signed using HS512 with a 1-hour expiration.

---

## 📑 Endpoints Catalog

### 1. Authentication Endpoints

#### Register User
- **Method**: `POST`
- **Path**: `/api/auth/register`
- **Access**: Public
- **Request Body**:
```json
{
  "name": "Alex Morgan",
  "email": "alex@example.com",
  "password": "Password@123"
}
```
- **Response**: `201 Created`
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "user": {
    "id": 1,
    "name": "Alex Morgan",
    "email": "alex@example.com",
    "role": "USER",
    "active": true
  }
}
```

#### Log In
- **Method**: `POST`
- **Path**: `/api/auth/login`
- **Access**: Public
- **Request Body**:
```json
{
  "email": "alex@example.com",
  "password": "Password@123"
}
```
- **Response**: `200 OK` (Same schema as register)

#### Get Current User Profile
- **Method**: `GET`
- **Path**: `/api/auth/me`
- **Access**: Authenticated (`Bearer <token>`)
- **Response**: `200 OK` (UserInfo object)

---

### 2. Transaction Simulation Endpoints

#### Simulate Payment
- **Method**: `POST`
- **Path**: `/api/transactions/simulate`
- **Access**: Authenticated
- **Request Body**:
```json
{
  "merchantName": "Amazon India",
  "amount": 1499.00,
  "currency": "INR",
  "maskedCardSuffix": "**** **** **** 1111",
  "cardholderName": "ALEX MORGAN",
  "description": "Electronics purchase",
  "idempotencyKey": "a93e3d64-42b7-4a02-b2f5-2579df64c001"
}
```
- **Response**: `201 Created`
```json
{
  "id": 1,
  "transactionReference": "TXN-2026-A1B2C3D4",
  "merchantName": "Amazon India",
  "amount": 1499.00,
  "currency": "INR",
  "maskedCardSuffix": "**** **** **** 1111",
  "cardholderName": "ALEX MORGAN",
  "status": "APPROVED",
  "reasonCode": "TEST_APPROVED",
  "statusMessage": "Simulated approval: Test card ending in 1111",
  "description": "Electronics purchase",
  "processingDurationMs": 24,
  "createdAt": "2026-10-11T13:45:00",
  "processedAt": "2026-10-11T13:45:00"
}
```

#### Get Filtered User Transactions
- **Method**: `GET`
- **Path**: `/api/transactions`
- **Access**: Authenticated (Scoped to user)
- **Query Parameters**:
  - `search`: string (merchant name or transaction reference)
  - `status`: string (`APPROVED`, `DECLINED`, `TIMEOUT`)
  - `fromDate`: ISO DateTime (`2026-10-01T00:00:00`)
  - `toDate`: ISO DateTime (`2026-10-11T23:59:59`)
  - `minAmount`: decimal
  - `maxAmount`: decimal
  - `sortBy`: string (`createdAt`, `amount`, `merchantName`, `status`)
  - `sortDir`: string (`asc`, `desc`)
  - `page`: integer (0-indexed)
  - `size`: integer (default 10)
- **Response**: `200 OK` (Spring Page object with `content`, `totalPages`, `totalElements`)

#### Get Single Transaction By ID
- **Method**: `GET`
- **Path**: `/api/transactions/{id}`
- **Access**: Authenticated (Ownership verified)

#### Export Transactions as CSV
- **Method**: `GET`
- **Path**: `/api/transactions/export/csv`
- **Access**: Authenticated
- **Response**: `200 OK` (`text/csv` stream with `Content-Disposition: attachment; filename="transactions.csv"`)

---

### 3. Analytics Dashboard Endpoints

#### User Dashboard Summary
- **Method**: `GET`
- **Path**: `/api/dashboard/summary`
- **Response**: `200 OK`
```json
{
  "totalTransactions": 25,
  "approvedCount": 18,
  "declinedCount": 5,
  "timeoutCount": 2,
  "approvalRate": 72.0,
  "totalAmount": 45200.50,
  "recentTransactions": [...]
}
```

#### Outcome Distribution
- **Method**: `GET`
- **Path**: `/api/dashboard/status-distribution`
- **Response**: `200 OK` (Array of status counts for pie/donut chart)

#### Daily Trends
- **Method**: `GET`
- **Path**: `/api/dashboard/trends?days=14`
- **Response**: `200 OK` (Array of daily volume data points for area charts)

---

### 4. RAG AI Payment Assistant Endpoints

#### Chat with Assistant
- **Method**: `POST`
- **Path**: `/api/ai/chat`
- **Request Body**:
```json
{
  "message": "What is the difference between authorization and settlement?",
  "conversationId": 1
}
```
- **Response**: `200 OK`
```json
{
  "conversationId": 1,
  "conversationTitle": "What is the difference...",
  "reply": "Authorization and settlement are two distinct phases...",
  "sources": [
    "Authorization vs Settlement",
    "What is a Payment Transaction?"
  ],
  "timestamp": "2026-10-11T14:00:00"
}
```

#### Get User Conversation Threads
- **Method**: `GET`
- **Path**: `/api/ai/conversations`

#### Get Single Conversation with Messages
- **Method**: `GET`
- **Path**: `/api/ai/conversations/{id}`

#### Delete Conversation Thread
- **Method**: `DELETE`
- **Path**: `/api/ai/conversations/{id}`
- **Response**: `204 No Content`

---

### 5. Administrator Governance Endpoints

*(Requires `ROLE_ADMIN`)*

- `GET /api/admin/dashboard`: Global system metrics
- `GET /api/admin/users`: Paginated list of users with transaction counts
- `PUT /api/admin/users/{id}/status?active={true|false}`: Toggle active status
- `PUT /api/admin/users/{id}/role?role={USER|ADMIN}`: Promote / demote role
- `GET /api/admin/transactions`: Global audit ledger across all users

---

## ⚠️ Error Handling & Status Codes

All errors return a standardized `ApiErrorResponse` payload:

```json
{
  "timestamp": "2026-10-11T13:45:00",
  "status": 409,
  "error": "DUPLICATE_RESOURCE",
  "message": "User already exists with email: alex@example.com",
  "path": "/api/auth/register"
}
```

| HTTP Status | Error Code | Meaning |
| :--- | :--- | :--- |
| `400 Bad Request` | `VALIDATION_FAILED` | Input validation failed or invalid payload |
| `401 Unauthorized` | `UNAUTHORIZED` | Missing, expired, or invalid JWT token |
| `403 Forbidden` | `ACCESS_DENIED` | Insufficient role permissions (e.g. non-admin accessing `/api/admin`) |
| `404 Not Found` | `RESOURCE_NOT_FOUND`| Requested entity does not exist |
| `409 Conflict` | `DUPLICATE_RESOURCE` | Email already registered or resource conflict |
| `429 Too Many Requests`| `RATE_LIMIT_EXCEEDED`| Rate limit exceeded; wait 60 seconds |
| `500 Internal Error`| `INTERNAL_ERROR` | Unexpected server error |
