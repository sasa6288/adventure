FROM maven:3.9.11-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S adventure && adduser -S adventure -G adventure \
    && mkdir -p /app/logs && chown -R adventure:adventure /app
WORKDIR /app
COPY --from=builder --chown=adventure:adventure /workspace/target/adventure-*.jar app.jar
USER adventure
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
