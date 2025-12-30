# Flash Sale Shop Backend ⚡️

Spring Boot 3.2 后端，基于 MySQL + MyBatis，为前端秒杀商城提供 REST API、登录/注册、秒杀、购物车、订单及管理员配置秒杀与商品。

## ✨ 功能
- 🔐 认证：手机号+密码注册/登录，返回 Bearer token
- 🛍️ 商城：商品查询、购物车添加/结算、订单生成
- ⚡ 秒杀：限购校验、库存扣减、秒杀下单
- 🛠️ 管理：创建秒杀活动、添加商品（内存存储，重启重置）

## 🧰 技术栈
- Spring Boot 3.2、MyBatis、MySQL、Spring Web、Validation、Lombok
- Maven Wrapper，Java 17

## 🚀 运行
1) 准备 MySQL 数据库
```sql
CREATE DATABASE IF NOT EXISTS flashsale_shop DEFAULT CHARSET utf8mb4;
```

2) 修改配置 `src/main/resources/application.properties` 中的数据库账号密码。

3) 启动后端  
```bash
cd flashsaleshop_backend
./mvnw spring-boot:run
```
默认端口 `8080`，启动时会自动执行 `src/main/resources/sql/schema.sql` 与 `data.sql` 初始化表结构与演示数据。

## 🔑 重要接口
- `POST /api/auth/register`、`POST /api/auth/login`：注册/登录，返回 `{ token, user }`（JWT）
- `GET /api/bootstrap`：返回用户、商品、秒杀、购物车、订单快照（需 `Authorization: Bearer <token>`）
- `POST /api/seckills/{id}/buy`：秒杀下单
- `POST /api/cart`：加购（普通/秒杀）
- `POST /api/orders/checkout`：结算
- 管理员：`POST /api/admin/seckills` 创建秒杀，`POST /api/admin/products` 创建商品

## 🧪 开发脚本
- `./mvnw spring-boot:run` 运行
- `./mvnw test` 测试
- `./mvnw clean package` 打包

## 🔍 结构概览
- `src/main/java/com/flashsaleshop`：启动类、Controller、Service、DTO、模型、异常
- `src/main/resources/application.properties`：基础配置（端口、数据库、MyBatis）
- `src/main/resources/sql/`：数据库表结构与演示数据
- `src/test/java/com/flashsaleshop`：示例启动测试

## 📝 提示
- 生产建议补充缓存、限流/排队与更完善的鉴权策略。
- 若需调整跨域或端口，可修改 `application.properties` 与 `WebConfig`。
