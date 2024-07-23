CREATE TABLE `quiz_category_list` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `quiz_id` bigint(20) NOT NULL,
  `category_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `quiz_category_list_quizId_idx` (`quiz_id`),
  KEY `quiz_category_list_categoryid_idx` (`category_id`),
  CONSTRAINT `quiz_category_list_categoryid` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `quiz_category_list_quizId` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB;
CREATE TABLE `quiz_topic_list` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `quiz_id` bigint(20) NOT NULL,
  `topic_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `quiz_topic_list_quizId_idx` (`quiz_id`),
  KEY `quiz_topic_list_categoryid_idx` (`topic_id`),
  CONSTRAINT `quiz_topic_list_categoryid` FOREIGN KEY (`topic_id`) REFERENCES `category` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `quiz_topic_list_quizId` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB;


