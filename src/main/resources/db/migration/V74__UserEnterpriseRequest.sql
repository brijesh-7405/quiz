CREATE TABLE `user_enterprise_request` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `enterprise_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `approve_status` tinyint(4) NOT NULL,
  PRIMARY KEY (`id`));