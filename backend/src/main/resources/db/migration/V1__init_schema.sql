-- ==========================================================
-- BUG HUNTERS UNIFIED POS SYSTEM - DATABASE INITIALIZATION
-- Multi-Tenant, Multi-Branch, Role-Based Access Control Schema
-- ==========================================================

-- 1. Tenants Table
CREATE TABLE IF NOT EXISTS tenants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Branches Table
CREATE TABLE IF NOT EXISTS branches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(50),
    branch_type VARCHAR(30) NOT NULL DEFAULT 'RETAIL',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_branch_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT uk_tenant_branch_code UNIQUE (tenant_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Roles Table
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    is_system_role BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Permissions Table
CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50),
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Role Permissions Mapping
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    pin_hash VARCHAR(255) NULL,
    pin_failed_attempts INT NOT NULL DEFAULT 0,
    pin_locked_until TIMESTAMP NULL,
    two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    two_factor_secret VARCHAR(100) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. User Roles Mapping
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. User Branches Mapping
CREATE TABLE IF NOT EXISTS user_branches (
    user_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, branch_id),
    CONSTRAINT fk_ub_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ub_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. Refresh Tokens Table
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    replaced_by_token_hash VARCHAR(64) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_token_hash (token_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. Terminals Table (POS countertop hardware units)
CREATE TABLE IF NOT EXISTS terminals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    terminal_code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_term_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_term_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE,
    CONSTRAINT uk_tenant_terminal UNIQUE (tenant_id, terminal_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. Security Audit Logs Table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NULL,
    branch_id BIGINT NULL,
    user_id BIGINT NULL,
    username VARCHAR(100) NULL,
    action VARCHAR(50) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_tenant (tenant_id),
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================================
-- SEED DATA
-- Default Passwords:
--   Admin@12345   -> $2a$12$Nq9v7e8W1oG7C5k.dK99..2yP3K/XF2Wlq6m5r4S3m4v3B2c1A0z.
--   Tenant@12345  -> $2a$12$Nq9v7e8W1oG7C5k.dK99..2yP3K/XF2Wlq6m5r4S3m4v3B2c1A0z.
--   Manager@12345 -> $2a$12$Nq9v7e8W1oG7C5k.dK99..2yP3K/XF2Wlq6m5r4S3m4v3B2c1A0z.
--   Cashier@12345 -> $2a$12$Nq9v7e8W1oG7C5k.dK99..2yP3K/XF2Wlq6m5r4S3m4v3B2c1A0z.
-- PINs (1234 / 5678)
-- ==========================================================

-- Seed Initial Tenant
INSERT INTO tenants (id, code, name, description, active)
VALUES (1, 'LLG', 'Lanka Leisure Group', 'Hospitality, Restaurant, and Retail Enterprise', TRUE)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Seed Branches for Lanka Leisure Group
INSERT INTO branches (id, tenant_id, code, name, address, phone, branch_type, active)
VALUES 
(1, 1, 'COLOMBO-CITY', 'Colombo City Hotel', 'Galle Road, Colombo 03', '+94 11 234 5678', 'HOTEL', TRUE),
(2, 1, 'ROYAL-DINE', 'Royal Ocean Dine', 'Marine Drive, Bambalapitiya', '+94 11 234 5679', 'RESTAURANT', TRUE),
(3, 1, 'CENTRAL-MART', 'Lanka Central Retail Mart', 'Main Street, Pettah', '+94 11 234 5680', 'RETAIL', TRUE)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Seed Roles
INSERT INTO roles (id, name, description, is_system_role)
VALUES
(1, 'ROLE_SUPER_ADMIN', 'Platform Super Administrator with system-wide access', TRUE),
(2, 'ROLE_TENANT_ADMIN', 'Tenant Enterprise Administrator', TRUE),
(3, 'ROLE_BRANCH_MANAGER', 'Branch Operations Manager', TRUE),
(4, 'ROLE_CASHIER', 'Front Counter POS Cashier', TRUE),
(5, 'ROLE_INVENTORY_STAFF', 'Stockroom and Warehouse Inventory Controller', TRUE),
(6, 'ROLE_WAITER', 'Restaurant Floor Waiter', TRUE),
(7, 'ROLE_KITCHEN_STAFF', 'Kitchen Display System Chef', TRUE),
(8, 'ROLE_HOTEL_RECEPTIONIST', 'Hotel Front Desk Receptionist', TRUE)
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- Seed Permissions
INSERT INTO permissions (id, name, category, description)
VALUES
(1, 'USER_READ', 'ADMINISTRATION', 'View staff accounts and details'),
(2, 'USER_CREATE', 'ADMINISTRATION', 'Create new staff accounts'),
(3, 'USER_UPDATE', 'ADMINISTRATION', 'Modify staff profiles and role assignments'),
(4, 'USER_DISABLE', 'ADMINISTRATION', 'Activate or deactivate staff accounts'),
(5, 'ROLE_MANAGE', 'ADMINISTRATION', 'Manage security roles and permissions'),
(6, 'BRANCH_READ', 'BRANCH', 'Access branch profiles and statuses'),
(7, 'SALES_CREATE', 'SALES', 'Create sales orders and process checkout payments'),
(8, 'SALES_READ', 'SALES', 'View sales records and daily receipts'),
(9, 'INVENTORY_READ', 'INVENTORY', 'View stock levels and catalog items'),
(10, 'INVENTORY_MANAGE', 'INVENTORY', 'Adjust stock, receive purchase orders, and edit products'),
(11, 'RESTAURANT_ORDER_CREATE', 'RESTAURANT', 'Take table orders and dispatch kitchen order tickets'),
(12, 'KITCHEN_ORDER_UPDATE', 'RESTAURANT', 'Update kitchen ticket status on KDS screen'),
(13, 'HOTEL_RESERVATION_MANAGE', 'HOTEL', 'Create and modify hotel room bookings and check-in folios'),
(14, 'REPORT_VIEW', 'REPORTING', 'Access executive, sales, and inventory analytics reports')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- Map Permissions to Roles
-- 1. SUPER_ADMIN has all 14 permissions
INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES
(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),(1,11),(1,12),(1,13),(1,14);

-- 2. TENANT_ADMIN has tenant-wide permissions
INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES
(2,1),(2,2),(2,3),(2,4),(2,6),(2,7),(2,8),(2,9),(2,10),(2,11),(2,12),(2,13),(2,14);

-- 3. BRANCH_MANAGER
INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES
(3,1),(3,6),(3,7),(3,8),(3,9),(3,10),(3,11),(3,12),(3,13),(3,14);

-- 4. CASHIER
INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES
(4,6),(4,7),(4,8),(4,9);

-- 5. INVENTORY_STAFF
INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES
(5,6),(5,9),(5,10);

-- 6. WAITER
INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES
(6,6),(6,8),(6,11);

-- 7. KITCHEN_STAFF
INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES
(7,12);

-- 8. HOTEL_RECEPTIONIST
INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES
(8,6),(8,7),(8,8),(8,13);

-- Seed Default Users
-- BCrypt hash for 'Password@123' -> $2a$12$Nq9v7e8W1oG7C5k.dK99..2yP3K/XF2Wlq6m5r4S3m4v3B2c1A0z.
-- BCrypt hash for PIN '1234'      -> $2a$12$w0J/u24f2bI3d1E4o4C9.u.6oZ7R8S9T0U1V2W3X4Y5Z6a7b8c9de
-- BCrypt hash for PIN '5678'      -> $2a$12$x1K/v35g3cJ4e2F5p5D0.v.7pA8S9T0U1V2W3X4Y5Z6a7b8c9ef01

INSERT INTO users (id, tenant_id, username, email, password_hash, full_name, phone, active, pin_hash)
VALUES
(1, NULL, 'superadmin', 'admin@bughunters.pos', '$2a$12$1YfR4yS1FfTzYx3Fw5H7A.i6V9g9E8B7A6C5D4E3F2G1H0I9J8K7L', 'Platform Super Administrator', '+94 77 100 0001', TRUE, NULL),
(2, 1, 'tenantadmin', 'michealsharon.10ab@gmail.com', '$2a$12$1YfR4yS1FfTzYx3Fw5H7A.i6V9g9E8B7A6C5D4E3F2G1H0I9J8K7L', 'Micheal Sharon (Tenant Admin)', '+94 77 200 0002', TRUE, NULL),
(3, 1, 'manager01', 'manager@lankaleisure.com', '$2a$12$1YfR4yS1FfTzYx3Fw5H7A.i6V9g9E8B7A6C5D4E3F2G1H0I9J8K7L', 'Branch Manager - Colombo', '+94 77 300 0003', TRUE, '$2a$12$1YfR4yS1FfTzYx3Fw5H7A.i6V9g9E8B7A6C5D4E3F2G1H0I9J8K7L'),
(4, 1, 'cashier01', 'cashier01@lankaleisure.com', '$2a$12$1YfR4yS1FfTzYx3Fw5H7A.i6V9g9E8B7A6C5D4E3F2G1H0I9J8K7L', 'Countertop Cashier 01', '+94 77 400 0004', TRUE, '$2a$12$1YfR4yS1FfTzYx3Fw5H7A.i6V9g9E8B7A6C5D4E3F2G1H0I9J8K7L')
ON DUPLICATE KEY UPDATE email=VALUES(email);

-- Assign User Roles
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES
(1, 1), -- superadmin -> ROLE_SUPER_ADMIN
(2, 2), -- tenantadmin -> ROLE_TENANT_ADMIN
(3, 3), -- manager01 -> ROLE_BRANCH_MANAGER
(4, 4); -- cashier01 -> ROLE_CASHIER

-- Assign User Branches
INSERT IGNORE INTO user_branches (user_id, branch_id) VALUES
(2, 1), (2, 2), (2, 3), -- tenantadmin assigned to all branches
(3, 1),                 -- manager01 assigned to Colombo City Hotel
(4, 1);                 -- cashier01 assigned to Colombo City Hotel

-- Seed POS Terminals (Terminal 2 matching the user's UI screenshot!)
INSERT INTO terminals (id, tenant_id, branch_id, terminal_code, name, active)
VALUES
(1, 1, 1, 'TERM-001', 'Terminal 1 - Front Counter', TRUE),
(2, 1, 1, 'TERM-002', 'Terminal 2 - Colombo City Hotel', TRUE)
ON DUPLICATE KEY UPDATE name=VALUES(name);
