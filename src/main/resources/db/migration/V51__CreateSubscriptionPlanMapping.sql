CREATE TABLE `subscription_plan_feature_mapping` (
  `mapping_id` BIGINT NOT NULL AUTO_INCREMENT,
  `subscription_id` BIGINT NOT NULL,
  `subscription_feature_id` BIGINT NOT NULL,
  `creation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`mapping_id`));
