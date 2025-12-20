-- MySQL dump for magic_call database
-- Database: magic_call
-- Backup Date: 2025-12-20
-- Server version: MySQL 8.0+

-- Create database
CREATE DATABASE IF NOT EXISTS `magic_call` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `magic_call`;

-- Drop tables if exists (for clean restore)
DROP TABLE IF EXISTS `user_roles`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `user_details`;
DROP TABLE IF EXISTS `roles`;

-- Table structure for table `user_details`
CREATE TABLE `user_details` (
  `id_user_details` BIGINT NOT NULL AUTO_INCREMENT,
  `date_of_birth` DATE DEFAULT NULL,
  `gender` VARCHAR(10) DEFAULT NULL,
  `address` VARCHAR(500) DEFAULT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `profile_photo` VARCHAR(500) DEFAULT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_user_details`),
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
  `id_user_details` BIGINT DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_username` (`username`),
  KEY `idx_username` (`username`),
  KEY `idx_created_at` (`created_at`),
  KEY `FK_user_details` (`id_user_details`),
  CONSTRAINT `FK_users_user_details` FOREIGN KEY (`id_user_details`) REFERENCES `user_details` (`id_user_details`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
-- Password for test users: 'password123' (BCrypt encrypted: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy)
-- Note: Use the signup API endpoint to create real users with properly encrypted passwords
-- ADMIN users must be created manually via database (cannot register through API)

-- Uncomment below to insert sample users
/*
-- Insert sample users
INSERT INTO `users` (`id`, `username`, `password`, `first_name`, `last_name`, `created_at`, `updated_at`, `enabled`)
VALUES
(1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'User', NOW(), NOW(), 1),
(2, 'testuser', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Test', 'User', NOW(), NOW(), 1);

-- Assign roles to users
-- Admin user gets ROLE_ADMIN
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES (1, 1);
-- Test user gets ROLE_USER
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES (2, 2);
*/

-- Grant privileges to tbuser
-- Note: Run these commands separately as MySQL root user
-- GRANT ALL PRIVILEGES ON `magic_call`.* TO 'tbuser'@'localhost';
-- FLUSH PRIVILEGES;

-- End of dump
