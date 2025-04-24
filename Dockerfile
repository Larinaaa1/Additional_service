FROM openjdk:21-jdk-slim
COPY --chown=appuser:appuser build /app/build
COPY --chown=appuser:appuser build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
