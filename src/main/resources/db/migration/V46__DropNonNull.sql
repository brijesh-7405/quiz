ALTER TABLE `questions` 
DROP FOREIGN KEY `questions_category_id`;
ALTER TABLE `questions` 
CHANGE COLUMN `category_id` `category_id` BIGINT(20) NULL ;
ALTER TABLE `questions` 
ADD CONSTRAINT `questions_category_id`
  FOREIGN KEY (`category_id`)
  REFERENCES `category` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
