ALTER TABLE `subscription_enterprise_purchase` 
ADD COLUMN `razorpay_subscription_id` VARCHAR(128) NULL AFTER `modified_date`;