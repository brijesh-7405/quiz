CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `first_name` VARCHAR(255) NOT NULL,
  `last_name` VARCHAR(255) NOT NULL,
  `dob` DATE NOT NULL,
  `primary_email` VARCHAR(255) NOT NULL,
  
  `secondary_email` VARCHAR(255) NULL,
  `mobile` VARCHAR(32) NOT NULL,
  `creation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`));
CREATE TABLE `user_location` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `city` VARCHAR(255) NULL,
  `state` VARCHAR(255) NULL,
  `country` VARCHAR(255) NULL,
  `address` VARCHAR(1024) NULL,
  `creation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `user_location_user_id_idx` (`user_id` ASC),
  CONSTRAINT `user_location_user_id`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
CREATE TABLE `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `description` VARCHAR(1024) NULL,
  `parent_id` BIGINT NULL,
  `creation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`));
ALTER TABLE `category` 
ADD INDEX `category_parent_id_idx` (`parent_id` ASC);

ALTER TABLE `category` 
ADD CONSTRAINT `category_parent_id`
  FOREIGN KEY (`parent_id`)
  REFERENCES `category` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
CREATE TABLE `questions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `category_id` BIGINT NOT NULL,
  `question` MEDIUMTEXT NOT NULL,
  `is_true_or_false` TINYINT NULL,
  `is_multiple_choice` TINYINT NULL,
  `is_text` TINYINT NULL,
  `creation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `questions_category_id_idx` (`category_id` ASC),
  CONSTRAINT `questions_category_id`
    FOREIGN KEY (`category_id`)
    REFERENCES `category` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
 CREATE TABLE `answers` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `question_id` BIGINT NOT NULL,
  `is_true_or_false_answer` TINYINT NULL,
  `is_multiple_choice_answer` INT NULL,
  `is_text_answer` VARCHAR(1024) NULL,
  `creation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `answers_question_id_idx` (`question_id` ASC),
  CONSTRAINT `answers_question_id`
    FOREIGN KEY (`question_id`)
    REFERENCES `questions` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
DROP table IF EXISTS quiz;
CREATE TABLE `quiz` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `enterprise_id` bigint(20) NOT NULL,
  `category_list_id` bigint(20) NOT NULL,
  `creation_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `level` varchar(45) NOT NULL DEFAULT 'BASIC',
  `target_audience` varchar(1024) NOT NULL DEFAULT 'ALL',
  `code` varchar(45) DEFAULT NULL,
  `expiry_date` date NOT NULL,
  `topic_list_id` bigint(20) NOT NULL,
  `quizcol` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `quiz_category_id_idx` (`category_list_id`),
  KEY `quiz_user_id_idx` (`enterprise_id`),
  CONSTRAINT `quiz_category_id` FOREIGN KEY (`category_list_id`) REFERENCES `category` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `quiz_user_id` FOREIGN KEY (`enterprise_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB;
 
