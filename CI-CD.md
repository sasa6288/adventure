# CI 与部署操作

## 本次配置

ci.yml：main推送或PR自动执行Java21 Maven verify，测试使用现有application-test.yaml（H2）以及临时Redis。报告上传Actions附件。测试成功后，main推送和手动运行将构建Dockerfile，发布ghcr.io/仓库所有者/仓库名:完整提交SHA以及latest。PR不发布。

deploy.yml：手动选择完整提交SHA，使用SSH更新/opt/adventure中的发布Compose。部署仅更新运行服务，不删除数据卷。健康检查以未登录任务接口返回401判断Web服务启动；它不替代业务冒烟测试。当前不包含自动回滚，失败时查看日志，再用旧SHA重新部署。数据库迁移不会随镜像回退而撤销。

## 1 提交

确认.env不在git跟踪中。将.github/workflows、compose.release.yaml和本文提交并推送到main。进入仓库Actions查看CI and image。若默认分支不是main，修改ci.yml中的两个main。

Actions失败时查看Verify日志及test-reports。现有测试调用Redis时会使用服务端口6379；原测试可能存在跨用例登录限流计数累积，需要隔离或清理测试Redis，不能清理开发Redis。测试Profile必须保持H2和Flyway关闭配置。

## 2 镜像

仓库Settings → Actions → General检查组织允许的工作流权限；image job请求packages:write。成功后在Packages查找镜像。完整SHA在提交页面复制。首次发布若包名已存在，需要确认包授予该仓库访问权限。

## 3 服务器准备（需要可被GitHub连接的Linux服务器）

在服务器安装Docker Engine与Compose插件，部署账号需要Docker操作权限。创建/opt/adventure，将compose.release.yaml复制进去；另行创建.env，填写DB_USERNAME、DB_PASSWORD、MYSQL_ROOT_PASSWORD、JWT_SECRET、SERVER_PORT。JWT_SECRET至少32字节。配置APP_IMAGE为已发布镜像的完整地址和SHA以便手动启动。

私有GHCR镜像需要在服务器上docker login ghcr.io，使用有read:packages权限的凭证，通过--password-stdin输入。凭证保存在部署账号下，Actions SSH应使用同一账号。

先手动执行：

```bash
cd /opt/adventure
docker compose -f compose.release.yaml up -d --wait --wait-timeout 180
docker compose -f compose.release.yaml logs --tail=100 app
```

应用仅监听主机127.0.0.1:8080，使用服务器Nginx转发。MySQL和Redis不公开端口。新服务器数据库为空，Flyway创建表，不自动迁移本机数据。保留同一目录和项目名以继续使用同一组卷。

## 4 GitHub部署配置

Settings → Environments创建production，可配置审批人。在该环境的Secrets添加：

- SERVER_HOST：服务器域名或IP（默认SSH端口22）。
- SERVER_USER：部署账号。
- SERVER_SSH_KEY：用于该账号的SSH私钥，公钥加入服务器authorized_keys。
- SERVER_KNOWN_HOSTS：已核验指纹的服务器known_hosts记录，可在可信本机通过ssh-keyscan取得并与服务器实际指纹核对。

Actions → Deploy → Run workflow，输入之前成功发布的完整40位SHA。手动触发部署会更新服务器应用。失败时先检查镜像是否存在、SSH访问及服务器日志。

## 本机开发

继续使用原compose.yaml：docker compose up -d --build。本地发布镜像练习可使用compose.release.yaml，但不能同时占用同一8080端口。WSL的localhost不能直接被GitHub托管Runner通过SSH访问；没有公网服务器时先完成CI和镜像发布，在WSL手动拉取镜像运行。

本次终端工具启动失败，未能执行编译、测试、Compose检查或检查现有测试文件；配置需要首次Actions运行验证。没有修改已有测试，测试覆盖沿用项目现有文件。
