# 🚀 Cloud Deployment Guide — Render

This guide outlines how to deploy the **Card Payment Transaction Simulator** to [Render](https://render.com) with the fewest practical steps.

---

## 📋 Prerequisites

1. A [GitHub](https://github.com) account.
2. A free [Render](https://render.com) account.
3. A cloud-hosted MySQL 8 database instance (options listed below).

---

## Step 1: Push Code to GitHub

After extracting the ZIP archive on your computer:

```bash
cd CARD_PAYMENT_TRANSACTION_SIMULATOR

# Initialize git repository
git init
git add .
git commit -m "Initial commit: Card Payment Transaction Simulator"

# Add your GitHub repository remote
git branch -M main
git remote add origin https://github.com/<your-username>/card-payment-transaction-simulator.git

# Push code to GitHub
git push -u origin main
```

*(Note: `.gitignore` automatically prevents `node_modules`, `target`, and secrets from being committed).*

---

## Step 2: Set Up a Free Cloud MySQL Database

Render offers managed PostgreSQL, but CPTS uses MySQL 8 with Flyway. You can acquire a free cloud MySQL instance in 2 minutes from any of these providers:

### Recommended Free MySQL Providers:
- **[Aiven](https://aiven.io)**: Free MySQL tier (reputable, low latency, standard MySQL 8 with persistent storage — **Recommended**).
- **[Railway](https://railway.app)**: Free/Starter tier with 1-click MySQL provisioning.
- **[Clever Cloud](https://www.clever-cloud.com)**: Free MySQL database addon.

> **💡 Render Pricing Transparency Note**: Render provides free tiers for Docker Web Services and Static Sites, but does **not** provide a free managed MySQL service (Render only provides PostgreSQL). If you wish to run MySQL as a container directly on Render instead of an external provider, it requires a paid Private Service ($7/mo) and a Persistent Disk ($0.25/GB/mo). Using Aiven or Railway for MySQL keeps your deployment **100% free**.

Once provisioned, note your connection details:
- **Host**: e.g., `mysql-xxxx.aivencloud.com`
- **Port**: e.g., `3306` (or custom port)
- **Database Name**: e.g., `defaultdb` or `cpts_db`
- **User**: e.g., `avnadmin`
- **Password**: `your-secure-password`

Your JDBC URL will look like:
```text
jdbc:mysql://<HOST>:<PORT>/<DATABASE>?createDatabaseIfNotExist=true&useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC
```

---

## Step 3: Deploy to Render

### Option A: Render Blueprint (Easiest)

1. Log into your **Render Dashboard**.
2. Click **Blueprints** ➡️ **New Blueprint Instance**.
3. Connect your GitHub repository `card-payment-transaction-simulator`.
4. Render will automatically parse the included [`render.yaml`](file:///c:/JFS/CARD_PAYMENT_TRANSACTION_SIMULATOR/render.yaml) file and configure both services:
   - `cpts-backend` (Docker Web Service)
   - `cpts-frontend` (Static Site)
5. Fill in the requested secret environment variables:
   - `DB_URL`: Your JDBC connection string from Step 2.
   - `DB_USERNAME`: Database username.
   - `DB_PASSWORD`: Database password.
6. Click **Apply**. Render will automatically build the backend Docker container and compile the React frontend bundle!

---

### Option B: Manual Web Service Deployment

If you prefer deploying services manually in Render:

#### 1. Deploy the Backend:
1. Click **New +** ➡️ **Web Service**.
2. Select your repository.
3. Configure settings:
   - **Name**: `cpts-backend`
   - **Root Directory**: `backend`
   - **Runtime**: `Docker`
   - **Health Check Path**: `/actuator/health`
4. Add the following **Environment Variables**:
   | Variable | Value |
   | :--- | :--- |
   | `PORT` | `8080` |
   | `DB_URL` | Your JDBC connection string |
   | `DB_USERNAME` | Database username |
   | `DB_PASSWORD` | Database password |
   | `JWT_SECRET` | A random 64+ character string |
   | `CORS_ALLOWED_ORIGINS` | `https://*.onrender.com,http://localhost:5173,http://localhost:3000` (or exact frontend URL) |
   | `AI_ENABLED` | `false` (or `true` if you provide an OpenAI key) |
5. Click **Create Web Service**. Note the assigned URL (e.g. `https://cpts-backend.onrender.com`).

#### 2. Deploy the Frontend:
1. Click **New +** ➡️ **Static Site**.
2. Select your repository.
3. Configure settings:
   - **Name**: `cpts-frontend`
   - **Root Directory**: `frontend`
   - **Build Command**: `npm install && npm run build`
   - **Publish Directory**: `dist`
4. Add the following **Environment Variable**:
   | Variable | Value |
   | :--- | :--- |
   | `VITE_API_BASE_URL` | Your backend URL from above (e.g. `https://cpts-backend.onrender.com`) |
5. In **Redirects/Rewrites**:
   - Add a rewrite: Source `/*` ➡️ Destination `/index.html` (Status `Rewrite`).
6. Click **Create Static Site**.

---

## 🔍 Verification & Health Check

1. **Backend Health**: Visit `https://<backend-url>/actuator/health`. It should return `{"status":"UP"}`.
2. **Swagger UI**: Visit `https://<backend-url>/swagger-ui.html` to explore all interactive REST APIs.
3. **Frontend UI**: Open your `cpts-frontend` URL, log in with demo credentials, and simulate a payment transaction.

---

## 🛠️ Common Render Troubleshooting

### 1. Free Tier "Cold Start" Delays
Render free web services spin down after 15 minutes of inactivity. The initial request may take ~50 seconds while the container wakes up. Subsequent requests respond in milliseconds.

### 2. CORS Errors
If your frontend reports CORS errors in the browser console:
- Ensure the backend environment variable `CORS_ALLOWED_ORIGINS` is set to `*` or includes your frontend domain URL (e.g. `https://cpts-frontend.onrender.com`).

### 3. Database SSL Handshake Failure
Ensure your `DB_URL` includes `&useSSL=true&allowPublicKeyRetrieval=true` if using cloud providers like Aiven or AWS.
