FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="vcall-team"
LABEL description="Video Service - 360 Video Processing"

RUN apk add --no-cache \
    ffmpeg \
    ffprobe \
    curl

RUN addgroup -S vcall && adduser -S vcall -G vcall

WORKDIR /app

COPY target/video-service.jar app.jar

RUN chown -R vcall:vcall /app

USER vcall

EXPOSE 8123

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8123/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
