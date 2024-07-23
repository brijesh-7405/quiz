ALTER TABLE `quiz` 
DROP FOREIGN KEY `quiz_category_id`;
ALTER TABLE `quiz` 
DROP COLUMN `category_list_id`,
DROP INDEX `quiz_category_id_idx` ;