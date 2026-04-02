# Build stage
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app

# Copy the pom.xml from chat-server to the current workdir
COPY chat-server/pom.xml .

# Copy the source code from chat-server/src to the current workdir/src
COPY chat-server/src ./src

# Build the application
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the backend port
EXPOSE 8081

# Run the backend
ENTRYPOINT ["java", "-jar", "app.jar"]
