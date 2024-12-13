  # Stage 1: Build stage
  FROM maven:3.8.8-eclipse-temurin-21 AS builder
  WORKDIR /build
  COPY pom.xml ./
  COPY src ./src
  RUN mvn clean package -DskipTests -q

  # Stage 2: Runtime stage
  FROM eclipse-temurin:21
  WORKDIR /app
  COPY --from=builder /build/target/*.jar /app/app.jar
  EXPOSE 8089
  ENTRYPOINT ["java", "-jar", "app.jar"]