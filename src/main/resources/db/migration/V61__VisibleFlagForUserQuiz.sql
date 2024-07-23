ALTER TABLE `user_quiz` 
ADD COLUMN `visible` TINYINT(4) NOT NULL DEFAULT '0';
ALTER TABLE `user_quiz_analytics_data` 
ADD COLUMN `visible` TINYINT(4) NOT NULL DEFAULT '0';
