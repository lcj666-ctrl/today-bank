# AI Paint 服务器部署脚本 (Windows PowerShell)
# 使用方法: 在 PowerShell 中执行 .\deploy-to-server.ps1

Write-Host "🚀 AI Paint 服务器部署脚本" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# 配置变量
$SERVER_IP = "8.136.210.193"
$SERVER_USER = "root"
$PROJECT_DIR = "/opt/ai-paint"
$LOCAL_DIR = "D:\IdeaProjects\today-bank"

Write-Host "服务器信息:" -ForegroundColor Cyan
Write-Host "  IP: $SERVER_IP" -ForegroundColor Cyan
Write-Host "  用户: $SERVER_USER" -ForegroundColor Cyan
Write-Host "  远程目录: $PROJECT_DIR" -ForegroundColor Cyan
Write-Host ""

# 检查必要文件
Write-Host "📋 检查本地文件..." -ForegroundColor Yellow

$requiredFiles = @(
    "target\ai-paint-backend-1.0-SNAPSHOT.jar",
    "docker-compose.prod.yml",
    "Dockerfile",
    "nginx.conf",
    "database.sql",
    "setup-server.sh",
    ".env.production"
)

$allFilesExist = $true
foreach ($file in $requiredFiles) {
    $fullPath = Join-Path $LOCAL_DIR $file
    if (Test-Path $fullPath) {
        Write-Host "  ✓ $file" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $file (缺失)" -ForegroundColor Red
        $allFilesExist = $false
    }
}

if (-not $allFilesExist) {
    Write-Host ""
    Write-Host "❌ 部分文件缺失，请确保所有必要文件都存在" -ForegroundColor Red
    Write-Host "请先执行: mvn clean package -DskipTests" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "✓ 文件检查通过" -ForegroundColor Green
Write-Host ""

# 步骤 1: 构建项目（如果需要）
Write-Host "📦 步骤 1: 检查项目构建..." -ForegroundColor Yellow

if (-not (Test-Path "$LOCAL_DIR\target\ai-paint-backend-1.0-SNAPSHOT.jar")) {
    Write-Host "JAR 文件不存在，开始构建项目..." -ForegroundColor Yellow
    Set-Location $LOCAL_DIR
    & mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ 项目构建失败" -ForegroundColor Red
        exit 1
    }
    Write-Host "✓ 项目构建成功" -ForegroundColor Green
} else {
    Write-Host "✓ JAR 文件已存在，跳过构建" -ForegroundColor Green
}

Write-Host ""

# 步骤 2: 准备部署文件
Write-Host "📁 步骤 2: 准备部署文件..." -ForegroundColor Yellow

$deployDir = "deploy-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
$deployPath = Join-Path $LOCAL_DIR $deployDir

New-Item -ItemType Directory -Path $deployPath -Force | Out-Null

# 复制文件
Copy-Item "$LOCAL_DIR\target\ai-paint-backend-1.0-SNAPSHOT.jar" "$deployPath\"
Copy-Item "$LOCAL_DIR\docker-compose.prod.yml" "$deployPath\docker-compose.yml"
Copy-Item "$LOCAL_DIR\Dockerfile" "$deployPath\"
Copy-Item "$LOCAL_DIR\nginx.conf" "$deployPath\"
Copy-Item "$LOCAL_DIR\database.sql" "$deployPath\"
Copy-Item "$LOCAL_DIR\setup-server.sh" "$deployPath\"
Copy-Item "$LOCAL_DIR\.env.production" "$deployPath\.env"

Write-Host "✓ 部署文件准备完成: $deployPath" -ForegroundColor Green
Write-Host ""

# 步骤 3: 上传到服务器
Write-Host "📤 步骤 3: 上传文件到服务器..." -ForegroundColor Yellow
Write-Host "正在连接到 $SERVER_IP..." -ForegroundColor Cyan

# 创建远程目录
Write-Host "创建远程目录..." -ForegroundColor Cyan
ssh "${SERVER_USER}@${SERVER_IP}" "mkdir -p $PROJECT_DIR"

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 无法连接到服务器，请检查:" -ForegroundColor Red
    Write-Host "  1. 服务器 IP 是否正确" -ForegroundColor Yellow
    Write-Host "  2. SSH 是否可以连接" -ForegroundColor Yellow
    Write-Host "  3. 服务器是否已开启" -ForegroundColor Yellow
    exit 1
}

# 上传文件
Write-Host "上传文件到服务器..." -ForegroundColor Cyan
$files = Get-ChildItem -Path $deployPath

foreach ($file in $files) {
    Write-Host "  上传: $($file.Name)" -ForegroundColor Gray
    $localFile = $file.FullName
    $remoteFile = "$PROJECT_DIR/$($file.Name)"
    
    scp $localFile "${SERVER_USER}@${SERVER_IP}:$remoteFile"
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ 上传失败: $($file.Name)" -ForegroundColor Red
        exit 1
    }
}

Write-Host "✓ 文件上传成功" -ForegroundColor Green
Write-Host ""

# 步骤 4: 在服务器上执行部署脚本
Write-Host "🐳 步骤 4: 在服务器上执行部署..." -ForegroundColor Yellow
Write-Host "这将在服务器上安装 Docker、Docker Compose 并启动服务" -ForegroundColor Cyan
Write-Host ""

$continue = Read-Host "是否继续? (y/n)"
if ($continue -ne 'y' -and $continue -ne 'Y') {
    Write-Host "部署已取消" -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "在服务器上执行部署脚本..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

ssh "${SERVER_USER}@${SERVER_IP}" "cd $PROJECT_DIR && chmod +x setup-server.sh && ./setup-server.sh"

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 服务器部署失败" -ForegroundColor Red
    exit 1
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✓ 服务器部署完成" -ForegroundColor Green
Write-Host ""

# 步骤 5: 清理本地临时文件
Write-Host "🧹 步骤 5: 清理临时文件..." -ForegroundColor Yellow

Remove-Item -Path $deployPath -Recurse -Force
Write-Host "✓ 临时文件清理完成" -ForegroundColor Green
Write-Host ""

# 部署完成
Write-Host "🎉 部署完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "访问地址:" -ForegroundColor Cyan
Write-Host "  - 应用: http://${SERVER_IP}:48080" -ForegroundColor Cyan
Write-Host "  - Nginx: http://${SERVER_IP}" -ForegroundColor Cyan
Write-Host ""
Write-Host "管理命令:" -ForegroundColor Cyan
Write-Host "  查看日志:   ssh ${SERVER_USER}@${SERVER_IP} 'cd $PROJECT_DIR && docker-compose logs -f app'" -ForegroundColor Cyan
Write-Host "  重启服务:   ssh ${SERVER_USER}@${SERVER_IP} 'cd $PROJECT_DIR && docker-compose restart'" -ForegroundColor Cyan
Write-Host "  停止服务:   ssh ${SERVER_USER}@${SERVER_IP} 'cd $PROJECT_DIR && docker-compose down'" -ForegroundColor Cyan
Write-Host ""
Write-Host "⚠️  重要提醒:" -ForegroundColor Yellow
Write-Host "1. 请确保阿里云安全组已开放 80, 443, 48080 端口" -ForegroundColor Yellow
Write-Host "2. 建议配置 SSL 证书启用 HTTPS" -ForegroundColor Yellow
Write-Host "3. 定期备份数据库数据" -ForegroundColor Yellow
Write-Host ""

# 询问是否测试连接
$testConnection = Read-Host "是否测试应用连接? (y/n)"
if ($testConnection -eq 'y' -or $testConnection -eq 'Y') {
    Write-Host ""
    Write-Host "测试应用连接..." -ForegroundColor Cyan
    try {
        $response = Invoke-WebRequest -Uri "http://${SERVER_IP}:48080" -Method GET -TimeoutSec 10 -ErrorAction SilentlyContinue
        Write-Host "✓ 应用响应正常 (状态码: $($response.StatusCode))" -ForegroundColor Green
    } catch {
        Write-Host "⚠️  应用可能还在启动中，请稍后手动测试" -ForegroundColor Yellow
        Write-Host "   访问: http://${SERVER_IP}:48080" -ForegroundColor Cyan
    }
}

Write-Host ""
Write-Host "部署流程完成！" -ForegroundColor Green
