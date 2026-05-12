FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY tournament-management-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
