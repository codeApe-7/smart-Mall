CREATE TABLE `sys_category` (
    `category_id` varchar(32) NOT NULL COMMENT '分类ID',
    `category_name` varchar(100) DEFAULT NULL COMMENT '分类名称',
    `p_category_id` varchar(32) DEFAULT NULL COMMENT '父级ID',
    `sort` int(11) DEFAULT NULL COMMENT '排序',
    PRIMARY KEY (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='分类表';


CREATE TABLE `sys_product_property` (
    `property_id` varchar(32) NOT NULL COMMENT '属性ID',
    `property_name` varchar(30) DEFAULT NULL COMMENT '属性名称',
    `p_category_id` varchar(32) DEFAULT NULL COMMENT '一级分类ID',
    `category_id` varchar(32) DEFAULT NULL COMMENT '二级分类ID',
    `property_sort` int(11) DEFAULT NULL COMMENT '排序',
    `cover_type` tinyint(1) DEFAULT NULL COMMENT '0:无需封面 1:需要封面',
    PRIMARY KEY (`property_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品属性表';


CREATE TABLE `product_info` (
    `product_id` varchar(32) NOT NULL COMMENT '商品ID',
    `product_name` varchar(200) DEFAULT NULL COMMENT '商品名称',
    `product_desc` text COMMENT '商品描述',
    `cover` varchar(500) DEFAULT NULL COMMENT '封面图',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `category_id` varchar(10) DEFAULT NULL COMMENT '分类ID',
    `p_category_id` varchar(10) DEFAULT NULL COMMENT '父级分类ID',
    `status` tinyint(1) DEFAULT '0' COMMENT '-1:已删除 0:下架 1:上架',
    `min_price` decimal(10,2) DEFAULT NULL COMMENT '最低价格',
    `max_price` decimal(10,2) DEFAULT NULL COMMENT '最高价格',
    `total_sale` int(11) DEFAULT '0' COMMENT '总销量',
    `commend_type` tinyint(1) DEFAULT '0' COMMENT '0:未推荐 1:推荐',
    PRIMARY KEY (`product_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='商品信息表';


CREATE TABLE `product_property_value` (
    `product_id` varchar(32) NOT NULL COMMENT '商品ID',
    `property_id` varchar(10) DEFAULT NULL COMMENT '属性ID',
    `property_name` varchar(30) DEFAULT NULL COMMENT '属性名称',
    `property_sort` int(11) DEFAULT NULL COMMENT '属性排序',
    `cover_type` tinyint(1) DEFAULT NULL COMMENT '0:无需封面 1:需要封面',
    `property_value_id` varchar(15) NOT NULL COMMENT '属性值ID',
    `property_value` varchar(60) NOT NULL COMMENT '属性值',
    `property_remark` varchar(100) DEFAULT NULL COMMENT '备注',
    `sort` int(11) DEFAULT NULL COMMENT '属性值排序',
    PRIMARY KEY (`product_id`, `property_value_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='商品属性值表';


CREATE TABLE `product_sku` (
    `product_id` varchar(32) NOT NULL COMMENT '商品ID',
    `property_value_id_hash` varchar(32) NOT NULL COMMENT '属性值组合Hash',
    `property_value_ids` varchar(500) DEFAULT NULL COMMENT '属性值ID组合',
    `price` decimal(10,2) DEFAULT NULL COMMENT '价格',
    `stock` int(11) DEFAULT NULL COMMENT '库存',
    `sort` int(11) DEFAULT NULL COMMENT '排序',
    PRIMARY KEY (`product_id`, `property_value_id_hash`) USING BTREE,
    KEY `idx_product_id` (`product_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='商品SKU表';


CREATE TABLE `shopping_cart` (
    `cart_id` varchar(32) NOT NULL COMMENT '购物车条目ID',
    `user_id` varchar(32) NOT NULL COMMENT '用户ID',
    `product_id` varchar(32) NOT NULL COMMENT '商品ID',
    `property_value_id_hash` varchar(32) NOT NULL COMMENT 'SKU哈希',
    `property_value_ids` varchar(500) DEFAULT NULL COMMENT 'SKU属性值ID组合',
    `quantity` int(11) NOT NULL DEFAULT '1' COMMENT '购买数量',
    `selected` tinyint(1) NOT NULL DEFAULT '1' COMMENT '0:未勾选 1:已勾选',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`cart_id`) USING BTREE,
    UNIQUE KEY `uk_user_product_sku` (`user_id`, `product_id`, `property_value_id_hash`) USING BTREE,
    KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='购物车表';


CREATE TABLE `order_info` (
    `order_id` varchar(32) NOT NULL COMMENT '订单ID',
    `order_no` varchar(32) NOT NULL COMMENT '订单号',
    `user_id` varchar(32) NOT NULL COMMENT '用户ID',
    `order_status` int(11) NOT NULL DEFAULT '0' COMMENT '0:待支付 10:已支付 20:已取消 30:已完成 40:已发货 50:已收货 60:退款申请中 70:已退款',
    `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
    `total_quantity` int(11) NOT NULL COMMENT '商品总数量',
    `receiver_name` varchar(50) NOT NULL COMMENT '收货人',
    `receiver_phone` varchar(30) NOT NULL COMMENT '收货电话',
    `receiver_address` varchar(255) NOT NULL COMMENT '收货地址',
    `order_remark` varchar(255) DEFAULT NULL COMMENT '订单备注',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
    `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
    `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
    `ship_time` datetime DEFAULT NULL COMMENT '发货时间',
    `receive_time` datetime DEFAULT NULL COMMENT '收货时间',
    `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
    PRIMARY KEY (`order_id`) USING BTREE,
    UNIQUE KEY `uk_order_no` (`order_no`) USING BTREE,
    KEY `idx_user_id_status` (`user_id`, `order_status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='订单主表';


CREATE TABLE `order_item` (
    `item_id` varchar(32) NOT NULL COMMENT '订单项ID',
    `order_id` varchar(32) NOT NULL COMMENT '订单ID',
    `product_id` varchar(32) NOT NULL COMMENT '商品ID',
    `product_name` varchar(200) DEFAULT NULL COMMENT '商品名称',
    `product_cover` varchar(500) DEFAULT NULL COMMENT '商品封面',
    `property_value_id_hash` varchar(32) NOT NULL COMMENT 'SKU哈希',
    `property_value_ids` varchar(500) DEFAULT NULL COMMENT 'SKU属性值组合',
    `sku_property_text` varchar(255) DEFAULT NULL COMMENT 'SKU属性文本',
    `price` decimal(10,2) NOT NULL COMMENT '成交单价',
    `quantity` int(11) NOT NULL COMMENT '购买数量',
    `total_amount` decimal(10,2) NOT NULL COMMENT '明细总金额',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`item_id`) USING BTREE,
    KEY `idx_order_id` (`order_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='订单明细表';


CREATE TABLE `payment_info` (
    `payment_id` varchar(32) NOT NULL COMMENT '支付流水ID',
    `payment_no` varchar(32) NOT NULL COMMENT '支付流水号',
    `order_id` varchar(32) NOT NULL COMMENT '订单ID',
    `order_no` varchar(32) NOT NULL COMMENT '订单号',
    `user_id` varchar(32) NOT NULL COMMENT '用户ID',
    `pay_channel` varchar(32) NOT NULL COMMENT '支付渠道',
    `pay_status` int(11) NOT NULL DEFAULT '0' COMMENT '0:待支付 10:支付成功 20:支付失败 30:已关闭',
    `pay_amount` decimal(10,2) NOT NULL COMMENT '支付金额',
    `gateway_trade_no` varchar(64) DEFAULT NULL COMMENT '第三方交易号',
    `callback_content` varchar(1000) DEFAULT NULL COMMENT '回调原文',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
    `callback_time` datetime DEFAULT NULL COMMENT '回调时间',
    PRIMARY KEY (`payment_id`) USING BTREE,
    UNIQUE KEY `uk_payment_no` (`payment_no`) USING BTREE,
    KEY `idx_order_id_status` (`order_id`, `pay_status`) USING BTREE,
    KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='支付流水表';


CREATE TABLE `refund_info` (
    `refund_id` varchar(32) NOT NULL COMMENT '退款ID',
    `refund_no` varchar(32) NOT NULL COMMENT '退款单号',
    `order_id` varchar(32) NOT NULL COMMENT '订单ID',
    `order_no` varchar(32) NOT NULL COMMENT '订单号',
    `user_id` varchar(32) NOT NULL COMMENT '用户ID',
    `refund_amount` decimal(10,2) NOT NULL COMMENT '退款金额',
    `refund_reason` varchar(255) DEFAULT NULL COMMENT '退款原因',
    `refund_status` int(11) NOT NULL DEFAULT '0' COMMENT '0:退款申请中 10:退款成功 20:退款拒绝',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
    PRIMARY KEY (`refund_id`) USING BTREE,
    UNIQUE KEY `uk_refund_no` (`refund_no`) USING BTREE,
    KEY `idx_order_id` (`order_id`) USING BTREE,
    KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='退款记录表';


CREATE TABLE `shipping_info` (
    `shipping_id` varchar(32) NOT NULL COMMENT '物流ID',
    `order_id` varchar(32) NOT NULL COMMENT '订单ID',
    `order_no` varchar(32) NOT NULL COMMENT '订单号',
    `user_id` varchar(32) NOT NULL COMMENT '用户ID',
    `tracking_no` varchar(64) NOT NULL COMMENT '物流单号',
    `shipping_company` varchar(50) DEFAULT NULL COMMENT '快递公司',
    `shipping_status` int(11) NOT NULL DEFAULT '0' COMMENT '0:已发货 10:运输中 20:已签收',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `receive_time` datetime DEFAULT NULL COMMENT '签收时间',
    PRIMARY KEY (`shipping_id`) USING BTREE,
    UNIQUE KEY `uk_tracking_no` (`tracking_no`) USING BTREE,
    KEY `idx_order_id` (`order_id`) USING BTREE,
    KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='物流记录表';


CREATE TABLE `product_review` (
    `review_id` varchar(32) NOT NULL COMMENT '评价ID',
    `order_id` varchar(32) NOT NULL COMMENT '订单ID',
    `item_id` varchar(32) NOT NULL COMMENT '订单项ID',
    `product_id` varchar(32) NOT NULL COMMENT '商品ID',
    `user_id` varchar(32) NOT NULL COMMENT '用户ID',
    `rating` int(11) NOT NULL COMMENT '评分(1-5)',
    `content` text COMMENT '评价内容',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `reply_content` varchar(500) DEFAULT NULL COMMENT '商家回复内容',
    `reply_time` datetime DEFAULT NULL COMMENT '商家回复时间',
    PRIMARY KEY (`review_id`) USING BTREE,
    UNIQUE KEY `uk_item_id` (`item_id`) USING BTREE,
    KEY `idx_order_id` (`order_id`) USING BTREE,
    KEY `idx_product_id` (`product_id`) USING BTREE,
    KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='商品评价表';

CREATE TABLE `assistant_chat_log` (
    `chat_id` varchar(32) NOT NULL COMMENT '智能购物会话消息ID',
    `session_id` varchar(32) NOT NULL COMMENT '会话ID',
    `user_id` varchar(32) NOT NULL COMMENT '用户ID',
    `request_text` varchar(500) NOT NULL COMMENT '用户消息',
    `intent_type` varchar(64) NOT NULL COMMENT '识别出的意图类型',
    `reply_text` varchar(1000) NOT NULL COMMENT '助手回复内容',
    `payload_summary` varchar(1000) DEFAULT NULL COMMENT '返回载荷摘要',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`chat_id`) USING BTREE,
    KEY `idx_session_id` (`session_id`) USING BTREE,
    KEY `idx_user_id_create_time` (`user_id`, `create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='智能购物会话日志表';


CREATE TABLE `user_preference` (
    `preference_id` varchar(32) NOT NULL COMMENT '偏好ID',
    `user_id` varchar(32) NOT NULL COMMENT '用户ID',
    `favorite_category_ids` varchar(500) DEFAULT NULL COMMENT '偏好分类ID(逗号分隔)',
    `favorite_category_names` varchar(500) DEFAULT NULL COMMENT '偏好分类名称(逗号分隔)',
    `min_price_preference` decimal(10,2) DEFAULT NULL COMMENT '价格偏好下限',
    `max_price_preference` decimal(10,2) DEFAULT NULL COMMENT '价格偏好上限',
    `recent_search_keywords` varchar(1000) DEFAULT NULL COMMENT '近期搜索关键词(逗号分隔)',
    `recent_product_ids` varchar(1000) DEFAULT NULL COMMENT '近期购买商品ID(逗号分隔)',
    `average_rating` decimal(3,1) DEFAULT NULL COMMENT '用户平均评分',
    `preference_tags` varchar(500) DEFAULT NULL COMMENT '偏好标签(逗号分隔)',
    `order_count` int(11) DEFAULT '0' COMMENT '历史订单数',
    `review_count` int(11) DEFAULT '0' COMMENT '历史评价数',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '最近更新时间',
    PRIMARY KEY (`preference_id`) USING BTREE,
    UNIQUE KEY `uk_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='用户偏好档案表';

CREATE TABLE `user_account` (
    `user_id` varchar(32) NOT NULL COMMENT '用户ID',
    `username` varchar(64) DEFAULT NULL COMMENT '用户名',
    `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
    `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
    `phone` varchar(30) DEFAULT NULL COMMENT '手机号',
    `password` varchar(64) DEFAULT NULL COMMENT '登录密码(MD5)',
    `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '0:禁用 1:启用',
    `remark` varchar(255) DEFAULT NULL COMMENT '备注',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `last_active_time` datetime DEFAULT NULL COMMENT '最近活跃时间',
    PRIMARY KEY (`user_id`) USING BTREE,
    UNIQUE KEY `uk_username` (`username`) USING BTREE,
    KEY `idx_status` (`status`) USING BTREE,
    KEY `idx_phone` (`phone`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='用户账户表';

CREATE TABLE `user_delivery_address` (
    `address_id` varchar(32) NOT NULL COMMENT '地址ID',
    `user_id` varchar(32) NOT NULL COMMENT '用户ID',
    `receiver_name` varchar(50) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` varchar(30) NOT NULL COMMENT '收货人手机号',
    `province` varchar(50) NOT NULL COMMENT '省份',
    `city` varchar(50) NOT NULL COMMENT '城市',
    `region` varchar(50) DEFAULT NULL COMMENT '区县',
    `detail_address` varchar(255) NOT NULL COMMENT '详细地址',
    `address_label` varchar(32) DEFAULT NULL COMMENT '地址标签',
    `default_address` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0:否 1:是',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`address_id`) USING BTREE,
    KEY `idx_user_default` (`user_id`, `default_address`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='用户收货地址表';

CREATE TABLE `sys_ai_config` (
    `config_id` varchar(32) NOT NULL COMMENT '配置ID',
    `config_code` varchar(64) NOT NULL COMMENT '配置编码',
    `config_name` varchar(100) DEFAULT NULL COMMENT '配置名称',
    `config_content` text COMMENT '配置内容(JSON)',
    `remark` varchar(255) DEFAULT NULL COMMENT '备注',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`config_id`) USING BTREE,
    UNIQUE KEY `uk_config_code` (`config_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='AI配置表';


CREATE TABLE `ai_monitor_event` (
    `event_id` varchar(32) NOT NULL COMMENT '事件ID',
    `event_source` varchar(64) DEFAULT NULL COMMENT '事件来源',
    `event_type` varchar(32) DEFAULT NULL COMMENT '事件类型',
    `event_code` varchar(64) DEFAULT NULL COMMENT '事件编码',
    `event_message` varchar(255) DEFAULT NULL COMMENT '事件说明',
    `user_id` varchar(32) DEFAULT NULL COMMENT '用户ID',
    `session_id` varchar(32) DEFAULT NULL COMMENT '会话ID',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`event_id`) USING BTREE,
    KEY `idx_event_source_time` (`event_source`, `create_time`) USING BTREE,
    KEY `idx_event_type_time` (`event_type`, `create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='AI监控事件表';

CREATE TABLE `sys_admin_account` (
    `account_id` varchar(32) NOT NULL COMMENT '后台账号ID',
    `account_name` varchar(64) NOT NULL COMMENT '登录账号',
    `password` varchar(64) NOT NULL COMMENT '登录密码(MD5)',
    `nickname` varchar(64) DEFAULT NULL COMMENT '账号昵称',
    `phone` varchar(30) DEFAULT NULL COMMENT '手机号',
    `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
    `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '0:禁用 1:启用',
    `super_admin` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0:普通管理员 1:超级管理员',
    `remark` varchar(255) DEFAULT NULL COMMENT '备注',
    `last_login_time` datetime DEFAULT NULL COMMENT '最近登录时间',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`account_id`) USING BTREE,
    UNIQUE KEY `uk_account_name` (`account_name`) USING BTREE,
    KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='后台管理员账号表';

CREATE TABLE `sys_admin_role` (
    `role_id` varchar(32) NOT NULL COMMENT '角色ID',
    `role_code` varchar(64) NOT NULL COMMENT '角色编码',
    `role_name` varchar(64) NOT NULL COMMENT '角色名称',
    `permission_codes` varchar(1000) DEFAULT NULL COMMENT '权限编码列表(逗号分隔)',
    `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '0:禁用 1:启用',
    `remark` varchar(255) DEFAULT NULL COMMENT '备注',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`role_id`) USING BTREE,
    UNIQUE KEY `uk_role_code` (`role_code`) USING BTREE,
    KEY `idx_role_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='后台管理员角色表';

CREATE TABLE `sys_admin_account_role` (
    `rel_id` varchar(32) NOT NULL COMMENT '关联ID',
    `account_id` varchar(32) NOT NULL COMMENT '后台账号ID',
    `role_id` varchar(32) NOT NULL COMMENT '角色ID',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`rel_id`) USING BTREE,
    UNIQUE KEY `uk_account_role` (`account_id`, `role_id`) USING BTREE,
    KEY `idx_role_id` (`role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='后台管理员账号角色关联表';

CREATE TABLE `sys_notice_message` (
    `notice_id` varchar(32) NOT NULL COMMENT '通知ID',
    `notice_title` varchar(120) NOT NULL COMMENT '通知标题',
    `notice_summary` varchar(255) DEFAULT NULL COMMENT '通知摘要',
    `notice_content` text COMMENT '通知内容',
    `message_type` varchar(32) NOT NULL COMMENT '通知类型',
    `target_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0:全体用户 1:指定用户',
    `target_user_id` varchar(32) DEFAULT NULL COMMENT '目标用户ID',
    `publish_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0:草稿 1:已发布 2:已下线',
    `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`notice_id`) USING BTREE,
    KEY `idx_publish_status` (`publish_status`) USING BTREE,
    KEY `idx_target_user` (`target_user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='系统消息通知表';

CREATE TABLE `user_notice_read` (
    `read_id` varchar(32) NOT NULL COMMENT '已读记录ID',
    `notice_id` varchar(32) NOT NULL COMMENT '通知ID',
    `user_id` varchar(32) NOT NULL COMMENT '用户ID',
    `read_time` datetime DEFAULT NULL COMMENT '已读时间',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`read_id`) USING BTREE,
    UNIQUE KEY `uk_notice_user` (`notice_id`, `user_id`) USING BTREE,
    KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='用户通知已读记录表';

CREATE TABLE `admin_operation_log` (
    `log_id` varchar(32) NOT NULL COMMENT '日志ID',
    `account_id` varchar(32) DEFAULT NULL COMMENT '后台账号ID',
    `account_name` varchar(64) DEFAULT NULL COMMENT '后台账号名',
    `operation_type` varchar(32) DEFAULT NULL COMMENT '操作类型',
    `operation_name` varchar(120) DEFAULT NULL COMMENT '操作名称',
    `request_uri` varchar(255) DEFAULT NULL COMMENT '请求地址',
    `request_method` varchar(20) DEFAULT NULL COMMENT '请求方法',
    `request_param` text COMMENT '请求参数',
    `operation_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '0:失败 1:成功',
    `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`log_id`) USING BTREE,
    KEY `idx_account_time` (`account_id`, `create_time`) USING BTREE,
    KEY `idx_operation_type_time` (`operation_type`, `create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='后台操作审计日志表';

