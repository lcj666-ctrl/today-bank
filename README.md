# AI Paint Backend

一个基于 Spring Boot 的 AI 绘画后端服务，集成阿里云 DashScope AI 模型，提供智能图像生成、用户管理、作品管理等功能。

## 项目简介

AI Paint Backend 是一个功能完整的 AI 绘画平台后端服务，支持用户通过微信登录、上传图片、选择绘画风格，系统会自动调用 AI 模型生成艺术作品。项目采用现代化的技术栈，注重安全性和可扩展性。

## 技术栈

### 后端框架
- **Spring Boot 2.7.18** - 核心框架
- **Spring Security** - 安全认证
- **MyBatis Plus 3.5.3.2** - ORM 框架

### 数据存储
- **MySQL 8.0** - 关系型数据库
- **Redis** - 缓存和会话管理

### 第三方服务
- **阿里云 OSS** - 对象存储服务
- **阿里云 DashScope** - AI 模型服务
- **阿里云短信服务** - 短信验证码
- **微信开放平台** - 用户认证

### 工具库
- **JWT** - Token 认证
- **Hutool** - Java 工具类库
- **OkHttp** - HTTP 客户端
- **Lombok** - 简化 Java 代码
- **Dotenv** - 环境变量管理

## 功能特性

### 用户管理
- ✅ 微信登录认证
- ✅ JWT Token 认证
- ✅ 短信验证码登录
- ✅ 用户信息管理

### AI 绘画
- ✅ 智能图像生成
- ✅ 多种绘画风格选择
- ✅ 异步任务处理
- ✅ 生成结果记录

### 作品管理
- ✅ 作品上传
- ✅ 作品列表查询
- ✅ 作品详情查看
- ✅ 作品删除

### 安全特性
- ✅ 限流保护（每日生成次数限制）
- ✅ 敏感信息加密存储
- ✅ 配置文件安全隔离
- ✅ 访问控制和权限管理

## 项目结构

```
today-bank/
├── src/main/
│   ├── java/com/aipaint/
│   │   ├── Application.java          # 应用启动类
│   │   ├── config/                   # 配置类
│   │   │   ├── DotenvConfig.java     # 环境变量配置
│   │   │   ├── RedisConfig.java      # Redis 配置
│   │   │   ├── SecurityConfig.java   # 安全配置
│   │   │   ├── ThreadPoolConfig.java # 线程池配置
│   │   │   └── WebConfig.java        # Web 配置
│   │   ├── controller/               # 控制器
│   │   │   ├── AiController.java     # AI 生成控制器
│   │   │   ├── DrawingController.java # 作品控制器
│   │   │   ├── UserController.java   # 用户控制器
│   │   │   └── GlobalExceptionHandler.java # 全局异常处理
│   │   ├── service/                  # 服务层
│   │   │   ├── AiGenerateService.java
│   │   │   ├── DrawingService.java
│   │   │   └── UserService.java
│   │   ├── mapper/                   # 数据访问层
│   │   │   ├── AiGenerateLogMapper.java
│   │   │   ├── DrawingMapper.java
│   │   │   └── UserMapper.java
│   │   ├── entity/                   # 实体类
│   │   │   ├── AiGenerateLog.java
│   │   │   ├── Drawing.java
│   │   │   └── User.java
│   │   ├── dto/                      # 数据传输对象
│   │   │   ├── AiGenerateDTO.java
│   │   │   ├── LoginDTO.java
│   │   │   └── SmsDTO.java
│   │   ├── vo/                       # 视图对象
│   │   │   ├── AiGenerateVO.java
│   │   │   ├── DrawingListVO.java
│   │   │   ├── DrawingVO.java
│   │   │   ├── LoginVO.java
│   │   │   └── UploadVO.java
│   │   ├── oss/                      # OSS 工具
│   │   │   └── OssUploadUtil.java
│   │   ├── sms/                      # 短信工具
│   │   │   ├── CheckSmsVerifyCode.java
│   │   │   ├── SendSmsVerifyCode.java
│   │   │   └── SmsUtil.java
│   │   ├── util/                     # 工具类
│   │   │   ├── AiImageGenerateUtil.java
│   │   │   ├── ImageCompressUtil.java
│   │   │   ├── JwtUtil.java
│   │   │   ├── RateLimiterUtil.java
│   │   │   ├── RedisUtil.java
│   │   │   ├── Result.java
│   │   │   ├── SecurityContextUtil.java
│   │   │   └── WechatUtil.java
│   │   ├── ai/                       # AI 相关
│   │   │   ├── AiImageGenerateUtil.java
│   │   │   └── PromptBuilder.java
│   │   ├── queue/                    # 队列服务
│   │   │   └── AiGenerateQueueService.java
│   │   └── filter/                   # 过滤器
│   │       └── JwtAuthenticationFilter.java
│   └── resources/
│       ├── application.yml           # 主配置文件
│       ├── application-local.yml     # 本地配置（不提交）
│       └── mapper/                   # MyBatis 映射文件
│           └── DrawingMapper.xml
├── database.sql                      # 数据库初始化脚本
├── pom.xml                           # Maven 配置
├── Dockerfile                        # Docker 镜像构建
├── docker-compose.yml                # Docker 编排
├── deploy.sh                         # 部署脚本
├── .env.example                      # 环境变量模板
├── .env.docker                       # Docker 环境变量模板
├── DEPLOYMENT.md                     # 部署指南
└── README.md                         # 项目说明
```

## 快速开始

### 环境要求

- JDK 8+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 1. 克隆项目

```bash
git clone <repository-url>
cd today-bank
```

### 2. 配置数据库

```bash
# 创建数据库
mysql -u root -p < database.sql
```

### 3. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件，填入真实配置
notepad .env
```

**必须配置的环境变量：**
```bash
# OSS 配置
OSS_ACCESS_KEY_ID=your_oss_access_key_id
OSS_ACCESS_KEY_SECRET=your_oss_access_key_secret
OSS_BUCKET_NAME=your_bucket_name

# 数据库配置
DB_PASSWORD=your_database_password

# JWT 配置
JWT_SECRET=your_jwt_secret

# 微信配置
WECHAT_APPID=your_wechat_appid
WECHAT_SECRET=your_wechat_secret
```

### 4. 安装依赖

```bash
mvn clean install
```

### 5. 启动应用

```bash
# 开发环境
mvn spring-boot:run

# 或者打包后运行
mvn package
java -jar target/ai-paint-backend-1.0-SNAPSHOT.jar
```

### 6. 访问应用

应用启动后，访问 `http://localhost:48080`

## 配置说明

### 主配置文件 (application.yml)

主配置文件使用环境变量引用，确保敏感信息不暴露在代码中：

```yaml
oss:
  endpoint: ${OSS_ENDPOINT:oss-cn-hangzhou.aliyuncs.com}
  accessKeyId: ${OSS_ACCESS_KEY_ID:}
  accessKeySecret: ${OSS_ACCESS_KEY_SECRET:}
  bucketName: ${OSS_BUCKET_NAME:lcj666}

spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/aipaint?...}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root}
```

### 配置优先级

Spring Boot 配置加载优先级（从高到低）：
1. 命令行参数
2. 系统环境变量
3. 外部配置文件
4. application-{profile}.yml
5. application.yml
6. .env 文件（通过 DotenvConfig）

### 多环境配置

**开发环境：**
```bash
# 使用 .env 文件
cp .env.example .env
```

**测试环境：**
```bash
# 使用环境变量
export OSS_ACCESS_KEY_ID=test_key_id
java -jar app.jar
```

**生产环境：**
```bash
# 使用环境变量或配置中心
export OSS_ACCESS_KEY_ID=prod_key_id
java -jar app.jar
```

## 部署指南

### 传统服务器部署

```bash
# 1. 设置环境变量
export OSS_ACCESS_KEY_ID=prod_key_id
export OSS_ACCESS_KEY_SECRET=prod_key_secret
export DB_PASSWORD=prod_password

# 2. 执行部署脚本
chmod +x deploy.sh
./deploy.sh
```

### Docker 部署

```bash
# 1. 复制 Docker 环境变量模板
cp .env.docker .env

# 2. 编辑 .env 文件
notepad .env

# 3. 构建并启动容器
docker-compose up -d
```

### Kubernetes 部署

```bash
# 1. 创建 Secret
kubectl create secret generic app-secrets \
  --from-literal=oss-access-key-id=prod_key_id

# 2. 部署应用
kubectl apply -f k8s-deployment.yaml
```

详细的部署指南请参考 [DEPLOYMENT.md](DEPLOYMENT.md)

## API 文档

### 用户认证

#### 微信登录
```http
POST /api/user/wechat/login
Content-Type: application/json

{
  "code": "wechat_auth_code"
}
```

#### 短信验证码登录
```http
POST /api/user/sms/login
Content-Type: application/json

{
  "phone": "13800138000",
  "code": "123456"
}
```

### AI 绘画

#### 生成 AI 图像
```http
POST /api/ai/generate
Content-Type: application/json
Authorization: Bearer <token>

{
  "drawingId": 1,
  "style": "油画",
  "prompt": "描述你想要的画面"
}
```

### 作品管理

#### 上传作品
```http
POST /api/drawing/upload
Content-Type: multipart/form-data
Authorization: Bearer <token>

drawing: <file>
drawType: "portrait"
```

#### 获取作品列表
```http
GET /api/drawing/list
Authorization: Bearer <token>
```

## 开发指南

### 代码规范

- 遵循阿里巴巴 Java 开发手册
- 使用 Lombok 简化代码
- 统一异常处理
- RESTful API 设计

### 安全最佳实践

- ✅ 敏感信息使用环境变量
- ✅ 密码和 Token 加密存储
- ✅ 接口限流保护
- ✅ SQL 注入防护
- ✅ XSS 攻击防护

### 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify
```

### 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 常见问题

### Q1: 如何获取阿里云 OSS 访问密钥？
A: 登录阿里云控制台，进入对象存储 OSS 服务，创建 AccessKey。

### Q2: 如何配置微信登录？
A: 在微信开放平台注册应用，获取 AppID 和 AppSecret，填入配置文件。

### Q3: 如何调整 AI 生成次数限制？
A: 修改 `AiController.java` 中的 `GENERATE_LIMIT` 常量。

### Q4: 如何切换不同的 AI 模型？
A: 修改 `AiImageGenerateUtil.java` 中的模型配置参数。

### Q5: 如何处理并发请求？
A: 项目已集成线程池配置，支持异步任务处理。

## 许可证

本项目采用 MIT 许可证 - 详见 LICENSE 文件

## 联系方式

- 项目主页: [GitHub Repository]
- 问题反馈:jasonwz0809@gmail.com
- 邮箱: jasonwz0809@gmail.com

## 致谢

感谢以下开源项目和第三方服务：

- Spring Boot
- MyBatis Plus
- 阿里云 DashScope
- 阿里云 OSS
- 微信开放平台

## 更新日志

### v1.0.0 (2024-03-17)
- ✨ 初始版本发布
- ✅ 支持微信登录
- ✅ 支持 AI 图像生成
- ✅ 支持作品管理
- ✅ 支持限流保护
- ✅ 完善的安全机制
