# Restaurant Management System

A desktop application built with Java and JavaFX that lets a restaurant team manage everything — from taking reservations to sending food to the kitchen and processing payments — all from one place.

This was built as a university project, but it works like a real system. Five different staff roles each get their own screen, and they all connect to the same database, so changes made by one person show up for everyone else in real time.

---

## What it does

When a customer walks in or calls to book a table, the **receptionist** searches for available tables by date, time, and party size. If one is free, they reserve it in seconds. The system automatically marks that table as reserved so no one double-books it.

When the customer arrives and sits down, the **waiter** opens a new order for their table, picks items from the menu for each seat, and sends the order to the kitchen with one click.

The **chef** sees every incoming order appear in their kitchen queue. They can expand any order to see exactly what each seat ordered, then mark it as ready when the food is plated.

The **cashier** pulls up the bill, applies a tip if the customer wants, and processes payment — cash, card, or check. The moment the bill is paid, the table automatically goes back to "free" so the receptionist can book it again.

The **manager** has an overview of everything — how many employees are working, how many tables are occupied, upcoming reservations — and can add or remove staff, update the menu, and manage tables.

On top of all this, the system automatically pops up a notification when a reservation is coming up in the next 30 minutes, so the front desk never misses a booking.

---

## Tech stack

- **Java 17** — the programming language
- **JavaFX 21** — for building the desktop UI (all in code, no FXML)
- **MySQL 8** — the database that stores everything
- **Maven** — handles dependencies and building the project
- **JDBC** — for talking to the database directly (no ORM, easy to read)

---

## Accounts for testing

All accounts use the password `1234`.

| Username | Role |
|----------|------|
| `manager` | Manager |
| `receptionist` | Receptionist |
| `waiter` | Waiter |
| `chef` | Chef |
| `cashier` | Cashier |

---

## Getting it running

### What you need installed first

- **JDK 17 or newer** — grab it from [adoptium.net](https://adoptium.net) if you don't have it
- **MySQL 8** — download the installer from [dev.mysql.com](https://dev.mysql.com/downloads/installer/)
- **MySQL Workbench** — comes with the MySQL installer, used to set up the database
- **IntelliJ IDEA** — Community Edition is free and works perfectly

### Step 1 — Set up the database

Open MySQL Workbench, connect to your local MySQL server, then go to:

**File → Open SQL Script → select `src/main/resources/sql/schema.sql`**

Click the lightning bolt ⚡ to run the whole script. It will create the `restaurant_db` database with all the tables and fill it with sample data — a menu, 6 tables, and one staff account per role.

### Step 2 — Enter your MySQL password

Open the file `src/main/resources/db.properties` in any text editor and change the password to whatever you set when you installed MySQL:

```
db.url=jdbc:mysql://localhost:3306/restaurant_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.user=root
db.password=your_password_here
```

Save it and close.

### Step 3 — Open in IntelliJ

Open IntelliJ IDEA and click **File → Open**, then select the `restaurant-management-system` folder — the one that contains `pom.xml`. Click OK.

When IntelliJ asks if you trust the project, click **Trust Project**. It will start downloading JavaFX and the MySQL connector automatically. This only happens once and takes about 30 seconds.

### Step 4 — Run the app

Look for the **Maven panel** on the right side of IntelliJ (click the `m` icon). Expand:

```
Plugins → javafx → javafx:run
```

Double-click `javafx:run`. The login screen should appear.

If you want a one-click shortcut for next time: go to **Run → Edit Configurations → + → Maven**, set the command to `javafx:run` and the working directory to the project folder. Save it, and from now on just press the green play button.

---

## Running on multiple computers at the same time

If you want to demo the app with multiple people connected to the same database (great for showing the real-time sync), one computer runs MySQL and the others connect to it over the network.

On the host computer, find your local IP address by running `ipconfig` in Command Prompt and looking for the IPv4 address. Then open MySQL Workbench and run:

```sql
CREATE USER 'root'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON restaurant_db.* TO 'root'@'%';
FLUSH PRIVILEGES;
```

Also allow MySQL through the firewall by running this in Command Prompt as Administrator:

```
netsh advfirewall firewall add rule name="MySQL" protocol=TCP dir=in localport=3306 action=allow
```

On each other computer, open `db.properties` and replace `localhost` with the host computer's IP address. Run the app normally. Everyone will be connected to the same database and can see each other's actions after clicking Refresh.

---

## Project structure

```
restaurant-management-system/
├── pom.xml
└── src/main/
    ├── java/com/restaurant/
    │   ├── app/
    │   │   └── Main.java               ← starts the app
    │   ├── db/
    │   │   └── Database.java           ← opens MySQL connections
    │   ├── model/                      ← plain Java classes (Employee, Order, Bill, etc.)
    │   ├── dao/                        ← all database queries live here
    │   ├── ui/                         ← one dashboard file per role
    │   └── util/
    │       ├── Session.java            ← tracks who is logged in
    │       ├── Dialogs.java            ← popup helpers
    │       └── NotificationService.java ← reservation reminders
    └── resources/
        ├── css/theme.css               ← warm bistro theme
        ├── db.properties               ← your MySQL credentials go here
        └── sql/schema.sql              ← run this once to create the database
```

---

## Common problems

**"Database connection failed" when starting**
Make sure MySQL is running. Open Task Manager, go to the Services tab, and look for MySQL80 — it should say Running. If it's stopped, right-click and start it. Also double-check your password in `db.properties`.

**The app opens but looks completely unstyled**
This happens if IntelliJ cached an old version of the CSS. Run `mvn clean javafx:run` in the terminal instead of using the play button.

**"JavaFX runtime components are missing"**
Don't run Main.java directly — use `javafx:run` from the Maven panel. That's what it's there for.

**Login doesn't work with the demo accounts**
Re-run `schema.sql` in MySQL Workbench. It resets everything back to the original state.

**My friend can't connect to my database**
Make sure they're on the same WiFi or hotspot as you. If you're using a school network, it might be blocking connections between computers — switch to a mobile hotspot instead. Also make sure you did the firewall step above.

---

## How the order flow works

It goes like this, step by step:

1. Receptionist reserves a table → table status changes to **Reserved**
2. Receptionist checks in the customer when they arrive
3. Waiter creates an order for the table → status changes to **Occupied**
4. Waiter adds items from the menu for each seat and sends to kitchen
5. Chef sees the order in the queue, marks it as **Ready** when done
6. Cashier issues the bill (10% tax added automatically), adds tip if any
7. Cashier processes payment (cash / card / check)
8. Table automatically goes back to **Free**

---

## Notes

Passwords are stored as plain text in the database. This is intentional — it keeps the code simple and makes it easy to inspect the data during a demo or grading. In a real production system you would hash them.

The notification system checks for upcoming reservations every 60 seconds and shows a popup if any reservation is within 30 minutes. It only alerts once per reservation so it doesn't spam you.

// we used GPT to write this one. not that essential, right?
