FROM maven:latest as builder

COPY src /src
COPY pom.xml /

RUN mvn clean package

FROM eclipse-temurin:25

COPY --from=builder /target/apksec-quickscan-*.jar /asqs.jar

ENTRYPOINT ["java", "-jar", "/asqs.jar"]