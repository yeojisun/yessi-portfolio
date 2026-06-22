# Build stage: compiles the code using Maven wrapper
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copy all backend source files
COPY . .

# Grant execute permissions to mvnw and package the jar
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Run stage: lightweight runtime environment
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/portfolio-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose backend port
EXPOSE 8085

# Start command
ENTRYPOINT ["java", "-jar", "app.jar"]
