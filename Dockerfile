#FROM eclipse-temurin:21-jdk
#COPY target/*.jar app.jar
#ENTRYPOINT ["java", "-jar", "/app.jar"]

# Stage 1: Build with Maven and Java 21
FROM maven:3.9.6-eclipse-temurin-21 as build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run with Java 21 JDK
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "Vehicle-Reservation-Producer-0.0.1-SNAPSHOT.jar"]

