-- Create voice_mapping_history table to store expired voice mappings
-- This table maintains a historical record of all expired trials and subscriptions

CREATE TABLE `voice_mapping_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `original_mapping_id` bigint NOT NULL COMMENT 'Original ID from voice_user_mapping table',
  `id_user` bigint NOT NULL,
  `id_voice_type` bigint NOT NULL,
  `is_purchased` bit(1) NOT NULL,
  `assigned_at` datetime(6) NOT NULL COMMENT 'When the mapping was originally assigned',
  `trial_expiry_date` datetime(6) DEFAULT NULL COMMENT 'Trial expiry date (if it was a trial)',
  `expiry_date` datetime(6) DEFAULT NULL COMMENT 'Subscription expiry date',
  `is_default` bit(1) NOT NULL,
  `expired_at` datetime(6) NOT NULL COMMENT 'When this mapping expired and was moved to history',
  `expiry_reason` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'TRIAL_EXPIRED or SUBSCRIPTION_EXPIRED',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_history` (`id_user`),
  KEY `idx_voice_type_history` (`id_voice_type`),
  KEY `idx_original_mapping` (`original_mapping_id`),
  KEY `idx_expired_at` (`expired_at`),
  CONSTRAINT `FK_history_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_history_voice_type` FOREIGN KEY (`id_voice_type`) REFERENCES `voice_types` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Historical record of expired voice mappings (trials and subscriptions)';

-- Add index for efficient querying by expiry reason
ALTER TABLE `voice_mapping_history`
ADD INDEX `idx_expiry_reason` (`expiry_reason`);

-- Add composite index for user + expiry date queries
ALTER TABLE `voice_mapping_history`
ADD INDEX `idx_user_expired` (`id_user`, `expired_at`);
