# Apifox 接口调试文档 - 商品属性管理

以下是针对 `SysCategoryController` 中新增和修改的接口的调试请求示例。

## 1. 查询分类列表（带商品属性）

> 合并后的 `/category/list` 接口，支持通过 `withProperty` 参数加载属性。

- **Method**: `POST`
- **URL**: `http://localhost:8080/category/list`
- **Content-Type**: `application/json`

### Body 示例
```json
{
    "tree": true,
    "withProperty": true,
    "categoryName": "电子"
}
```

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/category/list' \
--header 'Content-Type: application/json' \
--data-raw '{
    "tree": true,
    "withProperty": true
}'
```

---

## 2. 新增商品属性

- **Method**: `POST`
- **URL**: `http://localhost:8080/category/property/add`
- **Content-Type**: `application/json`

### Body 示例
```json
{
    "propertyName": "屏幕尺寸",
    "pCategoryId": "-1", 
    "categoryId": "10001",
    "coverType": 0
}
```
> **字段说明**:
> - `propertyName`: 属性名称 (必填)
> - `categoryId`: 所属二级分类ID (必填)
> - `pCategoryId`: 所属一级分类ID
> - `coverType`: 封面类型 (0:无需传封面, 1:需传封面)

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/category/property/add' \
--header 'Content-Type: application/json' \
--data-raw '{
    "propertyName": "屏幕尺寸",
    "categoryId": "10001",
    "coverType": 0
}'
```

---

## 3. 更新商品属性

- **Method**: `POST`
- **URL**: `http://localhost:8080/category/property/update`
- **Content-Type**: `application/json`

### Body 示例
```json
{
    "propertyId": "e3b6a2...",
    "propertyName": "屏幕尺寸(英寸)",
    "categoryId": "10001",
    "coverType": 1,
    "propertySort": 5
}
```
> **字段说明**:
> - `propertyId`: 属性ID (必填)
> - `propertyName`: 属性名称
> - `propertySort`: 排序值

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/category/property/update' \
--header 'Content-Type: application/json' \
--data-raw '{
    "propertyId": "REPLACE_WITH_REAL_ID",
    "propertyName": "屏幕尺寸(修改后)"
}'
```

---

## 4. 删除单个属性

- **Method**: `POST`
- **URL**: `http://localhost:8080/category/property/delete/{propertyId}`

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/category/property/delete/REPLACE_WITH_REAL_ID'
```

---

## 5. 批量删除属性

- **Method**: `POST`
- **URL**: `http://localhost:8080/category/property/deleteBatch`
- **Content-Type**: `application/json`

### Body 示例
```json
[
    "id_1",
    "id_2",
    "id_3"
]
```

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/category/property/deleteBatch' \
--header 'Content-Type: application/json' \
--data-raw '[
    "id_1",
    "id_2"
]'
```

---

# Apifox 接口调试文档 - 文件管理

以下是针对 `FileController` 的接口调试请求示例。

## 6. 上传图片

> 支持图片上传并可选生成缩略图。

- **Method**: `POST`
- **URL**: `http://localhost:8080/file/uploadImage`
- **Content-Type**: `multipart/form-data`

### Body 参数
- `file`: (File) 图片文件 (Required)
- `createThumbnail`: (Boolean) 是否生成缩略图 (Optional)

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/file/uploadImage' \
--form 'file=@"/path/to/image.jpg"' \
--form 'createThumbnail="true"'
```

---

## 7. 获取资源文件

> 获取上传的文件流（通常用于图片预览）。

- **Method**: `GET`
- **URL**: `http://localhost:8080/file/getResource`

### Query 参数
- `resourceName`: (String) 资源路径，例如 `2025/04/15/example.jpg` (Required)

### cURL 示例
```bash
curl --location --request GET 'http://localhost:8080/file/getResource?resourceName=2025/04/15/example.jpg'
```

---

# Apifox 接口调试文档 - 商品管理

以下是针对 `ProductInfoController` 的接口调试请求示例。

## 8. 查询商品列表（分页）

> 分页查询商品，返回额外字段：分类名称、SKU 数量、总库存。

- **Method**: `POST`
- **URL**: `http://localhost:8080/product/loadProductList`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "pageNo": 1,
    "pageSize": 10,
    "productName": "iPhone",
    "categoryIdOrPCategoryId": "1001",
    "commendType": 1
}
```
> **字段说明**:
> - `pageNo`/`pageSize`: 分页参数（默认 1/10）
> - `productName`: 商品名称模糊搜索（可选）
> - `categoryIdOrPCategoryId`: 分类ID 或 父分类ID（可选）
> - `commendType`: 0:未推荐 1:已推荐（可选）

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/product/loadProductList' \
--header 'Content-Type: application/json' \
--data-raw '{
    "pageNo": 1,
    "pageSize": 10
}'
```

---

## 9. 新增/更新商品

> 统一保存接口：根据 payload 中的 `productInfo.productId` 判断。
> - 若 `productId` 为空，则执行**新增**。
> - 若 `productId` 不为空，则执行**更新**（全量覆盖属性和SKU）。

- **Method**: `POST`
- **URL**: `http://localhost:8080/product/save`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "productInfo": {
        "productId": "", 
        "productName": "iPhone 15 Pro",
        "productDesc": "钛金属机身，A17 Pro 芯片",
        "cover": "2024/02/15/iphone_cover.jpg",
        "categoryId": "1001",
        "pCategoryId": "10"
    },
    "productPropertyList": [
        {
            "propertyId": "1",
            "propertyName": "颜色",
            "propertySort": 1,
            "coverType": 0,
            "propertyValueId": "101",
            "propertyValue": "钛金属原色",
            "propertyRemark": "热门色",
            "sort": 1
        },
        {
            "propertyId": "2",
            "propertyName": "存储容量",
            "propertySort": 2,
            "coverType": 0,
            "propertyValueId": "201",
            "propertyValue": "256GB",
            "propertyRemark": "",
            "sort": 1
        }
    ],
    "skuList": [
        {
            "propertyValueIdHash": "HASH_101_201",
            "propertyValueIds": "101,201",
            "price": 8999.00,
            "stock": 100,
            "sort": 1
        }
    ]
}
```
> **字段说明**:
> - `productInfo`: 商品基本信息
>   - `productId`: 更新时必填，新增时留空或不传
>   - `pCategoryId`: 父分类ID (注意 JSON 字段名为 `pCategoryId`)
> - `productPropertyList`: 商品属性具体值列表（如颜色：红色、蓝色）
> - `skuList`: SKU 库存及价格列表

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/product/save' \
--header 'Content-Type: application/json' \
--data-raw '{
    "productInfo": {
        "productName": "测试商品",
        "categoryId": "1001",
        "pCategoryId": "10"
    },
    "productPropertyList": [],
    "skuList": [
        {
            "propertyValueIdHash": "TEST_HASH",
            "price": 100,
            "stock": 10
        }
    ]
}'
```

---

## 10. 删除商品

> 级联删除商品信息、关联属性值及 SKU。

- **Method**: `POST`
- **URL**: `http://localhost:8080/product/delete/{productId}`

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/product/delete/123456'
```

---

# Apifox 接口调试文档 - 用户端购物车

以下内容对应提交记录“完成功能点：用户端购物车基础能力”。

> 说明：`smartMall-web` 当前配置了 `context-path: /api`，以下用户端接口统一使用 `http://localhost:8080/api` 作为访问前缀。
>
> 推荐联调顺序：`查询商品详情/确认 SKU` → `加入购物车` → `查询购物车` → `修改数量/勾选` → `删除购物车项`。

## 11. 查询购物车

> 查询指定用户的购物车汇总，返回购物车明细、总件数、选中件数与选中金额。

- **Method**: `GET`
- **URL**: `http://localhost:8080/api/cart/list`

### Query 参数
- `userId`: (String) 用户ID，例如 `u10001` (Required)

### cURL 示例
```bash
curl --location --request GET 'http://localhost:8080/api/cart/list?userId=u10001'
```

### 成功断言
- `code = 200`
- `data.items` 为数组
- `data.itemCount` 表示购物车行数
- `data.selectedCount`、`data.selectedAmount` 与已勾选商品一致

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| CART-LIST-01 | 用户存在且购物车为空 | `userId=u10001` | 返回 `code=200`，`data.items=[]`，`itemCount=0`，`selectedAmount=0` |
| CART-LIST-02 | 用户存在且购物车有数据 | `userId=u10001` | 返回 `code=200`，`data.items` 含商品行，且每行包含 `cartId/productId/quantity/selected/available/totalAmount` |
| CART-LIST-03 | 缺少 `userId` | 不传 `userId` | 返回错误响应，HTTP 仍为 200，业务 `code` 非 200 |

---

## 12. 加入购物车

> 将商品 SKU 加入购物车；若同一用户已存在相同 `productId + propertyValueIdHash`，则会合并数量。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/cart/add`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "userId": "u10001",
    "productId": "p10001",
    "propertyValueIdHash": "HASH_101_201",
    "quantity": 2
}
```

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/api/cart/add' \
--header 'Content-Type: application/json' \
--data-raw '{
    "userId": "u10001",
    "productId": "p10001",
    "propertyValueIdHash": "HASH_101_201",
    "quantity": 2
}'
```

### 成功断言
- `code = 200`
- 首次加入后，调用“查询购物车”可看到新增行
- 重复加入同一 SKU 时，数量累加而不是新增重复行

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| CART-ADD-01 | 首次加入有效 SKU | 标准请求体 | 返回 `code=200`，购物车新增一行，默认 `selected=true` |
| CART-ADD-02 | 重复加入同一 SKU | 与 `CART-ADD-01` 相同，再次调用 | 返回 `code=200`，购物车仍为同一行，`quantity` 累加 |
| CART-ADD-03 | `quantity` 小于 1 | `quantity=0` | 返回 `code=601`，提示 `quantity must be greater than 0` |
| CART-ADD-04 | 商品已下架或不存在 | 无效 `productId` | 返回 `code=405`，提示 `product is unavailable` |
| CART-ADD-05 | SKU 不存在 | 无效 `propertyValueIdHash` | 返回 `code=405`，提示 `product sku not found` |
| CART-ADD-06 | 加购数量超过库存 | `quantity` 大于可售库存 | 返回 `code=601`，提示 `cart quantity exceeds stock` |

---

## 13. 修改购物车数量

> 修改单个购物车项购买数量；会校验该 SKU 当前库存。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/cart/updateQuantity`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "userId": "u10001",
    "cartId": "c10001",
    "quantity": 3
}
```

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/api/cart/updateQuantity' \
--header 'Content-Type: application/json' \
--data-raw '{
    "userId": "u10001",
    "cartId": "c10001",
    "quantity": 3
}'
```

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| CART-QTY-01 | 正常修改数量 | `quantity=3` | 返回 `code=200`，购物车对应行数量更新为 3 |
| CART-QTY-02 | 购物车项不存在 | 无效 `cartId` | 返回 `code=405`，提示 `cart item not found` |
| CART-QTY-03 | 数量超过库存 | `quantity` 大于库存 | 返回 `code=601`，提示 `cart quantity exceeds stock` |
| CART-QTY-04 | 数量非法 | `quantity=0` | 返回 `code=601`，提示 `quantity must be greater than 0` |

---

## 14. 修改购物车勾选状态

> 切换购物车项是否参与结算。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/cart/updateSelected`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "userId": "u10001",
    "cartId": "c10001",
    "selected": true
}
```

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/api/cart/updateSelected' \
--header 'Content-Type: application/json' \
--data-raw '{
    "userId": "u10001",
    "cartId": "c10001",
    "selected": true
}'
```

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| CART-SELECT-01 | 勾选购物车项 | `selected=true` | 返回 `code=200`，查询购物车时该行 `selected=true` |
| CART-SELECT-02 | 取消勾选购物车项 | `selected=false` | 返回 `code=200`，查询购物车时该行 `selected=false`，汇总金额同步变化 |
| CART-SELECT-03 | 购物车项不存在 | 无效 `cartId` | 返回 `code=405`，提示 `cart item not found` |
| CART-SELECT-04 | 缺少 `selected` | 不传 `selected` | 返回 `code=601`，提示 `selected can not be null` |

---

## 15. 删除购物车项

> 批量删除购物车项；支持一次删除多个 `cartId`。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/cart/delete`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "userId": "u10001",
    "cartIds": ["c10001", "c10002"]
}
```

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/api/cart/delete' \
--header 'Content-Type: application/json' \
--data-raw '{
    "userId": "u10001",
    "cartIds": ["c10001", "c10002"]
}'
```

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| CART-DELETE-01 | 删除单个购物车项 | `cartIds=["c10001"]` | 返回 `code=200`，再次查询购物车时该行不存在 |
| CART-DELETE-02 | 批量删除多个购物车项 | `cartIds` 传多个值 | 返回 `code=200`，多个购物车项均被删除 |
| CART-DELETE-03 | 删除不存在的购物车项 | 无效 `cartIds` | 返回 `code=200`，接口幂等，不报错 |
| CART-DELETE-04 | `cartIds` 为空 | `cartIds=[]` | 返回 `code=601`，提示 `cartIds can not be empty` |

---

# Apifox 接口调试文档 - 用户端订单

以下内容对应提交记录“完成功能点：用户端订单创建与结算基础能力”。

> 推荐联调顺序：`订单预结算` → `创建订单` → `查询订单列表` → `查询订单详情` → `取消订单`。

## 16. 订单预结算

> 基于已勾选购物车项生成预结算结果，返回商品快照、总数量和总金额。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/order/preview`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "userId": "u10001",
    "cartIds": ["c10001", "c10002"]
}
```

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/api/order/preview' \
--header 'Content-Type: application/json' \
--data-raw '{
    "userId": "u10001",
    "cartIds": ["c10001", "c10002"]
}'
```

### 成功断言
- `code = 200`
- `data.items` 中每条记录包含 `productId/productName/price/quantity/totalAmount`
- `data.totalQuantity`、`data.totalAmount` 与购物车已勾选项一致

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| ORDER-PREVIEW-01 | 预结算已勾选购物车项 | 标准请求体 | 返回 `code=200`，汇总金额与购物车选中金额一致 |
| ORDER-PREVIEW-02 | 购物车项未勾选 | `cartIds` 包含未勾选项 | 返回 `code=601`，提示 `cart item is not selected` |
| ORDER-PREVIEW-03 | 购物车项不可售 | `cartIds` 包含下架/库存不足项 | 返回 `code=501`，提示 `cart item is unavailable` |
| ORDER-PREVIEW-04 | 购物车项不存在 | 无效 `cartIds` | 返回 `code=405`，提示 `selected cart items not found` |

---

## 17. 创建订单

> 基于已勾选购物车项创建待支付订单，创建成功后会清理对应购物车条目。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/order/create`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "userId": "u10001",
    "cartIds": ["c10001", "c10002"],
    "receiverName": "张三",
    "receiverPhone": "13800000000",
    "receiverAddress": "广东省深圳市南山区科技园 100 号",
    "orderRemark": "工作日白天送达"
}
```

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/api/order/create' \
--header 'Content-Type: application/json' \
--data-raw '{
    "userId": "u10001",
    "cartIds": ["c10001", "c10002"],
    "receiverName": "张三",
    "receiverPhone": "13800000000",
    "receiverAddress": "广东省深圳市南山区科技园 100 号",
    "orderRemark": "工作日白天送达"
}'
```

### 成功断言
- `code = 200`
- `data.orderId`、`data.orderNo` 非空
- `data.orderStatus = 0`（待支付）
- `data.totalAmount` 与预结算金额一致
- 成功后再次查询购物车，被下单的 `cartIds` 已被清理

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| ORDER-CREATE-01 | 正常创建订单 | 标准请求体 | 返回 `code=200`，生成待支付订单，购物车对应项被删除 |
| ORDER-CREATE-02 | 收货人为空 | `receiverName=""` | 返回 `code=601`，提示 `receiverName can not be blank` |
| ORDER-CREATE-03 | 收货手机号为空 | `receiverPhone=""` | 返回 `code=601`，提示 `receiverPhone can not be blank` |
| ORDER-CREATE-04 | 收货地址为空 | `receiverAddress=""` | 返回 `code=601`，提示 `receiverAddress can not be blank` |
| ORDER-CREATE-05 | `cartIds` 为空 | `cartIds=[]` | 返回 `code=601`，提示 `cartIds can not be empty` |
| ORDER-CREATE-06 | 包含未勾选或不可售购物车项 | 异常购物车项 | 返回对应错误，不生成订单 |

---

## 18. 查询订单列表

> 按用户分页查询订单；支持通过 `orderStatus` 过滤。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/order/list`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "userId": "u10001",
    "pageNo": 1,
    "pageSize": 10,
    "orderStatus": 0
}
```

> **状态说明**:
> - `0`: 待支付
> - `10`: 已支付
> - `20`: 已取消
> - `30`: 已完成
> - `40`: 已发货
> - `50`: 已收货
> - `60`: 退款申请中
> - `70`: 已退款

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/api/order/list' \
--header 'Content-Type: application/json' \
--data-raw '{
    "userId": "u10001",
    "pageNo": 1,
    "pageSize": 10,
    "orderStatus": 0
}'
```

### 成功断言
- `code = 200`
- `data.records` 为数组
- 每条订单含 `orderId/orderNo/orderStatus/orderStatusDesc/totalAmount/totalQuantity/createTime/payTime`

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| ORDER-LIST-01 | 查询全部订单 | 仅传 `userId` | 返回 `code=200`，`pageNo/pageSize` 使用默认或传入值 |
| ORDER-LIST-02 | 按待支付状态过滤 | `orderStatus=0` | 返回 `code=200`，记录均为待支付订单 |
| ORDER-LIST-03 | 查询空结果页 | 指定不存在的状态或无订单用户 | 返回 `code=200`，`records=[]` |
| ORDER-LIST-04 | `userId` 为空 | `userId=""` | 返回 `code=601`，提示 `userId is required` |

---

## 19. 查询订单详情

> 查询指定用户某一订单的详情与订单商品明细。

- **Method**: `GET`
- **URL**: `http://localhost:8080/api/order/detail`

### Query 参数
- `userId`: (String) 用户ID，例如 `u10001` (Required)
- `orderId`: (String) 订单ID，例如 `o10001` (Required)

### cURL 示例
```bash
curl --location --request GET 'http://localhost:8080/api/order/detail?userId=u10001&orderId=o10001'
```

### 成功断言
- `code = 200`
- 返回字段包含 `orderStatusDesc`、`payTime`、`cancelTime`、`items`
- `items` 中每条记录包含 `productId/productName/skuPropertyText/price/quantity/totalAmount`

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| ORDER-DETAIL-01 | 查询本人有效订单 | 有效 `userId + orderId` | 返回 `code=200`，可看到订单主信息和商品明细 |
| ORDER-DETAIL-02 | 订单不存在 | 无效 `orderId` | 返回 `code=405`，提示 `order not found` |
| ORDER-DETAIL-03 | 缺少 `orderId` | 不传 `orderId` 或空字符串 | 返回 `code=601`，提示 `orderId is required` |
| ORDER-DETAIL-04 | 缺少 `userId` | 不传 `userId` 或空字符串 | 返回 `code=601`，提示 `userId is required` |

---

## 20. 取消订单

> 仅允许取消“待支付”订单；取消后订单状态流转为“已取消”。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/order/cancel`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "userId": "u10001",
    "orderId": "o10001"
}
```

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/api/order/cancel' \
--header 'Content-Type: application/json' \
--data-raw '{
    "userId": "u10001",
    "orderId": "o10001"
}'
```

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| ORDER-CANCEL-01 | 取消待支付订单 | 标准请求体 | 返回 `code=200`，订单状态变更为 `20`，`cancelTime` 非空 |
| ORDER-CANCEL-02 | 取消已支付订单 | 已支付 `orderId` | 返回 `code=501`，提示 `order can not be canceled` |
| ORDER-CANCEL-03 | 取消不存在订单 | 无效 `orderId` | 返回 `code=405`，提示 `order not found` |
| ORDER-CANCEL-04 | 缺少 `orderId` | `orderId=""` | 返回 `code=601`，提示 `orderId can not be blank` |

---

# Apifox 接口调试文档 - 用户端支付

以下内容对应提交记录“完成功能点：用户端支付下单与支付回调基础能力”。

> 推荐联调顺序：`创建待支付订单` → `提交支付` → 使用返回的 `mockCallbackPayload` 调 `支付回调` → `查询订单详情/列表` 验证状态推进。

## 21. 提交支付

> 针对待支付订单创建支付流水；若同一订单存在未完成支付单，则直接复用。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/payment/submit`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "userId": "u10001",
    "orderId": "o10001",
    "payChannel": "ALIPAY_SANDBOX"
}
```

> **字段说明**:
> - `payChannel` 当前仅支持 `ALIPAY_SANDBOX`
> - 若不传 `payChannel`，服务端默认仍会使用 `ALIPAY_SANDBOX`

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/api/payment/submit' \
--header 'Content-Type: application/json' \
--data-raw '{
    "userId": "u10001",
    "orderId": "o10001",
    "payChannel": "ALIPAY_SANDBOX"
}'
```

### 成功断言
- `code = 200`
- `data.paymentId`、`data.paymentNo` 非空
- `data.payStatus = 0`
- `data.mockPayUrl = "/api/payment/callback"`
- `data.mockCallbackPayload` 可直接作为支付回调请求体复用

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| PAYMENT-SUBMIT-01 | 正常提交支付 | 标准请求体 | 返回 `code=200`，生成待支付流水，返回 `mockCallbackPayload` |
| PAYMENT-SUBMIT-02 | 不传支付渠道 | 不传 `payChannel` | 返回 `code=200`，`data.payChannel=ALIPAY_SANDBOX` |
| PAYMENT-SUBMIT-03 | 重复提交同一待支付订单 | 连续调用两次 | 返回 `code=200`，第二次返回相同 `paymentNo`（复用未完成支付单） |
| PAYMENT-SUBMIT-04 | 支付渠道不支持 | `payChannel="WECHAT"` | 返回 `code=601`，提示 `unsupported payChannel` |
| PAYMENT-SUBMIT-05 | 订单状态不是待支付 | 传已取消或已支付订单 | 返回 `code=501`，提示 `order status does not support payment` |
| PAYMENT-SUBMIT-06 | 订单不存在 | 无效 `orderId` | 返回业务错误，提示订单不存在 |

---

## 22. 支付回调

> 模拟第三方支付回调。支持成功、失败、关闭三类回调状态；成功回调后会推进订单为“已支付”。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/payment/callback`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "paymentNo": "202603070001",
    "callbackStatus": "TRADE_SUCCESS",
    "gatewayTradeNo": "ALI202603070001"
}
```

> **回调状态说明**:
> - 成功：`SUCCESS`、`TRADE_SUCCESS`
> - 失败：`FAILED`
> - 关闭：`CLOSED`、`TRADE_CLOSED`

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/api/payment/callback' \
--header 'Content-Type: application/json' \
--data-raw '{
    "paymentNo": "202603070001",
    "callbackStatus": "TRADE_SUCCESS",
    "gatewayTradeNo": "ALI202603070001"
}'
```

### 成功断言
- `code = 200`
- 成功回调后，订单详情中的 `orderStatus = 10`、`payTime` 非空
- 支付流水状态同步为成功/失败/关闭
- 对已成功支付单重复发送成功回调，应保持幂等

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| PAYMENT-CALLBACK-01 | 成功回调（推荐） | `callbackStatus="TRADE_SUCCESS"` | 返回 `code=200`，订单状态推进为已支付，`payTime` 写入 |
| PAYMENT-CALLBACK-02 | 成功回调简写 | `callbackStatus="SUCCESS"` | 返回 `code=200`，效果同上 |
| PAYMENT-CALLBACK-03 | 失败回调 | `callbackStatus="FAILED"` | 返回 `code=200`，支付流水标记失败，订单保持待支付 |
| PAYMENT-CALLBACK-04 | 关闭回调 | `callbackStatus="TRADE_CLOSED"` | 返回 `code=200`，支付流水标记关闭，订单保持原状态 |
| PAYMENT-CALLBACK-05 | 重复成功回调 | 对同一 `paymentNo` 连续回调两次成功 | 两次都返回 `code=200`，第二次不重复推进订单，结果保持幂等 |
| PAYMENT-CALLBACK-06 | 非法回调状态 | `callbackStatus="UNKNOWN"` | 返回 `code=601`，提示 `unsupported callbackStatus` |
| PAYMENT-CALLBACK-07 | 支付单不存在 | 无效 `paymentNo` | 返回 `code=405`，提示 `payment not found` |
| PAYMENT-CALLBACK-08 | 订单已取消后再回调成功 | 对已取消订单发成功回调 | 返回 `code=501`，提示 `order already canceled` |

---

# Apifox 接口调试文档 - 用户端退款

以下内容对应提交记录"完成功能点：用户端退款申请与退款状态流转"。

> 推荐联调顺序：先通过支付流程获得一个"已支付"订单 → `提交退款申请` → `查询退款详情` → `同意退款` 或 `拒绝退款` → `查询订单详情` 验证状态变化。
>
> **订单状态说明（含退款）**:
> - `0`: 待支付
> - `10`: 已支付
> - `20`: 已取消
> - `30`: 已完成
> - `60`: 退款申请中
> - `70`: 已退款

## 23. 提交退款申请

> 针对已支付订单提交退款申请，默认全额退款。若同一订单存在未处理退款单，则直接复用。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/refund/apply`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "userId": "u10001",
    "orderId": "o10001",
    "refundReason": "商品不满意"
}
```

> **字段说明**:
> - `userId`: 用户ID (必填)
> - `orderId`: 已支付订单ID (必填)
> - `refundReason`: 退款原因 (选填)

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/api/refund/apply' \
--header 'Content-Type: application/json' \
--data-raw '{
    "userId": "u10001",
    "orderId": "o10001",
    "refundReason": "商品不满意"
}'
```

### 成功断言
- `code = 200`
- `data.refundId`、`data.refundNo` 非空
- `data.refundStatus = 0`（退款申请中）
- `data.refundAmount` 等于订单总金额
- 提交后订单状态变为 `60`（退款申请中）

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| REFUND-APPLY-01 | 正常提交退款申请 | 标准请求体 | 返回 `code=200`，生成退款单，订单状态变为 `60` |
| REFUND-APPLY-02 | 重复提交退款申请 | 对同一订单连续调用两次 | 返回 `code=200`，第二次返回相同 `refundNo`（复用未处理退款单） |
| REFUND-APPLY-03 | 订单不是已支付状态 | 传待支付或已取消订单 | 返回 `code=501`，提示 `order status does not support refund` |
| REFUND-APPLY-04 | 订单不存在 | 无效 `orderId` | 返回 `code=405`，提示 `order not found` |
| REFUND-APPLY-05 | userId 与订单不匹配 | 他人 `userId` | 返回 `code=405`，提示 `order not found` |
| REFUND-APPLY-06 | 缺少 `userId` | `userId=""` | 返回 `code=601`，提示 `userId can not be blank` |
| REFUND-APPLY-07 | 缺少 `orderId` | `orderId=""` | 返回 `code=601`，提示 `orderId can not be blank` |

---

## 24. 查询退款详情

> 查询指定用户某一订单的最近退款记录。

- **Method**: `GET`
- **URL**: `http://localhost:8080/api/refund/detail`

### Query 参数
- `userId`: (String) 用户ID，例如 `u10001` (Required)
- `orderId`: (String) 订单ID，例如 `o10001` (Required)

### cURL 示例
```bash
curl --location --request GET 'http://localhost:8080/api/refund/detail?userId=u10001&orderId=o10001'
```

### 成功断言
- `code = 200`
- 返回字段包含 `refundId/refundNo/refundAmount/refundReason/refundStatus/refundStatusDesc/createTime/approveTime`

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| REFUND-DETAIL-01 | 查询存在退款记录的订单 | 有效 `userId + orderId` | 返回 `code=200`，可看到退款详情 |
| REFUND-DETAIL-02 | 订单无退款记录 | 未申请退款的订单 | 返回 `code=405`，提示 `refund record not found` |
| REFUND-DETAIL-03 | 订单不存在 | 无效 `orderId` | 返回 `code=405`，提示 `order not found` |
| REFUND-DETAIL-04 | userId 与订单不匹配 | 他人 `userId` | 返回 `code=405`，提示 `order not found` |
| REFUND-DETAIL-05 | 缺少参数 | 不传 `userId` 或 `orderId` | 返回 `code=601`，提示参数必填 |

---

## 25. 同意退款

> 模拟审批通过退款。退款通过后订单状态推进为"已退款"，退款单标记为已通过。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/refund/approve`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "refundId": "退款ID",
    "userId": "u10001"
}
```

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/api/refund/approve' \
--header 'Content-Type: application/json' \
--data-raw '{
    "refundId": "REPLACE_WITH_REAL_REFUND_ID",
    "userId": "u10001"
}'
```

### 成功断言
- `code = 200`
- 退款单状态变为 `10`（APPROVED），`approveTime` 非空
- 订单状态变为 `70`（已退款），`refundTime` 非空

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| REFUND-APPROVE-01 | 正常同意退款 | 标准请求体 | 返回 `code=200`，退款单标记通过，订单状态变为 `70` |
| REFUND-APPROVE-02 | 退款单不是待处理状态 | 已通过/已拒绝的退款单 | 返回 `code=501`，提示 `refund status does not support approval` |
| REFUND-APPROVE-03 | 退款单不存在 | 无效 `refundId` | 返回 `code=405`，提示 `refund record not found` |
| REFUND-APPROVE-04 | userId 与退款单不匹配 | 他人 `userId` | 返回 `code=501`，提示 `refund does not belong to user` |
| REFUND-APPROVE-05 | 缺少 `refundId` | `refundId=""` | 返回 `code=601`，提示 `refundId can not be blank` |

---

## 26. 拒绝退款

> 拒绝退款申请。拒绝后订单状态恢复为"已支付"，退款单标记为已拒绝。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/refund/reject`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "refundId": "退款ID",
    "userId": "u10001"
}
```

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/api/refund/reject' \
--header 'Content-Type: application/json' \
--data-raw '{
    "refundId": "REPLACE_WITH_REAL_REFUND_ID",
    "userId": "u10001"
}'
```

### 成功断言
- `code = 200`
- 退款单状态变为 `20`（REJECTED），`approveTime` 非空
- 订单状态恢复为 `10`（已支付），`refundTime` 清空

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| REFUND-REJECT-01 | 正常拒绝退款 | 标准请求体 | 返回 `code=200`，退款单标记拒绝，订单状态恢复为 `10` |
| REFUND-REJECT-02 | 退款单不是待处理状态 | 已通过/已拒绝的退款单 | 返回 `code=501`，提示 `refund status does not support rejection` |
| REFUND-REJECT-03 | 退款单不存在 | 无效 `refundId` | 返回 `code=405`，提示 `refund record not found` |
| REFUND-REJECT-04 | userId 与退款单不匹配 | 他人 `userId` | 返回 `code=501`，提示 `refund does not belong to user` |
| REFUND-REJECT-05 | 缺少 `refundId` | `refundId=""` | 返回 `code=601`，提示 `refundId can not be blank` |

---

# Apifox 接口调试文档 - 用户端物流

以下内容对应提交记录"完成功能点：用户端发货模拟与确认收货"。

> 推荐联调顺序：先通过支付流程获得一个"已支付"订单 → `模拟发货` → `查询物流` → `确认收货` → `查询订单详情` 验证状态变化。
>
> **订单状态说明（含物流）**:
> - `0`: 待支付
> - `10`: 已支付
> - `20`: 已取消
> - `30`: 已完成
> - `40`: 已发货
> - `50`: 已收货
> - `60`: 退款申请中
> - `70`: 已退款

## 27. 模拟发货

> 针对已支付订单模拟发货，自动生成物流单号。若同一订单已有物流记录，则直接复用。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/shipping/ship`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "orderId": "o10001",
    "userId": "u10001",
    "shippingCompany": "顺丰快递"
}
```

> **字段说明**:
> - `orderId`: 已支付订单ID (必填)
> - `userId`: 用户ID (必填)
> - `shippingCompany`: 快递公司 (选填，默认"模拟快递")

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/api/shipping/ship' \
--header 'Content-Type: application/json' \
--data-raw '{
    "orderId": "o10001",
    "userId": "u10001",
    "shippingCompany": "顺丰快递"
}'
```

### 成功断言
- `code = 200`
- `data.shippingId`、`data.trackingNo` 非空
- `data.shippingStatus = 0`（已发货）
- 发货后订单状态变为 `40`（已发货），`shipTime` 非空

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| SHIPPING-SHIP-01 | 正常发货 | 标准请求体 | 返回 `code=200`，生成物流单，订单状态变为 `40` |
| SHIPPING-SHIP-02 | 不传快递公司 | 不传 `shippingCompany` | 返回 `code=200`，`data.shippingCompany="模拟快递"` |
| SHIPPING-SHIP-03 | 重复发货 | 对同一订单连续调用两次 | 返回 `code=200`，第二次返回相同 `trackingNo`（复用已有物流单） |
| SHIPPING-SHIP-04 | 订单不是已支付状态 | 传待支付或已取消订单 | 返回 `code=501`，提示 `order status does not support shipping` |
| SHIPPING-SHIP-05 | 订单不存在 | 无效 `orderId` | 返回 `code=405`，提示 `order not found` |
| SHIPPING-SHIP-06 | userId 与订单不匹配 | 他人 `userId` | 返回 `code=405`，提示 `order not found` |

---

## 28. 查询物流详情

> 查询指定用户某一订单的物流信息。

- **Method**: `GET`
- **URL**: `http://localhost:8080/api/shipping/detail`

### Query 参数
- `userId`: (String) 用户ID，例如 `u10001` (Required)
- `orderId`: (String) 订单ID，例如 `o10001` (Required)

### cURL 示例
```bash
curl --location --request GET 'http://localhost:8080/api/shipping/detail?userId=u10001&orderId=o10001'
```

### 成功断言
- `code = 200`
- 返回字段包含 `shippingId/trackingNo/shippingCompany/shippingStatus/shippingStatusDesc/createTime/receiveTime`

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| SHIPPING-DETAIL-01 | 查询已发货订单物流 | 有效 `userId + orderId` | 返回 `code=200`，可看到物流详情 |
| SHIPPING-DETAIL-02 | 订单无物流记录 | 未发货的订单 | 返回 `code=405`，提示 `shipping record not found` |
| SHIPPING-DETAIL-03 | 订单不存在 | 无效 `orderId` | 返回 `code=405`，提示 `order not found` |
| SHIPPING-DETAIL-04 | 缺少参数 | 不传 `userId` 或 `orderId` | 返回 `code=601`，提示参数必填 |

---

## 29. 确认收货

> 用户确认收货，仅允许已发货订单操作。确认后物流状态变为"已签收"，订单状态推进为"已收货"。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/shipping/confirmReceive`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "orderId": "o10001",
    "userId": "u10001"
}
```

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/api/shipping/confirmReceive' \
--header 'Content-Type: application/json' \
--data-raw '{
    "orderId": "o10001",
    "userId": "u10001"
}'
```

### 成功断言
- `code = 200`
- 物流状态变为 `20`（已签收），`receiveTime` 非空
- 订单状态变为 `50`（已收货），`receiveTime` 非空

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| SHIPPING-RECEIVE-01 | 正常确认收货 | 标准请求体 | 返回 `code=200`，物流标记签收，订单状态变为 `50` |
| SHIPPING-RECEIVE-02 | 订单不是已发货状态 | 传已支付或已收货订单 | 返回 `code=501`，提示 `order status does not support confirming receive` |
| SHIPPING-RECEIVE-03 | 订单不存在 | 无效 `orderId` | 返回 `code=405`，提示 `order not found` |
| SHIPPING-RECEIVE-04 | userId 与订单不匹配 | 他人 `userId` | 返回 `code=405`，提示 `order not found` |
| SHIPPING-RECEIVE-05 | 缺少 `orderId` | `orderId=""` | 返回 `code=601`，提示 `orderId can not be blank` |

---

## 30. 提交商品评价

> 对已收货订单的订单项提交评价（评分 + 评价内容），全部订单项评价完成后订单自动推进到"已完成"状态。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/review/submit`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "userId": "u10001",
    "orderId": "o10001",
    "reviews": [
        {
            "itemId": "item001",
            "productId": "p10001",
            "rating": 5,
            "content": "质量很好，物流很快！"
        },
        {
            "itemId": "item002",
            "productId": "p10002",
            "rating": 4,
            "content": "还不错"
        }
    ]
}
```

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/api/review/submit' \
--header 'Content-Type: application/json' \
--data-raw '{
    "userId": "u10001",
    "orderId": "o10001",
    "reviews": [
        {
            "itemId": "item001",
            "productId": "p10001",
            "rating": 5,
            "content": "质量很好，物流很快！"
        }
    ]
}'
```

### 成功断言
- `code = 200`
- 返回评价列表，每条包含 `reviewId`、`rating`、`content`、`productName`、`createTime`
- 全部订单项评价完成时，订单状态推进到 `30`（已完成）

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| REVIEW-SUBMIT-01 | 正常提交评价 | 已收货订单 + 有效订单项 | 返回 `code=200`，评价创建成功 |
| REVIEW-SUBMIT-02 | 全部订单项评价完成 | 提交最后一个未评价项 | 返回 `code=200`，订单状态推进到 `30`（已完成） |
| REVIEW-SUBMIT-03 | 订单不是已收货状态 | 传待支付或已发货订单 | 返回 `code=501`，提示 `order status does not support review` |
| REVIEW-SUBMIT-04 | 重复评价同一订单项 | 已评价过的 `itemId` | 返回 `code=501`，提示 `order item already reviewed` |
| REVIEW-SUBMIT-05 | 订单项不存在 | 无效 `itemId` | 返回 `code=405`，提示 `order item not found` |
| REVIEW-SUBMIT-06 | productId 不匹配 | itemId 对应的商品不是传入的 productId | 返回 `code=601`，提示 `productId mismatch` |
| REVIEW-SUBMIT-07 | 评分超出范围 | `rating=0` 或 `rating=6` | 返回 `code=601`，提示 `rating must be between 1 and 5` |
| REVIEW-SUBMIT-08 | 缺少 reviews | `reviews=[]` | 返回 `code=601`，提示 `reviews can not be empty` |

---

## 31. 查询订单评价

> 查询某用户某订单下的全部评价记录。

- **Method**: `GET`
- **URL**: `http://localhost:8080/api/review/orderReviews?userId=u10001&orderId=o10001`

### cURL 示例
```bash
curl --location --request GET 'http://localhost:8080/api/review/orderReviews?userId=u10001&orderId=o10001'
```

### 成功断言
- `code = 200`
- 返回评价列表，每条包含 `reviewId`、`itemId`、`productId`、`productName`、`rating`、`content`、`createTime`

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| REVIEW-ORDER-01 | 正常查询 | 已有评价的订单 | 返回 `code=200`，评价列表 |
| REVIEW-ORDER-02 | 订单无评价 | 未评价的订单 | 返回 `code=200`，空列表 |
| REVIEW-ORDER-03 | 订单不存在 | 无效 `orderId` | 返回 `code=405`，提示 `order not found` |
| REVIEW-ORDER-04 | 缺少参数 | `userId` 或 `orderId` 为空 | 返回 `code=601`，提示参数缺失 |

---

## 32. 查询商品评价（分页）

> 分页查询某商品的全部评价记录，按时间倒序排列。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/review/productReviews`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "productId": "p10001",
    "pageNo": 1,
    "pageSize": 10
}
```

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/api/review/productReviews' \
--header 'Content-Type: application/json' \
--data-raw '{
    "productId": "p10001",
    "pageNo": 1,
    "pageSize": 10
}'
```

### 成功断言
- `code = 200`
- 返回分页结构：`pageNo`、`pageSize`、`totalCount`、`totalPages`、`records`
- `records` 中每条包含 `reviewId`、`productId`、`userId`、`rating`、`content`、`createTime`

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| REVIEW-PRODUCT-01 | 正常分页查询 | 标准请求体 | 返回 `code=200`，分页评价列表 |
| REVIEW-PRODUCT-02 | 商品无评价 | 无评价记录的商品 | 返回 `code=200`，空 `records` |
| REVIEW-PRODUCT-03 | 分页第二页 | `pageNo=2` | 返回 `code=200`，第二页数据或空 |
| REVIEW-PRODUCT-04 | 缺少 productId | `productId=""` | 返回 `code=601`，提示 `productId can not be blank` |

---

# Apifox 接口调试文档 - 用户端商品浏览

以下内容对应提交记录“完成功能点：用户端商品浏览与详情接口”。

> 推荐联调顺序：`查询商品列表` → `查询商品详情` → `查询推荐商品`。

## 33. 查询用户端商品列表

> 分页查询用户端可见商品，仅返回已上架商品。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/product/list`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "pageNo": 1,
    "pageSize": 10,
    "productName": "手机",
    "categoryIdOrPCategoryId": "c1001",
    "commendType": 1
}
```

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/api/product/list' \
--header 'Content-Type: application/json' \
--data-raw '{
    "pageNo": 1,
    "pageSize": 10,
    "productName": "手机"
}'
```

### 成功断言
- `code = 200`
- 返回分页结构：`pageNo/pageSize/totalCount/totalPages/records`
- `records` 中每条包含 `productId/productName/categoryName/skuCount/totalStock/minPrice/maxPrice`
- 仅返回在售商品

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| PRODUCT-LIST-01 | 查询全部在售商品 | 仅传分页参数 | 返回 `code=200`，`records` 为在售商品列表 |
| PRODUCT-LIST-02 | 按商品名称模糊查询 | `productName="手机"` | 返回 `code=200`，结果名称与关键词匹配 |
| PRODUCT-LIST-03 | 按分类查询 | `categoryIdOrPCategoryId="c1001"` | 返回 `code=200`，结果均属于目标分类或父分类 |
| PRODUCT-LIST-04 | 仅查询推荐商品 | `commendType=1` | 返回 `code=200`，结果为推荐商品 |
| PRODUCT-LIST-05 | 查询空结果页 | 传不存在的关键词 | 返回 `code=200`，`records=[]` |

---

## 34. 查询推荐商品

> 查询用户端推荐商品列表，默认返回 6 条。

- **Method**: `GET`
- **URL**: `http://localhost:8080/api/product/recommend?limit=4`

### cURL 示例
```bash
curl --location --request GET 'http://localhost:8080/api/product/recommend?limit=4'
```

### 成功断言
- `code = 200`
- 返回数组结构，每条记录包含 `productId/productName/minPrice/maxPrice`
- `limit` 不传时使用默认值

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| PRODUCT-RECOMMEND-01 | 正常查询推荐商品 | `limit=4` | 返回 `code=200`，数组长度不超过 4 |
| PRODUCT-RECOMMEND-02 | 不传 limit | 无 | 返回 `code=200`，使用默认推荐条数 |
| PRODUCT-RECOMMEND-03 | limit 小于 1 | `limit=0` | 返回 `code=200`，回退为默认条数 |
| PRODUCT-RECOMMEND-04 | limit 超出上限 | `limit=99` | 返回 `code=200`，按服务端最大限制返回 |

---

## 35. 查询商品详情

> 查询指定商品详情，仅允许查看已上架商品。

- **Method**: `GET`
- **URL**: `http://localhost:8080/api/product/detail/p10001`

### cURL 示例
```bash
curl --location --request GET 'http://localhost:8080/api/product/detail/p10001'
```

### 成功断言
- `code = 200`
- 返回字段包含 `productInfo/productPropertyList/skuList`
- `productPropertyList` 含属性及属性值集合
- `skuList` 含 SKU 价格、库存、属性值组合

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| PRODUCT-DETAIL-01 | 查询已上架商品详情 | 有效 `productId` | 返回 `code=200`，可看到商品、属性、SKU 明细 |
| PRODUCT-DETAIL-02 | 商品不存在 | 无效 `productId` | 返回 `code=405`，提示 `product not found` |
| PRODUCT-DETAIL-03 | 商品已下架 | 下架商品 `productId` | 返回 `code=405`，提示 `product is unavailable` |

---

# Apifox 接口调试文档 - 智能购物助手

以下内容对应提交记录“完成功能点：智能购物对话接入与商品检索基础能力”。

> 推荐联调顺序：`同步对话` → `查询会话历史` → `WebSocket 对话`。

## 36. 智能购物同步对话

> 通过 HTTP 发起一轮智能购物对话，当前支持商品搜索、推荐、商品详情、订单查询、取消订单。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/assistant/chat`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "userId": "u10001",
    "sessionId": "s10001",
    "message": "帮我找手机"
}
```

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/api/assistant/chat' \
--header 'Content-Type: application/json' \
--data-raw '{
    "userId": "u10001",
    "sessionId": "s10001",
    "message": "帮我找手机"
}'
```

### 成功断言
- `code = 200`
- `data.sessionId` 有值；未传时服务端自动生成
- `data.intentType` 为识别出的意图，如 `PRODUCT_SEARCH` / `ORDER_CANCEL`
- `data.reply` 为对话回复文本
- `data.payload` 中包含相应业务数据
- `data.suggestions` 返回后续可继续提问的建议短语

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| ASSISTANT-CHAT-01 | 商品搜索 | `message="帮我找手机"` | 返回 `code=200`，`intentType=PRODUCT_SEARCH`，`payload.productPage.records` 非空或为空列表 |
| ASSISTANT-CHAT-02 | 推荐商品 | `message="帮我推荐几款热销商品"` | 返回 `code=200`，`intentType=PRODUCT_RECOMMEND` |
| ASSISTANT-CHAT-03 | 商品详情 | `message="查看商品详情 p10001"` | 返回 `code=200`，`intentType=PRODUCT_DETAIL`，含 `payload.productDetail` |
| ASSISTANT-CHAT-04 | 查询订单列表 | `message="查询我的订单"` | 返回 `code=200`，`intentType=ORDER_LIST`，含 `payload.orderPage` |
| ASSISTANT-CHAT-05 | 查询订单详情 | `message="查看订单详情 o10001"` | 返回 `code=200`，`intentType=ORDER_DETAIL`，含 `payload.orderDetail` |
| ASSISTANT-CHAT-06 | 取消待支付订单 | `message="取消订单 o10001"` | 返回 `code=200`，`intentType=ORDER_CANCEL`，含 `payload.operation` |
| ASSISTANT-CHAT-07 | 取消不可取消订单 | 已支付或已取消订单 | 返回 `code=501`，提示 `order can not be canceled` |
| ASSISTANT-CHAT-08 | 缺少 userId | `userId=""` | 返回 `code=601`，提示 `userId can not be blank` |
| ASSISTANT-CHAT-09 | 缺少 message | `message=""` | 返回 `code=601`，提示 `message can not be blank` |

---

## 37. 查询智能购物会话历史

> 分页查询某用户的智能购物会话历史；可按 `sessionId` 过滤单个会话。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/assistant/history`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "userId": "u10001",
    "sessionId": "s10001",
    "pageNo": 1,
    "pageSize": 10
}
```

### cURL 示例
```bash
curl --location --request POST 'http://localhost:8080/api/assistant/history' \
--header 'Content-Type: application/json' \
--data-raw '{
    "userId": "u10001",
    "sessionId": "s10001",
    "pageNo": 1,
    "pageSize": 10
}'
```

### 成功断言
- `code = 200`
- 返回分页结构：`pageNo/pageSize/totalCount/totalPages/records`
- `records` 中每条包含 `chatId/sessionId/userId/requestText/intentType/intentDesc/replyText/createTime`
- 按 `createTime` 倒序排列

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| ASSISTANT-HISTORY-01 | 查询全部历史 | 仅传 `userId` | 返回 `code=200`，按时间倒序返回会话记录 |
| ASSISTANT-HISTORY-02 | 按会话过滤 | 传 `userId + sessionId` | 返回 `code=200`，结果仅属于该会话 |
| ASSISTANT-HISTORY-03 | 查询空结果 | 不存在的 `sessionId` | 返回 `code=200`，`records=[]` |
| ASSISTANT-HISTORY-04 | 缺少 userId | `userId=""` | 返回 `code=601`，提示 `userId can not be blank` |

---

## 38. WebSocket 智能购物对话

> 通过 WebSocket 方式发起智能购物对话。服务端返回的消息结构与同步对话保持一致。

- **Protocol**: `WebSocket`
- **URL**: `ws://localhost:8080/api/ws/assistant`

### 首条消息示例 (JSON)
```json
{
    "userId": "u10001",
    "sessionId": "s10002",
    "message": "帮我推荐几款热销商品"
}
```

### 成功断言
- 服务端返回 JSON 包装结构：`code/info/data`
- `data.intentType` 与同步对话接口保持一致
- `data.reply` 返回助手回复
- 业务异常时返回对应错误码和错误信息

### 测试用例
| 用例ID | 场景 | 请求消息 | 预期结果 |
| --- | --- | --- | --- |
| ASSISTANT-WS-01 | 推荐商品对话 | `message="帮我推荐几款热销商品"` | 返回 `code=200`，`data.intentType=PRODUCT_RECOMMEND` |
| ASSISTANT-WS-02 | 查询订单详情 | `message="查看订单详情 o10001"` | 返回 `code=200`，`data.intentType=ORDER_DETAIL` |
| ASSISTANT-WS-03 | 业务异常透传 | `message="取消订单 o10001"`，订单不可取消 | 返回对应业务错误码与错误信息 |
| ASSISTANT-WS-04 | 非法 JSON 消息 | 发送非 JSON 文本 | 返回 `code=500`，提示 `assistant websocket message parse failed` |

---

# Apifox 接口调试文档 - 智能购物助手扩展场景

以下内容对应提交记录“完成功能点：智能购物对话式订单操作扩展与 MCP 工具基础能力”。

> 以下场景统一调用 `POST /api/assistant/chat`，通过不同 `message` 和结构化字段触发对应能力。

## 39. 对话式退款申请

> 通过智能购物助手发起退款申请，底层复用用户端退款服务。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/assistant/chat`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "userId": "u10001",
    "sessionId": "s20001",
    "orderId": "o10001",
    "message": "申请退款",
    "refundReason": "商品不想要了"
}
```

### 成功断言
- `code = 200`
- `data.intentType = REFUND_APPLY`
- `data.payload.refundInfo` 含 `refundId/refundNo/refundAmount/refundStatus`

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| ASSISTANT-REFUND-01 | 正常申请退款 | 已支付或已发货订单 | 返回 `code=200`，`intentType=REFUND_APPLY` |
| ASSISTANT-REFUND-02 | 订单不可退款 | 待支付或已取消订单 | 返回 `code=501`，提示 `order status does not support refund` |
| ASSISTANT-REFUND-03 | 缺少 orderId | `orderId=""` 且消息中无订单号 | 返回业务错误，提示订单不存在或参数缺失 |

---

## 40. 对话式退款详情查询

> 通过智能购物助手查询某订单的退款详情。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/assistant/chat`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "userId": "u10001",
    "orderId": "o10001",
    "message": "查询退款详情"
}
```

### 成功断言
- `code = 200`
- `data.intentType = REFUND_DETAIL`
- `data.payload.refundInfo` 含退款状态与退款单号

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| ASSISTANT-REFUND-DETAIL-01 | 查询退款详情 | 有效 `userId + orderId` | 返回 `code=200`，`intentType=REFUND_DETAIL` |
| ASSISTANT-REFUND-DETAIL-02 | 无退款记录 | 未申请退款的订单 | 返回 `code=405`，提示 `refund record not found` |

---

## 41. 对话式确认收货

> 通过智能购物助手确认收货，底层复用物流服务。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/assistant/chat`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "userId": "u10001",
    "orderId": "o10001",
    "message": "确认收货"
}
```

### 成功断言
- `code = 200`
- `data.intentType = RECEIVE_CONFIRM`
- `data.payload.shippingInfo` 含物流单与签收状态

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| ASSISTANT-RECEIVE-01 | 正常确认收货 | 已发货订单 | 返回 `code=200`，`intentType=RECEIVE_CONFIRM` |
| ASSISTANT-RECEIVE-02 | 订单状态不支持收货 | 已支付或已收货订单 | 返回 `code=501`，提示 `order status does not support confirming receive` |

---

## 42. 对话式订单评价

> 通过智能购物助手查询订单评价，或通过结构化 `reviews` 字段提交评价。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/assistant/chat`
- **Content-Type**: `application/json`

### 查询评价示例 (JSON)
```json
{
    "userId": "u10001",
    "orderId": "o10001",
    "message": "查看订单评价"
}
```

### 提交评价示例 (JSON)
```json
{
    "userId": "u10001",
    "orderId": "o10001",
    "message": "提交评价",
    "reviews": [
        {
            "itemId": "item001",
            "productId": "p10001",
            "rating": 5,
            "content": "质量很好"
        }
    ]
}
```

### 成功断言
- 查询时：`data.intentType = ORDER_REVIEW_QUERY`
- 提交时：`data.intentType = REVIEW_SUBMIT`
- `data.payload.orderReviews` 返回评价记录列表

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| ASSISTANT-REVIEW-01 | 查询订单评价 | 有效 `userId + orderId` | 返回 `code=200`，`intentType=ORDER_REVIEW_QUERY` |
| ASSISTANT-REVIEW-02 | 提交订单评价 | 已收货订单 + 合法 `reviews` | 返回 `code=200`，`intentType=REVIEW_SUBMIT` |
| ASSISTANT-REVIEW-03 | 重复评价 | 已评价订单项再次提交 | 返回 `code=501`，提示重复评价错误 |
| ASSISTANT-REVIEW-04 | 评分越界 | `rating=0` 或 `rating=6` | 返回 `code=601` 或业务错误，提示评分范围非法 |

---

## 43. Spring AI Agent 对话入口

> 通过 `smartMall-web` 中的 Spring AI ChatClient 调用 MCP Client，联动 `smartMall-mcp` 工具处理商品、订单、退款等问题。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/assistant/agent/chat`
- **Content-Type**: `application/json`
- **前置说明**:
  - 启用 AI Agent：`SMART_MALL_ASSISTANT_AGENT_ENABLED=true`
  - 配置模型 Key：`OPENAI_API_KEY=...`
  - MCP 服务默认读取：`SMART_MALL_MCP_URL=http://127.0.0.1:8084`

### Body 示例 (JSON)
```json
{
    "userId": "u10001",
    "orderId": "o10001",
    "message": "帮我查询订单并告诉我下一步还能做什么"
}
```

### 成功断言
- AI Agent 可用时：`code = 200`
- AI Agent 可用时：`data.intentType = AI_AGENT`
- `data.reply` 返回模型生成的中文回复
- `data.sessionId` 不为空

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| ASSISTANT-AGENT-01 | AI Agent 查询订单 | 有效 `userId + orderId`，且已配置模型和 MCP | 返回 `code=200`，`intentType=AI_AGENT` |
| ASSISTANT-AGENT-02 | AI Agent 商品推荐 | 仅提供商品需求描述 | 返回 `code=200`，`intentType=AI_AGENT` |
| ASSISTANT-AGENT-03 | 未开启 Agent 开关 | `SMART_MALL_ASSISTANT_AGENT_ENABLED=false` | 返回 `code=200`，自动回退规则助手，`intentType` 为原有规则意图 |
| ASSISTANT-AGENT-04 | 未配置模型 Key | 缺少 `OPENAI_API_KEY` | 返回 `code=200`，自动回退规则助手 |

---

## 44. 用户端商品语义检索

> 在开启 `SMART_MALL_SEMANTIC_SEARCH_ENABLED=true` 时，用户端商品列表接口会优先调用 Elasticsearch 做语义检索；ES 不可用或未启用时自动回退数据库模糊搜索。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/product/list`
- **Content-Type**: `application/json`
- **前置说明**:
  - 开启语义检索：`SMART_MALL_SEMANTIC_SEARCH_ENABLED=true`
  - 配置 ES 地址：`SMART_MALL_ES_URL=http://127.0.0.1:9200`
  - 配置索引名：`SMART_MALL_PRODUCT_INDEX=smart_mall_product`

### Body 示例 (JSON)
```json
{
    "pageNo": 1,
    "pageSize": 10,
    "productName": "适合学生拍照和续航好的手机",
    "semanticSearch": true
}
```

### 成功断言
- `code = 200`
- `data.records` 返回在售商品列表
- `data.records[*].productId/productName/minPrice/maxPrice` 字段存在

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| PRODUCT-SEMANTIC-01 | 语义检索商品 | `semanticSearch=true` + 自然语言商品需求 | 返回 `code=200`，商品列表非空或正常空结果 |
| PRODUCT-SEMANTIC-02 | 语义检索按分类过滤 | 增加 `categoryIdOrPCategoryId` | 返回 `code=200`，结果仅包含对应分类商品 |
| PRODUCT-SEMANTIC-03 | ES 未启用自动回退 | `SMART_MALL_SEMANTIC_SEARCH_ENABLED=false` | 返回 `code=200`，接口继续走数据库模糊搜索 |
| PRODUCT-SEMANTIC-04 | ES 不可达自动回退 | 开启语义检索但 `SMART_MALL_ES_URL` 不可访问 | 返回 `code=200`，接口继续走数据库模糊搜索 |

---

## 45. 商品知识增强检索

> 基于商品检索结果聚合卖点摘要、评价摘要和售后说明，返回可直接供 AI Agent / MCP 使用的商品知识卡片。

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/product/knowledge/search`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "pageNo": 1,
    "pageSize": 3,
    "keyword": "适合学生的轻薄笔记本",
    "semanticSearch": true
}
```

### 成功断言
- `code = 200`
- `data.records[*].productId/productName/knowledgeText` 字段存在
- `data.records[*].sellingPointSummary/reviewSummary/afterSalesSummary` 字段存在

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| PRODUCT-KNOWLEDGE-01 | 按关键词检索知识卡片 | 自然语言商品需求 | 返回 `code=200`，知识卡片列表 |
| PRODUCT-KNOWLEDGE-02 | 结合语义检索召回 | `semanticSearch=true` | 返回 `code=200`，优先复用语义检索结果 |
| PRODUCT-KNOWLEDGE-03 | 指定分类过滤 | 增加 `categoryIdOrPCategoryId` | 返回 `code=200`，结果按分类过滤 |
| PRODUCT-KNOWLEDGE-04 | 无匹配商品 | 不存在的关键词 | 返回 `code=200`，空列表 |

---

## 46. 商品知识详情

> 按商品 ID 查询单个商品的知识增强卡片。

- **Method**: `GET`
- **URL**: `http://localhost:8080/api/product/knowledge/detail/p10001`

### 成功断言
- `code = 200`
- `data.productId = p10001`
- `data.knowledgeText` 为聚合后的知识文本

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| PRODUCT-KNOWLEDGE-DETAIL-01 | 查询单商品知识卡片 | 有效 `productId` | 返回 `code=200`，知识详情完整 |
| PRODUCT-KNOWLEDGE-DETAIL-02 | 商品不存在或已下架 | 无效 `productId` | 返回业务错误，提示商品不可用 |

---

## 47. 商品知识结构化对比
> 基于多个商品 ID 或自然语言关键词，聚合多张商品知识卡片并输出结构化对比结果，供前端、AI Agent 和 MCP 工具直接复用。
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/product/knowledge/compare`
- **Content-Type**: `application/json`

### Body 示例 (JSON)
```json
{
    "productIds": [
        "p10001",
        "p10002",
        "p10003"
    ],
    "maxCount": 3
}
```

### Body 示例 (关键词比较)
```json
{
    "keyword": "对比适合学生的轻薄笔记本和高续航办公本",
    "maxCount": 3,
    "semanticSearch": true
}
```

### 成功断言
- `code = 200`
- `data.products` 返回参与比较的商品知识卡片列表
- `data.dimensions[*].dimensionKey/dimensionName/values` 字段存在
- `data.compareSummary` 与 `data.comparisonText` 字段存在

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| PRODUCT-KNOWLEDGE-COMPARE-01 | 按多个商品ID对比 | 有效 `productIds` 2-3 个 | 返回 `code=200`，`data.comparable=true`，维度行完整 |
| PRODUCT-KNOWLEDGE-COMPARE-02 | 按自然语言关键词对比 | `keyword` 包含“对比/哪个好/vs”等比较诉求 | 返回 `code=200`，自动抽取多款商品生成比较结果 |
| PRODUCT-KNOWLEDGE-COMPARE-03 | 仅匹配到 1 款商品 | `keyword` 较窄或只传单个商品ID | 返回 `code=200`，`data.comparable=false` 或仅单商品结果，并提示补充条件 |
| PRODUCT-KNOWLEDGE-COMPARE-04 | 无匹配商品 | 不存在的关键词或空商品ID集合 | 返回 `code=200`，`data.products` 为空，给出无法比较提示 |

---

## 用户偏好档案

### 查询用户偏好档案

- **Method**: `GET`
- **URL**: `http://localhost:6050/api/preference/profile?userId=u10001`

### cURL 示例
```bash
curl --location --request GET 'http://localhost:6050/api/preference/profile?userId=u10001'
```

### 成功断言
- `code = 200`
- `data.userId` 等于请求参数 `userId`
- `data.favoriteCategoryIds`、`data.favoriteCategoryNames`、`data.preferenceTags` 为列表
- `data.minPricePreference`、`data.maxPricePreference` 为数值或 null
- `data.orderCount`、`data.reviewCount` 为整数

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| PREFERENCE-PROFILE-01 | 有购买历史的用户 | `userId=u10001` | 返回 `code=200`，`data` 包含偏好分类、价格区间、搜索关键词、偏好标签等字段 |
| PREFERENCE-PROFILE-02 | 无历史数据的用户 | `userId=u99999` | 返回 `code=200`，`data.favoriteCategoryIds=[]`，`data.orderCount=0` |

---

### 刷新用户偏好档案

- **Method**: `POST`
- **URL**: `http://localhost:6050/api/preference/refresh?userId=u10001`

### cURL 示例
```bash
curl --location --request POST 'http://localhost:6050/api/preference/refresh?userId=u10001'
```

### 成功断言
- `code = 200`
- `data.preferenceId` 不为空
- `data.updateTime` 不为空
- `data.favoriteCategoryIds`、`data.preferenceTags` 为列表

### 测试用例
| 用例ID | 场景 | 请求参数 | 预期结果 |
| --- | --- | --- | --- |
| PREFERENCE-REFRESH-01 | 首次刷新 | `userId=u10001`（无已有偏好记录） | 返回 `code=200`，新建偏好记录，`data.preferenceId` 不为空 |
| PREFERENCE-REFRESH-02 | 再次刷新 | `userId=u10001`（已有偏好记录） | 返回 `code=200`，更新偏好记录，`data.updateTime` 变化 |
| PREFERENCE-REFRESH-03 | 无行为数据 | `userId=u99999` | 返回 `code=200`，生成空偏好档案 |
