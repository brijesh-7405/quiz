CREATE TABLE `subscription_plans` (
  `subscription_id` BIGINT NOT NULL AUTO_INCREMENT,
  `subscription_name` VARCHAR(64) NOT NULL,
  `plan_type` VARCHAR(64) NOT NULL,
  `subscription_type` VARCHAR(64) NOT NULL,
  `total_cost` FLOAT NOT NULL,
  `actual_cost` FLOAT NOT NULL,
  `sgst` FLOAT NOT NULL,
  `cgst` FLOAT NOT NULL,
  `creation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`subscription_id`));
