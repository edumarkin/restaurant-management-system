
DROP DATABASE IF EXISTS restaurant_db;
CREATE DATABASE restaurant_db;
USE restaurant_db;

CREATE TABLE branch (
    branch_id    INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    location     VARCHAR(255) NOT NULL
);

CREATE TABLE employee (
    employee_id  INT AUTO_INCREMENT PRIMARY KEY,
    branch_id    INT,
    username     VARCHAR(50)  UNIQUE NOT NULL,
    password     VARCHAR(255) NOT NULL,
    full_name    VARCHAR(100) NOT NULL,
    email        VARCHAR(100),
    phone        VARCHAR(30),
    role         ENUM('MANAGER','RECEPTIONIST','WAITER','CHEF','CASHIER') NOT NULL,
    status       ENUM('ACTIVE','CLOSED','BLACKLISTED') DEFAULT 'ACTIVE',
    date_joined  DATE NOT NULL,
    FOREIGN KEY (branch_id) REFERENCES branch(branch_id) ON DELETE SET NULL
);

CREATE TABLE customer (
    customer_id  INT AUTO_INCREMENT PRIMARY KEY,
    full_name    VARCHAR(100) NOT NULL,
    phone        VARCHAR(30),
    email        VARCHAR(100),
    last_visited DATETIME
);

CREATE TABLE menu (
    menu_id      INT AUTO_INCREMENT PRIMARY KEY,
    branch_id    INT NOT NULL,
    title        VARCHAR(100) NOT NULL,
    description  VARCHAR(255),
    FOREIGN KEY (branch_id) REFERENCES branch(branch_id) ON DELETE CASCADE
);

CREATE TABLE menu_section (
    section_id   INT AUTO_INCREMENT PRIMARY KEY,
    menu_id      INT NOT NULL,
    title        VARCHAR(100) NOT NULL,
    description  VARCHAR(255),
    FOREIGN KEY (menu_id) REFERENCES menu(menu_id) ON DELETE CASCADE
);

CREATE TABLE menu_item (
    item_id      INT AUTO_INCREMENT PRIMARY KEY,
    section_id   INT NOT NULL,
    title        VARCHAR(100) NOT NULL,
    description  VARCHAR(255),
    price        DECIMAL(10,2) NOT NULL DEFAULT 0,
    FOREIGN KEY (section_id) REFERENCES menu_section(section_id) ON DELETE CASCADE
);

CREATE TABLE restaurant_table (
    table_id            INT AUTO_INCREMENT PRIMARY KEY,
    branch_id           INT NOT NULL,
    table_number        INT NOT NULL,
    max_capacity        INT NOT NULL,
    location_identifier VARCHAR(50),
    status              ENUM('FREE','RESERVED','OCCUPIED','OTHER') DEFAULT 'FREE',
    FOREIGN KEY (branch_id) REFERENCES branch(branch_id) ON DELETE CASCADE
);

CREATE TABLE reservation (
    reservation_id   INT AUTO_INCREMENT PRIMARY KEY,
    customer_id      INT NOT NULL,
    table_id         INT NOT NULL,
    reservation_time DATETIME NOT NULL,
    people_count     INT NOT NULL,
    status           ENUM('REQUESTED','PENDING','CONFIRMED','CHECKED_IN','CANCELED','ABANDONED') DEFAULT 'CONFIRMED',
    notes            VARCHAR(255),
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (table_id)    REFERENCES restaurant_table(table_id) ON DELETE CASCADE
);

CREATE TABLE `order` (
    order_id     INT AUTO_INCREMENT PRIMARY KEY,
    table_id     INT NOT NULL,
    waiter_id    INT,
    created_at   DATETIME NOT NULL,
    status       ENUM('RECEIVED','PREPARING','COMPLETE','CANCELED','NONE') DEFAULT 'RECEIVED',
    FOREIGN KEY (table_id)  REFERENCES restaurant_table(table_id) ON DELETE CASCADE,
    FOREIGN KEY (waiter_id) REFERENCES employee(employee_id) ON DELETE SET NULL
);

CREATE TABLE order_item (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id      INT NOT NULL,
    item_id       INT NOT NULL,
    quantity      INT NOT NULL DEFAULT 1,
    seat_number   INT,
    FOREIGN KEY (order_id) REFERENCES `order`(order_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id)  REFERENCES menu_item(item_id)
);

CREATE TABLE bill (
    bill_id     INT AUTO_INCREMENT PRIMARY KEY,
    order_id    INT NOT NULL UNIQUE,
    amount      DECIMAL(10,2) NOT NULL,
    tax         DECIMAL(10,2) DEFAULT 0,
    tip         DECIMAL(10,2) DEFAULT 0,
    is_paid     BOOLEAN DEFAULT FALSE,
    created_at  DATETIME NOT NULL,
    FOREIGN KEY (order_id) REFERENCES `order`(order_id) ON DELETE CASCADE
);

CREATE TABLE payment (
    payment_id    INT AUTO_INCREMENT PRIMARY KEY,
    bill_id       INT NOT NULL,
    amount        DECIMAL(10,2) NOT NULL,
    method        ENUM('CASH','CREDIT_CARD','CHECK') NOT NULL,
    paid_at       DATETIME NOT NULL,
    FOREIGN KEY (bill_id) REFERENCES bill(bill_id) ON DELETE CASCADE
);

INSERT INTO branch (name, location) VALUES
    ('NewUU Branch', 'Movoronahhr Street, Hamid Olimjon, Tashkent'),
    ('Yunusabad Branch', '45 Mustaqillik St, Tashkent');

INSERT INTO employee (branch_id, username, password, full_name, email, phone, role, date_joined) VALUES
    (1, 'manager',      '4321', 'Umarkin Manager',     '250103@newuu.uz',     '+998931111111', 'MANAGER',      CURDATE()),
    (1, 'receptionist', '4321', 'Umarbek Receptionist',    '250102@newuu.uz',   '+998902222222', 'RECEPTIONIST', CURDATE()),
    (1, 'waiter',       '4321', 'Diyor Waiter',     'Diyor@legend.uz',      '+998904444444', 'WAITER',       CURDATE()),
    (1, 'chef',         '4321', 'Shokhjahon Chef',       'NU@nu.uz',        '+998903333333', 'CHEF',         CURDATE()),
    (1, 'cashier',      '4321', 'Muslimbek Cashier',    'Muslimbek@gmail.uz',     '+998905555555', 'CASHIER',      CURDATE());

INSERT INTO customer (full_name, phone, email) VALUES
    ('Trump JR.',      '911', 'trump@us.com'),
    ('Connor McGregor',  '1111', 'conor@ufc.com');

INSERT INTO menu (branch_id, title, description) VALUES
    (1, 'Main Menu', 'House menu of the Downtown Branch');

INSERT INTO menu_section (menu_id, title, description) VALUES
    (1, 'Starters',  'Light bites to start your meal'),
    (1, 'Main Course','Hearty mains'),
    (1, 'Desserts',  'Sweet endings'),
    (1, 'Drinks',    'Soft drinks, juice, water');

INSERT INTO menu_item (section_id, title, description, price) VALUES
    (1, 'Caesar Salad',     'Crisp romaine, parmesan, croutons',     6.50),
    (1, 'Tomato Soup',      'Roasted tomato cream soup',             4.00),
    (2, 'Grilled Chicken',  'Chicken breast, rice, vegetables',     12.00),
    (2, 'Beef Steak',       '250g ribeye, mashed potatoes',         18.50),
    (2, 'Margherita Pizza', 'Tomato, mozzarella, basil',            10.00),
    (3, 'Tiramisu',         'Classic Italian tiramisu',              5.00),
    (3, 'Chocolate Cake',   'Warm chocolate fondant',                5.50),
    (4, 'Cola',             '0.5L bottle',                           1.50),
    (4, 'Fresh Orange Juice','Hand-squeezed',                        3.00),
    (4, 'Mineral Water',    'Still or sparkling',                    1.00);

INSERT INTO restaurant_table (branch_id, table_number, max_capacity, location_identifier, status) VALUES
    (1, 1, 2, 'Window',  'FREE'),
    (1, 2, 4, 'Window',  'FREE'),
    (1, 3, 4, 'Center',  'FREE'),
    (1, 4, 6, 'Center',  'FREE'),
    (1, 5, 8, 'VIP',     'FREE'),
    (1, 6, 2, 'Terrace', 'FREE');
