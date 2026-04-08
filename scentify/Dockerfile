# Use Java 21
FROM amazoncorretto:21-alpine-jdk

# Set working directory inside container
WORKDIR /app

# Copy your built JAR file into container
COPY target/scentify-0.0.1-SNAPSHOT.jar app.jar

# When container starts, run this command
ENTRYPOINT ["java", "-jar", "app.jar"]

# Expose port 8080
EXPOSE 8080
