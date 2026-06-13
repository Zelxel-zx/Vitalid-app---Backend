FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Cache de dependencias: copiamos solo el pom primero
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Ahora copiamos el código y empaquetamos
COPY src ./src
RUN find /workspace/src -name "*.java" -exec sed -i '1s/^\xef\xbb\xbf//' {} \;
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]