# Stage 1: Build using Maven
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run using Eclipse Temurin
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8085
# Ensure the app binds to the port Render provides
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=${PORT:8085}"]