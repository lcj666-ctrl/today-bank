# 部署配置指南

## 配置管理策略

### 核心原则
- **开发环境**：使用 `.env` 文件（不提交到 git）
- **测试/生产环境**：使用环境变量或配置中心
- **配置模板**：`.env.example` 提交到 git，供团队参考

## 1. 开发环境配置

### 1.1 本地开发
```bash
# 1. 复制配置模板
cp .env.example .env

# 2. 编辑 .env 文件，填入本地配置
notepad .env

# 3. 启动应用
mvn spring-boot:run
```

### 1.2 配置文件说明
- `.env`：本地开发配置（不提交 git）
- `.env.example`：配置模板（提交 git）
- `application-local.yml`：本地特定配置（不提交 git）

## 2. 测试环境部署

### 2.1 传统服务器部署
```bash
# 1. 准备部署脚本
chmod +x deploy.sh

# 2. 设置环境变量
export OSS_ACCESS_KEY_ID=test_key_id
export OSS_ACCESS_KEY_SECRET=test_key_secret
export DB_URL=jdbc:mysql://test-db:3306/aipaint

# 3. 执行部署
./deploy.sh
```

### 2.2 Docker 部署
```bash
# 1. 复制 Docker 环境变量模板
cp .env.docker .env

# 2. 编辑 .env 文件，填入测试环境配置
notepad .env

# 3. 构建并启动容器
docker-compose up -d
```

## 3. 生产环境部署

### 3.1 环境变量方式（推荐）
```bash
# 1. 在服务器上设置环境变量
export OSS_ACCESS_KEY_ID=prod_key_id
export OSS_ACCESS_KEY_SECRET=prod_key_secret
export DB_URL=jdbc:mysql://prod-db:3306/aipaint
export JWT_SECRET=prod_jwt_secret

# 2. 启动应用
java -jar ai-paint-backend-1.0-SNAPSHOT.jar
```

### 3.2 Docker 环境变量方式
```bash
# 1. 启动容器时传递环境变量
docker run -d \
  -e OSS_ACCESS_KEY_ID=prod_key_id \
  -e OSS_ACCESS_KEY_SECRET=prod_key_secret \
  -e DB_URL=jdbc:mysql://prod-db:3306/aipaint \
  -p 48080:48080 \
  ai-paint-backend:1.0
```

### 3.3 Kubernetes 部署
```yaml
# 1. 创建 Secret
kubectl create secret generic app-secrets \
  --from-literal=oss-access-key-id=prod_key_id \
  --from-literal=oss-access-key-secret=prod_key_secret

# 2. 在 Deployment 中引用 Secret
apiVersion: apps/v1
kind: Deployment
metadata:
  name: aipaint-app
spec:
  template:
    spec:
      containers:
      - name: app
        env:
        - name: OSS_ACCESS_KEY_ID
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: oss-access-key-id
```

## 4. 配置优先级

Spring Boot 配置加载优先级（从高到低）：
1. 命令行参数
2. 系统环境变量
3. 外部配置文件
4. application-{profile}.yml
5. application.yml
6. .env 文件（通过 DotenvConfig）

## 5. 安全最佳实践

### 5.1 密钥管理
- 定期轮换访问密钥
- 使用强密码和随机密钥
- 不同环境使用不同的密钥

### 5.2 访问控制
- 限制配置文件的访问权限
- 使用加密存储敏感信息
- 实施最小权限原则

### 5.3 监控和审计
- 监控配置变更
- 记录密钥使用情况
- 定期安全审计

## 6. 故障排查

### 6.1 配置不生效
```bash
# 检查环境变量
echo $OSS_ACCESS_KEY_ID

# 检查应用日志
tail -f logs/application.log | grep "OSS"
```

### 6.2 启动失败
```bash
# 查看详细错误信息
java -jar app.jar --debug

# 检查配置文件
cat application.yml
```

## 7. 团队协作

### 7.1 新成员加入
1. 克隆项目：`git clone <repository-url>`
2. 复制配置模板：`cp .env.example .env`
3. 填入自己的配置
4. 启动应用

### 7.2 配置变更
1. 更新 `.env.example` 模板
2. 提交模板到 git
3. 通知团队成员更新各自的配置

## 8. CI/CD 集成

### 8.1 GitHub Actions 示例
```yaml
name: Deploy to Production

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    
    - name: Deploy to server
      env:
        OSS_ACCESS_KEY_ID: ${{ secrets.OSS_ACCESS_KEY_ID }}
        OSS_ACCESS_KEY_SECRET: ${{ secrets.OSS_ACCESS_KEY_SECRET }}
      run: |
        ./deploy.sh
```

### 8.2 Jenkins 示例
```groovy
pipeline {
    agent any
    environment {
        OSS_ACCESS_KEY_ID = credentials('oss-access-key-id')
        OSS_ACCESS_KEY_SECRET = credentials('oss-access-key-secret')
    }
    stages {
        stage('Deploy') {
            steps {
                sh './deploy.sh'
            }
        }
    }
}
```

## 9. 配置文件清单

| 文件 | 用途 | 提交Git | 说明 |
|------|------|---------|------|
| `.env` | 本地开发配置 | ❌ | 包含真实敏感信息 |
| `.env.example` | 配置模板 | ✅ | 提供配置示例 |
| `.env.docker` | Docker配置模板 | ❌ | Docker部署模板 |
| `application.yml` | 主配置文件 | ✅ | 包含环境变量引用 |
| `application-local.yml` | 本地特定配置 | ❌ | 本地开发特定配置 |
| `deploy.sh` | 部署脚本 | ✅ | 生产部署脚本 |
| `Dockerfile` | Docker镜像构建 | ✅ | 容器化配置 |
| `docker-compose.yml` | Docker编排 | ✅ | 多容器编排 |

## 10. 常见问题

### Q1: 为什么 .env 文件不提交到 git？
A: .env 文件包含真实的敏感信息（如密钥、密码），提交到 git 会导致安全风险。

### Q2: 生产环境如何获取配置？
A: 生产环境通过环境变量、配置中心或密钥管理服务获取配置，不依赖 .env 文件。

### Q3: 如何在不同环境使用不同配置？
A: 使用 Spring Profile 功能，通过 `spring.profiles.active` 指定环境。

### Q4: 配置变更如何同步到团队？
A: 更新 `.env.example` 模板并提交到 git，团队成员根据模板更新各自的配置。
