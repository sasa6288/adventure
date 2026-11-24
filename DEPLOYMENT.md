# Adventure 项目配置说明

## 环境配置

- 默认使用 `dev`：本地 MySQL 默认地址为 `localhost:3306/adventure`，可通过 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 覆盖。
- 生产使用 `prod`：必须提供数据库连接变量和至少 32 字符的 `JWT_SECRET`；生产默认关闭 Swagger。
- 测试继续使用 `test`：H2 内存数据库，不执行 Flyway。

常用环境变量见 `.env.example`。生产启动时设置 `SPRING_PROFILES_ACTIVE=prod`。

## OpenAPI / Swagger

- Swagger UI：`/swagger-ui.html`
- OpenAPI JSON：`/v3/api-docs`
- 除注册和登录外，接口在文档中使用 Bearer JWT。生产环境如需临时开放文档，可设置 `OPENAPI_ENABLED=true`。

## Flyway

迁移脚本位于 `src/main/resources/db/migration`。应用启动时先执行迁移，再由 Hibernate 以 `validate` 模式校验实体与表结构。新增数据库变更时只追加更高版本的迁移文件，不修改已经执行过的脚本。

## 日志与错误响应

日志同时输出到控制台和文件，文件按日期及 20MB 大小滚动，保留 30 天、总量最多 1GB。客户端可传入 `X-Request-Id`，否则服务端自动生成；响应头、错误响应体和日志中使用同一个 ID。

统一错误响应包含：`status`、`error`、`message`、`path`、`timestamp`、`requestId`，参数校验失败时额外包含 `validationErrors`。

## Docker

复制 `.env.example` 为 `.env` 并替换所有密码与 JWT 密钥后，可使用 Compose 构建应用与 MySQL。`mysql-data` 和 `app-logs` 使用命名卷持久化。
