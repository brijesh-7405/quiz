ALTER TABLE `user` 
ADD COLUMN `access_code` VARCHAR(1024) NULL AFTER `enabled`;