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
