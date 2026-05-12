FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Cache de dependencias: copiamos solo el pom primero
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Ahora copiamos el código y empaquetamos
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

# Copiamos el JAR generado por la etapa de build.
# Ajusta el patrón si tu artifactId/version generan otro nombre.
COPY --from=build /workspace/target/*.jar app.jar

# EXPOSE es informativo; Render inyecta el puerto real en la variable PORT
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
