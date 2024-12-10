FROM namely/protoc-all:1.51_0 AS proto-builder
WORKDIR /proto
COPY ./src/main/proto/ .
RUN for file in *.proto; do \
      protoc --java_out=gen \
             --grpc-java_out=gen \
             --proto_path=/proto \
             $file; \
    done

FROM maven:3.8.8-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml ./
COPY src ./src/
COPY --from=proto-builder /proto/gen ./src/main/java/
RUN mvn clean install -DskipTests
