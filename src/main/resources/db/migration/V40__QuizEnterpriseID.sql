ALTER TABLE `quiz` 
DROP FOREIGN KEY `quiz_user_id`;
ALTER TABLE `quiz` 
ADD INDEX `quiz_enterprise_id_idx` (`enterprise_id` ASC),
DROP INDEX `quiz_user_id_idx` ;
;
ALTER TABLE `quiz` 
ADD CONSTRAINT `quiz_enterprise_id`
  FOREIGN KEY (`enterprise_id`)
  REFERENCES `enterprise` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
