# 使用 Docker 运行整个项目

需要安装并启动 Docker Desktop，使用 Linux containers。

## 首次启动

在 PowerShell 中进入项目：

```powershell
cd D:\java\homework\project\adventure
docker compose version
```

如果没有 .env，复制示例：

```powershell
Copy-Item .env.example .env
```

如果已有 .env，保留原文件，检查 DB_USERNAME、DB_PASSWORD、MYSQL_ROOT_PASSWORD 和 JWT_SECRET。JWT_SECRET 至少32字节。将 MYSQL_PORT 设置为3307，避免与电脑上的MySQL冲突。SERVER_PORT 为浏览器访问端口（默认8080）；如本地Java程序占用8080，停止该程序或设置 SERVER_PORT=8081。

学习Swagger可设置 OPENAPI_ENABLED=true；学习验证码演示可设置 VERIFICATION_EXPOSE_CODE=true（接口返回验证码，仅用于本地练习）。

```powershell
docker compose up -d --build
docker compose ps
docker compose logs -f app
```

首次构建需联网下载基础镜像与Maven依赖。Dockerfile在构建阶段使用Java21和Maven生成jar，运行阶段只保留Java21运行环境与jar。构建跳过测试。

MySQL、Redis健康检查通过后，应用才启动。Flyway自动运行已有V1、V2、V3脚本，在新的MySQL数据库中建立表。

## 访问

- Apifox基础地址：http://localhost:8080（如改SERVER_PORT则使用新端口）
- Swagger：http://localhost:8080/swagger-ui.html（需开启OPENAPI_ENABLED）
- 数据库工具连接：localhost:3307，数据库adventure，账号密码来自.env。
- Redis只对Compose内部网络开放，无需主机端口，也不与已有adventure-redis容器冲突。

容器内应用通过mysql:3306连接MySQL，通过redis:6379连接Redis；localhost在容器中指容器自身。

```powershell
docker compose exec redis redis-cli ping
docker compose exec redis redis-cli GET adventure:ranking:20
docker compose exec redis redis-cli TTL adventure:ranking:20
```

TTL返回-2表示键不存在（例如30秒缓存已经过期），-1表示没有过期时间，非负数表示剩余秒数。

## 停止、重启与更新

```powershell
docker compose down
docker compose up -d
```

修改代码后重新构建：

```powershell
docker compose up -d --build app
```

## 数据位置

MySQL数据存入mysql-data卷，Redis开启AOF并存入redis-data卷，应用日志存入app-logs卷。卷实际名称通常带项目名前缀，可用docker volume ls查看。

docker compose down保留数据；docker compose down -v会删除数据卷和全部数据库数据，请勿用于普通重启。数据卷不等于备份。

容器中的MySQL是新数据库，不会自动复制电脑原有MySQL的用户和任务。需要重新注册账号，或自行通过mysqldump导出导入原有数据。已有数据库卷创建后，修改.env密码不会自动修改MySQL中的账号密码。
