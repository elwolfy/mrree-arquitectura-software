---
description: Estrategia de pruebas y criterios de calidad
activation: always
---

# Pruebas y calidad

## Pirámide mínima por servicio

1. **Unitarias de dominio** (JUnit 5, sin Spring): reglas de negocio — capacidad del cupo, cita duplicada, cálculo de tarifa, transiciones de estado del trámite.
2. **Pruebas de arquitectura** (ArchUnit): reglas de dependencia hexagonal o clean según el proyecto; nombres de clases por paquete (`*UseCase` en `port.in`, `*Port` en `port.out`, `*Controller` en `adapter.in.web`); el dominio no usa Spring ni JPA.
3. **Spring Modulith** (solo monolito): `ApplicationModules.of(MonolitoApplication.class).verify()` y generación de documentación de módulos en `target/spring-modulith-docs`.
4. **Integración** (Testcontainers con PostgreSQL y, en M2+, RabbitMQ): un caso feliz por endpoint principal y los casos de error del contrato.
5. **Contrato de API**: prueba que obtiene `/v3/api-docs` y verifica que existen las rutas del contrato.

Las pruebas de Testcontainers se etiquetan `@Tag("integracion")` y se ejecutan en `mvn verify`; las unitarias y de arquitectura, en `mvn test`. El participante debe poder ejecutar `mvn test` desde Eclipse sin Docker en ejecución.

## Pruebas didácticas de arquitectura

En `m1-estilos/monolito-modular` y `comparativa-clean`, la clase `ReglasDeArquitecturaTest` contiene reglas con nombres legibles en español (`@DisplayName("El dominio no depende de JPA")`). Cada regla tiene un comentario que indica cómo romperla a propósito en clase (S2) para ver el fallo.

## Frontend

- Pruebas unitarias de servicios y componentes principales (formulario de cita, consulta).
- `npm run lint` sin errores.

## Criterios de "terminado" para cada prompt

Antes de cerrar la tarea, el agente debe:

1. Compilar con Docker (`mvn -q verify` en contenedor) sin errores ni pruebas omitidas.
2. Levantar el `docker-compose.yml` correspondiente y esperar a que todos los servicios estén `healthy`.
3. Ejecutar las verificaciones funcionales indicadas en el prompt (curl o navegador) y adjuntar la salida en el Walkthrough.
4. Revisar que no se introdujeron funcionalidades de asignaciones en `main`.
5. Actualizar el README del módulo si cambió algo que el participante deba saber.

## Calidad de código

- Sin advertencias de compilación relevantes; sin dependencias sin uso.
- Cobertura orientativa ≥ 70 % en `domain` y `application` (JaCoCo en el perfil `cobertura`), sin perseguir cobertura en adaptadores triviales.
- Perfil `calidad` en M4: SonarQube Community en compose (opcional, para la demostración de deuda técnica de S6).

## Integración continua

`.github/workflows/ci.yml`: en cada push y pull request, para cada carpeta de módulo, ejecutar `mvn -B verify` con JDK 21 y construir el frontend. Las pruebas ArchUnit forman parte del pipeline (gobernanza técnica, sección 4.4). El pipeline debe pasar en `main` y en cada `solucion/aN`.
