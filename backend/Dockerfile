# ==========================================
# Multi-stage Dockerfile for Spring Boot
# ==========================================

# Stage 1: Build stage with Maven and OpenJDK 21
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Cache dependency layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production jar
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal Runtime stage with JRE 21
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Security: Create non-root user
RUN groupadd -r cpts && useradd -r -g cpts cpts

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar
RUN chown -R cpts:cpts /app

USER cpts

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
