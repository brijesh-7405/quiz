ALTER TABLE `transaction_history` 
ADD COLUMN `invoice_number` VARCHAR(45) NULL AFTER `modified_date`;
