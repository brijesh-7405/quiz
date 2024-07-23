CREATE TABLE `user_expired_tokens`(
	`id` bigint(20) NOT NULL AUTO_INCREMENT,
    `user_id` bigint(20) NOT NULL,
    `access_token` text NOT NULL,
    PRIMARY KEY (`id`));
