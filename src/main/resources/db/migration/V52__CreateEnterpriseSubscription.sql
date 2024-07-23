CREATE TABLE `subscription_enterprise_purchase` (
  `subscription_purchase_id` BIGINT NOT NULL AUTO_INCREMENT,
  `enterprise_id` BIGINT NOT NULL,
  `auto_renew` TINYINT NOT NULL DEFAULT 1,
  `subscription_purchase_date` TIMESTAMP NOT NULL,
  `subscription_end_date` TIMESTAMP NOT NULL,
  `subscription_status` TINYINT NOT NULL,
  `creation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`subscription_purchase_id`));

  CREATE TABLE `subscription_enterprise_purchase_details` (
  	`subscription_purchase_details_id` BIGINT NOT NULL AUTO_INCREMENT,
  	`mapping_id` BIGINT NOT NULL,
  	`subscription_purchase_id` BIGINT NOT NULL,
  	`creation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  	`modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`subscription_purchase_details_id`));
  