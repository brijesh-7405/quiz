ALTER TABLE `questions` 
ADD COLUMN `question_type` VARCHAR(45) NOT NULL AFTER `options`;