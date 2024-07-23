ALTER TABLE `quiz` 
ADD COLUMN `activated_date` TIMESTAMP NULL AFTER `comments`,
ADD COLUMN `review_date` TIMESTAMP NULL AFTER `activated_date`,
ADD COLUMN `closed_date` TIMESTAMP NULL AFTER `review_date`,
ADD COLUMN `rejected_date` TIMESTAMP NULL AFTER `closed_date`;

ALTER TABLE `quiz_analytics_data` 
ADD COLUMN `activated_date` TIMESTAMP NULL,
ADD COLUMN `review_date` TIMESTAMP NULL,
ADD COLUMN `closed_date` TIMESTAMP NULL,
ADD COLUMN `rejected_date` TIMESTAMP NULL;