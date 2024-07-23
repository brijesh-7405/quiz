ALTER TABLE `subscription_plans` 
ADD COLUMN `ftotal_cost` FLOAT NULL AFTER `modified_date`,
ADD COLUMN `fcgst` FLOAT NULL AFTER `ftotal_cost`,
ADD COLUMN `fsgst` FLOAT NULL AFTER `fcgst`,
ADD COLUMN `factual_cost` FLOAT NULL AFTER `fsgst`;