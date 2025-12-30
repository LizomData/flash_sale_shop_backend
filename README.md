# Flash Sale Shop Backend ⚡️

Spring Boot 3.2 后端，基于 MySQL + MyBatis，为前端秒杀商城提供 REST API、登录/注册、秒杀、购物车、订单及管理员配置秒杀与商品。

## ✨ 功能
- 🔐 认证：手机号+密码注册/登录，JWT Bearer token；拦截器统一校验
- 🛍️ 商城：商品查询、购物车添加/结算、订单生成
- ⚡ 秒杀：限购校验、库存扣减、秒杀下单
- 🛠️ 管理：创建秒杀活动、添加商品（MySQL 持久化）

## 🧰 技术栈
- Spring Boot 3.2、MyBatis、MySQL、Spring Web、Validation、Lombok
- Maven Wrapper，Java 17

## 🚀 运行
1) 准备 MySQL 数据库
```sql
CREATE DATABASE IF NOT EXISTS flashsale_shop DEFAULT CHARSET utf8mb4;
```

2) 修改配置 `src/main/resources/application.properties` 中的数据库账号密码与 JWT 密钥。

3) 启动后端  
```bash
cd flashsaleshop_backend
./mvnw spring-boot:run
```
默认端口 `8080`，启动时会自动执行 `src/main/resources/sql/schema.sql` 与 `data.sql` 初始化表结构与演示数据。

## 🔑 接口一览
| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | 注册，返回 `{ token, user }`（JWT） | 否 |
| POST | `/api/auth/login` | 登录，返回 `{ token, user }`（JWT） | 否 |
| GET | `/api/bootstrap` | 获取用户/商品/秒杀/购物车/订单快照 | 是（Bearer Token） |
| GET | `/api/products` | 获取商品列表 | 是（Bearer Token） |
| GET | `/api/seckills` | 获取秒杀活动列表 | 是（Bearer Token） |
| POST | `/api/seckills/{eventId}/buy` | 秒杀下单 | 是（Bearer Token） |
| POST | `/api/cart` | 添加购物车（普通/秒杀） | 是（Bearer Token） |
| POST | `/api/orders/checkout` | 结算并生成订单 | 是（Bearer Token） |
| POST | `/api/admin/seckills` | 创建秒杀活动 | 是（Bearer Token） |
| POST | `/api/admin/products` | 创建商品 | 是（Bearer Token） |

## 👑 默认账号
- 管理员：手机号 `18800000000`，密码 `123456`，昵称 `admin`

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
- JWT 密钥位于 `application.properties`，可用环境变量覆盖：`JWT_SECRET`、`JWT_EXPIRATION_SECONDS`。
- 若需调整跨域或端口，可修改 `application.properties` 与 `WebConfig`。
