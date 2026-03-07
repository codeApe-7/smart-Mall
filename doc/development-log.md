# SmartMall 开发日志

## 2026-03-07 功能点：用户端购物车基础能力

### 本次目标
- 建立用户端购物车基础表结构与后端服务。
- 打通购物车列表、加入购物车、改数量、勾选、删除接口。
- 为下一个订单创建功能点提供待结算数据来源。

### 本次实现
- 新增购物车表结构说明：`shopping_cart`。
- 在 `smartMall-common` 新增购物车实体、DTO、VO、Mapper、Service、ServiceImpl。
- 在 `smartMall-web` 新增 `MallCartController`，提供以下接口：
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

### 测试记录
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
  - `POST /api/product/list`：分页查询已上架商品。
  - `GET /api/product/recommend`：查询推荐商品。
  - `GET /api/product/detail/{productId}`：查询已上架商品详情。
- 在 `ProductInfoService` / `ProductInfoServiceImpl` 中补充：
  - 用户端可见商品列表查询。
  - 用户端可见商品详情查询。
  - 推荐商品查询。
- 在 `ProductQueryDTO` 增加 `status` 条件，支持按商品状态过滤。
- 修正 `smartMall-web` 启动配置：移除 `DataSourceAutoConfiguration` 排除项，并增加 `@MapperScan`，保证 Web 端可以访问数据库与 MyBatis Mapper。
- 在父 `pom.xml` 增加 Lombok 注解处理编译配置，避免 Maven 构建时 `@Data`、`@Slf4j` 失效。
- 为 `smartMall-common`、`smartMall-web` 增加测试依赖，并补充对应测试。

### 测试记录
- 执行命令：
  - `mvn -q -pl smartMall-common,smartMall-web -am "-Dmaven.repo.local=C:\Users\15712\.m2\repository" "-Dmaven.test.skip=false" test`
- 测试结果：通过。
- 环境说明：
  - Maven 默认使用的 JDK 为 `25.0.1`，会触发 Lombok 与 `javac` 兼容问题。
  - 本次测试切换到 `D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64` 后通过。

### 关键日志
- `ProductInfoServiceImpl` 已输出用户端商品列表与推荐商品加载日志。
- `MallProductController` 已输出商品列表、推荐商品、商品详情访问日志。

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
