ALTER TABLE `transaction_history` 
ADD COLUMN `user_id` BIGINT(20) NULL AFTER `modified_date`,
ADD COLUMN `provider_payment_json` MEDIUMTEXT NULL AFTER `user_id`,
ADD COLUMN `workruit_payment_json` MEDIUMTEXT NULL AFTER `provider_payment_json`;