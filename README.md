# BillBot — Modern Billing & Invoice Management Android App

**BillBot** is an enterprise-grade, offline-first billing and invoice management application built for retail shops, grocery stores, wholesalers, and small-to-medium businesses.

---

## 📱 Technology Stack

- **Language:** Kotlin 2.0.20
- **UI Framework:** Jetpack Compose with Material 3 Design
- **Local Persistence:** Android Room Database (SQLite) with immutable line-item snapshots
- **Asynchronous Architecture:** Kotlin Coroutines & reactive Flow streams
- **Document Engine:** Android Native `android.graphics.pdf.PdfDocument` & `Canvas` vector rendering
- **File Sharing:** Android `FileProvider` sandboxed native sharing
- **Architecture:** MVVM + Clean Repository Pattern
- **Precision Arithmetic:** Decimal-safe `BigDecimal` with `RoundingMode.HALF_UP` for Indian Rupees (₹)

---

## 🌟 Key Features

### 1. 📊 Executive Dashboard
- **Today's Sales (₹):** Real-time aggregation of today's settled payments.
- **Total Invoices Count:** Total historical bills generated.
- **Pending Payments (₹):** Live tracking of outstanding customer balances.
- **Hero "+ Create New Bill" Card:** Fast POS entry point with gradient styling.
- **Low Stock Alerts:** Automatic warnings when catalog items fall below 5 units.
- **Recent Invoices:** Quick glance cards with payment status badges (Paid, Partial, Unpaid).

### 2. 📦 Product & Inventory Management
- **Catalog Management:** Add, edit, and delete products with name, unique product SKU/code, unit price, stock quantity, and unit of measurement (pcs, kg, litre, pack).
- **Instant Search:** Real-time search by product name or product SKU.
- **Stock Badges:** Dynamic color-coded stock chips (`In Stock`, `Low Stock ≤ 5`, `Out of Stock`).
- **Validation:** Strict validation preventing negative prices or invalid quantities.

### 3. 🧾 Fast Billing & POS
- **Product Catalog Picker:** Tap to add products directly to the bill with automatic quantity increments.
- **Line-Item Controls:** `[-]` and `[+]` quantity steppers, editable price overrides, and custom ad-hoc line items.
- **Live Calculations:** Subtotal, Discount percentage chips (0%, 5%, 10%, 15%, 20%), GST / Tax percentage toggle (0%, 5%, 12%, 18%, 28%), and Grand Total.
- **Customer Selection:** Fast toggle between **Walk-in Customer** (no registration needed) or **Registered Customer** selection.
- **Settlement & Payment Method:** Label payment modes as **Cash**, **UPI**, or **Card**.
- **Payment Status Logic:** Supports **Fully Paid**, **Partially Paid**, and **Unpaid (Credit)** invoices with real-time balance calculations.
- **Duplicate Prevention:** Safe submitting lock preventing double invoice clicks.

### 4. 👥 Customer Management
- **Customer Directory:** Store customer names, phone numbers, email addresses, and shop addresses.
- **Purchase History Modal:** View complete order history for each customer, including total amount spent and current pending balance.
- **Full CRUD:** Add, edit, or delete customer profiles without affecting historical invoices.

### 5. 📄 Invoice Management & Native PDF
- **Room Database Invoicing:** Atomic transactions saving invoice metadata and item snapshots.
- **Search & Filters:** Search by invoice number or customer name; filter by status (**All**, **Paid**, **Partial**, **Unpaid**).
- **Invoice Details Screen:** Complete breakdown of items, financial summary, and customer snapshot.
- **Record Payment Modal:** Collect additional payments on unpaid or partially paid bills with real-time balance updates.
- **Native A4 Vector PDF Generation:** Generates crisp, printable A4 PDF invoices featuring shop branding, GSTIN, customer details, striped item table, tax breakdown, and watermark status stamp.
- **Native Android Sharing:** Instantly share invoice PDFs to WhatsApp, Gmail, Drive, or Bluetooth via Android `FileProvider`.

### 6. 📈 Reports & Analytics
- **Time Period Filters:** View performance for **Today**, **This Week**, **This Month**, or **All Time**.
- **Revenue Metrics:** Total Sales, Paid Revenue, and Outstanding Credit.
- **Payment Modes Breakdown:** Separate revenue and invoice count for Cash, UPI, and Card.
- **Best-Selling Products:** Leaderboard ranking products by units sold and revenue generated.

### 7. ⚙️ Business Settings & Configuration
- **Shop Profile:** Customize Shop Name, Address, Phone, Email, and GSTIN.
- **Invoice Numbering:** Configure custom invoice prefix (e.g., `INV-`, `BILL-`) and starting sequence number (e.g., `1001`) with live preview.
- **Taxes & Currency:** Toggle GST by default, set default tax percentage, and preview Indian Rupee (INR - ₹) formatting.
- **Terms & Footer:** Customizable legal terms and invoice thank-you message.

---

## 🗄️ Database Architecture & Snapshot Integrity

When an invoice is issued, the exact item names, codes, unit prices, and quantities are copied directly into `InvoiceItemEntity`. If a product's price or description changes later in the catalog, **historical invoices and past financial reports remain completely unchanged**.

---

## 🧪 Unit Tests

The test suite validates calculations, sequence generators, and business invariants:
- **`BillingCalculatorTest`**: Validates line-item calculations, fractional prices, negative quantity sanitization, subtotal, discount percentages, GST calculations, grand totals, payment status categorization (`PAID`, `PARTIAL`, `UNPAID`), and balance calculations.
- **`InvoiceNumberGeneratorTest`**: Validates zero-padding, custom prefixes, and sequence extraction.
- **`InvoiceBusinessLogicTest`**: Validates payment state transitions ($0 \to \text{Partial} \to \text{Paid}$), stock deduction invariants, and snapshot preservation.

Run tests via command line:
```bash
.\gradlew.bat testDebugUnitTest
```

---

## 🚀 How to Run the App

### Option A: Using Android Studio
1. Open **Android Studio**.
2. Select **File > Open** and choose the `billbot` directory:
   ```
   c:\Users\Hp\OneDrive\Desktop\billbot
   ```
3. Allow Gradle to sync dependencies.
4. Select an Android Emulator (API 26+) or connect a physical Android device with **USB Debugging** enabled.
5. Click the green **Run ▶** button (or press `Shift + F10`).

### Option B: Installing the Debug APK Directly on a Device
The debug APK has already been compiled and is ready in the root directory:
```
c:\Users\Hp\OneDrive\Desktop\billbot\BillBot-debug.apk
```

To install on a connected device via ADB:
```bash
adb install -r BillBot-debug.apk
```
Or transfer `BillBot-debug.apk` to your Android phone via USB cable / WhatsApp / Drive, open the file, allow installation from unknown sources, and launch **BillBot**.

---

## 🛠️ How to Generate a Fresh Debug APK

Run the Gradle wrapper assemble command:
```bash
.\gradlew.bat assembleDebug
```
The newly generated APK will be output to:
```
C:\Users\Hp\.billbot_build\app\outputs\apk\debug\app-debug.apk
```
