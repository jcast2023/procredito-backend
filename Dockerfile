# ============================================================
# ETAPA 1: Compilar la aplicación con Maven
# ============================================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copiar primero el pom.xml para cachear dependencias de Maven
COPY pom.xml .

# Descargar todas las dependencias (se cachea si no cambia el pom.xml)
RUN mvn dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Compilar y empaquetar (sin ejecutar tests para ir rápido)
RUN mvn clean package -DskipTests

# ============================================================
# ETAPA 2: Runtime liviano (solo JRE, sin Maven ni código fuente)
# ============================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar solo el JAR compilado desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto (documental, no abre nada por sí solo)
EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]
