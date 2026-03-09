# Use a lightweight JDK 17 image as the base
FROM openjdk:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file from the target folder to the container
COPY target/*.jar app.jar

# Expose the port your service runs on
EXPOSE 8085

# Command to run the application with optimized memory settings
ENTRYPOINT ["java", "-jar", "app.jar"]