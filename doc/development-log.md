# SmartMall 开发日志

## 2026-03-08 功能点：管理后台订单管理基础能力

### 本次目标
- 按功能导图继续补齐管理后台“订单管理”能力。
- 提供后台订单列表、订单详情、后台发货、退款审批列表与操作接口。
- 复用已有用户端订单/物流/退款服务，避免实现两套状态流转逻辑。

### 本次实现
- 在 `smartMall-common` 新增后台订单管理 DTO：
  - `AdminOrderQueryDTO`
  - `AdminRefundQueryDTO`
  - `AdminShipOrderDTO`
- 在 `smartMall-common` 新增后台订单管理 VO：
  - `AdminOrderListVO`
  - `AdminRefundInfoVO`
- 在 `smartMall-common` 新增后台订单管理 Service / ServiceImpl：
  - `AdminOrderManageService`
  - `AdminOrderManageServiceImpl`
- 后台订单管理能力包括：
  - 分页查询全量订单，支持按订单号、用户ID、订单状态过滤
  - 查询单个订单详情（复用 `OrderInfoService.getOrderDetail`）
  - 后台对已支付订单发货（复用 `ShippingInfoService.shipOrder`）
  - 按订单查询物流详情
  - 分页查询退款单，支持按退款单号、订单号、用户ID、退款状态过滤
  - 后台同意/拒绝退款（复用 `RefundInfoService.approveRefund/rejectRefund`）
- 在 `smartMall-admin` 新增控制器：
  - `OrderManageController`
  - `RefundManageController`
- 新增后台接口：
  - `POST /api/order/list`
  - `GET /api/order/detail/{orderId}`
  - `POST /api/order/ship`
  - `GET /api/order/shipping/{orderId}`
  - `POST /api/refund/list`
  - `GET /api/refund/detail/{refundId}`
  - `POST /api/refund/approve/{refundId}`
  - `POST /api/refund/reject/{refundId}`
- 更新 `apifox_requests.md`：
  - 新增管理后台订单管理接口测试文档

### 验证记录
- 执行命令：
  - `mvn -q -pl smartMall-common,smartMall-admin -am "-Dmaven.repo.local=C:\Users\15712\.m2\repository" "-DfailIfNoTests=false" test`
- 环境说明：
  - Maven 使用 `D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64` 运行。
- 测试结果：编译与测试通过。

### 当前影响范围
- `doc/development-log.md`
- `apifox_requests.md`
- `smartMall-common`
- `smartMall-admin`

### 下一步建议
- 继续补齐管理后台用户管理能力，例如用户列表、用户下单/退款概览、启用/禁用状态维护。
- 补充管理后台订单评价管理或售后处理视图，承接功能导图中的订单评价与用户反馈场景。
- 若后台发货需要更贴近真实业务，可扩展批量发货、物流公司枚举和发货备注能力。

### 提交记录
- Git Commit: 本次功能点提交为"完成功能点：管理后台订单管理基础能力"。

## 2026-03-08 功能点：管理后台首页数据概览基础能力

### 本次目标
- 按功能导图补齐管理后台首页“数据概览”能力。
- 提供可直接给后台首页使用的统一接口，聚合销售、退款、待发货和库存预警数据。
- 为后续管理后台订单管理、用户管理等功能提供首页入口和数据基线。

### 本次实现
- 在 `smartMall-common` 新增后台概览配置：
  - `AdminDashboardProperties`
- 在 `smartMall-common` 新增后台概览 VO：
  - `AdminDashboardOverviewVO`
  - `AdminDashboardSummaryVO`
  - `AdminDashboardTrendVO`
  - `AdminPendingShipmentOrderVO`
  - `AdminLowStockProductVO`
- 在 `smartMall-common` 新增后台概览 Service / ServiceImpl：
  - `AdminDashboardService`
  - `AdminDashboardServiceImpl`
- 后台概览聚合内容包括：
  - 销售总额、订单总数、按订单去重的用户数
  - 退款总额、退款单数、待处理退款单数
  - 近 N 天销售趋势（基于支付时间）
  - 近 N 天退款趋势（基于退款申请时间）
  - 待发货订单列表（`PAID` 状态）
  - 在售商品库存预警列表（按 SKU 总库存聚合）
- 在 `smartMall-admin` 新增 `DashboardController`：
  - `GET /api/dashboard/overview`
- 在 `smartMall-admin/src/main/resources/application.yml` 新增：
  - `smart-mall.admin.dashboard.trend-days`
  - `smart-mall.admin.dashboard.low-stock-threshold`
  - `smart-mall.admin.dashboard.low-stock-limit`
  - `smart-mall.admin.dashboard.pending-shipment-limit`
- 更新 `apifox_requests.md`：
  - 新增管理后台首页数据概览接口测试文档

### 验证记录
- 执行命令：
  - `mvn -q -pl smartMall-common,smartMall-admin -am "-Dmaven.repo.local=C:\Users\15712\.m2\repository" "-DfailIfNoTests=false" test`
- 环境说明：
  - Maven 使用 `D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64` 运行。
- 测试结果：编译与测试通过。

### 当前影响范围
- `doc/development-log.md`
- `apifox_requests.md`
- `smartMall-common`
- `smartMall-admin`

### 下一步建议
- 继续补齐管理后台订单管理能力，例如待发货列表分页、退款审批列表、订单状态筛选查询。
- 补充后台用户管理与系统设置能力，例如用户封禁/启用、提示词管理、RAG 配置维护。
- 若后台首页需要继续增强，可补“热销商品排行”“分类销售分布”“近 7 日下单用户趋势”等看板数据。

### 提交记录
- Git Commit: 本次功能点提交为"完成功能点：管理后台首页数据概览基础能力"。

## 2026-03-08 功能点：用户偏好关键交易节点自动刷新

### 本次目标
- 将用户偏好自动刷新从“评价提交”扩展到更完整的交易链路。
- 在下单、支付成功、确认收货等关键节点完成后，自动异步刷新偏好档案，降低推荐滞后。
- 补充对应联调用例说明，方便通过 Apifox 验证偏好联动更新。

### 本次实现
- 修改 `OrderInfoServiceImpl.createOrder()`：
  - 订单创建成功并清理购物车后，异步触发 `refreshUserPreference`
- 修改 `PaymentInfoServiceImpl.handleSuccessCallback()`：
  - 支付成功回调推进订单状态后，异步触发 `refreshUserPreference`
- 修改 `ShippingInfoServiceImpl.confirmReceive()`：
  - 确认收货成功后，异步触发 `refreshUserPreference`
- 复用既有 `UserPreferenceRefreshTrigger`：
  - 使用不同 `triggerSource` 标识 `order_create`、`payment_success`、`confirm_receive`
- 更新 `apifox_requests.md`：
  - 为创建订单、支付回调、确认收货补充偏好联动断言和测试场景
  - 为偏好档案查询补充交易节点联动校验用例
- 本次未新增接口与数据库表结构，`doc/sql/smart-mall.sql` 无需变更

### 验证记录
- 执行命令：
  - `mvn -q -pl smartMall-common,smartMall-web -am "-Dmaven.repo.local=C:\Users\15712\.m2\repository" "-Dmaven.test.skip=false" test`
- 环境说明：
  - Maven 使用 `D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64` 运行。
- 测试结果：编译与测试通过。

### 当前影响范围
- `doc/development-log.md`
- `apifox_requests.md`
- `smartMall-common`

### 下一步建议
- 继续把退款申请、退款通过/拒绝等订单分支状态纳入偏好自动刷新触发点。
- 扩展 AI Agent 的偏好感知能力，例如根据最新交易阶段主动推荐相似商品、配件或新品。
- 若需要更细粒度的推荐，可在偏好聚合中区分“下单偏好”“已支付偏好”“已完成偏好”等权重来源。

### 提交记录
- Git Commit: 本次功能点提交为"完成功能点：用户偏好关键交易节点自动刷新"。

## 2026-03-08 功能点：用户偏好自动刷新与首页个性化推荐接口

### 本次目标
- 减少用户偏好档案对手工刷新接口的依赖，在关键用户行为后自动刷新偏好数据。
- 基于既有偏好档案能力补齐首页个性化推荐入口，支持用户首页直接读取偏好推荐结果。
- 补充接口测试文档，覆盖评价提交后的偏好联动与首页个性化推荐场景。

### 本次实现
- 在 `smartMall-common` 新增异步执行配置：
  - `AsyncConfig` 提供 `userPreferenceRefreshExecutor`
- 新增偏好异步刷新触发器：
  - `UserPreferenceRefreshTrigger`
  - `UserPreferenceRefreshTriggerImpl`
- 修改 `ProductReviewServiceImpl.submitReview()`：
  - 评价提交成功后异步触发 `refreshUserPreference`
  - 兼容“评价提交”与“最后一条评价导致订单完成”的联动刷新场景
- 修改 `ProductInfoServiceImpl`：
  - `loadPersonalizedRecommendProducts(userId, limit)` 在用户无偏好档案时先尝试自动刷新，再决定是否回退到通用推荐
  - `normalizeRecommendLimit()` 复用 `smart-mall.preference.default-recommend-limit` 配置项
- 修改 `MallProductController`：
  - 扩展 `GET /api/product/recommend`
  - 新增可选参数 `userId`
  - 未传 `userId` 时保持原有通用推荐逻辑，传入 `userId` 时返回首页个性化推荐结果
- 更新 `apifox_requests.md`：
  - 补充评价提交后的偏好联动测试用例
  - 补充首页个性化推荐测试用例
- 本次未新增数据库表结构，`doc/sql/smart-mall.sql` 无需变更

### 验证记录
- 执行命令：
  - `mvn -q -pl smartMall-common,smartMall-web -am "-Dmaven.repo.local=C:\Users\15712\.m2\repository" "-Dmaven.test.skip=false" test`
- 环境说明：
  - Maven 使用 `D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64` 运行。
- 测试结果：编译与测试通过。

### 当前影响范围
- `doc/development-log.md`
- `apifox_requests.md`
- `smartMall-common`
- `smartMall-web`

### 下一步建议
- 在订单创建、支付成功、确认收货等关键节点继续补齐偏好自动刷新触发点，降低推荐滞后。
- 扩展 AI Agent 的偏好感知能力，例如主动推荐用户近期可能感兴趣的新品或相似商品。
- 若首页需要继续细化，可在个性化推荐基础上拆分“猜你喜欢 / 最近偏好 / 新品推荐”等推荐分组。

### 提交记录
- Git Commit: 本次功能点提交为"完成功能点：用户偏好自动刷新与首页个性化推荐接口"。

## 2026-03-08 功能点：用户偏好记忆与历史偏好学习基础能力

### 本次目标
- 聚合用户的购买、搜索、评价、购物车行为数据，生成结构化偏好档案。
- 提供偏好档案查询和刷新接口。
- 基于偏好档案实现个性化推荐，替代规则助手的通用推荐。
- 为 AI Agent 注入用户偏好上下文，提升对话推荐质量。
- 新增 MCP 工具支持偏好查询和个性化推荐。

### 本次实现
- 在 `smartMall-common` 新增用户偏好领域模型：
  - `UserPreference` 用户偏好档案实体
- 新增偏好 VO：
  - `UserPreferenceVO` 偏好档案视图（列表字段转 List 展示）
- 新增偏好配置：
  - `UserPreferenceProperties` 偏好聚合参数配置
- 新增偏好 Mapper / Service / ServiceImpl：
  - `UserPreferenceMapper`
  - `UserPreferenceService`
  - `UserPreferenceServiceImpl`（核心聚合逻辑）
- 偏好聚合算法（`buildUserPreference`）：
  - 查询近 90 天有效订单 → 关联订单明细 → 提取购买商品 ID 和价格
  - 通过商品信息获取分类 ID → 频次排序取 top 5 分类
  - 购物车商品补充分类偏好
  - 取购买价格 min/max 作为价格偏好区间
  - 从 `assistant_chat_log`（intent=PRODUCT_SEARCH）提取近期搜索关键词
  - 从 `product_review` 计算用户平均评分和评价数
  - 分类名称 + 高频搜索词组合为偏好标签
  - Upsert 写入 `user_preference` 表
- 在 `ProductInfoService` 新增 `loadPersonalizedRecommendProducts(userId, limit)` 方法：
  - 偏好分类优先 + 价格区间过滤 + 排除已购商品
  - 按 `totalSale DESC` 排序
  - 不足时用通用推荐补齐
- 修改 `ProductInfoServiceImpl`：
  - 注入 `UserPreferenceService`（`@Lazy` 解决循环依赖）
  - 实现个性化推荐逻辑
- 修改 `MallAssistantServiceImpl.handleRecommend()`：
  - 有 userId 时改用 `loadPersonalizedRecommendProducts`
- 修改 `MallAssistantAgentServiceImpl.buildUserPrompt()`：
  - 新增 `buildPreferenceContext()` 注入偏好分类、价格区间、偏好标签等上下文
- 在 `smartMall-web` 新增 `MallPreferenceController`，提供接口：
  - `GET /api/preference/profile?userId=xxx` 查询用户偏好档案
  - `POST /api/preference/refresh?userId=xxx` 刷新用户偏好档案
- 在 `smartMall-mcp` 修改 `SmartMallMcpTools`，新增 MCP 工具：
  - `get_user_preference` 获取用户偏好档案
  - `personalized_recommend` 基于用户偏好推荐商品
- 在 `application.yml`（web + mcp）新增 `smart-mall.preference.*` 配置项
- 在 `doc/sql/smart-mall.sql` 追加 `user_preference` 表结构

### 验证记录
- 执行命令：
  - `mvn -q -pl smartMall-common,smartMall-web,smartMall-mcp -am "-Dmaven.repo.local=C:\Users\15712\.m2\repository" "-Dmaven.test.skip=false" test`
- 环境说明：
  - Maven 使用 `D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64` 运行。
- 测试结果：编译与测试通过。

### 当前影响范围
- `doc/sql`
- `doc/development-log.md`
- `apifox_requests.md`
- `smartMall-common`
- `smartMall-web`
- `smartMall-mcp`

### 下一步建议
- 实现用户偏好自动刷新（如订单完成、评价提交后触发异步刷新）。
- 基于偏好数据实现首页个性化推荐接口。
- 扩展 AI Agent 的偏好感知能力（如主动推荐用户可能感兴趣的新品）。

### 提交记录
- Git Commit: 本次功能点提交为"完成功能点：用户偏好记忆与历史偏好学习基础能力"。

## 2026-03-08 功能点：用户端商品评价与订单完成

### 本次目标
- 补齐已收货订单的商品评价能力。
- 支持按订单项逐一评价（评分 + 评价内容），全部评价完成后订单自动推进到"已完成"状态。
- 提供订单评价查询、商品评价分页查询接口。
- 为后续智能购物模式中的对话式商品推荐提供评价数据基础。

### 本次实现
- 在 `smartMall-common` 新增评价领域模型：
  - `ProductReview` 商品评价实体
- 新增评价 DTO：
  - `ReviewItemDTO` 单条评价项入参
  - `ReviewSubmitDTO` 批量提交评价入参
  - `ReviewQueryDTO` 商品评价查询入参（含分页）
- 新增评价 VO：
  - `ProductReviewVO` 评价信息视图
- 新增评价 Mapper / Service / ServiceImpl：
  - `ProductReviewMapper`
  - `ProductReviewService`
  - `ProductReviewServiceImpl`
- 在 `smartMall-web` 新增 `MallReviewController`，提供接口：
  - `POST /api/review/submit` 提交评价
  - `GET /api/review/orderReviews?userId=xxx&orderId=xxx` 查询订单评价
  - `POST /api/review/productReviews` 查询商品评价（分页）
- 评价能力已支持：
  - 仅已收货（RECEIVED）订单可提交评价。
  - 每个订单项只能评价一次，重复评价时报错。
  - 评分范围 1-5 星，评价内容可选。
  - 提交时校验 itemId 和 productId 归属关系。
  - 当订单全部订单项评价完成后，订单状态自动推进到"已完成"（COMPLETED）。
- 扩展 `OrderInfo` 新增 `completeTime` 字段，`OrderDetailVO` 同步回显。
- 在 `OrderInfoService` / `OrderInfoServiceImpl` 新增：
  - `markOrderCompleted` 标记订单为已完成
- 在 `doc/sql/smart-mall.sql` 追加：
  - `order_info.complete_time` 字段
  - `product_review` 表结构

### 验证记录
- 执行命令：
  - `mvn -q -pl smartMall-common,smartMall-web -am "-Dmaven.repo.local=C:\Users\15712\.m2\repository" "-Dmaven.test.skip=false" test`
- 环境说明：
  - Maven 使用 `D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64` 运行。
- 测试结果：编译与测试通过。

### 当前影响范围
- `doc/sql`
- `doc/development-log.md`
- `apifox_requests.md`
- `smartMall-common`
- `smartMall-web`

### 下一步建议
- 衔接智能购物模式：WebSocket 对话接入、Spring AI + RAG 商品检索、MCP 工具集成。
- 或补充管理后台功能：数据概览、商品管理、订单管理。

### 提交记录
- Git Commit: 本次功能点提交为"完成功能点：用户端商品评价与订单完成"。

## 2026-03-08 功能点：用户端发货模拟与确认收货

### 本次目标
- 补齐已支付订单到发货、收货的状态流转链路。
- 提供模拟发货接口（自动生成物流单号）、物流查询接口、确认收货接口。
- 扩展退款能力，允许已发货订单也可以申请退款。
- 为后续商品评价功能提供"已收货"状态基础。

### 本次实现
- 在 `smartMall-common` 新增物流领域模型与枚举：
  - `ShippingInfo` 物流记录实体
  - `ShippingStatusEnum`（SHIPPED / IN_TRANSIT / DELIVERED）
- 新增物流 DTO / VO：
  - `ShipOrderDTO` 模拟发货入参
  - `ConfirmReceiveDTO` 确认收货入参
  - `ShippingInfoVO` 物流信息视图
- 新增物流 Mapper / Service / ServiceImpl：
  - `ShippingInfoMapper`
  - `ShippingInfoService`
  - `ShippingInfoServiceImpl`
- 在 `smartMall-web` 新增 `MallShippingController`，提供接口：
  - `POST /api/shipping/ship` 模拟发货
  - `GET /api/shipping/detail?userId=xxx&orderId=xxx` 查询物流
  - `POST /api/shipping/confirmReceive` 确认收货
- 物流能力已支持：
  - 仅已支付（PAID）订单可发货。
  - 发货自动生成物流单号（SF 前缀 + 时间戳 + 随机数）。
  - 默认快递公司为"模拟快递"，支持自定义。
  - 重复发货时复用已有物流单。
  - 仅已发货（SHIPPED）订单可确认收货。
  - 确认收货后物流状态变为"已签收"，订单状态推进到"已收货"。
- 扩展 `OrderStatusEnum` 新增：
  - `SHIPPED(40, "已发货")`
  - `RECEIVED(50, "已收货")`
- `OrderInfo` 新增 `shipTime`、`receiveTime` 字段，`OrderDetailVO` 同步回显。
- 在 `OrderInfoService` / `OrderInfoServiceImpl` 新增：
  - `markOrderShipped` 标记订单为已发货
  - `markOrderReceived` 标记订单为已收货
- 退款申请扩展：已发货订单也可以申请退款（`RefundInfoServiceImpl` 更新判断条件）。
- 在 `doc/sql/smart-mall.sql` 追加：
  - `order_info.ship_time`、`order_info.receive_time` 字段
  - `order_info.order_status` 注释更新（含发货/收货状态）
  - `shipping_info` 表结构

### 验证记录
- 执行命令：
  - `mvn -q -pl smartMall-common,smartMall-web -am "-Dmaven.repo.local=C:\Users\15712\.m2\repository" "-Dmaven.test.skip=false" test`
- 环境说明：
  - Maven 使用 `D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64` 运行。
- 测试结果：编译与测试通过。

### 当前影响范围
- `doc/sql`
- `doc/development-log.md`
- `apifox_requests.md`
- `smartMall-common`
- `smartMall-web`

### 下一步建议
- 实现商品评价功能（已收货订单可评价，补 COMPLETED 状态流转）。
- 再衔接智能购物模式中的对话式订单操作。

### 提交记录
- Git Commit: 本次功能点提交为"完成功能点：用户端发货模拟与确认收货"。

## 2026-03-08 功能点：用户端退款申请与退款状态流转

### 本次目标
- 衔接已支付订单，补齐用户端退款申请能力。
- 提供退款审批（模拟同意/拒绝）入口，推动订单状态从已支付流转到退款申请中，再到已退款或恢复已支付。
- 为后续确认收货、订单完成、商品评价等功能打基础。

### 本次实现
- 在 `smartMall-common` 新增退款领域模型与枚举：
  - `RefundInfo` 退款记录实体
  - `RefundStatusEnum`（PENDING / APPROVED / REJECTED）
- 新增退款 DTO / VO：
  - `RefundApplyDTO` 退款申请入参
  - `RefundAuditDTO` 退款审批入参
  - `RefundInfoVO` 退款信息视图
- 新增退款 Mapper / Service / ServiceImpl：
  - `RefundInfoMapper`
  - `RefundInfoService`
  - `RefundInfoServiceImpl`
- 在 `smartMall-web` 新增 `MallRefundController`，提供接口：
  - `POST /api/refund/apply` 提交退款申请
  - `GET /api/refund/detail?userId=xxx&orderId=xxx` 查询退款详情
  - `POST /api/refund/approve` 同意退款（模拟）
  - `POST /api/refund/reject` 拒绝退款
- 退款能力已支持：
  - 仅已支付（PAID）订单可发起退款。
  - 重复申请退款时复用未处理的退款单。
  - 申请退款后订单状态推进到"退款申请中"。
  - 同意退款后订单状态推进到"已退款"，退款单标记为已通过。
  - 拒绝退款后订单状态恢复到"已支付"，退款单标记为已拒绝。
  - 退款金额默认等于订单总金额（全额退款）。
- 扩展 `OrderStatusEnum` 新增：
  - `REFUND_REQUESTED(60, "退款申请中")`
  - `REFUNDED(70, "已退款")`
- `OrderInfo` 新增 `refundTime` 字段，`OrderDetailVO` 同步回显退款时间。
- 在 `OrderInfoService` / `OrderInfoServiceImpl` 新增：
  - `markOrderRefundRequested` 标记订单为退款申请中
  - `markOrderRefunded` 标记订单为已退款
  - `revertOrderFromRefund` 退款拒绝后恢复订单状态
- 在 `doc/sql/smart-mall.sql` 追加：
  - `order_info.refund_time` 字段
  - `order_info.order_status` 注释更新（含退款状态）
  - `refund_info` 表结构

### 验证记录
- 执行命令：
  - `mvn -q -pl smartMall-common,smartMall-web -am "-Dmaven.repo.local=C:\Users\15712\.m2\repository" "-Dmaven.test.skip=false" test`
- 环境说明：
  - Maven 使用 `D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64` 运行。
- 测试结果：编译与测试通过。

### 当前影响范围
- `doc/sql`
- `doc/development-log.md`
- `smartMall-common`
- `smartMall-web`

### 下一步建议
- 实现确认收货与订单完成流程（补充 SHIPPED / RECEIVED 状态、模拟物流）。
- 补充商品评价功能。
- 再衔接智能购物模式中的对话式订单操作。

### 提交记录
- Git Commit: 本次功能点提交为"完成功能点：用户端退款申请与退款状态流转"。

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

## 2026-03-08 功能点：智能购物对话接入与商品检索基础能力

### 本次目标
- 衔接开发日志中的“智能购物模式”下一步建议，先落地可运行的对话接入层。
- 打通智能购物模式中的商品检索、商品推荐、商品详情、订单查询、取消订单五类高频对话场景。
- 为后续接入 Spring AI、RAG 商品检索与 MCP 工具调用保留统一入口。

### 本次实现
- 在 `smartMall-common` 新增智能购物会话领域模型：
  - `AssistantChatLog` 会话日志实体
  - `AssistantIntentEnum` 对话意图枚举
- 新增智能购物 DTO / VO：
  - `AssistantChatRequestDTO` 对话请求入参
  - `AssistantHistoryQueryDTO` 会话历史分页查询入参
  - `AssistantChatResponseVO` 对话响应视图
  - `AssistantChatPayloadVO` 对话载荷视图
  - `AssistantChatHistoryVO` 会话历史视图
  - `AssistantOperationVO` 对话操作结果视图
- 新增会话日志 Mapper / Service / ServiceImpl：
  - `AssistantChatLogMapper`
  - `AssistantChatLogService`
  - `AssistantChatLogServiceImpl`
- 新增智能购物编排服务：
  - `MallAssistantService`
  - `MallAssistantServiceImpl`
- 在 `smartMall-web` 新增智能购物入口：
  - `POST /api/assistant/chat` 同步对话接口
  - `POST /api/assistant/history` 会话历史分页查询接口
  - `WS /api/ws/assistant` WebSocket 对话入口
- 对话能力已支持：
  - 关键词商品搜索（复用用户端在售商品列表能力）
  - 推荐商品查询
  - 商品详情查询
  - 用户订单列表查询（支持从自然语言中识别常见状态）
  - 对话式取消待支付订单
  - 智能购物会话日志持久化
- 在 `doc/sql/smart-mall.sql` 追加：
  - `assistant_chat_log` 表结构
- 补充测试：
  - `MallAssistantServiceImplTest`
  - `MallAssistantControllerTest`
  - `MallAssistantWebSocketHandlerTest`

### 验证记录
- 执行命令：
  - `mvn -q -pl smartMall-common,smartMall-web -am "-Dmaven.repo.local=C:\Users\15712\.m2\repository" "-Dmaven.test.skip=false" test`
- 环境说明：
  - Maven 使用 `D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64` 运行。
- 测试结果：编译与测试通过。

### 当前影响范围
- `doc/sql`
- `doc/development-log.md`
- `apifox_requests.md`
- `smartMall-common`
- `smartMall-web`

### 下一步建议
- 接入 Spring AI 模型与提示词编排，替换当前规则式意图识别。
- 衔接 Elasticsearch / RAG 商品检索，支持更自然的商品需求描述。
- 在 `smartMall-mcp` 中补充商品查询、订单查询等 MCP 工具，并接入对话流程。
- 继续补齐对话式退款、确认收货、评价商品等订单操作。

### 提交记录
- Git Commit: 本次功能点提交为“完成功能点：智能购物对话接入与商品检索基础能力”。

## 2026-03-08 功能点：智能购物对话式订单操作扩展与 MCP 工具基础能力

### 本次目标
- 在已完成的智能购物助手基础上，继续补齐对话式订单操作能力。
- 让智能购物助手支持退款申请、退款详情查询、确认收货、订单评价查询、结构化评价提交。
- 在 `smartMall-mcp` 提供首批商品与订单工具，为后续 Spring AI Agent / MCP Client 接入做准备。

### 本次实现
- 扩展 `AssistantChatRequestDTO`：
  - 新增 `refundReason`
  - 新增 `reviews`
- 扩展 `AssistantIntentEnum`：
  - `REFUND_APPLY`
  - `REFUND_DETAIL`
  - `RECEIVE_CONFIRM`
  - `ORDER_REVIEW_QUERY`
  - `REVIEW_SUBMIT`
- 扩展 `AssistantChatPayloadVO`：
  - 新增 `refundInfo`
  - 新增 `shippingInfo`
  - 新增 `orderReviews`
- 扩展 `MallAssistantServiceImpl`，新增对话编排能力：
  - 对话式退款申请
  - 对话式退款详情查询
  - 对话式确认收货
  - 对话式订单评价查询
  - 结构化对话评价提交（通过 `reviews` 透传订单项评价）
- 在 `smartMall-mcp` 新增 `SmartMallMcpTools`，首批提供工具：
  - `search_visible_products`
  - `recommend_products`
  - `get_product_detail`
  - `list_orders`
  - `get_order_detail`
  - `cancel_order`
  - `apply_refund`
  - `get_refund_detail`
  - `confirm_receive`
  - `get_order_reviews`
- 更新 `smartMall-mcp/pom.xml`，补充测试依赖用于本地验证。

### 验证记录
- 执行命令：
  - `mvn -q -pl smartMall-common,smartMall-web,smartMall-mcp -am "-Dmaven.repo.local=C:\Users\15712\.m2\repository" "-Dmaven.test.skip=false" test`
- 环境说明：
  - Maven 使用 `D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64` 运行。
- 测试结果：编译与测试通过。
- 说明：
  - 本次测试文件仅用于本地验证，不纳入提交。

### 当前影响范围
- `doc/development-log.md`
- `apifox_requests.md`
- `smartMall-common`
- `smartMall-web`
- `smartMall-mcp`

### 下一步建议
- 在 `smartMall-web` 接入 Spring AI Client，通过 MCP Client 调用 `smartMall-mcp` 工具。
- 接入 Elasticsearch / RAG 检索，把规则式商品搜索升级为语义检索。
- 补齐对话式发货模拟、退款审批、结构化评价提交的前端会话交互约束。

### 提交记录
- Git Commit: 本次功能点提交建议为“完成功能点：智能购物对话式订单操作扩展与 MCP 工具基础能力”。

## 2026-03-08 功能点：智能购物 Spring AI Agent 接入与 MCP Client 联调基础能力

### 本次目标
- 在 `smartMall-web` 正式接入 Spring AI ChatClient。
- 通过 MCP Client 调用 `smartMall-mcp` 暴露的商品、订单与退款工具。
- 保持现有规则式助手可用，在未配置模型或工具不可用时自动回退。

### 本次实现
- 在 `smartMall-web/pom.xml` 增加 Spring AI 依赖：
  - `spring-ai-starter-model-openai`
  - `spring-ai-starter-mcp-client-webflux`
- 在 `smartMall-web` 新增 Agent 配置与服务：
  - `SmartMallAssistantAgentProperties`
  - `MallAssistantAgentService`
  - `MallAssistantAgentServiceImpl`
- 新增 `POST /api/assistant/agent/chat` 接口：
  - 已配置模型和 MCP 工具时，走 Spring AI Agent 对话流程
  - 未配置 `OPENAI_API_KEY` 或未连通 MCP 时，自动回退到现有规则助手
- 扩展 `MallAssistantService`：
  - 新增 `recordChat`，用于统一记录 AI Agent 与规则助手的对话历史
- 更新 `application.yml`：
  - 增加 `spring.ai.openai` 配置占位
  - 增加 `spring.ai.mcp.client.streamable-http` 连接配置
  - 增加 `smart-mall.assistant.agent.enabled` 开关

### 验证记录
- 执行命令：
  - `mvn -q -pl smartMall-common,smartMall-web,smartMall-mcp -am "-Dmaven.repo.local=C:\Users\15712\.m2\repository" "-Dmaven.test.skip=false" test`
- 环境说明：
  - Maven 使用 `D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64` 运行。
- 测试结果：编译与测试通过。
- 说明：
  - 本次测试文件仅用于本地验证，不纳入提交。

### 当前影响范围
- `doc/development-log.md`
- `apifox_requests.md`
- `smartMall-common`
- `smartMall-web`

### 下一步建议
- 接入 Elasticsearch / RAG 检索，把规则式商品搜索升级为语义检索。
- 为 AI Agent 输出补齐结构化商品比较、偏好记忆和历史偏好学习能力。
- 补齐对话式发货模拟、退款审批、结构化评价提交的前端会话交互约束。

### 提交记录
- Git Commit: 本次功能点提交建议为“完成功能点：智能购物 Spring AI Agent 接入与 MCP Client 联调基础能力”。

## 2026-03-08 功能点：商品 Elasticsearch 语义检索与数据库回退基础能力

### 本次目标
- 接入 Elasticsearch 商品检索能力，为用户端商品搜索、规则助手商品搜索和 MCP 商品搜索提供统一的语义检索入口。
- 在不破坏现有商品列表接口的前提下，把商品关键词搜索升级为“ES 优先、数据库回退”模式。
- 保持未配置 Elasticsearch 时系统仍可正常工作。

### 本次实现
- 在 `smartMall-common` 新增商品搜索配置：
  - `ProductSearchProperties`
- 扩展 `ProductQueryDTO`：
  - 新增 `semanticSearch`
- 增强 `ProductInfoServiceImpl`：
  - `loadVisibleProductList` 在启用语义检索且存在关键词时，优先调用 Elasticsearch
  - Elasticsearch 返回商品 ID 后，回查 MySQL 并复用现有商品列表组装逻辑
  - Elasticsearch 不可用、无结果、索引数据不一致或调用异常时，自动回退原有数据库模糊查询
  - 现有规则助手 `MallAssistantServiceImpl` 和 MCP 工具 `search_visible_products` 因复用 `ProductInfoService`，同步获得语义检索能力
- 更新运行配置：
  - `smartMall-web/src/main/resources/application.yml`
  - `smartMall-mcp/src/main/resources/application.yml`
  - 新增 `SMART_MALL_SEMANTIC_SEARCH_ENABLED`
  - 新增 `SMART_MALL_ES_URL`
  - 新增 `SMART_MALL_PRODUCT_INDEX`
  - 新增 `SMART_MALL_SEMANTIC_CANDIDATE_SIZE`

### 验证记录
- 执行命令：
  - `mvn -q -pl smartMall-common,smartMall-web,smartMall-mcp -am "-Dmaven.repo.local=C:\Users\15712\.m2\repository" "-Dmaven.test.skip=false" test`
- 环境说明：
  - Maven 使用 `D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64` 运行。
- 测试结果：编译与测试通过。
- 说明：
  - 本次测试文件仅用于本地验证，不纳入提交。

### 当前影响范围
- `doc/development-log.md`
- `apifox_requests.md`
- `smartMall-common`
- `smartMall-web`
- `smartMall-mcp`

### 下一步建议
- 在语义检索基础上继续补齐 RAG 知识增强检索，把商品卖点、评价摘要、售后说明纳入召回内容。
- 为 AI Agent 输出补齐结构化商品比较、偏好记忆和历史偏好学习能力。
- 补齐对话式发货模拟、退款审批、结构化评价提交的前端会话交互约束。

### 提交记录
- Git Commit: 本次功能点提交建议为“完成功能点：商品 Elasticsearch 语义检索与数据库回退基础能力”。

## 2026-03-08 功能点：RAG 商品知识增强检索基础能力

### 本次目标
- 在商品语义检索基础上，补齐可供 AI Agent 和 MCP 工具直接使用的商品知识增强层。
- 把商品卖点、评价摘要、售后说明组装成统一知识卡片，作为 RAG 检索结果返回。
- 让智能购物 Agent 在处理商品问题时，自动带上知识增强上下文。

### 本次实现
- 在 `smartMall-common` 新增商品知识配置：
  - `ProductKnowledgeProperties`
- 新增商品知识 DTO / VO：
  - `ProductKnowledgeQueryDTO`
  - `ProductKnowledgeVO`
- 新增商品知识服务：
  - `ProductKnowledgeService`
  - `ProductKnowledgeServiceImpl`
- 商品知识服务能力包括：
  - 基于现有商品搜索结果构建知识卡片
  - 聚合商品描述与属性信息，生成卖点摘要
  - 聚合商品评价分页结果，生成评价摘要与平均评分
  - 统一输出售后说明、知识标签和 `knowledgeText`
- 在 `smartMall-web` 新增商品知识接口：
  - `POST /api/product/knowledge/search`
  - `GET /api/product/knowledge/detail/{productId}`
- 增强 `MallAssistantAgentServiceImpl`：
  - 在商品相关提问场景中，自动追加商品知识增强上下文
  - 有 `productId` 时优先加载单商品知识
  - 无 `productId` 时按消息内容检索前 2 条知识卡片
- 扩展 `smartMall-mcp` 工具：
  - `search_product_knowledge`
  - `get_product_knowledge`
- 更新运行配置：
  - `smartMall-web/src/main/resources/application.yml`
  - `smartMall-mcp/src/main/resources/application.yml`
  - 新增 `SMART_MALL_RAG_PRODUCT_ENABLED`
  - 新增 `SMART_MALL_RAG_PRODUCT_PAGE_SIZE`
  - 新增 `SMART_MALL_RAG_REVIEW_SNIPPET_COUNT`
  - 新增 `SMART_MALL_RAG_PROPERTY_SNIPPET_COUNT`
  - 新增 `SMART_MALL_AFTER_SALES_1/2/3`

### 验证记录
- 执行命令：
  - `mvn -q -pl smartMall-common,smartMall-web,smartMall-mcp -am "-Dmaven.repo.local=C:\Users\15712\.m2\repository" "-Dmaven.test.skip=false" test`
- 环境说明：
  - Maven 使用 `D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64` 运行。
- 测试结果：编译与测试通过。
- 说明：
  - 本次测试文件仅用于本地验证，不纳入提交。

### 当前影响范围
- `doc/development-log.md`
- `apifox_requests.md`
- `smartMall-common`
- `smartMall-web`
- `smartMall-mcp`

### 下一步建议
- 为 AI Agent 输出补齐结构化商品比较能力，支持多个商品知识卡片并排比对。
- 增加用户偏好记忆与历史偏好学习，把最近搜索、购买和评价行为纳入推荐上下文。
- 补齐对话式发货模拟、退款审批、结构化评价提交的前端会话交互约束。

### 提交记录
- Git Commit: 本次功能点提交建议为“完成功能点：RAG 商品知识增强检索基础能力”。

## 2026-03-08 功能点：AI Agent 结构化商品比较基础能力

### 本次目标
- 在现有商品知识卡片基础上，补齐可直接供 AI Agent 和 MCP 工具使用的结构化商品比较能力。
- 支持按多个商品 ID 或自然语言关键词一次拉起多张知识卡片，并按价格、卖点、口碑、售后、标签并排比对。
- 让智能购物 Agent 在“对比/比较/哪个好/vs”等商品决策场景下，自动携带结构化比较上下文。

### 本次实现
- 在 `smartMall-common` 新增商品比较 DTO / VO：
  - `ProductKnowledgeCompareDTO`
  - `ProductKnowledgeCompareVO`
  - `ProductKnowledgeCompareDimensionVO`
  - `ProductKnowledgeCompareCellVO`
- 扩展 `ProductKnowledgeService` 与 `ProductKnowledgeServiceImpl`：
  - 新增 `compareKnowledge`
  - 支持按 `productIds` 直接比较
  - 支持按 `keyword` 复用现有 RAG 知识检索结果，自动抽取前 N 个商品卡片进行比较
  - 输出结构化维度行、推荐关注点、总结文案和可直接注入 Prompt 的 `comparisonText`
- 扩展 `ProductKnowledgeProperties`：
  - 新增 `maxCompareCount`
- 在 `smartMall-web` 扩展商品知识接口：
  - 新增 `POST /api/product/knowledge/compare`
- 增强 `MallAssistantAgentServiceImpl`：
  - 当用户消息包含“对比 / 比较 / 哪个好 / 怎么选 / 区别 / vs / pk”等关键词时
  - 自动走商品知识结构化比较服务
  - 将比较结果作为增强上下文注入 Spring AI Agent Prompt
- 扩展 `smartMall-mcp` 工具：
  - 新增 `compare_product_knowledge`
- 更新运行配置：
  - `smartMall-web/src/main/resources/application.yml`
  - `smartMall-mcp/src/main/resources/application.yml`
  - 新增 `SMART_MALL_RAG_COMPARE_COUNT`

### 验证记录
- 执行命令：
  - `mvn -q -pl smartMall-common,smartMall-web,smartMall-mcp -am "-Dmaven.repo.local=C:\Users\15712\.m2\repository" "-Dmaven.test.skip=false" test`
- 环境说明：
  - Maven 使用 `D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64` 运行。
- 测试结果：编译与测试通过。
- 说明：
  - 本次未新增数据库表结构。
  - 本次测试文件仅用于本地验证，不纳入提交。

### 当前影响范围
- `doc/development-log.md`
- `apifox_requests.md`
- `smartMall-common`
- `smartMall-web`
- `smartMall-mcp`

### 下一步建议
- 增加用户偏好记忆与历史偏好学习，把最近搜索、购买和评价行为纳入推荐上下文。
- 继续补齐对话式发货模拟、退款审批、结构化评价提交的前端会话交互约束。

### 提交记录
- Git Commit: 本次功能点提交建议为“完成功能点：AI Agent 结构化商品比较基础能力”。
## 2026-03-08 功能点：管理后台 AI 配置管理基础能力

### 本次目标
- 按功能导图与技术方案补齐管理后台“AI 配置管理”基础能力。
- 提供智能助手提示词、商品语义搜索和商品知识增强配置的统一查询/保存入口。
- 让后台保存的 AI 配置能直接影响 Web 端 Spring AI Agent 与商品知识服务运行时行为，而不是只做静态展示。

### 本次实现
- 在 `doc/sql/smart-mall.sql` 新增 `sys_ai_config` 表：
  - 存储后台维护的 AI 配置快照，按 `assistant_agent`、`product_search`、`product_knowledge` 三类配置分组。
- 在 `smartMall-common` 新增 AI 配置领域模型与服务：
  - `SysAiConfig`
  - `SysAiConfigMapper`
  - `AdminAiConfigSaveDTO`
  - `AdminAiConfigVO`
  - `AiConfigService`
  - `AiConfigServiceImpl`
- 在 `smartMall-common` 新增共享配置类：
  - `SmartMallAssistantAgentProperties`
  - 将智能助手默认提示词配置下沉到 `common`，供 `admin` 与 `web` 共同读取。
- 在 `smartMall-admin` 新增 `AiConfigController`：
  - `GET /api/ai-config/detail`
  - `POST /api/ai-config/save`
- 运行时配置联动：
  - `MallAssistantAgentServiceImpl` 改为优先读取后台保存的智能助手开关与提示词，未配置时回退 `application.yml`
  - `ProductInfoServiceImpl` 改为优先读取后台保存的语义搜索配置（ES 地址、索引名、候选数）
  - `ProductKnowledgeServiceImpl` 改为优先读取后台保存的商品知识增强配置（分页、评论摘要、属性摘要、比较数量、售后亮点）
- 更新 `smartMall-admin/src/main/resources/application.yml`：
  - 补齐后台默认 AI 配置项，避免后台展示值与运行默认值脱节。
- 更新 `apifox_requests.md`：
  - 新增后台 AI 配置查询/保存接口调试文档。

### 验证记录
- 执行命令：
  - `mvn -q -pl smartMall-common,smartMall-admin,smartMall-web -am "-Dmaven.repo.local=C:\Users\15712\.m2\repository" "-DfailIfNoTests=false" test`
- 环境说明：
  - Maven 使用 `D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64` 运行。
- 测试结果：编译与测试通过。

### 当前影响范围
- `doc/sql`
- `doc/development-log.md`
- `apifox_requests.md`
- `smartMall-common`
- `smartMall-admin`
- `smartMall-web`

### 下一步建议
- 继续补齐 AI 配置管理中的知识库维护能力，例如商品知识重建、索引同步、配置版本回滚。
- 补充 AI 服务监控能力，例如最近调用状态、降级原因统计、模型与 MCP 工具连通性检查。
- 若继续沿管理后台主线推进，也可并行补齐“用户账户与权限管理”模块。

### 提交记录
- Git Commit: 本次功能点提交建议为“完成功能点：管理后台 AI 配置管理基础能力”。
## 2026-03-08 功能点：管理后台 RAG 知识库维护基础能力

### 本次目标
- 按功能导图与技术方案补齐“RAG 知识库维护与更新”后台能力。
- 提供商品知识卡片预览、搜索索引概览、单商品同步和全量重建入口。
- 让后台可以直接维护 Elasticsearch 中的商品知识搜索索引，减少索引与数据库数据漂移。

### 本次实现
- 在 `smartMall-common` 新增后台知识维护 VO：
  - `AdminKnowledgeIndexSummaryVO`
  - `AdminKnowledgeIndexSyncResultVO`
- 在 `smartMall-common` 新增后台知识维护 Service / ServiceImpl：
  - `AdminKnowledgeManageService`
  - `AdminKnowledgeManageServiceImpl`
- 在 `smartMall-admin` 新增 `KnowledgeManageController`，提供接口：
  - `GET /api/knowledge/product/{productId}` 查询商品知识卡片预览
  - `GET /api/knowledge/index/summary` 查询知识索引概览
  - `POST /api/knowledge/index/sync/{productId}` 同步单个商品到搜索索引
  - `POST /api/knowledge/index/rebuild` 全量重建在售商品搜索索引
- 索引维护能力说明：
  - 单商品同步时，若商品为在售状态，则重建对应商品知识文档并写入 Elasticsearch
  - 若商品已下架，则从 Elasticsearch 删除该商品文档，避免索引脏数据
  - 全量重建时，先重建索引，再把当前全部在售商品按知识卡片内容批量写入
  - 索引文档同时写入 `productName`、`productDesc`、`categoryName`、`knowledgeText`、`reviewSummary`、`afterSalesSummary`、`knowledgeTags` 等字段，为后续搜索与知识增强共用
- 配置复用说明：
  - 复用 `AiConfigService` 中已维护的 Elasticsearch 地址、索引名、知识增强配置
  - 后台概览会显示语义搜索开关、知识增强开关、在售商品数、当前索引文档数与连通状态
- 本次未新增数据库表结构，`doc/sql/smart-mall.sql` 无需变更
- 更新 `apifox_requests.md`：
  - 新增后台知识维护接口调试文档

### 验证记录
- 执行命令：
  - `mvn -q -pl smartMall-common,smartMall-admin,smartMall-web -am "-Dmaven.repo.local=C:\Users\15712\.m2\repository" "-DfailIfNoTests=false" test`
- 环境说明：
  - Maven 使用 `D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64` 运行。
- 测试结果：编译与测试通过。

### 当前影响范围
- `doc/development-log.md`
- `apifox_requests.md`
- `smartMall-common`
- `smartMall-admin`

### 下一步建议
- 补充 AI 服务监控能力，例如模型调用状态、MCP 工具可用性、最近降级原因统计。
- 若要继续完善知识库维护，可补充索引映射初始化、增量同步日志和失败重试能力。
- 若仍按后台主线推进，可继续补齐用户账户与权限管理。

### 提交记录
- Git Commit: 本次功能点提交建议为“完成功能点：管理后台 RAG 知识库维护基础能力”。
## 2026-03-08 功能点：管理后台 AI 服务监控基础能力

### 本次目标
- 补齐 AI 配置管理后的下一步建议，提供后台可直接查看的 AI 服务监控概览。
- 覆盖智能助手、模型配置、MCP 连通性、语义搜索、商品知识增强等关键 AI 组件状态。
- 记录运行期降级与成功事件，避免后台只能看到静态配置，看不到真实运行情况。

### 本次实现
- 在 `doc/sql/smart-mall.sql` 新增 `ai_monitor_event` 表：
  - 存储 AI 运行期监控事件，包括来源、类型、事件编码、说明、用户与会话信息。
- 在 `smartMall-common` 新增监控配置：
  - `AdminAiMonitorProperties`
  - 支持配置最近统计天数、最近事件条数、OpenAI 基础地址与 MCP 地址。
- 在 `smartMall-common` 新增 AI 监控领域模型与服务：
  - `AiMonitorEvent`
  - `AiMonitorEventMapper`
  - `AiMonitorEventService`
  - `AiMonitorEventServiceImpl`
  - `AdminAiMonitorService`
  - `AdminAiMonitorServiceImpl`
- 在 `smartMall-common` 新增监控 VO：
  - `AdminAiMonitorOverviewVO`
  - `AdminAiServiceStatusVO`
  - `AdminAiMonitorMetricVO`
  - `AdminAiMonitorRecentEventVO`
- 在 `smartMall-admin` 新增 `AiMonitorController`：
  - `GET /api/ai-monitor/overview`
- 监控概览能力包括：
  - 聊天总量、AI Agent 处理量、规则助手处理量、今日聊天量、今日活跃用户数、最近一条聊天时间
  - 智能助手开关状态
  - OpenAI 配置与基础地址可达性检查
  - MCP 服务连通性检查
  - Elasticsearch / 语义搜索可达性与商品知识增强状态
  - 最近 N 天降级原因统计
  - 最近监控事件列表
- 运行期埋点：
  - `MallAssistantAgentServiceImpl` 在智能助手关闭、依赖不可用、调用成功、调用异常回退时记录监控事件
  - `ProductInfoServiceImpl` 在语义搜索成功与各类回退场景记录监控事件
  - 监控事件写入失败时仅记录日志，不影响主业务流程
- 更新 `smartMall-admin/src/main/resources/application.yml`：
  - 增加 `smart-mall.admin.ai-monitor.*` 默认配置项
- 更新 `apifox_requests.md`：
  - 新增后台 AI 服务监控接口调试文档

### 验证记录
- 执行命令：
  - `mvn -q -pl smartMall-common,smartMall-admin,smartMall-web -am "-Dmaven.repo.local=C:\Users\15712\.m2\repository" "-DfailIfNoTests=false" test`
- 环境说明：
  - Maven 使用 `D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64` 运行。
- 测试结果：编译与测试通过。

### 当前影响范围
- `doc/sql`
- `doc/development-log.md`
- `apifox_requests.md`
- `smartMall-common`
- `smartMall-admin`
- `smartMall-web`

### 下一步建议
- AI 监控能力完成后，按后台主线继续补齐用户账户与权限管理。
- 若仍需增强 AI 侧，可继续补模型调用耗时、错误码分布、按日趋势图等更细颗粒度指标。
- 若后续接入更多 Agent 工具，可将工具级成功率与异常率也纳入监控。

### 提交记录
- Git Commit: 本次功能点提交建议为“完成功能点：管理后台 AI 服务监控基础能力”。
