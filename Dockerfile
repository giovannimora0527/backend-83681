FROM maven:3.10.0-eclipse-temurin-21-noble AS build

WORKDIR /workspace
COPY pom.xml ./
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21.0.13_9-jre-noble AS runtime

WORKDIR /app
COPY --from=build /workspace/target/clinica-0.0.1-SNAPSHOT.jar /app/app.jar
RUN chown -R 10001:10001 /app
USER 10001:10001

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
