CREATE DATABASE IF NOT EXISTS traffic_fine_db;
USE traffic_fine_db;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'OWNER') NOT NULL,
    full_name VARCHAR(100),
    phone VARCHAR(15),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS vehicles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    plate_number VARCHAR(20) UNIQUE NOT NULL,
    owner_id INT,
    vehicle_type ENUM('CAR','BIKE','TRUCK','BUS') NOT NULL,
    brand VARCHAR(50),
    color VARCHAR(30),
    FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS violation_types (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    fine_amount DECIMAL(10,2) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS fines (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id INT NOT NULL,
    violation_id INT NOT NULL,
    officer_id INT NOT NULL,
    captured_image_path VARCHAR(255),
    plate_detected VARCHAR(20),
    fine_amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING','PAID','DISPUTED') DEFAULT 'PENDING',
    location VARCHAR(100),
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP NULL,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    FOREIGN KEY (violation_id) REFERENCES violation_types(id),
    FOREIGN KEY (officer_id) REFERENCES users(id)
);

INSERT IGNORE INTO violation_types (name, fine_amount, description) VALUES
('Red Light Jump', 500.00, 'Vehicle crossed red signal'),
('Over Speeding', 1000.00, 'Exceeded speed limit'),
('No Helmet', 300.00, 'Rider without helmet'),
('Wrong Parking', 200.00, 'Parked in no-parking zone'),
('No Seatbelt', 250.00, 'Driver without seatbelt'),
('Using Mobile While Driving', 750.00, 'Phone usage while driving'),
('Triple Riding', 400.00, 'More than 2 persons on bike'),
('No License', 1500.00, 'Driving without valid license');

INSERT IGNORE INTO users (username, password, role, full_name, phone) VALUES
('admin', 'admin123', 'ADMIN', 'Traffic Officer Singh', '9876543210'),
('owner1', 'owner123', 'OWNER', 'Rahul Sharma', '9123456780');

INSERT IGNORE INTO vehicles (plate_number, owner_id, vehicle_type, brand, color) VALUES
('MH12AB1234', 2, 'CAR', 'Maruti Swift', 'White'),
('MH14CD5678', 2, 'BIKE', 'Honda Activa', 'Black');
