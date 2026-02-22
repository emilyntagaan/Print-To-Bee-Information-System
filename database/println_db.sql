-- =============================================================
-- DATABASE: printing_system_db
-- =============================================================
CREATE DATABASE IF NOT EXISTS printing_system_db;
USE printing_system_db;

-- =============================================================
-- TABLE A: USERS
-- =============================================================
CREATE TABLE users (
    user_id INT(11) AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('Admin', 'Staff') NOT NULL,
    first_name VARCHAR(50) NOT NULL,    
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) DEFAULT NULL,
    contact_no VARCHAR(15) DEFAULT NULL,
    status ENUM('Active', 'Inactive') DEFAULT 'Active',
    date_created DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_login DATETIME DEFAULT NULL,
    address VARCHAR(100) DEFAULT NULL,
    profile_image VARCHAR(255) DEFAULT NULL,
    created_by INT(11),
    remarks VARCHAR(255) DEFAULT NULL,
    CONSTRAINT fk_user_created_by FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- =============================================================
-- TABLE B: CUSTOMERS
-- =============================================================
CREATE TABLE customers (
    customer_id INT(11) AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_no VARCHAR(15) DEFAULT NULL,
    email VARCHAR(100) DEFAULT NULL,
    address VARCHAR(150) DEFAULT NULL,
    date_registered DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_orders INT(11) DEFAULT 0,
    last_order_date DATE DEFAULT NULL,
    customer_type ENUM('Regular', 'New') DEFAULT 'New',
    status ENUM('Active', 'Inactive') DEFAULT 'Active',
    gender ENUM('Male', 'Female', 'Other') DEFAULT NULL,
    notes VARCHAR(255) DEFAULT NULL,
    city VARCHAR(50) DEFAULT NULL,
    created_by INT(11),
    data_updated DATETIME DEFAULT NULL,
    CONSTRAINT fk_customer_created_by FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- =============================================================
-- TABLE D: INVENTORY
-- =============================================================
CREATE TABLE inventory (
    inventory_id INT(11) AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) DEFAULT NULL,
    category VARCHAR(50) DEFAULT NULL,
    unit VARCHAR(20) NOT NULL,
    quantity INT(11) DEFAULT 0,
    reorder_level INT(11) DEFAULT 10,
    supplier_name VARCHAR(100) DEFAULT NULL,
    last_restock_date DATE DEFAULT NULL,
    status ENUM('Available', 'Low', 'Out of Stock') DEFAULT 'Available',
    added_by INT(11),
    date_added DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_updated DATETIME DEFAULT NULL,
    remarks VARCHAR(255) DEFAULT NULL,
    cost_per_unit DECIMAL(10,2) DEFAULT NULL,
    CONSTRAINT fk_inventory_added_by FOREIGN KEY (added_by) REFERENCES users(user_id)
);

-- =============================================================
-- TABLE C: PRODUCTS
-- =============================================================
CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    category VARCHAR(50) DEFAULT NULL,
    price DECIMAL(10,2) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    material_used VARCHAR(100) DEFAULT NULL,
    quantity_used INT DEFAULT 0,
    reorder_level INT DEFAULT 10,
    status ENUM('Active', 'Inactive') DEFAULT 'Active',
    date_added DATETIME DEFAULT CURRENT_TIMESTAMP,
    added_by INT DEFAULT NULL,
    print_time VARCHAR(50) DEFAULT NULL,
    size VARCHAR(50) DEFAULT NULL,
    notes VARCHAR(255) DEFAULT NULL,
    inventory_id INT DEFAULT NULL,
    CONSTRAINT fk_product_added_by FOREIGN KEY (added_by) REFERENCES users(user_id),
    CONSTRAINT fk_product_inventory FOREIGN KEY (inventory_id) REFERENCES inventory(inventory_id)
);

-- =============================================================
-- TABLE E: ORDERS
-- =============================================================
CREATE TABLE orders (
    order_id INT(11) AUTO_INCREMENT PRIMARY KEY,
    customer_id INT(11) DEFAULT NULL,
    user_id INT(11) NOT NULL,
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    due_date DATE DEFAULT NULL,
    status ENUM('Pending', 'In Progress', 'Completed') DEFAULT 'Pending',
    total_amount DECIMAL(10,2) NOT NULL,
    payment_status ENUM('Paid', 'Unpaid') DEFAULT 'Unpaid',
    payment_method VARCHAR(30) DEFAULT NULL,
    discount DECIMAL(10,2) DEFAULT 0.00,    
    remarks VARCHAR(255) DEFAULT NULL,
    quantity_total INT(11) DEFAULT 0,
    order_reference VARCHAR(50) UNIQUE,
    date_completed DATE DEFAULT NULL,
    printed_by INT(11) DEFAULT NULL,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_orders_printed_by FOREIGN KEY (printed_by) REFERENCES users(user_id)
);

-- =============================================================
-- TABLE F: ORDER DETAILS
-- =============================================================
CREATE TABLE order_details (
    orderdetail_id INT(11) AUTO_INCREMENT PRIMARY KEY,
    order_id INT(11) NOT NULL,
    product_id INT(11) NOT NULL,
    quantity INT(11) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    material_used VARCHAR(100) DEFAULT NULL,
    discount DECIMAL(10,2) DEFAULT 0.00,
    print_size VARCHAR(50) DEFAULT NULL,
    color_type VARCHAR(30) DEFAULT NULL,
    remarks VARCHAR(255) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by INT(11),
    date_updated DATETIME DEFAULT NULL,
    tax DECIMAL(10,2) DEFAULT 0.00,
    CONSTRAINT fk_orderdetails_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_orderdetails_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_orderdetails_user FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- =============================================================
-- TABLE G: LOGS
-- =============================================================
CREATE TABLE logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    action VARCHAR(100),
    description TEXT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- =============================================================
-- INSERT DEFAULT ADMIN ACCOUNT
-- =============================================================
SET FOREIGN_KEY_CHECKS=0;
INSERT INTO users (username, password, role, first_name, last_name)
VALUES ('admin', 'admin123', 'Admin', 'System', 'Administrator');
SET FOREIGN_KEY_CHECKS=1;