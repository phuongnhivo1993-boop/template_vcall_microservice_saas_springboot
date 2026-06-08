FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S vcall && adduser -S vcall -G vcall
WORKDIR /app
COPY target/*.jar app.jar
RUN chown -R vcall:vcall /app
EXPOSE 8117
USER vcall
ENTRYPOINT ["java", "-jar", "app.jar"]
HEALTHCHECK --interval=30s --timeout=3s --retries=3 CMD wget -qO- http://localhost:8117/actuator/health || exit 1
