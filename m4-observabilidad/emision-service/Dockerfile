# ==============================================================================
# Dockerfile — Microservicio emision-service (M4 - OpenTelemetry)
# Contexto de construcción: carpeta m4-observabilidad/
# ==============================================================================

# Etapa 1: Compilación y descarga del agente OpenTelemetry
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY citas-service/pom.xml citas-service/
COPY emision-service/pom.xml emision-service/
RUN --mount=type=cache,target=/root/.m2 mvn -q -pl emision-service -am dependency:go-offline
RUN --mount=type=cache,target=/root/.m2 mvn -q dependency:copy \
    -Dartifact=io.opentelemetry.javaagent:opentelemetry-javaagent:2.10.0:jar \
    -DoutputDirectory=/otel \
    -Dmdep.stripVersion=true
COPY emision-service/src emision-service/src
RUN --mount=type=cache,target=/root/.m2 mvn -q -pl emision-service -am package -DskipTests

# Etapa 2: Ejecución ligera con Agente OpenTelemetry
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /otel/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar
COPY --from=build /src/emision-service/target/*.jar app.jar
USER app
EXPOSE 8087
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=5 \
  CMD wget -qO- http://localhost:8087/actuator/health | grep -q UP || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
