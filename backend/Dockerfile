# Build stage: compiles the code using Maven wrapper
FROM openjdk:17-jdk-slim AS build
WORKDIR /app

# Copy all backend source files
COPY . .

# Grant execute permissions to mvnw and package the jar
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Run stage: lightweight runtime environment
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/portfolio-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose backend port
EXPOSE 8085

# Start command
ENTRYPOINT ["java", "-jar", "app.jar"]
