package pe.utec.consular.monolito.compartido.infraestructura;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Configuración de migraciones Flyway multi-esquema para el monolito modular.
 * Ejecuta migraciones independientes para citas, tramites y notificaciones.
 * <p>Concepto: Aislamiento de esquemas por módulo (sección 5 del contrato global).
 * <p>Sesión: S1 y S2.
 */
@Configuration
public class FlywayMultiSchemaConfig {

    @Bean
    public Flyway flyway(DataSource dataSource) {
        // 1. Migración esquema citas
        Flyway flywayCitas = Flyway.configure()
                .dataSource(dataSource)
                .schemas("citas")
                .defaultSchema("citas")
                .locations("classpath:db/migration/citas")
                .table("flyway_schema_history")
                .load();
        flywayCitas.migrate();

        // 2. Migración esquema tramites
        Flyway flywayTramites = Flyway.configure()
                .dataSource(dataSource)
                .schemas("tramites")
                .defaultSchema("tramites")
                .locations("classpath:db/migration/tramites")
                .table("flyway_schema_history")
                .load();
        flywayTramites.migrate();

        // 3. Migración esquema notificaciones
        Flyway flywayNotificaciones = Flyway.configure()
                .dataSource(dataSource)
                .schemas("notificaciones")
                .defaultSchema("notificaciones")
                .locations("classpath:db/migration/notificaciones")
                .table("flyway_schema_history")
                .load();
        flywayNotificaciones.migrate();

        return flywayCitas;
    }
}
