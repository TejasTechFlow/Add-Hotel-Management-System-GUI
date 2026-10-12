-- ============================================================
--  Hotel Management System - Database Schema
--  MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS hotel_management;
USE hotel_management;

-- ----------------------------------------------------------------
-- USERS  (Admin / Staff login)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id      INT           AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50)   NOT NULL UNIQUE,
    password     VARCHAR(255)  NOT NULL,          -- SHA-256 hashed
    role         ENUM('ADMIN','STAFF') NOT NULL DEFAULT 'STAFF',
    full_name    VARCHAR(100)  NOT NULL,
    email        VARCHAR(100),
    phone        VARCHAR(15),
    is_active    TINYINT(1)    NOT NULL DEFAULT 1,
    created_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------
-- ROOMS
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rooms (
    room_id      INT           AUTO_INCREMENT PRIMARY KEY,
    room_number  VARCHAR(10)   NOT NULL UNIQUE,
    room_type    ENUM('SINGLE','DOUBLE','SUITE','DELUXE') NOT NULL,
    price_per_night DECIMAL(10,2) NOT NULL,
    status       ENUM('AVAILABLE','BOOKED','MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    floor_number INT           NOT NULL DEFAULT 1,
    description  TEXT,
    created_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------
-- CUSTOMERS
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
    customer_id  INT           AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100)  NOT NULL,
    contact      VARCHAR(15)   NOT NULL,
    email        VARCHAR(100),
    address      TEXT,
    id_proof     VARCHAR(50),                     -- Passport / Aadhaar / Driving License
    id_number    VARCHAR(50),
    created_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------
-- BOOKINGS
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bookings (
    booking_id   INT           AUTO_INCREMENT PRIMARY KEY,
    customer_id  INT           NOT NULL,
    room_id      INT           NOT NULL,
    check_in     DATE          NOT NULL,
    check_out    DATE          NOT NULL,
    status       ENUM('CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    booked_by    INT,                             -- user_id of staff who created booking
    created_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT fk_booking_room     FOREIGN KEY (room_id)     REFERENCES rooms(room_id),
    CONSTRAINT fk_booking_user     FOREIGN KEY (booked_by)   REFERENCES users(user_id)
);

-- ----------------------------------------------------------------
-- BILLS
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bills (
    bill_id        INT           AUTO_INCREMENT PRIMARY KEY,
    booking_id     INT           NOT NULL UNIQUE,
    total_amount   DECIMAL(10,2) NOT NULL,
    tax_amount     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount       DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    net_amount     DECIMAL(10,2) NOT NULL,
    payment_status ENUM('PENDING','PAID','PARTIALLY_PAID','REFUNDED') NOT NULL DEFAULT 'PENDING',
    payment_method ENUM('CASH','CARD','UPI','NET_BANKING') DEFAULT NULL,
    generated_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    paid_at        TIMESTAMP     NULL,
    CONSTRAINT fk_bill_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
);

-- ============================================================
--  SAMPLE TEST DATA
-- ============================================================

-- Users (password = "admin123" and "staff123" — SHA-256 hashed)
INSERT INTO users (username, password, role, full_name, email, phone) VALUES
('admin',  SHA2('admin123', 256), 'ADMIN', 'System Administrator', 'admin@hotel.com',  '9000000001'),
('staff1', SHA2('staff123', 256), 'STAFF', 'Rahul Sharma',         'rahul@hotel.com', '9000000002'),
('staff2', SHA2('staff123', 256), 'STAFF', 'Priya Patil',          'priya@hotel.com', '9000000003');

-- Rooms
INSERT INTO rooms (room_number, room_type, price_per_night, floor_number, description) VALUES
('101', 'SINGLE',  1500.00, 1, 'Cozy single room with garden view'),
('102', 'SINGLE',  1500.00, 1, 'Cozy single room with pool view'),
('201', 'DOUBLE',  2500.00, 2, 'Spacious double room with city view'),
('202', 'DOUBLE',  2500.00, 2, 'Spacious double room with garden view'),
('301', 'DELUXE',  4000.00, 3, 'Deluxe room with king bed and balcony'),
('302', 'DELUXE',  4000.00, 3, 'Deluxe room with premium amenities'),
('401', 'SUITE',   8000.00, 4, 'Luxury suite with panoramic view'),
('402', 'SUITE',   8000.00, 4, 'Presidential suite with private terrace');

-- Customers
INSERT INTO customers (name, contact, email, address, id_proof, id_number) VALUES
('Amit Desai',    '9876543210', 'amit@email.com',   'Pune, Maharashtra',    'Aadhaar', 'XXXX-XXXX-1234'),
('Sneha Joshi',   '9876543211', 'sneha@email.com',  'Mumbai, Maharashtra',  'Passport','P1234567'),
('Rohan Kulkarni','9876543212', 'rohan@email.com',  'Nashik, Maharashtra',  'DL',      'MH-12-2019-001234'),
('Meena Iyer',    '9876543213', 'meena@email.com',  'Bangalore, Karnataka', 'Aadhaar', 'XXXX-XXXX-5678');

-- Bookings
INSERT INTO bookings (customer_id, room_id, check_in, check_out, status, booked_by) VALUES
(1, 1, CURDATE() - INTERVAL 3 DAY, CURDATE() - INTERVAL 1 DAY, 'CHECKED_OUT', 1),
(2, 3, CURDATE(),                  CURDATE() + INTERVAL 2 DAY,  'CHECKED_IN',  2),
(3, 5, CURDATE() + INTERVAL 1 DAY, CURDATE() + INTERVAL 3 DAY,  'CONFIRMED',   2),
(4, 7, CURDATE() - INTERVAL 1 DAY, CURDATE() + INTERVAL 1 DAY,  'CHECKED_IN',  1);

-- Bills
INSERT INTO bills (booking_id, total_amount, tax_amount, discount, net_amount, payment_status, payment_method, paid_at) VALUES
(1, 3000.00, 270.00, 0.00, 3270.00, 'PAID',    'CASH', NOW() - INTERVAL 1 DAY),
(2, 5000.00, 450.00, 0.00, 5450.00, 'PENDING', NULL,   NULL),
(4,16000.00,1440.00, 0.00,17440.00, 'PENDING', NULL,   NULL);

-- ============================================================
--  USEFUL VIEWS
-- ============================================================

CREATE OR REPLACE VIEW v_available_rooms AS
    SELECT room_id, room_number, room_type, price_per_night, floor_number, description
    FROM   rooms
    WHERE  status = 'AVAILABLE';

CREATE OR REPLACE VIEW v_booking_details AS
    SELECT b.booking_id, c.name AS customer_name, c.contact,
           r.room_number, r.room_type, r.price_per_night,
           b.check_in, b.check_out,
           DATEDIFF(b.check_out, b.check_in) AS nights,
           b.status AS booking_status,
           bl.net_amount, bl.payment_status
    FROM   bookings b
    JOIN   customers c ON b.customer_id = c.customer_id
    JOIN   rooms     r ON b.room_id     = r.room_id
    LEFT JOIN bills  bl ON b.booking_id = bl.booking_id;

CREATE OR REPLACE VIEW v_revenue_report AS
    SELECT DATE(bl.generated_at) AS report_date,
           COUNT(bl.bill_id)     AS total_bills,
           SUM(bl.net_amount)    AS total_revenue,
           SUM(CASE WHEN bl.payment_status = 'PAID' THEN bl.net_amount ELSE 0 END) AS collected_revenue
    FROM   bills bl
    GROUP  BY DATE(bl.generated_at);
