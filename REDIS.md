# Redis 功能

启动 Redis（需要 Docker）：

```sh
docker run -d --name adventure-redis -p 127.0.0.1:6379:6379 redis:7-alpine
```

应用默认连接 localhost:6379，可用 REDIS_HOST、REDIS_PORT、REDIS_PASSWORD 修改。
应用在 Docker 容器内运行时，应设置 REDIS_HOST 为 Redis 服务名。

## 功能

- GET /api/rankings?limit=20：缓存30秒，最多延迟30秒显示更新；Redis故障时回源MySQL。
- POST /api/auth/login：同一客户端IP在首次请求起60秒内最多10次，超限返回429。使用直接连接IP，不信任客户端提供的转发头。
- POST /api/auth/verification-codes：请求体 {"email":"alice@example.com"}。每邮箱60秒一次，验证码有效5分钟。
- POST /api/auth/verification-codes/verify：请求体 {"email":"alice@example.com","code":"123456"}。最多5次错误尝试，验证成功原子删除，只能使用一次。
- POST /api/auth/logout：携带 Authorization: Bearer TOKEN，成功返回204；Token的SHA-256摘要进入黑名单，保留至Token到期。

验证码为独立学习接口，尚未关联注册或找回密码业务。启动前设置 VERIFICATION_EXPOSE_CODE=true 后可在响应中获得演示验证码；默认关闭，返回503提示配置邮件服务。真实邮件发送需要配置邮件提供商，不能在生产环境开启返回验证码模式。

黑名单以整个Token摘要标识，兼容现有JWT。登录限流和Token撤销检查依赖Redis可用。Redis重启导致数据丢失会清除黑名单；正式使用应配置持久化、访问控制及可靠部署。

无需数据库迁移。MySQL继续保存用户、任务、经验和金币。
