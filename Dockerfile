# Build stage menggunakan Maven dan Java 21 untuk Render.com
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY backend/ .
RUN mvn clean package -DskipTests

# Run stage menggunakan OpenJDK 21 JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
