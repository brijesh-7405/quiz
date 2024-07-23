CREATE TABLE `transaction_history` (
  `transaction_id` BIGINT NOT NULL AUTO_INCREMENT,
  `enterprise_id` BIGINT NOT NULL,
  `transaction_date` TIMESTAMP NOT NULL,
  `transaction_status` VARCHAR(64) NOT NULL,
  `subscription_plan_id` BIGINT NOT NULL,
  `order_id` VARCHAR(128) DEFAULT NULL,
  `payment_id` VARCHAR(128) DEFAULT NULL,
  `signature` VARCHAR(256) DEFAULT NULL,
  `creation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`transaction_id`));