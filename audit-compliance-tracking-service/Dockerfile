  # Build stage
  FROM maven:3.8.8-eclipse-temurin-21 AS builder
  WORKDIR /build

  # Copy files needed for build
  COPY pom.xml ./
  COPY src ./src
  COPY libs ./libs

  # Install local dependency and build application
  RUN mvn install:install-file \
      -Dfile=libs/proto-1.0-SNAPSHOT.jar \
      -DgroupId=com.at \
      -DartifactId=proto \
      -Dversion=1.0-SNAPSHOT \
      -Dpackaging=jar \
      -DskipTests \
      && mvn clean package -DskipTests -q

  # Runtime stage
  FROM eclipse-temurin:21-jre
  WORKDIR /app
  COPY --from=builder /build/target/*.jar app.jar
  EXPOSE 8088
  ENTRYPOINT ["java", "-jar", "app.jar"]