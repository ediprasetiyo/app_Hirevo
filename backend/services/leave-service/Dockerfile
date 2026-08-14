FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -ntp -pl services/leave-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app && apk add --no-cache wget
WORKDIR /app
COPY --from=builder /build/services/leave-service/target/*.jar app.jar
USER app
EXPOSE 8084
HEALTHCHECK --interval=15s --timeout=3s CMD wget -qO- http://localhost:8084/actuator/health || exit 1
ENV JAVA_OPTS="-XX:+UseZGC -XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
