ALTER TABLE `quiz_analytics_data` 
ADD COLUMN `how_many_timedout_quiz` BIGINT(20) NOT NULL DEFAULT 0 AFTER `quiz_time_limit`;
