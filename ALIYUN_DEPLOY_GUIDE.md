# 阿里云服务器 Docker 部署完整指南

本文档详细介绍如何将 AI Paint 项目从零部署到阿里云服务器，使用 Docker 容器化部署。

## 目录

1. [准备工作](#1-准备工作)
2. [购买和配置阿里云服务器](#2-购买和配置阿里云服务器)
3. [服务器环境初始化](#3-服务器环境初始化)
4. [本地项目准备](#4-本地项目准备)
5. [部署到阿里云](#5-部署到阿里云)
6. [配置域名和SSL](#6-配置域名和ssl)
7. [运维管理](#7-运维管理)
8. [故障排查](#8-故障排查)

---

## 1. 准备工作

### 1.1 必要条件

- **阿里云账号**：注册并完成实名认证
- **域名**（可选）：用于 HTTPS 访问
- **SSL 证书**（可选）：用于 HTTPS 加密
- **本地环境**：
  - Java 8+
  - Maven 3.6+
  - Git
  - SSH 客户端（Windows 推荐 Git Bash 或 PowerShell）

### 1.2 准备清单

| 项目 | 说明 | 获取方式 |
|------|------|----------|
| 阿里云账号 | 用于购买服务器 | [阿里云官网](https://www.aliyun.com/) |
| OSS AccessKey | 阿里云对象存储 | 阿里云控制台 → OSS → AccessKey |
| 微信 AppID | 微信登录 | 微信开放平台 |
| 微信 Secret | 微信登录密钥 | 微信开放平台 |

---

## 2. 购买和配置阿里云服务器

### 2.1 购买 ECS 服务器

1. **登录阿里云控制台**
   - 访问 [阿里云 ECS 控制台](https://ecs.console.aliyun.com/)

2. **创建实例**
   - 点击"创建实例"
   - 选择配置：
     - **地域**：选择离用户最近的地区（如华东1-杭州）
     - **实例规格**：推荐 2核4G 或更高
     - **镜像**：CentOS 7.9 或 Ubuntu 20.04
     - **存储**：40GB SSD 或更高
     - **带宽**：3-5Mbps

3. **配置安全组**

   **必须开放的端口：**
   ```
   22    - SSH 访问
   80    - HTTP
   443   - HTTPS
   48080 - 应用端口
   3306  - MySQL（建议限制IP）
   6379  - Redis（建议限制IP）
   ```

   **安全组规则配置：**
   ```bash
   # 入方向规则
   允许 TCP 22    0.0.0.0/0    # SSH
   允许 TCP 80    0.0.0.0/0    # HTTP
   允许 TCP 443   0.0.0.0/0    # HTTPS
   允许 TCP 48080 0.0.0.0/0    # 应用
   
   # 数据库端口（建议限制IP）
   允许 TCP 3306  your-ip/32   # MySQL
   允许 TCP 6379  your-ip/32   # Redis
   ```

4. **设置登录密码**
   - 创建密钥对或设置密码
   - 保存好私钥文件（.pem）

### 2.2 配置安全组详细步骤

1. 进入 ECS 控制台 → 网络与安全 → 安全组
2. 点击"配置规则"
3. 添加入方向规则：

| 授权策略 | 协议类型 | 端口范围 | 授权对象 | 描述 |
|---------|---------|---------|---------|------|
| 允许 | 自定义 TCP | 22/22 | 0.0.0.0/0 | SSH |
| 允许 | 自定义 TCP | 80/80 | 0.0.0.0/0 | HTTP |
| 允许 | 自定义 TCP | 443/443 | 0.0.0.0/0 | HTTPS |
| 允许 | 自定义 TCP | 48080/48080 | 0.0.0.0/0 | 应用端口 |

---

## 3. 服务器环境初始化

### 3.1 连接服务器

```bash
# 使用密码登录
ssh root@your-server-ip

# 或使用密钥登录
ssh -i your-key.pem root@your-server-ip
```

### 3.2 系统更新

```bash
# CentOS
yum update -y

# Ubuntu
apt-get update && apt-get upgrade -y
```

### 3.3 安装 Docker

```bash
# 安装 Docker（CentOS）
curl -fsSL https://get.docker.com | sh

# 启动 Docker
systemctl start docker
systemctl enable docker

# 验证安装
docker --version
docker-compose --version
```

### 3.4 安装 Docker Compose

```bash
# 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# 添加执行权限
sudo chmod +x /usr/local/bin/docker-compose

# 创建软链接
sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose

# 验证安装
docker-compose --version
```

### 3.5 配置 Docker 镜像加速（推荐）

```bash
# 创建或编辑 Docker 配置文件
mkdir -p /etc/docker

# 编辑配置文件
cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.baidubce.com"
  ]
}
EOF

# 重启 Docker
systemctl restart docker
```

### 3.6 创建部署目录

```bash
# 创建项目目录
mkdir -p /opt/ai-paint
cd /opt/ai-paint

# 设置目录权限
chmod 755 /opt/ai-paint
```

---

## 4. 本地项目准备

### 4.1 更新配置文件

**1. 编辑 `.env.production` 文件**

```bash
# 复制模板
cp .env.production .env.production.backup

# 编辑生产环境配置
notepad .env.production
```

**必须修改的配置项：**
```bash
# OSS 配置（使用生产环境的 AccessKey）
OSS_ACCESS_KEY_ID=your_production_oss_access_key_id
OSS_ACCESS_KEY_SECRET=your_production_oss_access_key_secret
OSS_BUCKET_NAME=your_production_bucket_name

# 数据库配置（使用强密码）
DB_PASSWORD=YourStrongPassword123!

# Redis 配置（使用强密码）
REDIS_PASSWORD=YourStrongRedisPassword123!

# JWT 配置（使用随机生成的强密钥）
JWT_SECRET=YourRandomSecretKeyAtLeast32CharactersLong

# 微信配置（使用生产环境的 AppID）
WECHAT_APPID=your_production_wechat_appid
WECHAT_SECRET=your_production_wechat_secret
```

**生成随机 JWT 密钥：**
```bash
# 使用 OpenSSL 生成随机密钥
openssl rand -base64 32
```

### 4.2 构建项目

```bash
# 清理并构建
mvn clean package -DskipTests

# 验证构建结果
ls -la target/ai-paint-backend-1.0-SNAPSHOT.jar
```

### 4.3 本地测试 Docker 构建

```bash
# 构建 Docker 镜像
docker build -t ai-paint:test .

# 验证镜像
docker images | grep ai-paint
```

---

## 5. 部署到阿里云

### 5.1 方式一：使用自动化部署脚本（推荐）

**1. 配置部署脚本**

编辑 `deploy-to-aliyun.sh`：
```bash
# 修改服务器 IP
SERVER_IP="your-server-ip"  # 替换为你的服务器 IP
SERVER_USER="root"
```

**2. 配置 SSH 免密登录（可选但推荐）**

```bash
# 生成本地 SSH 密钥（如果还没有）
ssh-keygen -t rsa -b 4096

# 复制公钥到服务器
ssh-copy-id root@your-server-ip

# 测试免密登录
ssh root@your-server-ip
```

**3. 执行部署**

```bash
# 给脚本添加执行权限
chmod +x deploy-to-aliyun.sh

# 执行部署
./deploy-to-aliyun.sh
```

### 5.2 方式二：手动部署

**步骤 1：准备部署文件**

```bash
# 创建部署目录
mkdir -p aliyun-deploy
cd aliyun-deploy

# 复制必要文件
cp ../target/ai-paint-backend-1.0-SNAPSHOT.jar .
cp ../Dockerfile .
cp ../docker-compose.prod.yml docker-compose.yml
cp ../nginx.conf .
cp ../database.sql .
cp ../.env.production .env
```

**步骤 2：上传到服务器**

```bash
# 使用 scp 上传文件
scp -r aliyun-deploy/* root@your-server-ip:/opt/ai-paint/

# 或者使用 rsync
rsync -avz --progress aliyun-deploy/ root@your-server-ip:/opt/ai-paint/
```

**步骤 3：在服务器上启动**

```bash
# SSH 登录服务器
ssh root@your-server-ip

# 进入项目目录
cd /opt/ai-paint

# 启动服务
docker-compose up -d

# 查看容器状态
docker-compose ps

# 查看应用日志
docker-compose logs -f app
```

### 5.3 验证部署

**1. 检查容器状态**

```bash
# 查看所有容器
docker-compose ps

# 预期输出：
# NAME                STATUS              PORTS
# ai-paint_app_1      Up 2 minutes        0.0.0.0:48080->48080/tcp
# ai-paint_mysql_1    Up 2 minutes        0.0.0.0:3306->3306/tcp
# ai-paint_redis_1    Up 2 minutes        0.0.0.0:6379->6379/tcp
# ai-paint_nginx_1    Up 2 minutes        0.0.0.0:80->80/tcp
```

**2. 测试应用接口**

```bash
# 测试健康检查
curl http://your-server-ip:48080/actuator/health

# 预期返回：
# {"status":"UP"}
```

**3. 查看应用日志**

```bash
# 查看应用日志
docker-compose logs app

# 查看最后 100 行日志
docker-compose logs --tail=100 app

# 实时查看日志
docker-compose logs -f app
```

---

## 6. 配置域名和SSL

### 6.1 配置域名解析

1. **登录域名服务商控制台**
2. **添加 A 记录**：
   - 主机记录：`@` 或 `www`
   - 记录值：你的服务器 IP
   - TTL：600

### 6.2 申请 SSL 证书

**方式一：使用阿里云免费证书**

1. 登录 [阿里云 SSL 证书控制台](https://www.aliyun.com/product/cas)
2. 点击"购买证书" → 选择"免费版"
3. 填写域名信息并验证
4. 下载证书文件（Nginx 格式）

**方式二：使用 Let's Encrypt（免费）**

```bash
# 安装 certbot
yum install certbot python3-certbot-nginx

# 申请证书
certbot --nginx -d your-domain.com

# 自动续期
certbot renew --dry-run
```

### 6.3 配置 Nginx SSL

**1. 上传证书到服务器**

```bash
# 创建 SSL 目录
mkdir -p /opt/ai-paint/ssl

# 上传证书文件
scp your-domain.crt root@your-server-ip:/opt/ai-paint/ssl/
scp your-domain.key root@your-server-ip:/opt/ai-paint/ssl/
```

**2. 修改 nginx.conf**

取消 HTTPS 配置的注释：
```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    ssl_certificate /etc/nginx/ssl/your-domain.crt;
    ssl_certificate_key /etc/nginx/ssl/your-domain.key;
    
    location / {
        proxy_pass http://aipaint_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**3. 重启 Nginx**

```bash
cd /opt/ai-paint
docker-compose restart nginx
```

---

## 7. 运维管理

### 7.1 日常管理命令

```bash
# 进入项目目录
cd /opt/ai-paint

# 查看容器状态
docker-compose ps

# 查看日志
docker-compose logs -f app

# 重启服务
docker-compose restart

# 重启单个服务
docker-compose restart app

# 停止服务
docker-compose down

# 停止并删除数据卷（谨慎使用）
docker-compose down -v

# 进入容器内部
docker-compose exec app sh
docker-compose exec mysql bash

# 查看资源使用
docker stats
```

### 7.2 数据库备份

```bash
# 创建备份脚本
cat > /opt/backup.sh <<'EOF'
#!/bin/bash
BACKUP_DIR="/opt/backups"
DATE=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR

# 备份 MySQL
docker exec ai-paint_mysql_1 mysqldump -uroot -pYourPassword aipaint > $BACKUP_DIR/aipaint_$DATE.sql

# 压缩备份
gzip $BACKUP_DIR/aipaint_$DATE.sql

# 保留最近 7 天的备份
find $BACKUP_DIR -name "*.gz" -mtime +7 -delete

echo "Backup completed: $BACKUP_DIR/aipaint_$DATE.sql.gz"
EOF

chmod +x /opt/backup.sh

# 添加到定时任务
crontab -e
# 添加：0 2 * * * /opt/backup.sh
```

### 7.3 监控和告警

**安装 Prometheus + Grafana（可选）**

```bash
# 创建监控目录
mkdir -p /opt/monitoring
cd /opt/monitoring

# 创建 docker-compose.yml
cat > docker-compose.yml <<EOF
version: '3.8'
services:
  prometheus:
    image: prom/prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
  
  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
EOF

# 启动监控
docker-compose up -d
```

### 7.4 日志管理

```bash
# 查看应用日志
docker-compose logs app

# 查看 Nginx 访问日志
docker-compose logs nginx

# 查看 MySQL 日志
docker-compose logs mysql

# 清理旧日志
docker system prune -f
```

---

## 8. 故障排查

### 8.1 常见问题

**问题 1：容器无法启动**

```bash
# 查看错误日志
docker-compose logs app

# 检查端口占用
netstat -tlnp | grep 48080

# 检查磁盘空间
df -h
```

**问题 2：数据库连接失败**

```bash
# 检查 MySQL 容器状态
docker-compose ps mysql

# 查看 MySQL 日志
docker-compose logs mysql

# 测试数据库连接
docker-compose exec mysql mysql -uroot -p -e "SHOW DATABASES;"
```

**问题 3：应用无法访问**

```bash
# 检查防火墙
firewall-cmd --list-all

# 检查安全组规则（阿里云控制台）

# 测试本地访问
curl http://localhost:48080

# 测试外部访问
curl http://your-server-ip:48080
```

**问题 4：内存不足**

```bash
# 查看内存使用
free -h

# 查看容器内存使用
docker stats

# 重启容器释放内存
docker-compose restart
```

### 8.2 性能优化

**1. JVM 参数优化**

编辑 `Dockerfile`：
```dockerfile
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "app.jar"]
```

**2. MySQL 优化**

编辑 `docker-compose.yml`，添加 MySQL 配置：
```yaml
mysql:
  command: >
    --default-authentication-plugin=mysql_native_password
    --character-set-server=utf8mb4
    --collation-server=utf8mb4_unicode_ci
    --innodb-buffer-pool-size=512M
    --max-connections=200
```

**3. Nginx 优化**

编辑 `nginx.conf`，添加缓存配置：
```nginx
http {
    # 开启 gzip
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css application/json;
    
    # 连接优化
    keepalive_timeout 65;
    client_max_body_size 50M;
}
```

---

## 9. 安全加固

### 9.1 服务器安全

```bash
# 修改 SSH 端口
vi /etc/ssh/sshd_config
# Port 2222

# 禁止 root 登录
PermitRootLogin no

# 重启 SSH
systemctl restart sshd

# 配置防火墙
firewall-cmd --permanent --add-port=2222/tcp
firewall-cmd --reload
```

### 9.2 Docker 安全

```bash
# 使用非 root 用户运行容器
# 在 Dockerfile 中添加
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# 限制容器资源
docker-compose.yml:
services:
  app:
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 1G
```

### 9.3 定期更新

```bash
# 更新系统
yum update -y

# 更新 Docker 镜像
docker-compose pull
docker-compose up -d

# 清理旧镜像
docker image prune -f
```

---

## 10. 总结

### 部署检查清单

- [ ] 购买阿里云 ECS 服务器
- [ ] 配置安全组规则
- [ ] 安装 Docker 和 Docker Compose
- [ ] 配置 Docker 镜像加速
- [ ] 更新生产环境配置文件
- [ ] 构建项目并测试
- [ ] 上传文件到服务器
- [ ] 启动 Docker 容器
- [ ] 验证应用正常运行
- [ ] 配置域名解析（可选）
- [ ] 配置 SSL 证书（可选）
- [ ] 设置自动备份
- [ ] 配置监控告警（可选）

### 重要提醒

1. **安全第一**：定期更新密码，限制端口访问，配置防火墙
2. **数据备份**：设置自动备份，定期测试恢复
3. **监控告警**：配置监控，及时发现和处理问题
4. **文档记录**：记录所有配置和修改，方便维护

### 联系方式

如遇到问题，请查看：
- [项目文档](README.md)
- [部署指南](DEPLOYMENT.md)
- [GitHub Issues](https://github.com/your-repo/issues)

---

**祝你部署顺利！🎉**
