# 后端 Dockerfile - 多阶段构建

# 构建阶段
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# 复制 pom.xml 和 Maven 配置
COPY pom.xml .
COPY settings.xml /root/.m2/settings.xml

# 下载依赖
RUN mvn dependency:go-offline -B -s /root/.m2/settings.xml

# 复制源代码并构建
COPY src ./src
RUN mvn package -DskipTests -B -s /root/.m2/settings.xml

# 运行阶段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 创建日志目录
RUN mkdir -p /app/logs

# 从构建阶段复制 jar 包
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8000

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8000/api/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
