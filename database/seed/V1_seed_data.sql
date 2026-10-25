-- Seed data for testing and initial boot
-- Roles
INSERT INTO roles (id, name) VALUES (1, 'ROLE_SUPER_ADMIN'), (2, 'ROLE_BRANCH_MANAGER'), (3, 'ROLE_CASHIER'), (4, 'ROLE_WAITER'), (5, 'ROLE_KITCHEN_STAFF') ON CONFLICT DO NOTHING;
