ALTER TABLE `quiz` 
ADD COLUMN `quiz_time_limit` VARCHAR(45) NOT NULL AFTER `uuid`;
ALTER TABLE `user_quiz` 
ADD COLUMN `quiz_start_time` TIMESTAMP NULL AFTER `visible`;