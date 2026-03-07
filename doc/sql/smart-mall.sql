CREATE TABLE `sys_category` (
                                `category_id` varchar(32) NOT NULL COMMENT '分类ID',
                                `category_name` varchar(100) DEFAULT NULL COMMENT '分类名称',
                                `p_category_id` varchar(32) DEFAULT NULL COMMENT '父id',
                                `sort` int(11) DEFAULT NULL COMMENT '排序',
                                PRIMARY KEY (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `sys_product_property` (
                                        `property_id` varchar(32) NOT NULL COMMENT '属性ID',
                                        `property_name` varchar(30) DEFAULT NULL COMMENT '属性名称',
                                        `p_category_id` varchar(32) DEFAULT NULL COMMENT '一级分类',
                                        `category_id` varchar(32) DEFAULT NULL COMMENT '二级分类',
                                        `property_sort` int(11) DEFAULT NULL COMMENT '排序',
                                        `cover_type` tinyint(1) DEFAULT NULL COMMENT '0:无需传封面 1:需传封面',
                                        PRIMARY KEY (`property_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品属性表';


CREATE TABLE `product_info` (
                                `product_id` varchar(32) NOT NULL COMMENT '商品ID',
                                `product_name` varchar(200) DEFAULT NULL COMMENT '商品名称',
                                `product_desc` text COMMENT '商品描述',
                                `cover` varchar(500) DEFAULT NULL COMMENT '封面',
                                `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                `category_id` varchar(10) DEFAULT NULL COMMENT '分类ID',
                                `p_category_id` varchar(10) DEFAULT NULL COMMENT '分类父ID',
                                `status` tinyint(1) DEFAULT '0' COMMENT '-1:已删除 0:下架 1:上架',
                                `min_price` decimal(10,2) DEFAULT NULL COMMENT '最低价格',
                                `max_price` decimal(10,2) DEFAULT NULL COMMENT '最高价格',
                                `total_sale` int(11) DEFAULT '0' COMMENT '销量',
                                `commend_type` tinyint(1) DEFAULT '0' COMMENT '0:未推荐 1:已经推荐',
                                PRIMARY KEY (`product_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='商品信息';


CREATE TABLE `product_property_value` (
                                          `product_id` varchar(32) NOT NULL COMMENT '商品ID',
                                          `property_id` varchar(10) DEFAULT NULL COMMENT '属性ID',
                                          `property_name` varchar(30) DEFAULT NULL COMMENT '属性名称',
                                          `property_sort` int(11) DEFAULT NULL COMMENT '属性排序',
                                          `cover_type` tinyint(1) DEFAULT NULL COMMENT '0:无需封面 1:需封面',
                                          `property_value_id` varchar(15) NOT NULL COMMENT '属性封面',
                                          `property_value` varchar(60) NOT NULL COMMENT '属性值',
                                          `property_remark` varchar(100) DEFAULT NULL COMMENT '备注',
                                          `sort` int(11) DEFAULT NULL COMMENT '属性值排序',
                                          PRIMARY KEY (`product_id`, `property_value_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='商品属性';


CREATE TABLE `product_sku` (
                               `product_id` varchar(32) NOT NULL COMMENT '商品ID',
                               `property_value_id_hash` varchar(32) NOT NULL COMMENT '属性值ID hash', # 一个 SKU 由多个属性值组合而成（比如 “颜色 = 红色 + 尺寸 = XL”），直接用多个属性值 ID 作为主键会很繁琐，哈希值可以把这个组合转换成一个固定长度的字符串，方便作为联合主键的一部分。
                               `property_value_ids` varchar(500) DEFAULT NULL COMMENT '属性值ID组',
                               `price` decimal(10,2) DEFAULT NULL COMMENT '价格',
                               `stock` int(11) DEFAULT NULL COMMENT '库存',
                               `sort` int(11) DEFAULT NULL COMMENT '排序',
                               PRIMARY KEY (`product_id`, `property_value_id_hash`) USING BTREE,
                               KEY `idx_product_id` (`product_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='商品SKU';

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
                                 UNIQUE KEY `uk_user_product_sku` (`user_id`,`product_id`,`property_value_id_hash`) USING BTREE,
                                 KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='购物车';


CREATE TABLE `order_info` (
                              `order_id` varchar(32) NOT NULL COMMENT '订单ID',
                              `order_no` varchar(32) NOT NULL COMMENT '订单号',
                              `user_id` varchar(32) NOT NULL COMMENT '用户ID',
                              `order_status` int(11) NOT NULL DEFAULT '0' COMMENT '0:待支付 10:已支付 20:已取消 30:已完成',
                              `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
                              `total_quantity` int(11) NOT NULL COMMENT '商品总数量',
                              `receiver_name` varchar(50) NOT NULL COMMENT '收货人',
                              `receiver_phone` varchar(30) NOT NULL COMMENT '收货电话',
                              `receiver_address` varchar(255) NOT NULL COMMENT '收货地址',
                              `order_remark` varchar(255) DEFAULT NULL COMMENT '订单备注',
                              `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                              `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                              `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
                              PRIMARY KEY (`order_id`) USING BTREE,
                              UNIQUE KEY `uk_order_no` (`order_no`) USING BTREE,
                              KEY `idx_user_id_status` (`user_id`,`order_status`) USING BTREE
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='订单明细';