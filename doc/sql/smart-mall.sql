CREATE TABLE `sys_category` (
                                `category_id` varchar(32) NOT NULL COMMENT '鍒嗙被ID',
                                `category_name` varchar(100) DEFAULT NULL COMMENT '鍒嗙被鍚嶇О',
                                `p_category_id` varchar(32) DEFAULT NULL COMMENT '鐖秈d',
                                `sort` int(11) DEFAULT NULL COMMENT '鎺掑簭',
                                PRIMARY KEY (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `sys_product_property` (
                                        `property_id` varchar(32) NOT NULL COMMENT '灞炴€D',
                                        `property_name` varchar(30) DEFAULT NULL COMMENT '灞炴€у悕绉?,
                                        `p_category_id` varchar(32) DEFAULT NULL COMMENT '涓€绾у垎绫?,
                                        `category_id` varchar(32) DEFAULT NULL COMMENT '浜岀骇鍒嗙被',
                                        `property_sort` int(11) DEFAULT NULL COMMENT '鎺掑簭',
                                        `cover_type` tinyint(1) DEFAULT NULL COMMENT '0:鏃犻渶浼犲皝闈?1:闇€浼犲皝闈?,
                                        PRIMARY KEY (`property_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='鍟嗗搧灞炴€ц〃';


CREATE TABLE `product_info` (
                                `product_id` varchar(32) NOT NULL COMMENT '鍟嗗搧ID',
                                `product_name` varchar(200) DEFAULT NULL COMMENT '鍟嗗搧鍚嶇О',
                                `product_desc` text COMMENT '鍟嗗搧鎻忚堪',
                                `cover` varchar(500) DEFAULT NULL COMMENT '灏侀潰',
                                `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
                                `category_id` varchar(10) DEFAULT NULL COMMENT '鍒嗙被ID',
                                `p_category_id` varchar(10) DEFAULT NULL COMMENT '鍒嗙被鐖禝D',
                                `status` tinyint(1) DEFAULT '0' COMMENT '-1:宸插垹闄?0:涓嬫灦 1:涓婃灦',
                                `min_price` decimal(10,2) DEFAULT NULL COMMENT '鏈€浣庝环鏍?,
                                `max_price` decimal(10,2) DEFAULT NULL COMMENT '鏈€楂樹环鏍?,
                                `total_sale` int(11) DEFAULT '0' COMMENT '閿€閲?,
                                `commend_type` tinyint(1) DEFAULT '0' COMMENT '0:鏈帹鑽?1:宸茬粡鎺ㄨ崘',
                                PRIMARY KEY (`product_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='鍟嗗搧淇℃伅';


CREATE TABLE `product_property_value` (
                                          `product_id` varchar(32) NOT NULL COMMENT '鍟嗗搧ID',
                                          `property_id` varchar(10) DEFAULT NULL COMMENT '灞炴€D',
                                          `property_name` varchar(30) DEFAULT NULL COMMENT '灞炴€у悕绉?,
                                          `property_sort` int(11) DEFAULT NULL COMMENT '灞炴€ф帓搴?,
                                          `cover_type` tinyint(1) DEFAULT NULL COMMENT '0:鏃犻渶灏侀潰 1:闇€灏侀潰',
                                          `property_value_id` varchar(15) NOT NULL COMMENT '灞炴€у皝闈?,
                                          `property_value` varchar(60) NOT NULL COMMENT '灞炴€у€?,
                                          `property_remark` varchar(100) DEFAULT NULL COMMENT '澶囨敞',
                                          `sort` int(11) DEFAULT NULL COMMENT '灞炴€у€兼帓搴?,
                                          PRIMARY KEY (`product_id`, `property_value_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='鍟嗗搧灞炴€?;


CREATE TABLE `product_sku` (
                               `product_id` varchar(32) NOT NULL COMMENT '鍟嗗搧ID',
                               `property_value_id_hash` varchar(32) NOT NULL COMMENT '灞炴€у€糏D hash', # 涓€涓?SKU 鐢卞涓睘鎬у€肩粍鍚堣€屾垚锛堟瘮濡?鈥滈鑹?= 绾㈣壊 + 灏哄 = XL鈥濓級锛岀洿鎺ョ敤澶氫釜灞炴€у€?ID 浣滀负涓婚敭浼氬緢绻佺悙锛屽搱甯屽€煎彲浠ユ妸杩欎釜缁勫悎杞崲鎴愪竴涓浐瀹氶暱搴︾殑瀛楃涓诧紝鏂逛究浣滀负鑱斿悎涓婚敭鐨勪竴閮ㄥ垎銆?
                               `property_value_ids` varchar(500) DEFAULT NULL COMMENT '灞炴€у€糏D缁?,
                               `price` decimal(10,2) DEFAULT NULL COMMENT '浠锋牸',
                               `stock` int(11) DEFAULT NULL COMMENT '搴撳瓨',
                               `sort` int(11) DEFAULT NULL COMMENT '鎺掑簭',
                               PRIMARY KEY (`product_id`, `property_value_id_hash`) USING BTREE,
                               KEY `idx_product_id` (`product_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='鍟嗗搧SKU';


CREATE TABLE `shopping_cart` (

                                 `cart_id` varchar(32) NOT NULL COMMENT '璐墿杞︽潯鐩甀D',

                                 `user_id` varchar(32) NOT NULL COMMENT '鐢ㄦ埛ID',

                                 `product_id` varchar(32) NOT NULL COMMENT '鍟嗗搧ID',

                                 `property_value_id_hash` varchar(32) NOT NULL COMMENT 'SKU鍝堝笇',

                                 `property_value_ids` varchar(500) DEFAULT NULL COMMENT 'SKU灞炴€у€糏D缁勫悎',

                                 `quantity` int(11) NOT NULL DEFAULT '1' COMMENT '璐拱鏁伴噺',

                                 `selected` tinyint(1) NOT NULL DEFAULT '1' COMMENT '0:鏈嬀閫?1:宸插嬀閫?,

                                 `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',

                                 `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',

                                 PRIMARY KEY (`cart_id`) USING BTREE,

                                 UNIQUE KEY `uk_user_product_sku` (`user_id`,`product_id`,`property_value_id_hash`) USING BTREE,

                                 KEY `idx_user_id` (`user_id`) USING BTREE

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='璐墿杞?;



CREATE TABLE `order_info` (

                              `order_id` varchar(32) NOT NULL COMMENT '璁㈠崟ID',

                              `order_no` varchar(32) NOT NULL COMMENT '璁㈠崟鍙?,

                              `user_id` varchar(32) NOT NULL COMMENT '鐢ㄦ埛ID',

                              `order_status` int(11) NOT NULL DEFAULT '0' COMMENT '0:寰呮敮浠?10:宸叉敮浠?20:宸插彇娑?30:宸插畬鎴?,

                              `total_amount` decimal(10,2) NOT NULL COMMENT '璁㈠崟鎬婚噾棰?,

                              `total_quantity` int(11) NOT NULL COMMENT '鍟嗗搧鎬绘暟閲?,

                              `receiver_name` varchar(50) NOT NULL COMMENT '鏀惰揣浜?,

                              `receiver_phone` varchar(30) NOT NULL COMMENT '鏀惰揣鐢佃瘽',

                              `receiver_address` varchar(255) NOT NULL COMMENT '鏀惰揣鍦板潃',

                              `order_remark` varchar(255) DEFAULT NULL COMMENT '璁㈠崟澶囨敞',

                              `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',

                              `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',

                              `cancel_time` datetime DEFAULT NULL COMMENT '鍙栨秷鏃堕棿',

                              PRIMARY KEY (`order_id`) USING BTREE,

                              UNIQUE KEY `uk_order_no` (`order_no`) USING BTREE,

                              KEY `idx_user_id_status` (`user_id`,`order_status`) USING BTREE

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='璁㈠崟涓昏〃';



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


ALTER TABLE `order_info`
    ADD COLUMN `pay_time` datetime DEFAULT NULL COMMENT '支付时间' AFTER `update_time`;


CREATE TABLE `payment_info` (
                                `payment_id` varchar(32) NOT NULL COMMENT '支付流水ID',
                                `payment_no` varchar(32) NOT NULL COMMENT '支付流水号',
                                `order_id` varchar(32) NOT NULL COMMENT '订单ID',
                                `order_no` varchar(32) NOT NULL COMMENT '订单号',
                                `user_id` varchar(32) NOT NULL COMMENT '用户ID',
                                `pay_channel` varchar(32) NOT NULL COMMENT '支付渠道',
                                `pay_status` int(11) NOT NULL DEFAULT '0' COMMENT '0:待支付 10:支付成功 20:支付失败 30:已关闭',
                                `pay_amount` decimal(10,2) NOT NULL COMMENT '支付金额',
                                `gateway_trade_no` varchar(64) DEFAULT NULL COMMENT '三方交易号',
                                `callback_content` varchar(1000) DEFAULT NULL COMMENT '回调原文',
                                `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
                                `callback_time` datetime DEFAULT NULL COMMENT '回调时间',
                                PRIMARY KEY (`payment_id`) USING BTREE,
                                UNIQUE KEY `uk_payment_no` (`payment_no`) USING BTREE,
                                KEY `idx_order_id_status` (`order_id`,`pay_status`) USING BTREE,
                                KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='支付流水表';

