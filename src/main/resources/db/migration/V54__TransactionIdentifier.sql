ALTER TABLE `transaction_history` 
ADD COLUMN `transaction_uuid` VARCHAR(64) NULL AFTER `signature`;
