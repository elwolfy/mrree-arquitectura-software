package pe.utec.consular.clean;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.DisplayName;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Pruebas automatizadas de arquitectura con ArchUnit para validar la Regla de Dependencia Concéntrica
 * de Clean Architecture (Robert C. Martin).
 * <p>Concepto: Clean Architecture – Regla de Dependencia Concéntrica y gobernanza en CI (secciones 1.3 y 4.4 del plan).
 * <ul>
 *   <li>{@code entities} (Círculo 1): no depende de nadie (solo {@code java..} y {@code ..entities..}).</li>
 *   <li>{@code usecase} (Círculo 2): depende solo de {@code entities} (sin Spring ni JPA).</li>
 *   <li>{@code interfaceadapters} (Círculo 3): depende solo de {@code usecase} y {@code entities}.</li>
 *   <li>{@code frameworks} (Círculo 4): puede depender de todos los círculos internos.</li>
 * </ul>
 * <p>Sesión: S2. Cada regla indica cómo romperla a propósito en clase para observar el fallo.
 */
@AnalyzeClasses(packages = "pe.utec.consular.clean", importOptions = ImportOption.DoNotIncludeTests.class)
@DisplayName("Reglas de Clean Architecture y Dependencia Concéntrica (ArchUnit)")
class ReglasDeArquitecturaTest {

    // Cómo romperla en clase (S2): Añadir "import jakarta.persistence.Entity;" en entities/Cita.java
    @ArchTest
    static final ArchRule entities_no_depende_de_nadie = classes()
            .that().resideInAPackage("..entities..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("java..", "..entities..")
            .andShould(noImportarFrameworksEnCodigoFuente("entities"))
            .as("El círculo 'entities' (Reglas Empresariales) no debe depender de ninguna otra capa ni de frameworks como JPA o Spring");

    // Cómo romperla en clase (S2): Añadir @Service o importar CitaPresenter en RegistrarCitaInteractor.java
    @ArchTest
    static final ArchRule usecase_depende_solo_de_entities = classes()
            .that().resideInAPackage("..usecase..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("java..", "..entities..", "..usecase..")
            .andShould(noImportarFrameworksEnCodigoFuente("usecase"))
            .as("El círculo 'usecase' (Casos de Uso) solo debe depender de 'entities' y no debe usar anotaciones ni clases de Spring o JPA");

    // Cómo romperla en clase (S2): Importar CitaJpaEntity desde CitaController.java o CitaJpaGateway.java
    @ArchTest
    static final ArchRule interfaceadapters_depende_solo_de_usecase_y_entities = noClasses()
            .that().resideInAPackage("..interfaceadapters..")
            .should().dependOnClassesThat()
            .resideInAPackage("..frameworks..")
            .as("El círculo 'interfaceadapters' solo debe depender hacia adentro ('usecase' y 'entities'), nunca hacia 'frameworks'");

    // Regla concéntrica global por capas de Clean Architecture
    @ArchTest
    static final ArchRule regla_de_dependencia_concentrica_clean_architecture = Architectures.layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Entities").definedBy("..entities..")
            .layer("UseCases").definedBy("..usecase..")
            .layer("InterfaceAdapters").definedBy("..interfaceadapters..")
            .layer("Frameworks").definedBy("..frameworks..")
            .whereLayer("Entities").mayNotAccessAnyLayer()
            .whereLayer("Entities").mayOnlyBeAccessedByLayers("UseCases", "InterfaceAdapters", "Frameworks")
            .whereLayer("UseCases").mayOnlyAccessLayers("Entities")
            .whereLayer("UseCases").mayOnlyBeAccessedByLayers("InterfaceAdapters", "Frameworks")
            .whereLayer("InterfaceAdapters").mayOnlyAccessLayers("UseCases", "Entities")
            .whereLayer("InterfaceAdapters").mayOnlyBeAccessedByLayers("Frameworks")
            .whereLayer("Frameworks").mayOnlyAccessLayers("InterfaceAdapters", "UseCases", "Entities")
            .as("Las dependencias en Clean Architecture deben apuntar estrictamente hacia adentro: frameworks -> interfaceadapters -> usecase -> entities");

    // Cómo romperla en clase (S2): Crear un @RestController fuera de interfaceadapters.controller
    @ArchTest
    static final ArchRule controladores_residen_en_interfaceadapters_controller = classes()
            .that().areAnnotatedWith(RestController.class)
            .or().areAnnotatedWith(Controller.class)
            .should().resideInAPackage("..interfaceadapters.controller..")
            .andShould().haveSimpleNameEndingWith("Controller")
            .as("Los controladores REST deben residir en 'interfaceadapters.controller' y terminar en 'Controller'");

    private static ArchCondition<JavaClass> noImportarFrameworksEnCodigoFuente(String nombreCirculo) {
        return new ArchCondition<>("no contener imports de jakarta.persistence ni org.springframework en el código fuente del círculo '" + nombreCirculo + "'") {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                if (item.getEnclosingClass().isPresent()) {
                    return;
                }
                String relativePath = item.getPackageName().replace('.', '/') + "/" + item.getSimpleName() + ".java";
                List<Path> candidatos = List.of(
                        Path.of("src/main/java", relativePath),
                        Path.of("comparativa-clean/src/main/java", relativePath)
                );
                for (Path ruta : candidatos) {
                    if (Files.exists(ruta)) {
                        try {
                            List<String> lineas = Files.readAllLines(ruta);
                            for (int i = 0; i < lineas.size(); i++) {
                                String linea = lineas.get(i).trim();
                                if (linea.startsWith("import jakarta.persistence.")
                                        || linea.startsWith("import org.springframework.")) {
                                    String mensaje = String.format(
                                            "Violación de Clean Architecture en %s (línea %d): el círculo '%s' no puede importar frameworks externos ('%s')",
                                            item.getName(), i + 1, nombreCirculo, linea);
                                    events.add(SimpleConditionEvent.violated(item, mensaje));
                                }
                            }
                        } catch (IOException ignored) {
                            // Si no se puede leer el archivo fuente, la validación de bytecode de ArchUnit sigue activa
                        }
                        break;
                    }
                }
            }
        };
    }
}

