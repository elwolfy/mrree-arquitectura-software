# ==============================================================================
# Dockerfile — Funciones Serverless de Citas M1 (consular-m1-funciones-citas)
# Regla 03: Multi-etapa con Maven Temurin 21 -> JRE Alpine 21
# Contexto de construcción: carpeta m1-estilos/
# ==============================================================================

# Etapa 1: Compilación
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY monolito-modular/pom.xml monolito-modular/
COPY comparativa-clean/pom.xml comparativa-clean/
COPY microservicios/citas-service/pom.xml microservicios/citas-service/
COPY microservicios/notificaciones-service/pom.xml microservicios/notificaciones-service/
COPY serverless/funciones-citas/pom.xml serverless/funciones-citas/
RUN --mount=type=cache,target=/root/.m2 mvn -q -pl serverless/funciones-citas -am dependency:go-offline
COPY serverless/funciones-citas/src serverless/funciones-citas/src
RUN --mount=type=cache,target=/root/.m2 mvn -q -pl serverless/funciones-citas -am package -DskipTests

# Etapa 2: Ejecución ligera
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /src/serverless/funciones-citas/target/*.jar app.jar
USER app
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q UP || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]

