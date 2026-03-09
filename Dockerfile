# Stage 1: Build the JAR
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run the Application
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8085
# Force the port binding for Render
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=${PORT:8085}"]