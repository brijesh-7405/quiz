CREATE TABLE `user_quiz_analytics_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userquiz_id` bigint(20) NOT NULL,
  `enterprise_id` bigint(20) NOT NULL,
  `quiz_id` bigint(20) NOT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `analytics_time` timestamp NOT NULL,
  `creation_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `quiz_status` varchar(512) NOT NULL,
  `posted_date` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=146583 DEFAULT CHARSET=utf8;
