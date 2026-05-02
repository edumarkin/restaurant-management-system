# 🍽️ Restaurant Management System

A JavaFX desktop application for managing a restaurant — branches, menus, tables, reservations, orders, kitchen workflow, billing, and payments. Built with **Java 17 + JavaFX 21 + MySQL 8 + Maven**, designed to be readable and beginner-friendly.

---

## ✨ Features

Five fully-working roles, each with their own dashboard:

| Role | What they can do |
|------|------------------|
| **Manager** | Overview stats · CRUD employees · CRUD menu items · CRUD tables |
| **Receptionist** | Search free tables by date/time/people · Reserve · Auto-create new customers · Check-in · Cancel reservations |
| **Waiter** | Pick a table · Start a new order · Add menu items per seat with quantity · Send order to kitchen · View active orders |
| **Chef** | Kitchen queue showing all `PREPARING` orders · Inspect order items · Mark as ready |
| **Cashier** | Issue check (auto 10 % tax) · Add tip · Pay with **Cash / Credit Card / Check** · Auto-completes order and frees the table |

Plus: dark themed UI with a warm orange accent, login screen with one demo account per role, automatic table-status tracking (FREE → RESERVED → OCCUPIED → FREE), and reservation-conflict detection (±2 hours).

---

## 🧰 Prerequisites

You need these installed:

1. **JDK 17 or newer** — [Adoptium / Temurin](https://adoptium.net/) is free and works great
2. **MySQL Server 8.x** — running locally on `localhost:3306`
3. **IntelliJ IDEA** — Community Edition is fine
4. **MySQL Workbench** (or the `mysql` CLI) — for running the schema file

> ⚠️ Maven is **not** required separately — IntelliJ has it bundled.

---

## 🚀 Setup — Step by Step

### Step 1 — Create the database

Open **MySQL Workbench** (or the terminal `mysql` client) and run the SQL file at:

```
src/main/resources/sql/schema.sql
```

In Workbench: `File → Open SQL Script…`, choose `schema.sql`, then click the ⚡ lightning bolt to execute the whole script.
In CLI:
```bash
mysql -u root -p < src/main/resources/sql/schema.sql
```

This creates the `restaurant_db` database, all tables, and the seed data (1 demo account per role + sample menu + 6 tables).

### Step 2 — Configure the connection

Open **`src/main/resources/db.properties`** and edit the `db.user` and `db.password` so they match your local MySQL:

```properties
db.url=jdbc:mysql://localhost:3306/restaurant_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.user=root
db.password=YOUR_MYSQL_PASSWORD_HERE
```

### Step 3 — Open the project in IntelliJ

1. Launch IntelliJ IDEA.
2. **File → Open…** → select the `restaurant-management-system` folder (the one containing `pom.xml`) → click **OK**.
3. IntelliJ will detect it as a Maven project. When the popup appears, click **Load Maven Project** / **Trust Project**.
4. Wait for the bottom-right progress bar to finish (it's downloading JavaFX and the MySQL connector — first time only, ~30 seconds).
5. Make sure the project SDK is set: **File → Project Structure → Project → SDK** → pick a 17 or higher JDK. Set **Language Level** to **17**.

### Step 4 — Run it

You have two options. **Option A** is the easiest.

#### ✅ Option A — Run via the Maven JavaFX plugin (recommended)

1. Open the **Maven** tool window on the right side of IntelliJ.
2. Expand `restaurant-management-system → Plugins → javafx`.
3. Double-click **`javafx:run`**.

That's it — the app launches.

#### Option B — Run `Main.java` directly

1. Open `src/main/java/com/restaurant/app/Main.java`.
2. Click the green ▶ next to the `main` method.

If JavaFX complains about missing modules with this option, use Option A instead, or add these VM options to the run configuration (**Run → Edit Configurations… → Modify options → Add VM options**):

```
--module-path "/path/to/javafx-sdk-21/lib" --add-modules javafx.controls,javafx.fxml
```

### Step 5 — Log in

The login screen accepts these demo accounts (all use password `1234`):

| Username | Role |
|----------|------|
| `manager` | Manager |
| `receptionist` | Receptionist |
| `waiter` | Waiter |
| `chef` | Chef |
| `cashier` | Cashier |

---

## 🎬 Suggested demo flow (impress your professor)

1. Log in as **receptionist** → reserve Table 4 for 4 people for tonight at 19:00 → use phone `+998935551122` (existing customer John Doe).
2. Log out, log in as **waiter** → pick Table 4 → "Start new order" → add Caesar Salad (seat 1), Beef Steak (seat 1), Margherita Pizza (seat 2), Cola ×2 → click **Send to kitchen**.
3. Log in as **chef** → see the order in the kitchen queue → click on it to view items per seat → click **Mark as Ready**.
4. Log in as **cashier** → "Issue check" tab → select the order → **Issue check** (10% tax computed automatically) → switch to "Unpaid bills" tab → set tip $5 → **Pay (CASH)**.
5. Log in as **manager** → Overview shows updated stats; Tables tab shows Table 4 is back to FREE.

---

## 🛠 Project Structure

```
restaurant-management-system/
├── pom.xml                         ← Maven config (JavaFX + MySQL deps)
└── src/main/
    ├── java/com/restaurant/
    │   ├── app/        Main.java   ← Entry point
    │   ├── db/         Database    ← JDBC connection helper
    │   ├── model/      POJOs       ← Plain data classes
    │   ├── dao/        *DAO        ← All SQL lives here
    │   ├── ui/         *Dashboard  ← One file per role + LoginView, MainView
    │   └── util/       Session, Dialogs
    └── resources/
        ├── css/theme.css           ← Dark theme styling
        ├── db.properties           ← MySQL credentials (you edit this)
        └── sql/schema.sql          ← DB schema + seed data
```

---

## 🐞 Troubleshooting

**"Database connection failed" popup at startup**
- Is the MySQL service running? On Windows check Services; on Mac/Linux: `mysql.server status` or `systemctl status mysql`.
- Did you edit `db.properties` with the correct password?
- Did you run `schema.sql`? Verify with `SHOW DATABASES;` — you should see `restaurant_db`.
- If the error mentions `Public Key Retrieval is not allowed`, the URL in `db.properties` already has `allowPublicKeyRetrieval=true` — make sure you didn't remove it.

**"Error: JavaFX runtime components are missing"**
- Use the Maven `javafx:run` goal (Option A above) — that's literally what it's there to fix.

**Maven dependencies fail to download**
- Check your internet connection.
- In IntelliJ, right-click `pom.xml` → **Maven → Reload Project**.

**Login fails with the demo accounts**
- Re-run `schema.sql` (it does a `DROP DATABASE IF EXISTS` first, so it's safe to run again).

**Port 3306 already in use / MySQL on a different port**
- Update `db.url` in `db.properties` to point at your actual port, e.g. `jdbc:mysql://localhost:3307/restaurant_db?...`

---

## 📝 Notes for the assignment

- Maps to the spec's actors: Manager, Receptionist, Waiter, Chef, Cashier (all five implemented).
- Implements the requested use cases: Add/Modify Tables, Search Tables, Place Order, Update Order, Create Reservation, Cancel Reservation, Check-in, Make Payment.
- Order status flow: `RECEIVED` → `PREPARING` (waiter sends to kitchen) → back to `RECEIVED` ("ready" — chef has plated it) → `COMPLETE` (cashier processes payment).
- Passwords are stored in plain text on purpose — this is a school project, kept simple so it's easy to demo and inspect the database.

Have fun! 🍕
