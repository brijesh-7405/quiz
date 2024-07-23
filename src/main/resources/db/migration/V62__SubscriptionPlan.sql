ALTER TABLE `subscription_enterprise_purchase` 
ADD COLUMN `subscription_plan_id` BIGINT(20) NULL AFTER `razorpay_subscription_id`;
