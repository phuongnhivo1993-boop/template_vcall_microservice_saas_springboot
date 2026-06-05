FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="vcall-team"
LABEL description="Streaming Service - HLS/DASH Manifest and CDN Origin"

RUN apk add --no-cache curl

RUN addgroup -S vcall && adduser -S vcall -G vcall

WORKDIR /app

COPY target/streaming-service.jar app.jar

RUN chown -R vcall:vcall /app

USER vcall

EXPOSE 8124

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8124/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
