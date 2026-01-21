# Use Java 21 JDK Alpine image
FROM amazoncorretto:21-alpine-jdk

# Set working directory inside container
WORKDIR /app

# Copy your built jar into the container
COPY target/product-service-0.0.1-SNAPSHOT.jar product-service.jar

# Expose the port your app runs on
EXPOSE 8083

# Command to run the jar
ENTRYPOINT ["java","-jar","product-service.jar"]
