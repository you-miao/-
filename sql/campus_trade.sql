-- =========================================================
-- 校园闲置物交易平台 数据库建表脚本
-- 数据库：MySQL 8.0
-- 字符集：utf8mb4
-- =========================================================

CREATE DATABASE IF NOT EXISTS `campus_trade` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `campus_trade`;

-- =========================================================
-- 一、用户管理模块
-- =========================================================

-- 1. 用户表
CREATE TABLE `sys_user` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`      VARCHAR(50)  NOT NULL                COMMENT '用户名（学号）',
    `password`      VARCHAR(200) NOT NULL                COMMENT '密码（BCrypt加密）',
    `nickname`      VARCHAR(50)  DEFAULT NULL            COMMENT '昵称',
    `real_name`     VARCHAR(50)  DEFAULT NULL            COMMENT '真实姓名',
    `gender`        TINYINT      DEFAULT 0               COMMENT '性别：0-未知 1-男 2-女',
    `phone`         VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
    `email`         VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
    `avatar`        VARCHAR(500) DEFAULT NULL            COMMENT '头像URL',
    `student_no`    VARCHAR(30)  DEFAULT NULL            COMMENT '学号',
    `balance`       DECIMAL(10,2) NOT NULL DEFAULT 0.00  COMMENT '账户余额（平台内）',
    `status`        TINYINT      DEFAULT 1               COMMENT '账号状态：0-禁用 1-正常',
    `last_login_time` DATETIME   DEFAULT NULL            COMMENT '最后登录时间',
    `last_login_ip`   VARCHAR(50) DEFAULT NULL           COMMENT '最后登录IP',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT      DEFAULT 0               COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_student_no` (`student_no`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 角色表
CREATE TABLE `sys_role` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name`   VARCHAR(50) NOT NULL                COMMENT '角色名称',
    `role_key`    VARCHAR(50) NOT NULL                COMMENT '角色标识（ROLE_USER / ROLE_ADMIN）',
    `sort_order`  INT         DEFAULT 0               COMMENT '排序',
    `status`      TINYINT     DEFAULT 1               COMMENT '状态：0-禁用 1-正常',
    `remark`      VARCHAR(200) DEFAULT NULL           COMMENT '备注',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_key` (`role_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. 用户角色关联表
CREATE TABLE `sys_user_role` (
    `id`      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL                COMMENT '用户ID',
    `role_id` BIGINT NOT NULL                COMMENT '角色ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`),
    CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 4. 用户收货地址表
CREATE TABLE `user_address` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '地址ID',
    `user_id`      BIGINT       NOT NULL                COMMENT '用户ID',
    `receiver`     VARCHAR(50)  NOT NULL                COMMENT '收货人姓名',
    `phone`        VARCHAR(20)  NOT NULL                COMMENT '联系电话',
    `campus`       VARCHAR(100) DEFAULT NULL            COMMENT '校区',
    `detail`       VARCHAR(300) NOT NULL                COMMENT '详细地址',
    `is_default`   TINYINT      DEFAULT 0               COMMENT '是否默认：0-否 1-是',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT      DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收货地址表';

-- 5. 登录日志表
CREATE TABLE `sys_login_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id`     BIGINT       DEFAULT NULL            COMMENT '用户ID',
    `username`    VARCHAR(50)  DEFAULT NULL            COMMENT '登录账号',
    `login_type`  TINYINT      DEFAULT 1               COMMENT '登录方式：1-密码 2-验证码',
    `login_ip`    VARCHAR(50)  DEFAULT NULL            COMMENT '登录IP',
    `login_location` VARCHAR(100) DEFAULT NULL         COMMENT '登录地点',
    `device`      VARCHAR(200) DEFAULT NULL            COMMENT '设备/浏览器信息',
    `status`      TINYINT      DEFAULT 1               COMMENT '登录状态：0-失败 1-成功',
    `msg`         VARCHAR(300) DEFAULT NULL            COMMENT '提示信息',
    `login_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_login_time` (`login_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- =========================================================
-- 二、商品管理模块
-- =========================================================

-- 6. 商品分类表
CREATE TABLE `product_category` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `parent_id`   BIGINT      DEFAULT 0               COMMENT '父分类ID（0为顶级分类）',
    `name`        VARCHAR(50) NOT NULL                COMMENT '分类名称',
    `icon`        VARCHAR(300) DEFAULT NULL           COMMENT '分类图标URL',
    `sort_order`  INT         DEFAULT 0               COMMENT '排序值',
    `status`      TINYINT     DEFAULT 1               COMMENT '状态：0-禁用 1-启用',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 7. 商品标签表
CREATE TABLE `product_tag` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `name`        VARCHAR(30) NOT NULL                COMMENT '标签名称',
    `color`       VARCHAR(20) DEFAULT NULL            COMMENT '标签颜色（用于前端展示）',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品标签表';

-- 8. 商品表
CREATE TABLE `product` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `user_id`        BIGINT        NOT NULL                COMMENT '发布者用户ID',
    `category_id`    BIGINT        DEFAULT NULL            COMMENT '分类ID',
    `title`          VARCHAR(150)  NOT NULL                COMMENT '商品标题',
    `description`    TEXT                                   COMMENT '商品描述',
    `cover_img`      VARCHAR(500)  DEFAULT NULL            COMMENT '封面图URL',
    `product_type`   TINYINT       NOT NULL DEFAULT 1      COMMENT '商品类型：1-售卖物 2-交换物',
    `price`          DECIMAL(10,2) DEFAULT 0.00            COMMENT '售价（售卖物适用）',
    `exchange_desc`  VARCHAR(500)  DEFAULT NULL            COMMENT '期望交换条件（交换物适用）',
    `campus`         VARCHAR(100)  DEFAULT NULL            COMMENT '所在校区',
    `contact_info`   VARCHAR(100)  DEFAULT NULL            COMMENT '联系方式',
    `quality`        TINYINT       DEFAULT 5               COMMENT '成色：1-10（10为全新）',
    `view_count`     INT           DEFAULT 0               COMMENT '浏览量',
    `favorite_count` INT           DEFAULT 0               COMMENT '收藏数（冗余字段，提升查询性能）',
    `status`         TINYINT       NOT NULL DEFAULT 0      COMMENT '状态：0-待审核 1-已上架 2-已下架 3-已交换 4-已售出 5-审核驳回',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT       DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_product_type` (`product_type`),
    KEY `idx_status` (`status`),
    KEY `idx_campus` (`campus`),
    KEY `idx_price` (`price`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_status_type_time` (`status`, `product_type`, `create_time`),
    FULLTEXT KEY `ft_title_desc` (`title`, `description`),
    CONSTRAINT `fk_product_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`),
    CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `product_category`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 9. 商品图片表
CREATE TABLE `product_image` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '图片ID',
    `product_id` BIGINT       NOT NULL                COMMENT '商品ID',
    `url`        VARCHAR(500) NOT NULL                COMMENT '图片URL',
    `sort_order` INT          DEFAULT 0               COMMENT '排序（0为主图）',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    CONSTRAINT `fk_image_product` FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

-- 10. 商品-标签关联表
CREATE TABLE `product_tag_relation` (
    `id`         BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_id` BIGINT NOT NULL                COMMENT '商品ID',
    `tag_id`     BIGINT NOT NULL                COMMENT '标签ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_tag` (`product_id`, `tag_id`),
    KEY `idx_tag_id` (`tag_id`),
    CONSTRAINT `fk_ptr_product` FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_ptr_tag` FOREIGN KEY (`tag_id`) REFERENCES `product_tag`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品标签关联表';

-- 11. 用户收藏表
CREATE TABLE `user_favorite` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id`     BIGINT   NOT NULL                COMMENT '用户ID',
    `product_id`  BIGINT   NOT NULL                COMMENT '商品ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_fav_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_fav_product` FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- 11. 用户浏览记录表（用于个性推荐）
CREATE TABLE `user_browse_log` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id`     BIGINT   NOT NULL                COMMENT '用户ID',
    `product_id`  BIGINT   NOT NULL                COMMENT '商品ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户浏览记录';

-- =========================================================
-- 三、交易 / 交换流程模块
-- =========================================================

-- 12. 交换申请表
CREATE TABLE `exchange_request` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '申请ID',
    `request_no`        VARCHAR(32)  NOT NULL                COMMENT '申请编号',
    `product_id`        BIGINT       NOT NULL                COMMENT '目标商品ID（被申请交换的商品）',
    `applicant_id`      BIGINT       NOT NULL                COMMENT '申请人ID',
    `owner_id`          BIGINT       NOT NULL                COMMENT '商品发布者ID（冗余，减少关联查询）',
    `offer_product_id`  BIGINT       DEFAULT NULL            COMMENT '拟交换物品的商品ID（申请人自己发布的商品）',
    `offer_desc`        VARCHAR(500) DEFAULT NULL            COMMENT '拟交换物品描述',
    `offer_images`      VARCHAR(2000) DEFAULT NULL           COMMENT '拟交换物品图片URL（JSON数组）',
    `message`           VARCHAR(500) DEFAULT NULL            COMMENT '沟通说明',
    `exchange_location` VARCHAR(200) DEFAULT NULL            COMMENT '约定交换地点',
    `exchange_time`     DATETIME     DEFAULT NULL            COMMENT '约定交换时间',
    `status`            TINYINT      NOT NULL DEFAULT 0      COMMENT '状态：0-待处理 1-已同意 2-已拒绝 3-已完成 4-已取消',
    `reject_reason`     VARCHAR(300) DEFAULT NULL            COMMENT '拒绝理由',
    `applicant_confirm` TINYINT      DEFAULT 0               COMMENT '申请方确认完成：0-未确认 1-已确认',
    `owner_confirm`     TINYINT      DEFAULT 0               COMMENT '发布方确认完成：0-未确认 1-已确认',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_request_no` (`request_no`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_applicant_id` (`applicant_id`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_offer_product_id` (`offer_product_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_exchange_product` FOREIGN KEY (`product_id`) REFERENCES `product`(`id`),
    CONSTRAINT `fk_exchange_applicant` FOREIGN KEY (`applicant_id`) REFERENCES `sys_user`(`id`),
    CONSTRAINT `fk_exchange_owner` FOREIGN KEY (`owner_id`) REFERENCES `sys_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交换申请表';

-- 13. 交换沟通记录表
CREATE TABLE `exchange_message` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `exchange_id`   BIGINT       NOT NULL                COMMENT '关联的交换申请ID',
    `sender_id`     BIGINT       NOT NULL                COMMENT '发送者ID',
    `receiver_id`   BIGINT       NOT NULL                COMMENT '接收者ID',
    `content`       TEXT         NOT NULL                COMMENT '消息内容',
    `msg_type`      TINYINT      DEFAULT 1               COMMENT '消息类型：1-文字 2-图片',
    `is_read`       TINYINT      DEFAULT 0               COMMENT '是否已读：0-未读 1-已读',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    KEY `idx_exchange_id` (`exchange_id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_msg_exchange` FOREIGN KEY (`exchange_id`) REFERENCES `exchange_request`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_msg_sender` FOREIGN KEY (`sender_id`) REFERENCES `sys_user`(`id`),
    CONSTRAINT `fk_msg_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `sys_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交换沟通记录表';

-- 14. 交易订单表（售卖物购买）
CREATE TABLE `trade_order` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no`        VARCHAR(32)   NOT NULL                COMMENT '订单编号',
    `product_id`      BIGINT        NOT NULL                COMMENT '商品ID',
    `seller_id`       BIGINT        NOT NULL                COMMENT '卖家ID（冗余）',
    `buyer_id`        BIGINT        NOT NULL                COMMENT '买家ID',
    `price`           DECIMAL(10,2) NOT NULL                COMMENT '订单金额',
    `delivery_type`   TINYINT       DEFAULT 1               COMMENT '交付方式：1-自提 2-送货上门',
    `address_id`      BIGINT        DEFAULT NULL            COMMENT '收货地址ID（送货时使用）',
    `receiver_name`   VARCHAR(50)   DEFAULT NULL            COMMENT '收货人（冗余快照）',
    `receiver_phone`  VARCHAR(20)   DEFAULT NULL            COMMENT '收货电话（冗余快照）',
    `receiver_address` VARCHAR(300) DEFAULT NULL            COMMENT '收货地址（冗余快照）',
    `pickup_location` VARCHAR(200)  DEFAULT NULL            COMMENT '自提地点',
    `pickup_time`     DATETIME      DEFAULT NULL            COMMENT '自提时间',
    `status`          TINYINT       NOT NULL DEFAULT 0      COMMENT '状态：0-待付款 1-待收货 2-已完成 3-已取消 4-退款中 5-已退款',
    `pay_time`        DATETIME      DEFAULT NULL            COMMENT '付款时间',
    `receive_time`    DATETIME      DEFAULT NULL            COMMENT '收货确认时间',
    `cancel_time`     DATETIME      DEFAULT NULL            COMMENT '取消时间',
    `cancel_reason`   VARCHAR(300)  DEFAULT NULL            COMMENT '取消原因',
    `contact_info`    VARCHAR(100)  DEFAULT NULL            COMMENT '买家联系方式',
    `remark`          VARCHAR(300)  DEFAULT NULL            COMMENT '订单备注',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_buyer_id` (`buyer_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_buyer_status` (`buyer_id`, `status`),
    KEY `idx_seller_status` (`seller_id`, `status`),
    CONSTRAINT `fk_order_product` FOREIGN KEY (`product_id`) REFERENCES `product`(`id`),
    CONSTRAINT `fk_order_seller` FOREIGN KEY (`seller_id`) REFERENCES `sys_user`(`id`),
    CONSTRAINT `fk_order_buyer` FOREIGN KEY (`buyer_id`) REFERENCES `sys_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单表';

-- 15. 支付记录表
CREATE TABLE `payment_record` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
    `payment_no`     VARCHAR(64)   NOT NULL                COMMENT '支付流水号',
    `order_id`       BIGINT        NOT NULL                COMMENT '关联订单ID',
    `order_no`       VARCHAR(32)   NOT NULL                COMMENT '关联订单编号（冗余）',
    `user_id`        BIGINT        NOT NULL                COMMENT '付款用户ID',
    `amount`         DECIMAL(10,2) NOT NULL                COMMENT '支付金额',
    `pay_method`     TINYINT       DEFAULT 1               COMMENT '支付方式：1-线下支付 2-平台余额',
    `status`         TINYINT       NOT NULL DEFAULT 0      COMMENT '状态：0-待支付 1-支付成功 2-支付失败 3-已退款',
    `pay_time`       DATETIME      DEFAULT NULL            COMMENT '支付成功时间',
    `remark`         VARCHAR(300)  DEFAULT NULL            COMMENT '备注',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`) REFERENCES `trade_order`(`id`),
    CONSTRAINT `fk_payment_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- 15b. 充值记录表（模拟充值入账）
CREATE TABLE `recharge_record` (
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

-- =========================================================
-- 四、评论模块
-- =========================================================

-- 16. 评论表
CREATE TABLE `comment` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `product_id`  BIGINT   NOT NULL                COMMENT '商品ID',
    `user_id`     BIGINT   NOT NULL                COMMENT '评论人ID',
    `parent_id`   BIGINT   DEFAULT 0               COMMENT '父评论ID（0为顶级评论，实现回复功能）',
    `reply_to_user_id` BIGINT DEFAULT NULL         COMMENT '回复的目标用户ID',
    `content`     TEXT     NOT NULL                COMMENT '评论内容',
    `images`      VARCHAR(2000) DEFAULT NULL       COMMENT '评论图片URL（JSON数组）',
    `rating`      TINYINT  DEFAULT NULL            COMMENT '评分：1-5（仅顶级评论可评分）',
    `like_count`  INT      DEFAULT 0               COMMENT '点赞数（冗余字段）',
    `status`      TINYINT  NOT NULL DEFAULT 0      COMMENT '审核状态：0-待审核 1-已通过 2-已驳回 3-已举报待处理',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT  DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`),
    KEY `idx_product_status_time` (`product_id`, `status`, `create_time`),
    CONSTRAINT `fk_comment_product` FOREIGN KEY (`product_id`) REFERENCES `product`(`id`),
    CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 17. 评论点赞表
CREATE TABLE `comment_like` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
    `comment_id`  BIGINT   NOT NULL                COMMENT '评论ID',
    `user_id`     BIGINT   NOT NULL                COMMENT '点赞用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_like_comment` FOREIGN KEY (`comment_id`) REFERENCES `comment`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表';

-- 18. 评论举报表
CREATE TABLE `comment_report` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '举报ID',
    `comment_id`  BIGINT       NOT NULL                COMMENT '被举报评论ID',
    `reporter_id` BIGINT       NOT NULL                COMMENT '举报人ID',
    `reason`      VARCHAR(500) NOT NULL                COMMENT '举报理由',
    `status`      TINYINT      NOT NULL DEFAULT 0      COMMENT '处理状态：0-待处理 1-已处理(违规) 2-已处理(正常)',
    `handler_id`  BIGINT       DEFAULT NULL            COMMENT '处理人（管理员）ID',
    `handle_result` VARCHAR(300) DEFAULT NULL          COMMENT '处理结果说明',
    `handle_time` DATETIME     DEFAULT NULL            COMMENT '处理时间',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
    PRIMARY KEY (`id`),
    KEY `idx_comment_id` (`comment_id`),
    KEY `idx_reporter_id` (`reporter_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_report_comment` FOREIGN KEY (`comment_id`) REFERENCES `comment`(`id`),
    CONSTRAINT `fk_report_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `sys_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论举报表';

-- =========================================================
-- 五、后台管理模块
-- =========================================================

-- 19. 审核记录表（商品审核 & 评论审核统一）
CREATE TABLE `audit_record` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '审核记录ID',
    `target_type`   TINYINT      NOT NULL                COMMENT '审核对象类型：1-商品 2-评论',
    `target_id`     BIGINT       NOT NULL                COMMENT '审核对象ID',
    `auditor_id`    BIGINT       NOT NULL                COMMENT '审核人（管理员）ID',
    `audit_action`  TINYINT      NOT NULL                COMMENT '审核操作：1-通过 2-驳回 3-下架 4-删除',
    `reason`        VARCHAR(500) DEFAULT NULL            COMMENT '审核理由（驳回/下架时填写）',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    PRIMARY KEY (`id`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_auditor_id` (`auditor_id`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_audit_auditor` FOREIGN KEY (`auditor_id`) REFERENCES `sys_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核记录表';

-- 20. 操作日志表
CREATE TABLE `sys_operation_log` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id`        BIGINT        DEFAULT NULL            COMMENT '操作人ID',
    `username`       VARCHAR(50)   DEFAULT NULL            COMMENT '操作人用户名（冗余）',
    `module`         VARCHAR(50)   DEFAULT NULL            COMMENT '操作模块',
    `operation`      VARCHAR(100)  DEFAULT NULL            COMMENT '操作描述',
    `method`         VARCHAR(300)  DEFAULT NULL            COMMENT '请求方法',
    `request_url`    VARCHAR(500)  DEFAULT NULL            COMMENT '请求URL',
    `request_method` VARCHAR(10)   DEFAULT NULL            COMMENT 'HTTP方法（GET/POST/PUT/DELETE）',
    `request_params` TEXT          DEFAULT NULL            COMMENT '请求参数',
    `response_code`  INT           DEFAULT NULL            COMMENT '响应状态码',
    `ip`             VARCHAR(50)   DEFAULT NULL            COMMENT '操作IP',
    `elapsed_time`   BIGINT        DEFAULT NULL            COMMENT '耗时（毫秒）',
    `status`         TINYINT       DEFAULT 1               COMMENT '操作状态：0-失败 1-成功',
    `error_msg`      TEXT          DEFAULT NULL            COMMENT '错误信息',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_module` (`module`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 21. 数据统计日报表（预聚合，提升统计查询性能）
CREATE TABLE `stat_daily` (
    `id`                  BIGINT  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `stat_date`           DATE    NOT NULL                COMMENT '统计日期',
    `new_user_count`      INT     DEFAULT 0               COMMENT '新增用户数',
    `active_user_count`   INT     DEFAULT 0               COMMENT '活跃用户数（当日有登录）',
    `new_product_count`   INT     DEFAULT 0               COMMENT '新增商品数',
    `trade_order_count`   INT     DEFAULT 0               COMMENT '交易订单数',
    `trade_amount`        DECIMAL(12,2) DEFAULT 0.00      COMMENT '交易总金额',
    `exchange_count`      INT     DEFAULT 0               COMMENT '交换完成数',
    `new_comment_count`   INT     DEFAULT 0               COMMENT '新增评论数',
    `create_time`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据统计日报表';

-- 22. 热门分类统计表
CREATE TABLE `stat_category_hot` (
    `id`            BIGINT  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `stat_date`     DATE    NOT NULL                COMMENT '统计日期',
    `category_id`   BIGINT  NOT NULL                COMMENT '分类ID',
    `category_name` VARCHAR(50) DEFAULT NULL        COMMENT '分类名称（冗余）',
    `product_count` INT     DEFAULT 0               COMMENT '商品发布数',
    `trade_count`   INT     DEFAULT 0               COMMENT '交易/交换数',
    `view_count`    INT     DEFAULT 0               COMMENT '浏览量',
    `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_date_category` (`stat_date`, `category_id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='热门分类统计表';

-- =========================================================
-- 六、初始化数据
-- =========================================================

-- 初始化角色（可重复执行）
INSERT INTO `sys_role` (`id`, `role_name`, `role_key`, `sort_order`, `status`, `remark`) VALUES
(1, '管理员', 'ROLE_ADMIN', 1, 1, '系统管理员，拥有全部权限'),
(2, '普通用户', 'ROLE_USER', 2, 1, '普通注册用户'),
(3, '社团人员', 'ROLE_CHARITY', 3, 1, '爱心捐赠后台运营账号')
ON DUPLICATE KEY UPDATE
`role_name` = VALUES(`role_name`),
`sort_order` = VALUES(`sort_order`),
`status` = VALUES(`status`),
`remark` = VALUES(`remark`);

-- 初始化系统账号（BCrypt 密码）
-- admin / admin123（管理员）
-- charity / charity123（社团人员）
INSERT INTO `sys_user`
(`username`, `password`, `nickname`, `real_name`, `gender`, `status`, `balance`, `deleted`)
VALUES
('admin', '$2a$10$Edwng6lKAJRSCIOz7Bd0CeQsfZKe/HETt46N2I90VCOYCullEUnAK', '系统管理员', '管理员', 1, 1, 0.00, 0),
('charity', '$2a$10$JJtiMWQSnDHTFjf29dC4kueI.GnbfIyOmJAILUS1OoB2Ngruic6C6', '社团人员', '社团人员', 1, 1, 0.00, 0)
ON DUPLICATE KEY UPDATE
`password` = VALUES(`password`),
`nickname` = VALUES(`nickname`),
`real_name` = VALUES(`real_name`),
`gender` = VALUES(`gender`),
`status` = VALUES(`status`),
`balance` = VALUES(`balance`),
`deleted` = VALUES(`deleted`);

-- 绑定系统账号角色
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `sys_user` u
JOIN `sys_role` r ON r.role_key = 'ROLE_ADMIN'
WHERE u.username = 'admin';

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `sys_user` u
JOIN `sys_role` r ON r.role_key = 'ROLE_CHARITY'
WHERE u.username = 'charity';

-- 普通测试用户由 Spring Boot DataInitializer 自动创建
-- zhangsan, lisi, wangwu, zhaoliu, sunqi / 123456（普通用户）

-- 初始化商品分类
INSERT INTO `product_category` (`id`, `parent_id`, `name`, `sort_order`, `status`) VALUES
(1,  0, '数码电子',   1, 1),
(2,  0, '图书教材',   2, 1),
(3,  0, '生活用品',   3, 1),
(4,  0, '服饰鞋包',   4, 1),
(5,  0, '运动户外',   5, 1),
(6,  0, '美妆护肤',   6, 1),
(7,  0, '文具办公',   7, 1),
(8,  0, '其他',       8, 1),
(9,  1, '手机',       1, 1),
(10, 1, '电脑/平板',  2, 1),
(11, 1, '耳机/音箱',  3, 1),
(12, 1, '相机',       4, 1),
(13, 2, '教材',       1, 1),
(14, 2, '考试用书',   2, 1),
(15, 2, '小说文学',   3, 1),
(16, 3, '寝室用品',   1, 1),
(17, 3, '厨具餐具',   2, 1),
(18, 5, '球类',       1, 1),
(19, 5, '健身器材',   2, 1);

-- 初始化常用标签
INSERT INTO `product_tag` (`name`, `color`) VALUES
('全新未拆封', '#67C23A'),
('九成新', '#409EFF'),
('有瑕疵', '#E6A23C'),
('急出', '#F56C6C'),
('可议价', '#909399'),
('包邮', '#67C23A'),
('限校内', '#409EFF'),
('毕业清仓', '#F56C6C');
