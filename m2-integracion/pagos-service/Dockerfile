# ==============================================================================
# Dockerfile — Microservicio pagos-service (M2)
# Regla 03: Multi-etapa con Maven Temurin 21 -> JRE Alpine 21
# Contexto de construcción: carpeta m2-integracion/
# ==============================================================================

# Etapa 1: Compilación
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY citas-service/pom.xml citas-service/
COPY pagos-service/pom.xml pagos-service/
RUN --mount=type=cache,target=/root/.m2 mvn -q -pl pagos-service -am dependency:go-offline
COPY pagos-service/src pagos-service/src
RUN --mount=type=cache,target=/root/.m2 mvn -q -pl pagos-service -am package -DskipTests

# Etapa 2: Ejecución ligera
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /src/pagos-service/target/*.jar app.jar
USER app
EXPOSE 8083
HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=5 \
  CMD wget -qO- http://localhost:8083/actuator/health | grep -q UP || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]

