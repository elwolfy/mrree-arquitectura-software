# Guía de Contribución para Docentes y Colaboradores

Este documento establece el flujo de trabajo y las normas de desarrollo para los docentes y colaboradores que mantienen y amplían el repositorio `mrree-arquitectura-software`.

---

## 1. Modelo de trabajo y ramas

- La rama `main` está **protegida**. Todo cambio debe ingresar exclusivamente mediante un **Pull Request (PR)** revisado y aprobado.
- `main` contiene la aplicación base completa y funcional, pero **sin** las implementaciones correspondientes a las asignaciones prácticas A1 a A4.
- Las soluciones de las asignaciones se desarrollan en ramas independientes que parten de `main`:
  - `solucion/a1`: Reprogramación de citas (M1).
  - `solucion/a2`: Eventos y observación de trámites (M2).
  - `solucion/a3`: Compensación de saga ante fallo de emisión (M3).
  - `solucion/a4`: Caché Redis en consulta de trámites (M3).
- Las ramas de solución **no se encadenan** entre sí; cada una nace y se fusiona o rebasea contra `main`.

---

## 2. Flujo de Pull Request hacia `main`

1. **Crear rama de trabajo:**
   ```bash
   git checkout main
   git pull origin main
   git checkout -b chore/descripcion-del-cambio
   ```
2. **Convención de commits:**
   Se utiliza el estándar Conventional Commits con descripciones claras en **español**:
   - `feat(m1): implementar bloqueo pesimista en reserva de cupos`
   - `fix(m2): corregir mapeo de estados en adaptador gRPC`
   - `docs: actualizar diagrama C4 de contenedores`
   - `test(m3): agregar prueba ArchUnit para puertos de persistencia`
   - `chore: actualizar dependencias en POM agregador de m4`
3. **Verificación local previa al PR:**
   - Todo servicio backend debe compilar y pasar las pruebas ejecutadas en contenedor:
     ```bash
     docker run --rm -v "$PWD/<modulo>":/w -w /w maven:3.9-eclipse-temurin-21 mvn -q verify
     ```
   - Las pruebas de arquitectura (ArchUnit) y Spring Modulith deben estar en verde.
   - Si se realizaron cambios en Docker Compose, levantar el entorno completo y comprobar que todos los servicios pasen a estado `healthy`.
4. **Creación del PR:**
   - Completar todos los puntos de la plantilla [`.github/pull_request_template.md`](.github/pull_request_template.md).
   - Adjuntar evidencia de ejecución o pruebas de integración exitosas.

---

## 3. Normas arquitectónicas y de calidad

1. **Fuente de verdad:** Toda API, esquema, evento o puerto debe respetar estrictamente `docs/especificacion/contrato-global.md`.
2. **Arquitectura Hexagonal:** Salvo el servicio didáctico `m1-estilos/comparativa-clean` (que utiliza Clean Architecture), todos los servicios adoptan arquitectura hexagonal (`domain`, `application.port.{in,out}`, `application.service`, `adapter.{in,out}`).
3. **Reglas ArchUnit inquebrantables:** Bajo ninguna circunstancia se debe relajar o comentar una prueba de arquitectura para permitir que el pipeline pase.
4. **Comentarios didácticos:** Las clases principales que introducen conceptos teóricos deben incluir Javadoc con las etiquetas:
   ```java
   /**
    * Descripción clara y concisa de la clase.
    * <p>Concepto: <nombre del concepto de arquitectura> (sección X.Y del plan).
    * <p>Sesión: SX.
    */
   ```
5. **Idioma:** Código de dominio, entidades, comentarios, excepciones y mensajes de log en español.
6. **Datos de prueba:** Utilizar únicamente datos ficticios de laboratorio (DNI `40000001`–`40000050`, oficinas `MAD`, `BUE`, etc.). Nunca emplear datos reales ni credenciales de producción.
