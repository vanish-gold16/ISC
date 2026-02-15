FROM openjdk:21.0.1-jdk-slim
WORKDIR /app
// TODO COPY
ENTRYPOINT ["java", "-jar", "phrases.jar"]