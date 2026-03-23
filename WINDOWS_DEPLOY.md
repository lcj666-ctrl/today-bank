# Windows 部署到阿里云服务器指南

## 快速开始

### 方式一：使用 PowerShell 脚本（推荐）

#### 1. 准备工作

确保你的 Windows 系统已安装：
- **Git for Windows**（包含 Git Bash 和 SSH）
  - 下载地址：https://git-scm.com/download/win
- **Maven**
  - 下载地址：https://maven.apache.org/download.cgi
- **Java 8+**

#### 2. 配置 SSH 免密登录（可选但推荐）

```powershell
# 打开 PowerShell
# 生成 SSH 密钥
ssh-keygen -t rsa -b 4096

# 复制公钥到服务器（输入服务器密码）
ssh-copy-id root@8.136.210.193

# 测试免密登录
ssh root@8.136.210.193
```

#### 3. 执行部署

```powershell
# 进入项目目录
cd D:\IdeaProjects\today-bank

# 执行部署脚本
.\deploy-to-server.ps1
```

脚本会自动完成：
1. ✅ 检查必要文件
2. ✅ 构建项目（如果需要）
3. ✅ 准备部署文件
4. ✅ 上传到服务器
5. ✅ 在服务器上安装 Docker
6. ✅ 在服务器上安装 Docker Compose
7. ✅ 配置环境变量
8. ✅ 启动服务

### 方式二：手动分步部署

#### 步骤 1：构建项目

```powershell
# 进入项目目录
cd D:\IdeaProjects\today-bank

# 构建项目
mvn clean package -DskipTests
```

#### 步骤 2：上传文件到服务器

```powershell
# 创建部署目录（在服务器上）
ssh root@8.136.210.193 "mkdir -p /opt/ai-paint"

# 上传必要文件
scp target\ai-paint-backend-1.0-SNAPSHOT.jar root@8.136.210.193:/opt/ai-paint/
scp docker-compose.prod.yml root@8.136.210.193:/opt/ai-paint/docker-compose.yml
scp Dockerfile root@8.136.210.193:/opt/ai-paint/
scp nginx.conf root@8.136.210.193:/opt/ai-paint/
scp database.sql root@8.136.210.193:/opt/ai-paint/
scp setup-server.sh root@8.136.210.193:/opt/ai-paint/
scp .env.production root@8.136.210.193:/opt/ai-paint/.env
```

#### 步骤 3：在服务器上执行部署脚本

```powershell
# SSH 登录服务器
ssh root@8.136.210.193

# 进入项目目录
cd /opt/ai-paint

# 给脚本添加执行权限
chmod +x setup-server.sh

# 执行部署脚本
./setup-server.sh
```

脚本会交互式地：
1. 安装 Docker
2. 安装 Docker Compose
3. 配置 Docker 镜像加速
4. 创建环境变量文件
5. 询问是否继续启动服务
6. 启动 Docker 容器

#### 步骤 4：配置环境变量（重要）

部署脚本会创建 `.env` 文件，你需要编辑它：

```bash
# 在服务器上编辑
nano /opt/ai-paint/.env
# 或
vim /opt/ai-paint/.env
```

**必须修改的配置项：**
```bash
OSS_ACCESS_KEY_ID=your_real_oss_access_key_id
OSS_ACCESS_KEY_SECRET=your_real_oss_access_key_secret
OSS_BUCKET_NAME=your_real_bucket_name
DB_PASSWORD=your_strong_database_password
REDIS_PASSWORD=your_strong_redis_password
JWT_SECRET=your_strong_jwt_secret_key
WECHAT_APPID=your_real_wechat_appid
WECHAT_SECRET=your_real_wechat_secret
```

#### 步骤 5：启动服务

如果之前没有启动，手动启动：

```bash
cd /opt/ai-paint
docker-compose up -d
```

## 部署后管理

### 查看服务状态

```powershell
# 查看容器状态
ssh root@8.136.210.193 "cd /opt/ai-paint && docker-compose ps"

# 查看应用日志
ssh root@8.136.210.193 "cd /opt/ai-paint && docker-compose logs -f app"

# 查看所有日志
ssh root@8.136.210.193 "cd /opt/ai-paint && docker-compose logs -f"
```

### 常用管理命令

```powershell
# 重启服务
ssh root@8.136.210.193 "cd /opt/ai-paint && docker-compose restart"

# 停止服务
ssh root@8.136.210.193 "cd /opt/ai-paint && docker-compose down"

# 进入应用容器
ssh root@8.136.210.193 "cd /opt/ai-paint && docker-compose exec app sh"

# 进入 MySQL 容器
ssh root@8.136.210.193 "cd /opt/ai-paint && docker-compose exec mysql bash"

# 查看资源使用
ssh root@8.136.210.193 "docker stats"
```

### 更新部署

当你更新了代码，需要重新部署：

```powershell
# 方法 1：使用脚本（推荐）
cd D:\IdeaProjects\today-bank
.\deploy-to-server.ps1

# 方法 2：手动更新
# 1. 重新构建
mvn clean package -DskipTests

# 2. 上传新的 JAR 文件
scp target\ai-paint-backend-1.0-SNAPSHOT.jar root@8.136.210.193:/opt/ai-paint/

# 3. 在服务器上重启
ssh root@8.136.210.193 "cd /opt/ai-paint && docker-compose restart app"
```

## 故障排查

### 问题 1：PowerShell 执行策略限制

**错误信息：**
```
无法加载文件，因为在此系统上禁止运行脚本
```

**解决方法：**
```powershell
# 以管理员身份运行 PowerShell
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser

# 输入 Y 确认
```

### 问题 2：SSH 连接失败

**检查清单：**
1. 服务器 IP 是否正确（8.136.210.193）
2. 服务器是否已开启
3. 阿里云安全组是否已开放 22 端口
4. 服务器防火墙是否允许 SSH

**测试连接：**
```powershell
ssh root@8.136.210.193
```

### 问题 3：Docker 安装失败

**手动安装 Docker：**

```bash
# 在服务器上执行
curl -fsSL https://get.docker.com | sh
systemctl start docker
systemctl enable docker
```

### 问题 4：应用无法启动

**查看日志：**
```powershell
ssh root@8.136.210.193 "cd /opt/ai-paint && docker-compose logs app"
```

**常见问题：**
1. 环境变量未配置 - 编辑 `.env` 文件
2. 端口被占用 - 检查 48080 端口
3. 内存不足 - 升级服务器配置

### 问题 5：环境变量配置错误

**重新配置：**
```bash
# 在服务器上
nano /opt/ai-paint/.env

# 修改后重启
cd /opt/ai-paint
docker-compose restart
```

## 安全建议

### 1. 修改默认密码

```bash
# 在服务器上修改数据库密码
nano /opt/ai-paint/.env

# 修改后重启
docker-compose restart
```

### 2. 配置防火墙

```bash
# 在服务器上
firewall-cmd --permanent --add-port=80/tcp
firewall-cmd --permanent --add-port=443/tcp
firewall-cmd --permanent --add-port=48080/tcp
firewall-cmd --reload
```

### 3. 配置阿里云安全组

确保已开放以下端口：
- 22 (SSH)
- 80 (HTTP)
- 443 (HTTPS)
- 48080 (应用)

## 配置文件清单

| 文件 | 用途 | 位置 |
|------|------|------|
| `setup-server.sh` | 服务器部署脚本 | 服务器: /opt/ai-paint/ |
| `deploy-to-server.ps1` | Windows 部署脚本 | 本地 |
| `.env` | 环境变量配置 | 服务器: /opt/ai-paint/ |
| `docker-compose.yml` | Docker 编排 | 服务器: /opt/ai-paint/ |

## 联系支持

如遇到问题：
1. 查看详细日志：`docker-compose logs -f`
2. 检查配置文件：`cat .env`
3. 查看容器状态：`docker-compose ps`

祝你部署顺利！🎉
