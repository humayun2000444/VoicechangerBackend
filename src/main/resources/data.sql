-- Default Data Initialization for Magic Call
-- This script runs automatically when the application starts
-- Uses INSERT IGNORE to prevent duplicate key errors

-- Insert default roles
INSERT IGNORE INTO roles (id, name) VALUES (1, 'ROLE_ADMIN');
INSERT IGNORE INTO roles (id, name) VALUES (2, 'ROLE_USER');

-- Insert default voice types
-- Voice types 1 and 2 are purchasable (require admin approval)
INSERT IGNORE INTO voice_types (id, voice_name, code, created_at, updated_at)
VALUES (1, 'Male Voice', '901', NOW(), NOW());

INSERT IGNORE INTO voice_types (id, voice_name, code, created_at, updated_at)
VALUES (2, 'Female Voice', '902', NOW(), NOW());

-- Voice type 3 is auto-assigned to new users as 3-day trial (can be purchased after trial expires)
INSERT IGNORE INTO voice_types (id, voice_name, code, created_at, updated_at)
VALUES (3, 'Child Voice', '903', NOW(), NOW());

-- Voice type 4 is auto-assigned to new users permanently (free forever)
INSERT IGNORE INTO voice_types (id, voice_name, code, created_at, updated_at)
VALUES (4, 'Robot Voice', '904', NOW(), NOW());

-- Note: Admin user should be created via /api/setup/create-admin endpoint
-- This ensures proper password encryption and FreeSWITCH configuration
