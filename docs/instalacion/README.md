# Guía de Instalación y Configuración del Entorno

Esta guía detalla los pasos requeridos para preparar el entorno de desarrollo y ejecución de la **Plataforma Digital de Servicios Consulares y de Legalización**.

---

## 1. Requisitos de hardware y software

- **Sistema Operativo:** Windows 10/11 de 64 bits (Professional, Enterprise o Home con WSL 2), macOS o Linux.
- **Memoria RAM:** Mínimo 8 GB (16 GB recomendado).
- **Almacenamiento:** Mínimo 15 GB de espacio libre en disco.
- **Docker Desktop:** Versión reciente con el motor WSL 2 activado en Windows.
- **Git:** Versión 2.40 o superior.
- **Java Development Kit:** JDK 21 (Eclipse Temurin recomendado).
- **IDE:** Spring Tools 4 for Eclipse (STS), IntelliJ IDEA o Visual Studio Code.

> **Importante:** No es indispensable contar con Maven ni Node.js/npm instalados localmente en su máquina host. Toda la construcción y ejecución de aplicaciones se realiza dentro de contenedores Docker mediante imágenes oficiales (`maven:3.9-eclipse-temurin-21` y `node:22-alpine`).

---

## 2. Configuración de Docker Desktop en Windows

1. Asegúrese de que la virtualización por hardware esté habilitada en la BIOS/UEFI de su equipo.
2. En la configuración de Docker Desktop:
   - Activar la opción **Use the WSL 2 based engine**.
   - En *Resources > Advanced*, asignar al menos **4 núcleos de CPU** y **6 GB de RAM** (o configurar en su archivo `.wslconfig`).
3. Verifique el funcionamiento abriendo una terminal de PowerShell y ejecutando:
   ```powershell
   docker --version
   docker compose version
   ```

---

## 3. Modo de ejecución Híbrido (Desarrollo en Eclipse + Docker)

Para depurar servicios Java desde Eclipse:
1. Levante la infraestructura común del módulo con Docker Compose (por ejemplo, PostgreSQL, RabbitMQ, Mailpit):
   ```bash
   cd <modulo>
   docker compose up -d postgres mailpit
   ```
2. En Eclipse, importe el servicio como proyecto Maven existente (*File > Import > Existing Maven Projects*).
3. Ejecute la clase principal (`*Application.java`) con el perfil de Spring Boot `local`:
   - *Run As > Run Configurations > Spring Boot App*
   - Active Profile: `local`
4. Esto conectará el servicio a `localhost` utilizando los puertos expuestos del contenedor según lo definido en `application-local.yml`.
