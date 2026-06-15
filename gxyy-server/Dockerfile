# ============ 构建阶段 ============
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# 配置 Maven 国内镜像
COPY settings.xml /root/.m2/settings.xml

# 复制源码
COPY pom.xml .
COPY src ./src

# 编译打包（跳过测试）
RUN mvn package -DskipTests -B -s /root/.m2/settings.xml

# ============ 运行阶段 ============
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
# 创建上传目录
RUN mkdir -p /app/uploads

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
