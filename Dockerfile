# Stage 1: Build stage
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app

# Step 1: Copy only the pom.xml first
COPY pom.xml .

# Step 2: Pre-download dependencies. 
# This layer will be cached and skipped unless you change pom.xml
RUN mvn dependency:go-offline -B

# Step 3: Now copy the source code and build
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Final Run stage
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Standard Port Binding for Cloud Providers
ENV PORT=8085
EXPOSE 8085

# Correctly pass the port as a System Property
ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]