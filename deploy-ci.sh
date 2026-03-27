#!/bin/bash

# CI 部署脚本
# 用于 GitHub Actions 自动部署

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置变量
PROJECT_DIR="/opt/ai-paint"

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 停止旧容器
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
    log_info "应用可能还在启动中，请稍后检查"
fi

# 显示容器日志
log_info "显示应用日志（最近 20 行）..."
docker-compose logs --tail=20 app

log_success "部署完成！"
