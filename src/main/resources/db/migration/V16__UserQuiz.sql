CREATE TABLE `user_quiz` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `quiz_id` bigint(20) NOT NULL,
  `creation_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `quiz_status` varchar(45) NOT NULL DEFAULT 'NOT_STARTED',
  `quiz_result` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_quiz_user_id_idx` (`user_id`),
  KEY `user_quiz_quiz_id_idx` (`quiz_id`),
  CONSTRAINT `user_quiz_quiz_id` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `user_quiz_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
DROP TABLE IF EXISTS `user_answers`; 
CREATE TABLE `user_answers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `question_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `user_quiz_id` bigint(20) NOT NULL,
  `creation_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `answer` mediumtext,
  PRIMARY KEY (`id`),
  KEY `user_answers_question_id_idx` (`question_id`),
  KEY `user_answers_user_id_idx` (`user_id`),
  KEY `user_answers_user_quiz_id_idx` (`user_quiz_id`),
  CONSTRAINT `user_answers_question_id` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`),
  CONSTRAINT `user_answers_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `user_answers_user_quiz_id` FOREIGN KEY (`user_quiz_id`) REFERENCES `user_quiz` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
