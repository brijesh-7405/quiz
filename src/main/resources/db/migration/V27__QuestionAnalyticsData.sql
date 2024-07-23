CREATE TABLE `question_analytics_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `question_id` BIGINT NOT NULL,
  `question_type` VARCHAR(25) NOT NULL,
  `question` MEDIUMTEXT NOT NULL,
  `answer` MEDIUMTEXT NOT NULL,
  `correct_count` BIGINT NOT NULL,
  `in_correctcount` BIGINT NOT NULL,
  `inreview_count` BIGINT NOT NULL,
  `analytics_time` TIMESTAMP NOT NULL,
  `creation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`));
