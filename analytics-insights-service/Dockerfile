 # Build stage
 FROM eclipse-temurin:21 AS builder
 WORKDIR /build
 COPY . .
 RUN ./mvnw clean package -DskipTests

 # Run stage
 FROM eclipse-temurin:21-jre-alpine
 WORKDIR /app
 COPY --from=builder /build/target/*.jar app.jar
 EXPOSE 8080
 ENTRYPOINT ["java", "-jar", "app.jar"]

 # Health check
 HEALTHCHECK --interval=30s --timeout=3s \
   CMD wget -q --spider http://localhost:8080/actuator/health || exit 1
