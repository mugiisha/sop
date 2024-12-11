 FROM maven:3.8.8-eclipse-temurin-21 AS builder
 WORKDIR /build
 COPY pom.xml ./
 COPY src ./src
 COPY libs ./libs

 RUN mvn install:install-file \
     -Dfile=libs/proto-1.0-SNAPSHOT.jar \
     -DgroupId=com.at \
     -DartifactId=proto \
     -Dversion=1.0-SNAPSHOT \
     -Dpackaging=jar \
     -DSkipTests

 RUN mvn clean package -DskipTests -q

 FROM eclipse-temurin:21-jre
 WORKDIR /app
 COPY --from=builder /build/target/*.jar /app/app.jar
 EXPOSE 8081
 ENTRYPOINT ["java", "-jar", "/app/app.jar"]