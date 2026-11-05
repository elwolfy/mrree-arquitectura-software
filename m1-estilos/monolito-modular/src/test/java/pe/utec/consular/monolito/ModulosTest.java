package pe.utec.consular.monolito;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Prueba de verificación de módulos con Spring Modulith.
 * Asegura que los módulos mantengan límites claros y no violen el encapsulamiento.
 * <p>Concepto: Monolito modular y diseño guiado por el dominio (sección 1.2 y 2.4).
 * <p>Sesión: S1 y S2.
 */
@DisplayName("Verificación modular con Spring Modulith (ModulosTest)")
class ModulosTest {

    private final ApplicationModules modules = ApplicationModules.of(MonolitoApplication.class);

    @Test
    @DisplayName("Verifica que no existan ciclos ni dependencias indebidas entre módulos")
    void verificarModulos() {
        modules.verify();
    }

    @Test
    @DisplayName("Genera diagramas de arquitectura de módulos en target/spring-modulith-docs")
    void generarDocumentacion() {
        new Documenter(modules).writeDocumentation();
    }
}
