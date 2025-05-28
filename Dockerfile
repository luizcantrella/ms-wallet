FROM maven:3.9.9-amazoncorretto-21-alpine AS builder

WORKDIR /build

COPY pom.xml .
COPY ./src ./src

RUN mvn clean package -DskipTests

FROM amazoncorretto:21-alpine3.21

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]