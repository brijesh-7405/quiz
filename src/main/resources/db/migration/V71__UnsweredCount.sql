ALTER TABLE `question_analytics_data` 
ADD COLUMN `unanswered_count` BIGINT(20) NOT NULL AFTER `quiz_id`;