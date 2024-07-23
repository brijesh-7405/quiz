ALTER TABLE `user_quiz` 
ADD COLUMN `quiz_timedout` TINYINT NOT NULL DEFAULT 0 AFTER `quiz_start_time`;
