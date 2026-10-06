# OfflinePaymentSystem

**S3 BTech College Project**  
*Educational Proof-of-Concept for an Interoperable Offline Payment System using Bank-Authorized Spending Tokens*

---

## 📌 Project Overview

In environments with limited or no internet connectivity (e.g., transit systems, rural markets, flights, and disaster zones), standard digital payment gateways (like UPI, card networks, and net banking) fail because both the customer and merchant devices cannot establish an active connection to the central bank.

**OfflinePaymentSystem** demonstrates an **interoperable offline payment model**:
1. **Bank-Authorized Spending Tokens**: The issuing bank pre-allocates an offline spending limit/token to the user during an online session.
2. **Interoperable Handover**: The customer generates a dynamic QR code containing payment details, merchant identification, and a simulated bank authorization digest.
3. **Offline Merchant Verification**: The merchant scans the QR code from an image file (PNG) using ZXing, verifies the details on screen, and accepts or rejects the transaction completely offline.
4. **Store-and-Forward Settlement**: Accepted transactions are saved locally with a **`PENDING_SYNC`** status. When the merchant terminal reconnects to the network, the transaction batch is settled with the central banking ledger.

---

## ⚠️ Important Educational Limitations

* **Simulation Only**: This project is strictly an educational prototype and is **not** integrated with real banking systems, NPCI, UPI, or card payment networks.
* **No Cryptographic Security Claims**: The project uses a simulated SHA-256 digest for demonstration purposes. It does **not** claim to be cryptographically secure against real-world adversarial attacks (real systems require Hardware Security Modules (HSM), Trusted Execution Environments (TEE/eSE), and certified PKI digital signatures).
* **In-Memory Storage**: For this initial version, all transaction records and token balances are stored in-memory.
* **Pre-Loaded Sample Tokens**: The application provides sample bank tokens (State Bank of India, HDFC Bank, Canara Bank) to demonstrate multi-bank interoperability.

---

## 🛠️ Technology Stack

* **Programming Language**: Java 17+
* **Build & Dependency Management**: Apache Maven
* **User Interface**: Java Swing with modern FlatLaf styling
* **QR Code Engine**: ZXing (Zebra Crossing) `core:3.5.3` and `javase:3.5.3`

---

## 🚀 How to Run the Application

### Prerequisites
Make sure **Java 17+** and **Maven** are installed.  
*(On this machine, Java 17 and Maven 3.9.6 are configured in `C:\Users\LENOVO\AppData\Local\Programs\jdk` and `C:\Users\LENOVO\AppData\Local\Programs\maven`).*

### Method 1: Using Maven (Recommended)
Open a terminal in the project directory (`c:\Users\LENOVO\OneDrive\Scans\Offline payment system`) and execute:

```bash
# 1. Compile project
mvn clean compile

# 2. Run the application
mvn exec:java
```

### Method 2: One-Click Windows Batch Script
You can double-click or run:
```cmd
run.bat
```

### Method 3: Build & Run Executable Standalone JAR
```bash
# Package the standalone JAR with all dependencies
mvn clean package

# Run the packaged JAR
java -jar target/OfflinePaymentSystem-1.0.0.jar
```

---

## 📋 Step-by-Step Demonstration Guide (For Project Viva / Evaluation)

### Step 1: Customer Screen (Initiate Payment)
1. Open the application. The **Customer Screen** tab is selected by default.
2. Select an authorized bank token from the dropdown (e.g., `TOK-SBI-8821` with available balance ₹3,500.00).
3. Enter the **Merchant Name** (e.g., *Apollo Pharmacy* or choose a quick chip).
4. Enter the **Payment Amount** (e.g., *₹250.00*).
5. Click **Generate Payment QR Code**.
6. A 260x260 QR code will appear on the right side along with transaction details and simulated authorization digest.

### Step 2: Save QR Image as PNG
* Click **Quick Save (qr_payment.png)** to save the QR code directly to the workspace folder, or
* Click **Save QR as PNG...** to choose a custom save destination and filename.

### Step 3: Merchant Screen (Scan & Verify)
1. Switch to the **Merchant Screen (Scan & Verify)** tab.
2. Notice the Merchant Terminal is set to *Apollo Pharmacy* (or customize it).
3. Click **Quick Scan (qr_payment.png)** or **Browse & Scan QR Image** to select the saved PNG.
4. The system decodes the QR code via ZXing and displays:
   - Scanned Amount (₹250.00)
   - Merchant Name & match status
   - Customer Name & ID
   - Spending Token ID & Issuing Bank
   - Transaction ID & Timestamp
   - Simulated Authorization Hash
5. Click **Accept Payment (Store Offline)**.
   - The token balance is deducted.
   - The transaction is recorded in the offline ledger with **`PENDING_SYNC`** status.
   - An offline acceptance receipt is displayed.

### Step 4: Transaction History & Settlement Sync
1. Switch to the **Transaction History** tab.
2. Observe the transaction row:
   - Status badge: **⏳ Pending Sync**
   - Details: Tx ID, Customer, Merchant, Amount, Token ID.
   - Counter metrics at the top update dynamically.
3. Click **Simulate Bank Sync (Go Online)**:
   - Simulates internet reconnection and batch transmission to the central bank.
   - The transaction status transitions to **✅ Synced & Settled**.
   - Demonstrates the complete store-and-forward lifecycle to project examiners.

### Step 5: Architecture & Viva Notes
1. Open the **Architecture & Viva Notes** tab in the application for an on-screen summary of:
   - Offline token architectures
   - Interoperability principles
   - Comparison with online UPI systems
   - Limitations and theoretical future enhancements (e.g., double-spending prevention, hardware counters).

---

## 📂 Project Directory Structure

```
OfflinePaymentSystem/
├── pom.xml                               # Maven project configuration & ZXing dependencies
├── README.md                             # Project documentation and presentation guide
├── run.bat                               # Windows launcher script
├── build.bat                             # Windows build script
└── src/
    └── main/
        └── java/
            └── com/
                └── offlinepay/
                    ├── Main.java         # Application entry point (Look & Feel + Launch)
                    ├── model/
                    │   ├── SpendingToken.java      # Bank-authorized spending token representation
                    │   ├── PaymentPayload.java     # QR payload structure, serializer & parser
                    │   ├── TransactionRecord.java  # Offline ledger transaction model
                    │   └── TransactionStatus.java  # PENDING_SYNC, SYNCED, REJECTED statuses
                    ├── service/
                    │   ├── QRCodeService.java      # ZXing QR generation and image decoding
                    │   ├── TokenService.java       # Multi-bank token management & balances
                    │   └── TransactionStore.java   # In-memory ledger and batch bank sync
                    └── ui/
                        ├── ThemeConstants.java     # Modern styling, colors, and badge utilities
                        ├── MainFrame.java          # Tabbed main window & limitations dialog
                        ├── CustomerPanel.java      # Customer QR creation and PNG export
                        ├── MerchantPanel.java      # Merchant QR scanning & accept/reject
                        └── HistoryPanel.java       # Transaction table with pending-sync badges
```

---

## 🎓 S3 BTech Viva Q&A Cheat Sheet

1. **Q: Why are bank-authorized spending tokens needed?**  
   *A:* In offline mode, neither party can query the bank's live database. A bank-authorized token acts as a pre-committed offline spending quota verified by signature or hash.

2. **Q: How does the system handle interoperability?**  
   *A:* The QR payload follows a standard data protocol (`OFFLINE_PAY_V1`). Any merchant terminal adhering to the specification can decode and accept tokens from multiple banks (e.g., SBI, HDFC, Canara).

3. **Q: What does "Pending Sync" mean?**  
   *A:* The merchant has verified and accepted the token offline. The funds have not yet moved between accounts on the core banking system. The transaction remains in `PENDING_SYNC` until network connectivity enables batch settlement.

4. **Q: What prevents double spending in real-world offline payment systems?**  
   *A:* In production CBDC / offline systems, double spending is prevented using tamper-resistant secure hardware elements (e.g. smartcards, eSIM, Secure Enclaves) with monotonically increasing hardware transaction counters. In this educational prototype, it is modeled through simulated balances and batch reconciliation.
