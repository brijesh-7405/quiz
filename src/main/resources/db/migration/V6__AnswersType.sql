ALTER TABLE `answers` 
DROP COLUMN `is_text_answer`,
DROP COLUMN `is_multiple_choice_answer`,
DROP COLUMN `is_true_or_false_answer`;
ALTER TABLE `answers` 
ADD COLUMN `answer_type` VARCHAR(45) NOT NULL,
ADD COLUMN `option` INT NOT NULL;
