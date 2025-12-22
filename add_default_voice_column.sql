-- Add is_default column to voice_user_mapping table
-- This allows users to select which voice should be used by default when making calls

ALTER TABLE `voice_user_mapping`
ADD COLUMN `is_default` TINYINT(1) NOT NULL DEFAULT 0 AFTER `expiry_date`;

-- Add index for faster lookups
ALTER TABLE `voice_user_mapping`
ADD INDEX `idx_user_default` (`id_user`, `is_default`);

-- Ensure only one default voice per user (optional but recommended)
-- Note: MySQL doesn't support filtered unique indexes, so this is enforced in application logic

-- Update comment for clarity
ALTER TABLE `voice_user_mapping`
COMMENT = 'Maps users to their voice types with default selection';
