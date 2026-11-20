package pe.utec.consular.citas;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Pruebas de arquitectura automatizadas mediante ArchUnit para validar las capas hexagonales de citas-service.
 * <p>Concepto: Gobernanza de arquitectura, verificación en CI y reglas hexagonales (secciones 1.3 y 4.4).
 * <p>Sesión: S3.
 */
@AnalyzeClasses(packages = "pe.utec.consular.citas", importOptions = ImportOption.DoNotIncludeTests.class)
@DisplayName("Reglas de Arquitectura Hexagonal en citas-service (ArchUnit)")
class ReglasDeArquitecturaTest {

    @ArchTest
    static final ArchRule dominio_no_depende_de_infraestructura = classes()
            .that().resideInAPackage("..citas.domain..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                    "java..",
                    "..citas.domain.."
            )
            .as("El dominio de citas no debe acoplarse a JPA, Spring, RabbitMQ ni adaptadores externos");

    @ArchTest
    static final ArchRule aplicacion_no_depende_de_adaptadores = noClasses()
            .that().resideInAPackage("..citas.application..")
            .should().dependOnClassesThat()
            .resideInAPackage("..citas.adapter..")
            .as("Los casos de uso y servicios de aplicación deben comunicarse con infraestructura solo mediante puertos");

    @ArchTest
    static final ArchRule convencion_nombres_puertos_entrada = classes()
            .that().resideInAPackage("..port.in..")
            .should().haveSimpleNameEndingWith("UseCase")
            .orShould().haveSimpleNameEndingWith("Command")
            .as("Las interfaces primarias de casos de uso deben seguir el estándar <Verbo><Sustantivo>UseCase");

    @ArchTest
    static final ArchRule convencion_nombres_puertos_salida = classes()
            .that().resideInAPackage("..port.out..")
            .should().haveSimpleNameEndingWith("Port")
            .as("Los contratos de salida hacia infraestructura deben terminar en Port");

    @ArchTest
    static final ArchRule controladores_deben_residir_en_adapter_web = classes()
            .that().areAnnotatedWith(RestController.class)
            .or().areAnnotatedWith(Controller.class)
            .should().resideInAPackage("..adapter.in.web..")
            .andShould().haveSimpleNameEndingWith("Controller")
            .as("Los controladores web deben ser adaptadores primarios en el paquete adapter.in.web");

    @ArchTest
    static final ArchRule controladores_no_exponen_entidades_jpa = noClasses()
            .that().resideInAPackage("..adapter.in.web..")
            .should().dependOnClassesThat()
            .resideInAPackage("..adapter.out.persistence..")
            .as("La capa web no debe acoplarse a las entidades JPA de persistencia; debe usar DTO records");
}

