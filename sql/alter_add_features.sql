-- 订单表新增联系方式字段
ALTER TABLE `trade_order` ADD COLUMN `contact_info` VARCHAR(100) DEFAULT NULL COMMENT '买家联系方式' AFTER `cancel_reason`;

-- 用户浏览记录表（用于个性推荐）
CREATE TABLE IF NOT EXISTS `user_browse_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户浏览记录';

-- 交换申请表新增交换物商品ID字段（关联申请人自己发布的商品）
ALTER TABLE `exchange_request` ADD COLUMN `offer_product_id` BIGINT DEFAULT NULL COMMENT '拟交换物品的商品ID（申请人自己发布的商品）' AFTER `owner_id`;
ALTER TABLE `exchange_request` ADD INDEX `idx_offer_product_id` (`offer_product_id`);
