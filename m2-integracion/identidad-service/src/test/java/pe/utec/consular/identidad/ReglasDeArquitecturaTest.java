package pe.utec.consular.identidad;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Pruebas de arquitectura con ArchUnit para verificar el aislamiento del dominio
 * y de la Capa Anticorrupción (ACL) en identidad-service.
 */
@AnalyzeClasses(packages = "pe.utec.consular.identidad", importOptions = ImportOption.DoNotIncludeTests.class)
@DisplayName("Reglas de Arquitectura Hexagonal y Capa Anticorrupción en identidad-service (ArchUnit)")
class ReglasDeArquitecturaTest {

    @ArchTest
    static final ArchRule dominio_y_aplicacion_no_usan_tipos_de_pide = noClasses()
            .that().resideInAnyPackage("..identidad.domain..", "..identidad.application..")
            .should().dependOnClassesThat()
            .resideInAPackage("..identidad.adapter.out.pide..")
            .as("La Capa Anticorrupción (ACL) en adapter.out.pide no debe filtrarse hacia domain ni application");

    @ArchTest
    static final ArchRule dominio_no_depende_de_spring_ni_adaptadores = classes()
            .that().resideInAPackage("..identidad.domain..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                    "java..",
                    "..identidad.domain.."
            )
            .as("El dominio de identidad debe permanecer puro y sin dependencias de Spring o adaptadores");

    @ArchTest
    static final ArchRule aplicacion_no_depende_de_adaptadores = noClasses()
            .that().resideInAPackage("..identidad.application..")
            .should().dependOnClassesThat()
            .resideInAPackage("..identidad.adapter..")
            .as("Los casos de uso de identidad deben comunicarse con infraestructura únicamente mediante puertos");
}

