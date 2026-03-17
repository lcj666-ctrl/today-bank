FROM openjdk:8-jre-slim

WORKDIR /app

# 复制应用jar包
COPY target/ai-paint-backend-1.0-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 48080

# 启动应用（不依赖 .env 文件）
ENTRYPOINT ["java", "-jar", "app.jar"]
