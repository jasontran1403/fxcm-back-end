FROM openjdk:17-ea-33-jdk-slim-buster

WORKDIR /app
COPY ./target/fxcmholdings-1.0.jar /app

CMD ["java", "-jar", "fxcmholdings-1.0.jar"]
