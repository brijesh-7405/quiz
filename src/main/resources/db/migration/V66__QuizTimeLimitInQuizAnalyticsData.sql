ALTER TABLE `quiz_analytics_data` 
ADD COLUMN `quiz_time_limit` VARCHAR(45) NULL DEFAULT '00:00:00' AFTER `rejected_date`;