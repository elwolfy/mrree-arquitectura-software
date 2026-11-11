# ==============================================================================
# Dockerfile — Monolito Modular Hexagonal (consular-monolito)
# Regla 03: Multi-etapa con Maven Temurin 21 -> JRE Alpine 21
# Contexto de construcción: carpeta m1-estilos/
# ==============================================================================

# Etapa 1: Compilación
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY monolito-modular/pom.xml monolito-modular/
COPY comparativa-clean/pom.xml comparativa-clean/
RUN --mount=type=cache,target=/root/.m2 mvn -q -pl monolito-modular -am dependency:go-offline
COPY monolito-modular/src monolito-modular/src
RUN --mount=type=cache,target=/root/.m2 mvn -q -pl monolito-modular -am package -DskipTests

# Etapa 2: Ejecución ligera
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /src/monolito-modular/target/*.jar app.jar
USER app
EXPOSE 8081
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=5 \
  CMD wget -qO- http://localhost:8081/actuator/health | grep -q UP || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
