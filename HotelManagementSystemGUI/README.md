# 🏨 Hotel Management System — GUI Edition
**Java Swing + JDBC + MySQL | Full Graphical Interface**

---

## 📁 Project Structure

```
HotelManagementSystemGUI/
├── src/com/hotel/
│   ├── HotelManagementApp.java          ← GUI entry point
│   ├── model/        User, Room, Customer, Booking, Bill
│   ├── dao/          UserDAO, RoomDAO, CustomerDAO, BookingDAO, BillDAO
│   ├── service/      AuthService, RoomService, CustomerService,
│   │                 BookingService, BillingService, ReportService
│   ├── util/         DatabaseConnection, PasswordUtil, InputValidator
│   ├── exception/    DatabaseException, EntityNotFoundException, ValidationException
│   └── ui/
│       ├── LoginFrame.java              ← Branded login window
│       ├── MainFrame.java               ← Shell (sidebar + CardLayout)
│       ├── components/
│       │   ├── Theme.java               ← Colours, fonts, sizes
│       │   ├── UIUtils.java             ← Buttons, inputs, tables, toasts
│       │   ├── Sidebar.java             ← Dark nav rail
│       │   └── StatsCard.java           ← KPI metric tile
│       ├── panels/
│       │   ├── DashboardPanel.java      ← Overview + recent bookings
│       │   ├── RoomPanel.java           ← Room CRUD
│       │   ├── CustomerPanel.java       ← Customer CRUD
│       │   ├── BookingPanel.java        ← Full booking lifecycle
│       │   ├── BillingPanel.java        ← Bill generation & payment
│       │   ├── ReportPanel.java         ← 4-tab report viewer
│       │   └── UserPanel.java           ← Admin user management
│       └── dialogs/
│           ├── RoomDialog.java          ← Add/Edit room modal
│           ├── CustomerDialog.java      ← Add/Edit customer modal
│           ├── BookingDialog.java       ← New booking wizard
│           └── UserDialog.java          ← Add/Edit user modal
├── sql/
│   └── hotel_schema.sql                 ← DB schema + sample data
├── lib/
│   └── mysql-connector-j-8.x.x.jar     ← Place JDBC driver here
├── build.sh                             ← Mac/Linux compile & run
├── build.bat                            ← Windows compile & run
└── README.md
```

---

## 🖥️ GUI Features

| Screen | What You Can Do |
|--------|----------------|
| **Login** | Branded split-screen with credential validation |
| **Dashboard** | 4 KPI cards (rooms, customers, bookings, revenue) + recent bookings table + today's check-in/out list |
| **Rooms** | Full CRUD table, search, filter by status, inline Edit/Delete/Maintenance buttons |
| **Customers** | Full CRUD, live search by name/phone/email |
| **Bookings** | Create bookings with room selector, inline Check-In / Check-Out / Cancel per row |
| **Billing** | Generate bills with discount, process payment via modal, receipt viewer |
| **Reports** | 4 tabs: Daily Bookings, Available Rooms, Revenue (date range), Today's Summary |
| **Users** | Admin-only — add/edit/deactivate staff accounts |

---

## ⚙️ OOP Concepts in the GUI

| Concept | Where |
|---------|-------|
| **Encapsulation** | All model POJOs; Theme keeps UI constants private |
| **Inheritance** | All panels are `JPanel` subclasses; dialogs extend `JDialog` |
| **Polymorphism** | `MainFrame` calls `show()` via panel references; `ActionRenderer`/`ActionEditor` override Swing interfaces |
| **Abstraction** | `UIUtils` factory methods hide Swing boilerplate; `Theme` centralises all styling |

---

## 🚀 Setup & Run

### Step 1 — Database
```sql
source sql/hotel_schema.sql
```

### Step 2 — JDBC Driver
Download `mysql-connector-j-8.x.x.jar` from https://dev.mysql.com/downloads/connector/j/  
Place the JAR in the `lib/` folder.

### Step 3 — Credentials
Edit `src/com/hotel/util/DatabaseConnection.java`:
```java
private static final String USER     = "root";
private static final String PASSWORD = "your_password";
```

### Step 4 — Compile & Run

**Mac / Linux:**
```bash
chmod +x build.sh
./build.sh
```

**Windows:**
```cmd
build.bat
```

**IntelliJ IDEA / Eclipse:**
1. Open folder as project
2. Mark `src/` as Sources Root
3. Add JDBC JAR: Project Structure → Libraries → +
4. Run `HotelManagementApp.java`

---

## 🔐 Default Logins

| Username | Password  | Role  | Access |
|----------|-----------|-------|--------|
| `admin`  | `admin123`| ADMIN | Full access including user management & revenue reports |
| `staff1` | `staff123`| STAFF | Rooms, customers, bookings, billing |

---

## 🗄️ Database Tables

```
users       (user_id, username, password, role, full_name, email, phone, is_active)
rooms       (room_id, room_number, room_type, price_per_night, status, floor_number, description)
customers   (customer_id, name, contact, email, address, id_proof, id_number)
bookings    (booking_id, customer_id, room_id, check_in, check_out, status, booked_by)
bills       (bill_id, booking_id, total_amount, tax_amount, discount, net_amount, payment_status, payment_method)
```

---

## 🧪 Quick Test Flow

```
1. Run app → Login: admin / admin123
2. Dashboard → review KPI tiles
3. Rooms → Add Room (e.g. Room 501, SUITE, ₹10000, Floor 5)
4. Customers → Add Customer (any name + 10-digit phone)
5. Bookings → New Booking → select customer + room + dates → Confirm
6. Bookings → Click "Check-In" on the new booking row
7. Billing → Generate Bill (enter Booking ID) → Process Payment → CASH
8. Reports → Revenue tab → today's date range
```
