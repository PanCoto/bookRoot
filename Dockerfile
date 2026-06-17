
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

COPY pom.xml .
RUN apk add --no-cache maven && \
    mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S bookroot && adduser -S bookroot -G bookroot

COPY --from=builder /app/target/*.jar app.jar

RUN chown bookroot:bookroot app.jar

USER bookroot

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]