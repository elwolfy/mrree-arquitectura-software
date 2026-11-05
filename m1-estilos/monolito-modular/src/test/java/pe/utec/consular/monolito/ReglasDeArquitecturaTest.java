package pe.utec.consular.monolito;

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
 * Pruebas de arquitectura automatizadas mediante ArchUnit para validar la arquitectura hexagonal.
 * <p>Concepto: Gobernanza de arquitectura, verificación en CI y pruebas de arquitectura (sección 4.4).
 * <p>Sesión: S2. Cada regla incluye una instrucción didáctica sobre cómo romperla a propósito.
 */
@AnalyzeClasses(packages = "pe.utec.consular.monolito", importOptions = ImportOption.DoNotIncludeTests.class)
@DisplayName("Reglas de Arquitectura Hexagonal y Estándares de Diseño (ArchUnit)")
class ReglasDeArquitecturaTest {

    // Cómo romperla en clase: Añadir "import jakarta.persistence.Entity;" o @Autowired en Cita.java
    @ArchTest
    static final ArchRule dominio_no_depende_de_infraestructura = classes()
            .that().resideInAPackage("..citas.domain..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                    "java..",
                    "..citas.domain..",
                    "..compartido.domain.."
            )
            .as("El dominio de citas no debe acoplarse a JPA, Spring ni adaptadores externos");

    // Cómo romperla en clase: En RegistrarCitaService.java, importar directamente CitaJpaAdapter o CitaJpaEntity
    @ArchTest
    static final ArchRule aplicacion_no_depende_de_adaptadores = noClasses()
            .that().resideInAPackage("..citas.application..")
            .should().dependOnClassesThat()
            .resideInAPackage("..citas.adapter..")
            .as("Los casos de uso y servicios de aplicación deben comunicarse con infraestructura solo mediante puertos");

    // Cómo romperla en clase: Renombrar RegistrarCitaUseCase a RegistrarCitaManager o crear una clase sin sufijo UseCase en port.in
    @ArchTest
    static final ArchRule convencion_nombres_puertos_entrada = classes()
            .that().resideInAPackage("..port.in..")
            .should().haveSimpleNameEndingWith("UseCase")
            .orShould().haveSimpleNameEndingWith("Command")
            .as("Las interfaces primarias de casos de uso deben seguir el estándar <Verbo><Sustantivo>UseCase");

    // Cómo romperla en clase: Renombrar CitaRepositoryPort a CitaRepository o CitaDao
    @ArchTest
    static final ArchRule convencion_nombres_puertos_salida = classes()
            .that().resideInAPackage("..port.out..")
            .should().haveSimpleNameEndingWith("Port")
            .as("Los contratos de salida hacia infraestructura deben terminar en Port");

    // Cómo romperla en clase: Crear un @RestController fuera de adapter.in.web o sin el sufijo Controller
    @ArchTest
    static final ArchRule controladores_deben_residir_en_adapter_web = classes()
            .that().areAnnotatedWith(RestController.class)
            .or().areAnnotatedWith(Controller.class)
            .should().resideInAPackage("..adapter.in.web..")
            .andShould().haveSimpleNameEndingWith("Controller")
            .as("Los controladores web deben ser adaptadores primarios en el paquete adapter.in.web");

    // Cómo romperla en clase: En CitaController.java, devolver directamente CitaJpaEntity en vez de CitaDto
    @ArchTest
    static final ArchRule controladores_no_exponen_entidades_jpa = noClasses()
            .that().resideInAPackage("..adapter.in.web..")
            .should().dependOnClassesThat()
            .resideInAPackage("..adapter.out.persistence..")
            .as("La capa web no debe acoplarse a las entidades JPA de persistencia; debe usar DTO records");
}
