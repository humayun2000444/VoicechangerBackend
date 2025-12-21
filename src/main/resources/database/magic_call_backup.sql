-- MySQL dump for magic_call database
-- Database: magic_call
-- Backup Date: 2025-12-21
-- Server version: MySQL 8.0+

-- Create database
CREATE DATABASE IF NOT EXISTS `magic_call` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `magic_call`;

-- Drop tables if exists (for clean restore)
DROP TABLE IF EXISTS `call_history`;
DROP TABLE IF EXISTS `voice_purchases`;
DROP TABLE IF EXISTS `voice_user_mapping`;
DROP TABLE IF EXISTS `balances`;
DROP TABLE IF EXISTS `transactions`;
DROP TABLE IF EXISTS `voice_types`;
DROP TABLE IF EXISTS `user_roles`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `user_details`;
DROP TABLE IF EXISTS `roles`;

-- Table structure for table `user_details`
CREATE TABLE `user_details` (
  `id_user_details` BIGINT NOT NULL AUTO_INCREMENT,
  `id_user` BIGINT NOT NULL,
  `date_of_birth` DATE DEFAULT NULL,
  `gender` VARCHAR(10) DEFAULT NULL,
  `address` VARCHAR(500) DEFAULT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `profile_photo` VARCHAR(500) DEFAULT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_user_details`),
  UNIQUE KEY `UK_user_id` (`id_user`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table structure for table `roles`
CREATE TABLE `roles` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_role_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table structure for table `users`
CREATE TABLE `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `first_name` VARCHAR(100) NOT NULL,
  `last_name` VARCHAR(100) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `enabled` TINYINT(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_username` (`username`),
  KEY `idx_username` (`username`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add foreign key to user_details after users table is created
ALTER TABLE `user_details`
  ADD CONSTRAINT `FK_user_details_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE CASCADE;

-- Table structure for table `user_roles` (junction table)
CREATE TABLE `user_roles` (
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`),
  KEY `FK_role_id` (`role_id`),
  CONSTRAINT `FK_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_user_roles_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default roles
INSERT INTO `roles` (`id`, `name`) VALUES
(1, 'ROLE_ADMIN'),
(2, 'ROLE_USER');

-- Sample data (optional - for testing purposes)
-- Password for admin: 'admin123' (BCrypt encrypted: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7q5kQjL86)
-- Password for testuser: 'password123' (BCrypt encrypted: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy)
-- Note: Use the signup API endpoint to create real users with properly encrypted passwords
-- ADMIN users must be created manually via database (cannot register through API)

-- Insert sample users (enabled by default)
INSERT INTO `users` (`id`, `username`, `password`, `first_name`, `last_name`, `created_at`, `updated_at`, `enabled`)
VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7q5kQjL86', 'Admin', 'User', NOW(), NOW(), 1),
(2, 'testuser', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Test', 'User', NOW(), NOW(), 1);

-- Assign roles to users
-- Admin user gets both ROLE_ADMIN and ROLE_USER
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES (1, 1), (1, 2);
-- Test user gets ROLE_USER
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES (2, 2);

-- Table structure for table `voice_types`
CREATE TABLE `voice_types` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `voice_name` VARCHAR(100) NOT NULL,
  `code` VARCHAR(10) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_voice_code` (`code`),
  KEY `idx_voice_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table structure for table `transactions`
CREATE TABLE `transactions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `id_user` BIGINT NOT NULL COMMENT 'User ID who made the transaction',
  `transaction_method` VARCHAR(50) NOT NULL COMMENT 'Payment method: bKash, Nagad, Rocket, etc.',
  `amount` DECIMAL(10,2) NOT NULL COMMENT 'Transaction amount in BDT',
  `tnx_id` VARCHAR(100) NOT NULL COMMENT 'Unique transaction ID from payment provider',
  `date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Transaction creation date',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT 'Transaction status: PENDING, SUCCESS, FAILED',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_tnx_id` (`tnx_id`),
  KEY `idx_tnx_id` (`tnx_id`),
  KEY `idx_date` (`date`),
  KEY `idx_status` (`status`),
  KEY `idx_id_user` (`id_user`),
  CONSTRAINT `FK_transactions_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Top-up transactions with manual admin approval';

-- Table structure for table `balances`
CREATE TABLE `balances` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `purchase_amount` BIGINT NOT NULL DEFAULT 0 COMMENT 'Total purchased duration in seconds',
  `last_used_amount` BIGINT NOT NULL DEFAULT 0 COMMENT 'Last used duration in seconds',
  `total_used_amount` BIGINT NOT NULL DEFAULT 0 COMMENT 'Total used duration in seconds',
  `remain_amount` BIGINT NOT NULL DEFAULT 0 COMMENT 'Remaining duration in seconds',
  `id_user` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_balance_user` (`id_user`),
  CONSTRAINT `FK_balances_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table structure for table `call_history`
CREATE TABLE `call_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `aparty` VARCHAR(50) NOT NULL COMMENT 'Calling party (username)',
  `bparty` VARCHAR(50) DEFAULT NULL COMMENT 'Called party',
  `uuid` VARCHAR(100) NOT NULL UNIQUE COMMENT 'FreeSWITCH call UUID',
  `source_ip` VARCHAR(50) DEFAULT NULL COMMENT 'Source IP address',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Call creation time',
  `start_time` TIMESTAMP NULL DEFAULT NULL COMMENT 'Call answer time',
  `end_time` TIMESTAMP NULL DEFAULT NULL COMMENT 'Call end time',
  `duration` BIGINT NOT NULL DEFAULT 0 COMMENT 'Call duration in seconds',
  `status` VARCHAR(20) DEFAULT NULL COMMENT 'Call status: RESERVED, ANSWERED, COMPLETED, REJECTED, FAILED',
  `hangup_cause` VARCHAR(50) DEFAULT NULL COMMENT 'Hangup cause from FreeSWITCH',
  `codec` VARCHAR(50) DEFAULT NULL COMMENT 'Audio codec used for the call',
  `id_user` BIGINT DEFAULT NULL COMMENT 'User ID (FK to users table)',
  PRIMARY KEY (`id`),
  KEY `idx_aparty` (`aparty`),
  KEY `idx_uuid` (`uuid`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_status` (`status`),
  KEY `FK_call_history_user` (`id_user`),
  CONSTRAINT `FK_call_history_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Call history and CDR records';

-- Insert sample voice types
INSERT INTO `voice_types` (`id`, `voice_name`, `code`, `created_at`, `updated_at`) VALUES
(1, 'Male Voice', '901', NOW(), NOW()),
(2, 'Female Voice', '902', NOW(), NOW()),
(3, 'Child Voice', '903', NOW(), NOW()),
(4, 'Robot Voice', '904', NOW(), NOW());

-- Table structure for table `voice_user_mapping`
CREATE TABLE `voice_user_mapping` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `id_user` BIGINT NOT NULL COMMENT 'User ID',
  `id_voice_type` BIGINT NOT NULL COMMENT 'Voice Type ID',
  `is_purchased` BIT(1) NOT NULL DEFAULT 0 COMMENT 'false = free (auto-assigned), true = purchased',
  `assigned_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When voice type was assigned to user',
  `trial_expiry_date` DATETIME(6) DEFAULT NULL COMMENT 'null = no trial (permanent), non-null = trial expires at this date',
  `expiry_date` DATETIME(6) DEFAULT NULL COMMENT 'null = permanent access, non-null = subscription expires at this date',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_user_voice` (`id_user`, `id_voice_type`),
  KEY `idx_id_user` (`id_user`),
  KEY `idx_id_voice_type` (`id_voice_type`),
  KEY `idx_trial_expiry` (`trial_expiry_date`),
  KEY `idx_expiry` (`expiry_date`),
  CONSTRAINT `FK_voice_user_mapping_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_voice_user_mapping_voice_type` FOREIGN KEY (`id_voice_type`) REFERENCES `voice_types` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Maps users to their assigned/purchased voice types';

-- Table structure for table `voice_purchases`
CREATE TABLE `voice_purchases` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `id_user` BIGINT NOT NULL COMMENT 'User ID who made the purchase',
  `id_voice_type` BIGINT NOT NULL COMMENT 'Voice Type ID being purchased',
  `id_transaction` BIGINT DEFAULT NULL COMMENT 'Reference to transaction (optional, for backward compatibility)',
  `transaction_method` VARCHAR(50) DEFAULT NULL COMMENT 'Payment method: bkash, nagad, rocket',
  `tnx_id` VARCHAR(100) DEFAULT NULL COMMENT 'Payment transaction ID from provider',
  `subscription_type` VARCHAR(20) DEFAULT NULL COMMENT 'Subscription type: monthly, yearly',
  `amount` DECIMAL(10,2) DEFAULT NULL COMMENT 'Purchase amount',
  `purchase_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Purchase request date',
  `expiry_date` DATETIME(6) DEFAULT NULL COMMENT 'When the subscription expires',
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT 'Purchase status: pending, approved, rejected',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
  PRIMARY KEY (`id`),
  KEY `idx_id_user` (`id_user`),
  KEY `idx_id_voice_type` (`id_voice_type`),
  KEY `idx_status` (`status`),
  KEY `idx_purchase_date` (`purchase_date`),
  KEY `idx_tnx_id` (`tnx_id`),
  CONSTRAINT `FK_voice_purchases_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_voice_purchases_voice_type` FOREIGN KEY (`id_voice_type`) REFERENCES `voice_types` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Voice type purchase requests with admin approval workflow';

-- Grant privileges to tbuser
-- Note: Run these commands separately as MySQL root user
-- GRANT ALL PRIVILEGES ON `magic_call`.* TO 'tbuser'@'localhost';
-- FLUSH PRIVILEGES;

-- End of dump
