# SmartMall 开发日志

## 2026-03-07 功能点：用户端支付下单与支付回调基础能力

### 本次目标
- 衔接已创建的待支付订单，补齐用户端支付下单能力。
- 提供支付回调入口，并推动订单状态从待支付流转到已支付。
- 为后续退款、确认收货、评价等订单后续链路打基础。

### 本次实现
- 在 `smartMall-common` 新增支付领域模型与枚举：
  - `PaymentInfo`
  - `PaymentStatusEnum`
  - `PaymentChannelEnum`
- 新增支付 DTO / VO：
  - `PaymentSubmitDTO`
  - `PaymentCallbackDTO`
  - `PaymentSubmitVO`
- 新增支付 Mapper / Service / ServiceImpl：
  - `PaymentInfoMapper`
  - `PaymentInfoService`
  - `PaymentInfoServiceImpl`
- 在 `smartMall-web` 新增 `MallPaymentController`，提供接口：
  - `POST /api/payment/submit`
  - `POST /api/payment/callback`
- 支付能力已支持：
  - 针对待支付订单创建支付流水。
  - 重复发起支付时复用未完成的支付单。
  - 支付回调按状态处理成功、失败、关闭三类结果。
  - 成功回调后幂等更新支付流水，并把订单推进到“已支付”。
  - 返回模拟回调示例，方便当前阶段联调与手工验证。
- 订单领域同步补充 `payTime` 字段，并在订单详情、订单列表中可回显支付时间。
- 在 `doc/sql/smart-mall.sql` 追加：
  - `order_info.pay_time` 字段说明
  - `payment_info` 表结构说明

### 验证记录
- 本次会执行本地 Maven 校验。
- Maven 继续使用 `D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64`。
- 若需要临时测试文件进行支付链路验证，仅本地使用，验证后删除，不纳入提交。

### 当前影响范围
- `doc/sql`
- `doc/development-log.md`
- `smartMall-common`
- `smartMall-web`

### 下一步建议
- 继续实现退款申请与退款状态流转。
- 补确认收货、订单完成、商品评价。
- 再衔接智能购物模式中的对话式订单操作。

### 提交记录
- Git Commit: 本次功能点提交为“完成功能点：用户端支付下单与支付回调基础能力”。

## 2026-03-07 功能点：用户端订单创建与结算基础能力

### 本次目标
- 打通从购物车已选商品到订单预结算、创建订单的主流程。
- 补齐用户端订单列表、订单详情、取消订单基础能力。
- 为下一个支付功能点提供可直接衔接的订单状态基础。

### 本次实现
- 新增根目录规则文件：`.Agentmd`、`.claude.md`。
- 在 `smartMall-common` 新增订单领域模型：
  - `OrderInfo`
  - `OrderItem`
  - `OrderStatusEnum`
- 新增订单 DTO：
  - `OrderPreviewDTO`
  - `OrderCreateDTO`
  - `OrderCancelDTO`
  - `OrderQueryDTO`
- 新增订单 VO：
  - `OrderItemVO`
  - `OrderPreviewVO`
  - `OrderCreateVO`
  - `OrderInfoListVO`
  - `OrderDetailVO`
- 新增订单 Mapper / Service / ServiceImpl：
  - `OrderInfoMapper`
  - `OrderItemMapper`
  - `OrderInfoService`
  - `OrderItemService`
  - `OrderInfoServiceImpl`
  - `OrderItemServiceImpl`
- 在 `smartMall-web` 新增 `MallOrderController`，提供接口：
  - `POST /api/order/preview`
  - `POST /api/order/create`
  - `POST /api/order/list`
  - `GET /api/order/detail?userId=xxx&orderId=xxx`
  - `POST /api/order/cancel`
- 订单创建逻辑已支持：
  - 基于购物车已选项预结算。
  - 创建待支付订单。
  - 固化订单快照到订单明细。
  - 创建成功后清理对应购物车条目。
  - 只允许待支付订单取消。
- 在 `doc/sql/smart-mall.sql` 追加：
  - `order_info`
  - `order_item`

### 验证记录
- 执行命令：
  - `mvn -q -pl smartMall-common,smartMall-web -am "-Dmaven.repo.local=C:\Users\15712\.m2\repository" "-Dmaven.test.skip=false" test`
- 环境说明：
  - 使用 `D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64` 运行 Maven。
- 测试结果：通过。
- 说明：
  - 本次曾创建两份临时订单测试文件用于本地验证。
  - 验证通过后已删除，未纳入提交。

### 当前影响范围
- 根目录规则文件
- `doc/sql`
- `doc/development-log.md`
- `smartMall-common`
- `smartMall-web`

### 下一步建议
- 继续实现支付下单能力。
- 补订单支付、支付回调、订单状态推进。
- 再接退款、确认收货、评价等后续订单流程。

### 提交记录
- Git Commit: 本次功能点提交为“用户端订单创建与结算基础能力”。

## 2026-03-07 功能点：用户端购物车基础能力

### 本次目标
- 建立用户端购物车基础表结构与后端服务。
- 打通购物车列表、加入购物车、改数量、勾选、删除接口。
- 为下一个订单创建功能点提供待结算数据来源。

### 本次实现
- 新增购物车表结构说明：`shopping_cart`。
- 在 `smartMall-common` 新增购物车实体、DTO、VO、Mapper、Service、ServiceImpl。
- 在 `smartMall-web` 新增 `MallCartController`，提供接口：
  - `GET /api/cart/list?userId=xxx`
  - `POST /api/cart/add`
  - `POST /api/cart/updateQuantity`
  - `POST /api/cart/updateSelected`
  - `POST /api/cart/delete`
- 购物车服务支持：
  - 同 SKU 重复加入自动合并数量。
  - 加购与改数量时校验库存。
  - 列表接口聚合商品、SKU、属性值，并返回已选金额汇总。
  - 已下架或库存不足商品在列表中标记为 `available=false`。
- 新增购物车服务层与控制器层测试。

### 验证记录
- 执行命令：
  - `mvn -q -pl smartMall-common,smartMall-web -am "-Dmaven.repo.local=C:\Users\15712\.m2\repository" "-Dmaven.test.skip=false" test`
- 环境说明：
  - 继续使用 `D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64` 运行 Maven。
- 测试结果：通过。

### 当前影响范围
- `doc/sql`
- `smartMall-common`
- `smartMall-web`

### 下一步建议
- 继续实现订单创建与结算流程。
- 把购物车已选商品直接转换为订单项。
- 再补取消订单、确认收货等订单状态流转。

### 提交记录
- Git Commit: 本次功能点提交为“用户端购物车基础能力”。

## 2026-03-07 功能点：用户端商品浏览与详情接口

### 本次目标
- 打通用户端商品列表、推荐商品、商品详情三类接口。
- 让 `smartMall-web` 具备直接访问商品数据的运行条件。
- 为后续购物车、订单、智能购物入口提供商品查询基础。

### 本次实现
- 在 `smartMall-web` 新增用户端商品浏览控制器 `MallProductController`。
- 新增接口：
  - `POST /api/product/list`
  - `GET /api/product/recommend`
  - `GET /api/product/detail/{productId}`
- 在 `ProductInfoService` / `ProductInfoServiceImpl` 中补充：
  - 用户端可见商品列表查询。
  - 用户端可见商品详情查询。
  - 推荐商品查询。
- 在 `ProductQueryDTO` 增加 `status` 条件，支持按商品状态过滤。
- 修正 `smartMall-web` 启动配置：移除 `DataSourceAutoConfiguration` 排除项，并增加 `@MapperScan`。
- 在父 `pom.xml` 增加 Lombok 注解处理编译配置。
- 为 `smartMall-common`、`smartMall-web` 增加测试依赖，并补充对应测试。

### 验证记录
- 执行命令：
  - `mvn -q -pl smartMall-common,smartMall-web -am "-Dmaven.repo.local=C:\Users\15712\.m2\repository" "-Dmaven.test.skip=false" test`
- 测试结果：通过。
- 环境说明：
  - Maven 默认使用的 JDK 为 `25.0.1`，会触发 Lombok 与 `javac` 兼容问题。
  - 本次测试切换到 `D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64` 后通过。

### 当前影响范围
- `smartMall-common`
- `smartMall-web`
- 根项目 `pom.xml`

### 下一步建议
- 优先实现用户端购物车模块。
- 衔接订单创建与取消订单基础流程。
- 再向智能购物模式补 MCP / RAG 商品检索入口。

### 提交记录
- Git Commit: 本次功能点提交为“用户端商品浏览与详情接口”。
