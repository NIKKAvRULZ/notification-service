# Step 1: Use the official Eclipse Temurin JDK 17 on Alpine Linux for a lightweight container
FROM eclipse-temurin:17-jdk-alpine

# Step 2: Set the working directory inside the container
WORKDIR /app

# Step 3: Copy the JAR file from your target folder to the container
# Ensure you have run 'mvn clean package' on your local machine before pushing
COPY target/*.jar app.jar

# Step 4: Expose the port your Notification Service is configured to use
EXPOSE 8085

# Step 5: Execute the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]