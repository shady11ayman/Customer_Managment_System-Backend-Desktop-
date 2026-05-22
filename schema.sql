-- ============================================================
--  Customer Management System — SQL Setup Script
--  Compatible with: MySQL 8+
-- ============================================================

-- 1. Create the database (run once)
CREATE DATABASE IF NOT EXISTS customer_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE customer_db;

-- 2. Create the customers table
CREATE TABLE IF NOT EXISTS customers (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(100) NOT NULL,
    phone      VARCHAR(50)  NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_customers_email (email)
);

-- 3. Seed data for quick testing
INSERT INTO customers (name, email, phone) VALUES
    ('shady',    'shady@gmail.com',  '+01222489564'),
    ('ayman',  'ayman@gmail.com',  '+01548597926');
    
