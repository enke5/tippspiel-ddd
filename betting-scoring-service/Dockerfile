FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY betting-scoring-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
