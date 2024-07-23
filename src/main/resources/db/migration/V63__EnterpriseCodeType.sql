ALTER TABLE `enterprise` 
ADD COLUMN `enterprise_code` VARCHAR(45) NULL DEFAULT NULL AFTER `status`,
ADD COLUMN `enterprise_type` VARCHAR(45) NOT NULL DEFAULT 'PUBLIC' AFTER `enterprise_code`;
