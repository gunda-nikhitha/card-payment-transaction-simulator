-- =============================================
-- V5__seed_knowledge_documents.sql
-- Seed educational content for RAG
-- =============================================

INSERT INTO knowledge_documents (title, category, content, source_type) VALUES

('What is a Payment Transaction?', 'PAYMENT_BASICS',
'A payment transaction is the process of transferring money from a buyer (cardholder) to a seller (merchant) in exchange for goods or services. In a card payment system, this involves several parties: the cardholder who initiates the payment, the merchant who accepts it, the acquiring bank (merchant''s bank), the issuing bank (cardholder''s bank), and the card network (like Visa or Mastercard) that facilitates communication between banks. Each transaction goes through multiple stages including authorization, clearing, and settlement. The entire process typically takes just seconds for authorization but can take days for full settlement.',
'INTERNAL'),

('Authorization vs Settlement', 'PAYMENT_BASICS',
'Authorization and settlement are two distinct phases in payment processing. Authorization is the real-time process where the issuing bank approves or declines a transaction request. When you tap your card at a store, an authorization request travels from the merchant''s terminal through the acquiring bank and card network to the issuing bank, which checks if the card is valid and has sufficient funds. If approved, the funds are placed on hold. Settlement happens later (usually 1-3 business days) when the actual money transfer occurs between the issuing bank and acquiring bank. During settlement, the held funds are actually debited from the cardholder''s account and credited to the merchant''s account, minus any processing fees.',
'INTERNAL'),

('Transaction Outcomes: Approved, Declined, Timeout', 'TRANSACTION_STATUS',
'Every payment transaction results in one of these outcomes: APPROVED means the issuing bank has authorized the transaction and the payment will proceed. The cardholder has sufficient funds, the card is valid, and no fraud is suspected. DECLINED means the issuing bank has rejected the transaction. Common decline reasons include insufficient funds, expired card, incorrect PIN, suspected fraud, or exceeding daily limits. The decline code helps identify the specific reason. TIMEOUT means the system did not receive a response within the expected timeframe. This can happen due to network issues, bank system outages, or high processing loads. Timeouts require special handling because the transaction state is uncertain — it might have been processed by the issuing bank but the response was lost.',
'INTERNAL'),

('Merchant Information in Payments', 'PAYMENT_BASICS',
'Merchant information is crucial in payment processing. Every transaction includes a Merchant Name that appears on the cardholder''s statement, a Merchant Category Code (MCC) that classifies the business type (e.g., 5411 for grocery stores, 5812 for restaurants), and a Merchant ID assigned by the acquiring bank. The MCC affects interchange fees, rewards eligibility, and fraud detection rules. In our simulator, we capture the merchant name to simulate realistic transaction records. The merchant name helps identify where a payment was made and is essential for transaction history and dispute resolution.',
'INTERNAL'),

('Transaction Lifecycle', 'TRANSACTION_STATUS',
'A payment transaction follows this lifecycle in our simulator: 1) RECEIVED - The transaction request arrives at the server and passes initial validation. 2) PROCESSING - The simulation engine evaluates the transaction based on configured rules. 3) Final State - One of three outcomes: APPROVED (payment accepted), DECLINED (payment rejected with a reason code), or TIMEOUT (processing did not complete in time). In a real payment system, additional states might include CAPTURED (funds transferred), REFUNDED (money returned), VOIDED (authorization cancelled before settlement), and CHARGED_BACK (disputed by cardholder). Our simulator focuses on the authorization phase to demonstrate the core concepts.',
'INTERNAL'),

('HTTP Status Codes in REST APIs', 'API_CONCEPTS',
'HTTP status codes indicate the result of an API request. 200 OK means the request succeeded. 201 Created means a new resource was created (used after registration or transaction creation). 400 Bad Request means the client sent invalid data (missing fields, wrong format). 401 Unauthorized means authentication is required or credentials are invalid. 403 Forbidden means the user is authenticated but lacks permission. 404 Not Found means the requested resource does not exist. 409 Conflict means the request conflicts with current state (like duplicate email registration). 422 Unprocessable Entity means validation failed. 429 Too Many Requests means rate limit exceeded. 500 Internal Server Error means something went wrong on the server. Good API design returns appropriate status codes with meaningful error messages.',
'INTERNAL'),

('REST API Design Principles', 'API_CONCEPTS',
'REST (Representational State Transfer) APIs follow key principles: 1) Resources are identified by URLs (e.g., /api/v1/transactions). 2) HTTP methods define actions: GET (read), POST (create), PUT/PATCH (update), DELETE (remove). 3) Stateless: each request contains all needed information. 4) Consistent response format with proper status codes. 5) Versioning (e.g., /api/v1/) allows API evolution. In this simulator, the API follows RESTful conventions: POST /api/v1/transactions/simulate creates a new transaction, GET /api/v1/transactions lists them with filtering and pagination. DTOs (Data Transfer Objects) separate the API contract from internal database entities.',
'INTERNAL'),

('JWT Authentication Explained', 'SECURITY',
'JSON Web Tokens (JWT) provide stateless authentication for APIs. When a user logs in with valid credentials, the server generates a JWT containing the user''s ID, email, role, and an expiration time. This token is signed with a secret key. The client includes this token in the Authorization header of subsequent requests: "Authorization: Bearer <token>". The server validates the token signature and expiration on each request without needing to query a session store. JWT consists of three parts: Header (algorithm info), Payload (user claims), and Signature (verification hash). Benefits include scalability (no server-side session storage), cross-domain support, and self-contained user information. Security best practices include using short expiration times, HTTPS only, and never storing sensitive data in the token payload.',
'INTERNAL'),

('Spring Security and Authorization', 'SECURITY',
'Spring Security provides comprehensive security for Java applications. In this simulator, it handles: 1) Authentication - verifying user identity via JWT tokens. 2) Authorization - controlling access based on roles (USER, ADMIN). 3) Password Encoding - using BCrypt to hash passwords before storage. 4) CORS - controlling which frontend origins can access the API. 5) CSRF - disabled for stateless JWT-based APIs. The security filter chain processes each request: JwtAuthenticationFilter extracts and validates the token, loads user details, and sets the security context. Method-level security (@PreAuthorize) restricts specific endpoints to certain roles. Users can only access their own data, while admins can view all data.',
'INTERNAL'),

('How This Simulator Differs from Real Payment Gateways', 'SIMULATOR_INFO',
'This Card Payment Transaction Simulator is an educational tool, NOT a real payment gateway. Key differences: 1) No real money is transferred - all transactions are simulated. 2) No real card validation - we use masked test card numbers (e.g., **** **** **** 1234). 3) No connection to banking networks - outcomes are determined by configurable simulation rules. 4) No PCI DSS compliance required - we never collect or store real card data. 5) Simplified lifecycle - we simulate authorization only, not the full capture/settlement cycle. 6) Deterministic outcomes - you can trigger specific results for testing. Real payment gateways like Stripe, Razorpay, or PayPal connect to banking infrastructure, implement extensive fraud detection, comply with PCI DSS security standards, and handle complex edge cases like partial captures, recurring payments, and multi-currency conversions.',
'INTERNAL'),

('Common Errors and Troubleshooting', 'TROUBLESHOOTING',
'Common issues and solutions: 1) "401 Unauthorized" - Your JWT token has expired or is missing. Log in again to get a fresh token. 2) "403 Forbidden" - You don''t have the required role. Admin endpoints need ADMIN role. 3) "400 Bad Request" - Check that all required fields are provided and valid (amount > 0, merchant name present). 4) "409 Conflict" - Duplicate idempotency key or email already registered. 5) Transaction stuck in PROCESSING - In the simulator, this shouldn''t happen; contact support. 6) Database connection errors - Ensure MySQL is running and credentials are correct. 7) CORS errors - The frontend origin must be in the allowed origins list. 8) AI chatbot not responding - Check if AI_API_KEY is configured. The chatbot works in fallback mode without an API key.',
'INTERNAL'),

('Understanding Idempotency in Payments', 'PAYMENT_BASICS',
'Idempotency ensures that performing the same operation multiple times produces the same result. In payment processing, this is critical: if a network error occurs after submitting a payment, the client might retry the request. Without idempotency, this could result in double charges. Our simulator implements idempotency using an idempotency key - a unique string sent with the transaction request. If the server receives a request with a key that was already processed, it returns the original result instead of creating a duplicate transaction. The key is stored in the database with a unique constraint. Best practices: generate a UUID as the idempotency key on the client side, include it in the request header or body, and implement server-side duplicate detection.',
'INTERNAL'),

('Card Data Security and PCI DSS', 'SECURITY',
'PCI DSS (Payment Card Industry Data Security Standard) governs how organizations handle card data. In real payment systems, card numbers must be encrypted in transit and at rest, access must be restricted, and systems must be regularly tested. Our simulator deliberately avoids handling real card data: we only accept masked card suffixes (e.g., **** **** **** 1234) and never ask for CVV, PIN, or full card numbers. This is both a safety measure and an educational point — students learn that proper payment systems must comply with strict security standards. Never store real card numbers, CVVs, or PINs in any database. In production systems, use tokenization to replace card numbers with non-sensitive tokens.',
'INTERNAL'),

('Dashboard Analytics Explained', 'SIMULATOR_INFO',
'The simulator dashboard provides analytics derived from actual transaction data in MySQL. Key metrics include: Total Transactions (count of all simulation attempts), Approval Rate (approved / total completed × 100), Transaction Volume Over Time (daily/weekly trends), Status Distribution (pie/bar chart of APPROVED vs DECLINED vs TIMEOUT), and Total Amount Simulated (sum of all transaction amounts). These analytics help understand transaction patterns. In real payment systems, similar dashboards help merchants monitor their business health, identify fraud patterns, optimize payment routing, and track revenue. All dashboard data comes from database aggregate queries, not hardcoded values.',
'INTERNAL');
