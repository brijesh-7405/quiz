ALTER TABLE `quiz_analytics_data` 
ADD COLUMN `level` VARCHAR(55) NULL;
ALTER TABLE `quiz_analytics_data` 
ADD COLUMN `expiry_date` DATE NULL;
ALTER TABLE `quiz_analytics_data` 
ADD COLUMN `topic` VARCHAR(1024) NULL;
ALTER TABLE `quiz_analytics_data` 
ADD COLUMN `category` VARCHAR(255) NULL;