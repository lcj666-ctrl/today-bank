#!/bin/bash

# AI Paint 服务器自动化部署脚本
# 使用方法: 在服务器上执行 ./setup-server.sh

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置变量
PROJECT_NAME="ai-paint"
PROJECT_DIR="/opt/${PROJECT_NAME}"
JAR_FILE="ai-paint-backend-1.0-SNAPSHOT.jar"

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查是否为 root 用户
check_root() {
    if [[ $EUID -ne 0 ]]; then
        log_error "请使用 root 用户执行此脚本"
        exit 1
    fi
}

# 检测操作系统
detect_os() {
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$NAME
        VERSION=$VERSION_ID
    else
        log_error "无法检测操作系统类型"
        exit 1
    fi
    
    log_info "检测到操作系统: $OS $VERSION"
}

# 安装 Docker
install_docker() {
    log_info "开始安装 Docker..."
    
    # 检查是否已安装 Docker
    if command -v docker &> /dev/null; then
        log_warning "Docker 已安装，版本: $(docker --version)"
        return 0
    fi
    
    # 根据操作系统类型安装 Docker
    case "$OS" in
        "CentOS Linux"|"CentOS"|"Alibaba Cloud Linux")
            # CentOS/阿里云 Linux 安装 Docker（使用阿里云镜像）
            log_info "为 CentOS/阿里云 Linux 安装 Docker..."
            
            # 卸载旧版本
            yum remove -y docker docker-client docker-client-latest docker-common docker-latest docker-latest-logrotate docker-logrotate docker-engine 2>/dev/null || true
            
            # 安装依赖
            yum install -y yum-utils device-mapper-persistent-data lvm2
            
            # 添加阿里云 Docker 镜像源
            yum-config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
            
            # 安装 Docker
            yum install -y docker-ce docker-ce-cli containerd.io
            
            log_success "Docker 安装完成"
            ;;
        "Ubuntu"|"Debian GNU/Linux")
            # Ubuntu/Debian 安装 Docker
            log_info "为 Ubuntu/Debian 安装 Docker..."
            apt-get update
            apt-get remove -y docker docker-engine docker.io containerd runc 2>/dev/null || true
            apt-get install -y apt-transport-https ca-certificates curl gnupg lsb-release
            curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
            echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
            apt-get update
            apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
            ;;
        *)
            # 通用安装方式（使用阿里云镜像）
            log_info "使用阿里云镜像安装 Docker..."
            
            # 使用阿里云镜像下载 Docker 安装脚本
            curl -fsSL https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo -o /etc/yum.repos.d/docker-ce.repo
            yum install -y docker-ce docker-ce-cli containerd.io
            ;;
    esac
    
    # 启动 Docker 服务
    systemctl start docker
    systemctl enable docker
    
    # 验证安装
    if command -v docker &> /dev/null; then
        log_success "Docker 安装成功: $(docker --version)"
    else
        log_error "Docker 安装失败"
        exit 1
    fi
}

# 安装 Docker Compose
install_docker_compose() {
    log_info "开始安装 Docker Compose..."
    
    # 检查是否已安装 Docker Compose
    if command -v docker-compose &> /dev/null; then
        log_warning "Docker Compose 已安装，版本: $(docker-compose --version)"
        return 0
    fi
    
    # 创建目录
    mkdir -p /usr/local/bin
    
    COMPOSE_VERSION="v2.20.0"
    
    # 方法1：使用 GitHub 官方源（优先）
    log_info "从 GitHub 下载 Docker Compose $COMPOSE_VERSION..."
    GITHUB_URL="https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-Linux-x86_64"
    curl -fsSL "$GITHUB_URL" -o /usr/local/bin/docker-compose
    
    if [ $? -eq 0 ] && [ -s /usr/local/bin/docker-compose ]; then
        # 检查文件是否是二进制文件
        if file /usr/local/bin/docker-compose | grep -q "ELF"; then
            chmod +x /usr/local/bin/docker-compose
            
            # 创建软链接
            if [ ! -f /usr/bin/docker-compose ]; then
                ln -sf /usr/local/bin/docker-compose /usr/bin/docker-compose
            fi
            
            # 验证安装
            if command -v docker-compose &> /dev/null; then
                log_success "Docker Compose 安装成功: $(docker-compose --version)"
                return 0
            fi
        fi
    fi
    
    log_warning "GitHub 下载失败，尝试其他镜像源..."
    
    # 方法2：使用 DaoCloud 镜像
    log_info "尝试 DaoCloud 镜像..."
    DAOCLOUD_URL="https://get.daocloud.io/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-Linux-x86_64"
    curl -fsSL "$DAOCLOUD_URL" -o /usr/local/bin/docker-compose
    
    if [ $? -eq 0 ] && [ -s /usr/local/bin/docker-compose ]; then
        if file /usr/local/bin/docker-compose | grep -q "ELF"; then
            chmod +x /usr/local/bin/docker-compose
            
            if [ ! -f /usr/bin/docker-compose ]; then
                ln -sf /usr/local/bin/docker-compose /usr/bin/docker-compose
            fi
            
            if command -v docker-compose &> /dev/null; then
                log_success "Docker Compose 安装成功: $(docker-compose --version)"
                return 0
            fi
        fi
    fi
    
    # 方法3：使用 yum 安装（如果可用）
    log_info "尝试使用 yum 安装 Docker Compose..."
    yum install -y docker-compose-plugin 2>/dev/null
    
    if command -v docker-compose &> /dev/null || docker compose version &> /dev/null; then
        log_success "Docker Compose 安装成功"
        return 0
    fi
    
    log_error "Docker Compose 安装失败"
    log_info "请手动安装 Docker Compose:"
    log_info "  curl -fsSL https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-Linux-x86_64 -o /usr/local/bin/docker-compose"
    log_info "  chmod +x /usr/local/bin/docker-compose"
    exit 1
}

# 配置 Docker 镜像加速
configure_docker_mirror() {
    log_info "配置 Docker 镜像加速..."
    
    mkdir -p /etc/docker
    
    cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.baidubce.com",
    "https://docker.m.daocloud.io"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
EOF
    
    # 重启 Docker
    systemctl restart docker
    
    log_success "Docker 镜像加速配置完成"
}

# 创建项目目录
setup_project_directory() {
    log_info "创建项目目录..."
    
    mkdir -p $PROJECT_DIR
    cd $PROJECT_DIR
    
    log_success "项目目录创建完成: $PROJECT_DIR"
}

# 配置环境变量
setup_environment() {
    log_info "配置环境变量..."
    
    # 检查是否存在 .env 文件
    if [ -f "$PROJECT_DIR/.env" ]; then
        log_warning "环境变量文件已存在，是否覆盖? (y/n)"
        read -r response
        if [[ ! "$response" =~ ^[Yy]$ ]]; then
            log_info "保留现有环境变量配置"
            return 0
        fi
    fi
    
    # 创建环境变量文件
    cat > $PROJECT_DIR/.env <<EOF
# AI Paint 生产环境配置
# 生成时间: $(date '+%Y-%m-%d %H:%M:%S')

# ============================================
# OSS 配置 (必填)
# ============================================
OSS_ENDPOINT=oss-cn-hangzhou.aliyuncs.com
OSS_ACCESS_KEY_ID=your_oss_access_key_id
OSS_ACCESS_KEY_SECRET=your_oss_access_key_secret
OSS_BUCKET_NAME=your_bucket_name

# ============================================
# 数据库配置 (必填)
# ============================================
DB_URL=jdbc:mysql://mysql:3306/aipaint?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=your_strong_database_password

# ============================================
# Redis 配置 (必填)
# ============================================
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=your_strong_redis_password
REDIS_DATABASE=0

# ============================================
# JWT 配置 (必填)
# ============================================
JWT_SECRET=your_strong_jwt_secret_key_at_least_32_characters_long
JWT_EXPIRE=86400
JWT_REFRESH_THRESHOLD=3600

# ============================================
# 微信配置 (必填)
# ============================================
WECHAT_APPID=your_wechat_appid
WECHAT_SECRET=your_wechat_secret

# ============================================
# 服务器配置
# ============================================
SERVER_PORT=48080
EOF
    
    log_success "环境变量文件创建完成"
    log_warning "请编辑 $PROJECT_DIR/.env 文件，填入真实的配置值"
    
    # 提示用户编辑配置文件
    echo ""
    echo -e "${YELLOW}重要提示:${NC}"
    echo "请使用以下命令编辑配置文件:"
    echo -e "${GREEN}nano $PROJECT_DIR/.env${NC} 或 ${GREEN}vim $PROJECT_DIR/.env${NC}"
    echo ""
    echo "必须修改的配置项:"
    echo "  - OSS_ACCESS_KEY_ID"
    echo "  - OSS_ACCESS_KEY_SECRET"
    echo "  - OSS_BUCKET_NAME"
    echo "  - DB_PASSWORD"
    echo "  - REDIS_PASSWORD"
    echo "  - JWT_SECRET"
    echo "  - WECHAT_APPID"
    echo "  - WECHAT_SECRET"
    echo ""
}

# 检查必要的文件
check_required_files() {
    log_info "检查必要的文件..."
    
    # 检查 JAR 文件
    if [ ! -f "$PROJECT_DIR/$JAR_FILE" ]; then
        log_error "未找到 JAR 文件: $JAR_FILE"
        log_info "请将 JAR 文件上传到: $PROJECT_DIR/"
        exit 1
    fi
    
    # 检查 docker-compose.yml 文件
    if [ ! -f "$PROJECT_DIR/docker-compose.yml" ]; then
        log_error "未找到 docker-compose.yml 文件"
        log_info "请将 docker-compose.yml 文件上传到: $PROJECT_DIR/"
        exit 1
    fi
    
    log_success "所有必要文件已就位"
}

# 启动服务
start_services() {
    log_info "启动 Docker 服务..."
    
    cd $PROJECT_DIR
    
    # 检查环境变量是否已配置
    if [ ! -f "$PROJECT_DIR/.env" ]; then
        log_error "环境变量文件不存在，正在创建..."
        setup_environment
    fi
    
    # 检查关键环境变量
    if [ -f "$PROJECT_DIR/.env" ]; then
        source "$PROJECT_DIR/.env"
    else
        log_error "无法创建环境变量文件"
        exit 1
    fi
    
    if [ -z "$OSS_ACCESS_KEY_ID" ] || [ "$OSS_ACCESS_KEY_ID" = "your_oss_access_key_id" ]; then
        log_error "环境变量未配置，请先编辑 .env 文件"
        log_info "编辑命令: nano $PROJECT_DIR/.env"
        exit 1
    fi
    
    # 停止旧容器（如果存在）
    log_info "停止旧容器..."
    docker-compose down 2>/dev/null || true
    
    # 拉取最新镜像
    log_info "拉取 Docker 镜像..."
    docker-compose pull
    
    # 构建并启动容器
    log_info "构建并启动容器..."
    docker-compose up -d --build
    
    # 等待服务启动
    log_info "等待服务启动..."
    sleep 15
    
    # 检查容器状态
    log_info "检查容器状态..."
    docker-compose ps
    
    # 检查应用是否正常运行
    log_info "检查应用健康状态..."
    sleep 5
    
    if curl -s http://localhost:48080/actuator/health > /dev/null 2>&1; then
        log_success "应用启动成功！"
    else
        log_warning "应用可能还在启动中，请稍后检查"
    fi
    
    # 显示容器日志
    log_info "显示应用日志（最近 20 行）..."
    docker-compose logs --tail=20 app
}

# 配置防火墙
configure_firewall() {
    log_info "配置防火墙..."
    
    # 检查防火墙状态
    if command -v firewall-cmd &> /dev/null; then
        # CentOS/RHEL
        systemctl start firewalld
        firewall-cmd --permanent --add-port=80/tcp
        firewall-cmd --permanent --add-port=443/tcp
        firewall-cmd --permanent --add-port=48080/tcp
        firewall-cmd --reload
        log_success "防火墙配置完成 (firewalld)"
    elif command -v ufw &> /dev/null; then
        # Ubuntu
        ufw allow 80/tcp
        ufw allow 443/tcp
        ufw allow 48080/tcp
        log_success "防火墙配置完成 (ufw)"
    else
        log_warning "未检测到防火墙工具，请手动配置端口"
    fi
}

# 显示部署信息
show_deployment_info() {
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}     部署完成！${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo -e "${BLUE}访问地址:${NC}"
    echo "  - 应用: http://$(curl -s ifconfig.me):48080"
    echo "  - Nginx: http://$(curl -s ifconfig.me)"
    echo ""
    echo -e "${BLUE}管理命令:${NC}"
    echo "  查看容器状态:   docker-compose ps"
    echo "  查看应用日志:   docker-compose logs -f app"
    echo "  重启服务:       docker-compose restart"
    echo "  停止服务:       docker-compose down"
    echo "  进入容器:       docker-compose exec app sh"
    echo ""
    echo -e "${BLUE}项目目录:${NC} $PROJECT_DIR"
    echo -e "${BLUE}配置文件:${NC} $PROJECT_DIR/.env"
    echo ""
    echo -e "${YELLOW}重要提醒:${NC}"
    echo "1. 请确保阿里云安全组已开放 80, 443, 48080 端口"
    echo "2. 建议配置 SSL 证书启用 HTTPS"
    echo "3. 定期备份数据库数据"
    echo "4. 监控服务器资源使用情况"
    echo ""
}

# 主函数
main() {
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  AI Paint 服务器自动化部署脚本${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    
    # 检查 root 权限
    check_root
    
    # 检测操作系统
    detect_os
    
    # 安装 Docker
    install_docker
    
    # 安装 Docker Compose
    install_docker_compose
    
    # 配置 Docker 镜像加速
    configure_docker_mirror
    
    # 创建项目目录
    setup_project_directory
    
    # 配置环境变量
    setup_environment
    
    # 询问是否继续启动
    echo ""
    log_warning "请确认已完成以下步骤:"
    echo "1. 已将 JAR 文件上传到: $PROJECT_DIR/$JAR_FILE"
    echo "2. 已将 docker-compose.yml 上传到: $PROJECT_DIR/"
    echo "3. 已编辑 .env 文件，填入真实的配置值"
    echo ""
    read -p "是否继续启动服务? (y/n): " -n 1 -r
    echo ""
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "部署已取消，请完成上述步骤后重新运行脚本"
        exit 0
    fi
    
    # 检查必要文件
    check_required_files
    
    # 配置防火墙
    configure_firewall
    
    # 启动服务
    start_services
    
    # 显示部署信息
    show_deployment_info
}

# 执行主函数
main "$@"
