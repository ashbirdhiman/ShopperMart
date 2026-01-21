# Use Java 17 JDK slim image
FROM amazoncorretto:21-alpine-jdk

# Set working directory inside container
WORKDIR /app

# Copy your built jar into the container
COPY target/eureka-server-0.0.1-SNAPSHOT.jar eureka-server.jar

# Expose the port your app runs on
EXPOSE 8080

# Command to run the jar file
ENTRYPOINT ["java", "-jar", "eureka-server.jar"]
