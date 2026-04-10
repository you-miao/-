-- 已有库升级：账户余额与充值表（在已执行旧版 campus_trade.sql 后执行一次）
USE `campus_trade`;

-- 若已存在 balance 列会报错，可忽略或手动注释本行
ALTER TABLE `sys_user`
    ADD COLUMN `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额（平台内）' AFTER `campus`;

CREATE TABLE IF NOT EXISTS `recharge_record` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '充值记录ID',
    `recharge_no`    VARCHAR(64)   NOT NULL                COMMENT '充值流水号',
    `user_id`        BIGINT        NOT NULL                COMMENT '用户ID',
    `amount`         DECIMAL(10,2) NOT NULL                COMMENT '充值金额',
    `balance_before` DECIMAL(10,2) DEFAULT NULL            COMMENT '充值前余额',
    `balance_after`  DECIMAL(10,2) DEFAULT NULL            COMMENT '充值后余额',
    `recharge_method` TINYINT      DEFAULT 1               COMMENT '方式：1-模拟充值',
    `status`         TINYINT       NOT NULL DEFAULT 0      COMMENT '状态：0-处理中 1-成功 2-失败',
    `recharge_time`  DATETIME      DEFAULT NULL            COMMENT '到账时间',
    `remark`         VARCHAR(300)  DEFAULT NULL            COMMENT '备注',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_recharge_no` (`recharge_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_recharge_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值记录表';
