CREATE TABLE `subscription_features` (
  `subscription_feature_id` BIGINT NOT NULL AUTO_INCREMENT,
  `feature_name` VARCHAR(64) NOT NULL,
  `feature_count` BIGINT NOT NULL,
  `creation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`subscription_feature_id`));
