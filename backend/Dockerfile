# ETAPA 1: Build con Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar primero el pom.xml para cachear dependencias
COPY pom.xml .

# Descargar dependencias (se cachea si no cambia el pom.xml)
RUN mvn dependency:go-offline -B

# Copiar el codigo fuente
COPY src ./src

# Compilar y empaquetar (sin ejecutar tests para ir rapido)
RUN mvn clean package -DskipTests

# ETAPA 2: Runtime liviano (solo JRE)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
