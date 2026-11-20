FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests package && mv target/clinica-*.jar /build/clinica.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/clinica.jar ./clinica.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "clinica.jar"]
