package pe.utec.consular.clean.citas.frameworks.config;

import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Configuración de migraciones Flyway multi-esquema ({@code citas} y {@code tramites}) para {@code comparativa-clean}.
 * Garantiza que {@code entityManagerFactory} dependa del bean {@code flyway} antes de ejecutar {@code ddl-auto: validate}.
 * <p>Concepto: Aislamiento de esquemas de base de datos (sección 5 del contrato global).
 */
@Configuration
public class FlywayMultiSchemaConfig {

    @Bean(name = "flyway")
    public Flyway flyway(DataSource dataSource) {
        Flyway flywayCitas = Flyway.configure()
                .dataSource(dataSource)
                .schemas("citas")
                .defaultSchema("citas")
                .locations("classpath:db/migration/citas")
                .table("flyway_schema_history")
                .load();
        flywayCitas.migrate();

        Flyway flywayTramites = Flyway.configure()
                .dataSource(dataSource)
                .schemas("tramites")
                .defaultSchema("tramites")
                .locations("classpath:db/migration/tramites")
                .table("flyway_schema_history")
                .load();
        flywayTramites.migrate();

        return flywayCitas;
    }

    @Bean
    public static BeanFactoryPostProcessor flywayEntityManagerDependencyPostProcessor() {
        return beanFactory -> {
            String[] emfNames = beanFactory.getBeanNamesForType(EntityManagerFactory.class, true, false);
            for (String emfName : emfNames) {
                BeanDefinition bd = beanFactory.getBeanDefinition(emfName);
                bd.setDependsOn("flyway");
            }
            if (beanFactory.containsBeanDefinition("entityManagerFactory")) {
                beanFactory.getBeanDefinition("entityManagerFactory").setDependsOn("flyway");
            }
        };
    }
}
