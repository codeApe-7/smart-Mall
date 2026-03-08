# Claude Project Rules

## 需求来源
- 以 `system-architecture/` 目录为功能蓝图和开发优先级的唯一来源。
  - `architecture.png` — 系统架构图
  - `business-pipline.png` — 业务流程图
  - `Functional map.png` — 功能导图
  - `技术方案与架构.md` — 技术方案与架构文档
- 结合 `doc/development-log.md` 中"下一步建议"确定当前应实现的功能点。
- 按业务流程顺序推进，一次完成一个功能点。

## 开发流程
1. 确认要实现的功能点（对照架构文档 + 开发日志的下一步建议）。
2. 按现有代码模式实现（Entity → Enum → DTO → VO → Mapper → Service → ServiceImpl → Controller）。
3. 更新 `doc/sql/smart-mall.sql` 中的表结构。
4. Maven 编译与测试验证通过。
5. 更新 `doc/development-log.md` 记录本次功能。
6. 更新 `apifox_requests.md` 记录接口测试用例。
7. Git 提交本次功能点。

## 提交规范
- 每个功能点独立提交。
- 提交信息使用中文，格式：`完成功能点：xxx`。
- 每次提交只包含当前功能相关的文件，不夹带无关改动。

## 文档规范
- **开发日志** (`doc/development-log.md`)：记录功能目标、实现内容、验证记录、影响范围、下一步建议、提交记录。不放接口测试用例。
- **接口测试用例** (`apifox_requests.md`)：记录接口的 cURL 示例、成功断言、测试用例表格，格式与已有条目保持一致。
- 新功能完成后两个文件都要更新。

## 测试规范
- 每个功能点必须通过 Maven 编译和测试验证。
- Maven 使用 JDK 21（路径：`D:\Java\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64`）。
- 临时测试文件仅本地使用，验证后删除，不纳入提交。

## 编码规范
- 遵循现有项目模式，不做无关重构。
- 忽略与当前功能无关的 untracked 文件。
- 保持分层一致性：common 放领域模型和业务逻辑，web 放控制器。

## 技术栈
- Java 21 + Spring Boot 3.x + MyBatis-Plus 3.5.5
- MySQL 8+ / Redis / Elasticsearch
- Alipay Sandbox（支付集成）
- Spring AI + RAG + MCP（智能购物模式）
