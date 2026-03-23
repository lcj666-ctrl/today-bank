#!/bin/bash

# AI Paint 阿里云服务器部署脚本
# 使用方法: ./deploy-to-aliyun.sh

set -e

echo "🚀 开始部署 AI Paint 到阿里云服务器..."

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置变量
SERVER_IP="8.136.210.193"
SERVER_USER="root"
PROJECT_NAME="ai-paint"
REMOTE_DIR="/opt/${PROJECT_NAME}"

# 检查必要的环境变量
if [ -z "$SERVER_IP" ] || [ "$SERVER_IP" = "8.136.210.193" ]; then
    echo -e "${RED}❌ 错误: 请设置服务器 IP 地址${NC}"
    echo "修改脚本中的 SERVER_IP 变量"
    exit 1
fi

echo -e "${GREEN}✓ 配置检查通过${NC}"
echo "服务器 IP: $SERVER_IP"
echo "部署目录: $REMOTE_DIR"

# 步骤 1: 构建项目
echo -e "${YELLOW}📦 步骤 1: 构建项目...${NC}"
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ 项目构建失败${NC}"
    exit 1
fi
echo -e "${GREEN}✓ 项目构建成功${NC}"

# 步骤 2: 准备部署文件
echo -e "${YELLOW}📁 步骤 2: 准备部署文件...${NC}"
DEPLOY_DIR="deploy-$(date +%Y%m%d-%H%M%S)"
mkdir -p $DEPLOY_DIR

# 复制必要文件
cp target/ai-paint-backend-1.0-SNAPSHOT.jar $DEPLOY_DIR/
cp Dockerfile $DEPLOY_DIR/
cp docker-compose.prod.yml $DEPLOY_DIR/docker-compose.yml
cp nginx.conf $DEPLOY_DIR/
cp database.sql $DEPLOY_DIR/
cp .env.production $DEPLOY_DIR/.env

echo -e "${GREEN}✓ 部署文件准备完成${NC}"

# 步骤 3: 上传到服务器
echo -e "${YELLOW}📤 步骤 3: 上传文件到服务器...${NC}"

# 创建远程目录
ssh $SERVER_USER@$SERVER_IP "mkdir -p $REMOTE_DIR"

# 上传文件
scp -r $DEPLOY_DIR/* $SERVER_USER@$SERVER_IP:$REMOTE_DIR/

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ 文件上传失败${NC}"
    exit 1
fi
echo -e "${GREEN}✓ 文件上传成功${NC}"

# 步骤 4: 在服务器上执行部署
echo -e "${YELLOW}🐳 步骤 4: 在服务器上执行部署...${NC}"

ssh $SERVER_USER@$SERVER_IP << EOF
    cd $REMOTE_DIR
    
    echo "停止旧容器..."
    docker-compose down 2>/dev/null || true
    
    echo "拉取最新镜像..."
    docker-compose pull 2>/dev/null || true
    
    echo "构建并启动容器..."
    docker-compose up -d --build
    
    echo "等待服务启动..."
    sleep 10
    
    echo "检查容器状态..."
    docker-compose ps
    
    echo "查看应用日志..."
    docker-compose logs --tail=20 app
EOF

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ 服务器部署失败${NC}"
    exit 1
fi

echo -e "${GREEN}✓ 服务器部署成功${NC}"

# 步骤 5: 清理本地临时文件
echo -e "${YELLOW}🧹 步骤 5: 清理临时文件...${NC}"
rm -rf $DEPLOY_DIR
echo -e "${GREEN}✓ 临时文件清理完成${NC}"

# 部署完成
echo ""
echo -e "${GREEN}🎉 部署完成！${NC}"
echo ""
echo "访问地址:"
echo "  - 应用: http://$SERVER_IP:48080"
echo "  - Nginx: http://$SERVER_IP"
echo ""
echo "管理命令:"
echo "  - 查看日志: ssh $SERVER_USER@$SERVER_IP 'cd $REMOTE_DIR && docker-compose logs -f app'"
echo "  - 重启服务: ssh $SERVER_USER@$SERVER_IP 'cd $REMOTE_DIR && docker-compose restart'"
echo "  - 停止服务: ssh $SERVER_USER@$SERVER_IP 'cd $REMOTE_DIR && docker-compose down'"
echo ""
echo -e "${YELLOW}⚠️  重要提醒:${NC}"
echo "1. 请确保 .env.production 文件中的配置已更新为生产环境值"
echo "2. 建议配置 SSL 证书以启用 HTTPS"
echo "3. 建议配置防火墙规则，限制端口访问"
echo "4. 定期备份数据库数据"
