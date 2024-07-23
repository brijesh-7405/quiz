CREATE TABLE `quiz_analytics_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `enterprise_id` BIGINT NOT NULL,
  `enterprise_name` VARCHAR(1024) NULL,
  `quiz_id` BIGINT NOT NULL,
  `how_many_took_quiz` BIGINT NOT NULL,
  `how_many_inprogress_quiz` BIGINT NOT NULL,
  `how_many_inreview_quiz` BIGINT NOT NULL,
  `how_many_notstarted_quiz` BIGINT NOT NULL,
  `how_many_completed_quiz` BIGINT NOT NULL,
  `analytics_time` TIMESTAMP NOT NULL,
  `creation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`));
