# ==============================================================================
# Dockerfile — Microservicio emision-service (M3)
# Build multi-etapa (no requiere Maven instalado en el equipo host)
# Contexto de construcción: carpeta m3-datos/
# ==============================================================================

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY citas-service/pom.xml citas-service/
COPY emision-service/pom.xml emision-service/
RUN --mount=type=cache,target=/root/.m2 mvn -q -pl emision-service -am dependency:go-offline
COPY emision-service/src emision-service/src
RUN --mount=type=cache,target=/root/.m2 mvn -q -pl emision-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /src/emision-service/target/*.jar app.jar
USER app
EXPOSE 8087
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=5 \
  CMD wget -qO- http://localhost:8087/actuator/health | grep -q UP || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]

