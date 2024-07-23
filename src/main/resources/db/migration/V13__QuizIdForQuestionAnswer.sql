DROP TABLE IF EXISTS `answers`;
DROP TABLE IF EXISTS `questions`;
CREATE TABLE `questions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `category_id` bigint(20) NOT NULL,
  `question` mediumtext NOT NULL,
  `options` mediumtext NOT NULL,
  `question_type` varchar(45) NOT NULL,
  `creation_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `quiz_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `questions_category_id_idx` (`category_id`),
  KEY `questions_quiz_id_idx` (`quiz_id`),
  CONSTRAINT `questions_category_id` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `questions_quiz_id` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
CREATE TABLE `answers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `question_id` bigint(20) NOT NULL,
  `creation_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `option` mediumtext NOT NULL,
  PRIMARY KEY (`id`),
  KEY `answers_question_id_idx` (`question_id`),
  CONSTRAINT `answers_question_id` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
