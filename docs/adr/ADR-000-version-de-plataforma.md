# ADR-000: Selección de versión de plataforma (Spring Boot 3.4.x)

## Estado

Aceptado

## Contexto

La sección 2 del contrato global (`docs/especificacion/contrato-global.md`) establece como objetivo inicial Spring Boot 4.0.x y define la siguiente regla explícita:

> *"Spring Boot 4.0.x (último parche estable). Si una dependencia requerida no es compatible, usar 3.5.x [o rama estable 3.4.x] en todo el repositorio y registrarlo en docs/adr/ADR-000-version-de-plataforma.md"*.

Al desplegar e integrar las herramientas del ecosistema requeridas por el curso:
1. `springdoc-openapi-starter-webmvc-ui` (OpenAPI 3 / Swagger UI) depende de clases internas de autoconfiguración servlet (`org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties`) que fueron reestructuradas en Spring Boot 4 / Spring Framework 7, generando `NoClassDefFoundError` al iniciar el contenedor.
2. Spring Modulith 2.x y Spring Cloud 2025.x presentan dependencias en evolución rápida sobre Spring Boot 4, mientras que Spring Modulith 1.3.x y Spring Cloud 2024.x ofrecen estabilidad probada para entornos de laboratorio y docencia universitaria.

## Decisión

Se adopta de forma uniforme en **todo el repositorio**:
- **Java**: 21 LTS (Eclipse Temurin).
- **Spring Boot**: 3.4.3 (línea 3.4.x / 3.x estable).
- **Spring Cloud**: 2024.0.0.
- **Spring Modulith**: 1.3.3 (`spring-modulith-bom`).
- **SpringDoc OpenAPI**: 2.8.5 (`springdoc-openapi-starter-webmvc-ui`).
- **Testcontainers**: 1.20.5.
- **Resilience4j**: 2.2.0.

## Consecuencias

- **Positivas**:
  - Compatibilidad completa entre Spring Boot, Spring Modulith, Springdoc Swagger UI, Hibernate 6.6 y PostgreSQL.
  - La interfaz Swagger UI responde de forma nativa en `/swagger-ui.html` sin excepciones de introspección de beans.
  - El entorno Docker y la ejecución híbrida en Eclipse funcionan sin fricción técnica para los estudiantes.
- **Negativas**:
  - Se difiere la adopción de Spring Framework 7 / Spring Boot 4 hasta que las librerías satélite (SpringDoc, gRPC Spring) publiquen versiones finales estables compatibles.
