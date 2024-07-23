CREATE TABLE `user_details` (
  `user_id` BIGINT NOT NULL,
  `summary` VARCHAR(150) NOT NULL,
  `description` VARCHAR(150) NOT NULL,
  `college_name` VARCHAR(150) NULL,
  `current_company_name` VARCHAR(150) NULL,
  `creation_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `user_details_user_id`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
CREATE TABLE `user_interests_categories` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `category_id` BIGINT NOT NULL,
  `creation_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  
  PRIMARY KEY (`id`),
  INDEX `user_interests_categories_user_id_idx` (`user_id` ASC),
  INDEX `user_interests_categories_category_id_idx` (`category_id` ASC),
  CONSTRAINT `user_interests_categories_user_id`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `user_interests_categories_category_id`
    FOREIGN KEY (`category_id`)
    REFERENCES `category` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
CREATE TABLE `user_interests_topics` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `topic_id` BIGINT NOT NULL,
  `creation_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `user_interests_topics_user_id_idx` (`user_id` ASC),
  INDEX `user_interests_topics_topic_id_idx` (`topic_id` ASC),
  CONSTRAINT `user_interests_topics_user_id`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `user_interests_topics_topic_id`
    FOREIGN KEY (`topic_id`)
    REFERENCES `category` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

