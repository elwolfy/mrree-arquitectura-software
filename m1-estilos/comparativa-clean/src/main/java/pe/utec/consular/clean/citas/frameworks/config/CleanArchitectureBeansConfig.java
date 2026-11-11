package pe.utec.consular.clean.citas.frameworks.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;
import pe.utec.consular.clean.citas.entities.ModoConsistencia;
import pe.utec.consular.clean.citas.interfaceadapters.gateway.CitaJpaGateway;
import pe.utec.consular.clean.citas.interfaceadapters.gateway.CitaPersistenceDataSource;
import pe.utec.consular.clean.citas.interfaceadapters.gateway.CupoJpaGateway;
import pe.utec.consular.clean.citas.interfaceadapters.gateway.CupoPersistenceDataSource;
import pe.utec.consular.clean.citas.interfaceadapters.gateway.DisponibilidadCaffeineGateway;
import pe.utec.consular.clean.citas.interfaceadapters.gateway.NotificadorConsolaGateway;
import pe.utec.consular.clean.citas.interfaceadapters.gateway.OficinaJpaGateway;
import pe.utec.consular.clean.citas.interfaceadapters.gateway.OficinaPersistenceDataSource;
import pe.utec.consular.clean.citas.interfaceadapters.presenter.CitaPresenter;
import pe.utec.consular.clean.citas.interfaceadapters.presenter.DisponibilidadPresenter;
import pe.utec.consular.clean.citas.interfaceadapters.presenter.OficinaPresenter;
import pe.utec.consular.clean.citas.usecase.CitaGateway;
import pe.utec.consular.clean.citas.usecase.CupoGateway;
import pe.utec.consular.clean.citas.usecase.NotificadorGateway;
import pe.utec.consular.clean.citas.usecase.OficinaGateway;
import pe.utec.consular.clean.citas.usecase.cancelar.CancelarCitaInputBoundary;
import pe.utec.consular.clean.citas.usecase.cancelar.CancelarCitaInteractor;
import pe.utec.consular.clean.citas.usecase.consultar.ConsultarCitaInputBoundary;
import pe.utec.consular.clean.citas.usecase.consultar.ConsultarCitaInteractor;
import pe.utec.consular.clean.citas.usecase.disponibilidad.ConsultarDisponibilidadInputBoundary;
import pe.utec.consular.clean.citas.usecase.disponibilidad.ConsultarDisponibilidadInteractor;
import pe.utec.consular.clean.citas.usecase.oficinas.ListarOficinasInputBoundary;
import pe.utec.consular.clean.citas.usecase.oficinas.ListarOficinasInteractor;
import pe.utec.consular.clean.citas.usecase.registrar.RegistrarCitaInputBoundary;
import pe.utec.consular.clean.citas.usecase.registrar.RegistrarCitaInteractor;

import java.util.function.Supplier;

/**
 * Ensamblado explícito de beans de Clean Architecture para el módulo de Citas.
 * <p>Concepto: Clean Architecture – Círculo 4 ({@code frameworks.config}, Main / Ensamblador, sección 1.3 del plan).
 * <p><b>Diferencia visible con el monolito hexagonal:</b>
 * En {@code monolito-modular}, {@code RegistrarCitaService} lleva {@code @Service} y {@code @Transactional}
 * directamente en la capa de aplicación. Aquí, las clases de {@code entities} y {@code usecase} son Java puro
 * sin ninguna anotación ni import de Spring; este archivo {@code @Configuration} instancia y conecta
 * {@code Interactor}, {@code Presenter} y {@code Gateway}, y aplica la demarcación transaccional desde afuera
 * mediante {@link TransactionTemplate}.
 * <p>Sesión: S2.
 */
@Configuration
public class CleanArchitectureBeansConfig {

    @Bean
    public Supplier<ModoConsistencia> proveedorModoConsistencia(ConsistenciaProperties consistenciaProperties) {
        return consistenciaProperties::getModo;
    }

    // =========================================================================
    // Presenters (Output Boundaries -> ViewModels)
    // =========================================================================

    @Bean
    public CitaPresenter citaPresenter() {
        return new CitaPresenter();
    }

    @Bean
    public DisponibilidadPresenter disponibilidadPresenter() {
        return new DisponibilidadPresenter();
    }

    @Bean
    public OficinaPresenter oficinaPresenter() {
        return new OficinaPresenter();
    }

    // =========================================================================
    // Gateways (implementan las interfaces del círculo usecase)
    // =========================================================================

    @Bean
    public CitaGateway citaGateway(CitaPersistenceDataSource dataSource) {
        return new CitaJpaGateway(dataSource);
    }

    @Bean
    public CupoGateway cupoGateway(CupoPersistenceDataSource dataSource) {
        return new CupoJpaGateway(dataSource);
    }

    @Bean
    public OficinaGateway oficinaGateway(OficinaPersistenceDataSource dataSource) {
        return new OficinaJpaGateway(dataSource);
    }

    @Bean
    public NotificadorGateway notificadorGateway(NotificacionesProperties notificacionesProperties) {
        return new NotificadorConsolaGateway(notificacionesProperties::isSimularFalla);
    }

    @Bean
    public DisponibilidadCaffeineGateway disponibilidadCaffeineGateway(CupoGateway cupoGateway,
                                                                       OficinaGateway oficinaGateway) {
        return new DisponibilidadCaffeineGateway(cupoGateway, oficinaGateway);
    }

    // =========================================================================
    // Interactors (Input Boundaries) con demarcación transaccional externa
    // =========================================================================

    @Bean
    public RegistrarCitaInputBoundary registrarCitaInputBoundary(CitaGateway citaGateway,
                                                                 CupoGateway cupoGateway,
                                                                 OficinaGateway oficinaGateway,
                                                                 NotificadorGateway notificadorGateway,
                                                                 CitaPresenter citaPresenter,
                                                                 TransactionTemplate transactionTemplate) {
        RegistrarCitaInteractor interactor = new RegistrarCitaInteractor(
                citaGateway,
                cupoGateway,
                oficinaGateway,
                notificadorGateway,
                citaPresenter
        );
        return requestModel -> transactionTemplate.executeWithoutResult(status -> interactor.ejecutar(requestModel));
    }

    @Bean
    public ConsultarCitaInputBoundary consultarCitaInputBoundary(CitaGateway citaGateway,
                                                                 CitaPresenter citaPresenter) {
        return new ConsultarCitaInteractor(citaGateway, citaPresenter);
    }

    @Bean
    public CancelarCitaInputBoundary cancelarCitaInputBoundary(CitaGateway citaGateway,
                                                               CupoGateway cupoGateway,
                                                               CitaPresenter citaPresenter,
                                                               TransactionTemplate transactionTemplate) {
        CancelarCitaInteractor interactor = new CancelarCitaInteractor(
                citaGateway,
                cupoGateway,
                citaPresenter
        );
        return citaId -> transactionTemplate.executeWithoutResult(status -> interactor.cancelar(citaId));
    }

    @Bean
    public ConsultarDisponibilidadInputBoundary consultarDisponibilidadInputBoundary(
            CupoGateway cupoGateway,
            OficinaGateway oficinaGateway,
            DisponibilidadCaffeineGateway disponibilidadCaffeineGateway,
            Supplier<ModoConsistencia> proveedorModoConsistencia,
            DisponibilidadPresenter disponibilidadPresenter) {
        return new ConsultarDisponibilidadInteractor(
                cupoGateway,
                oficinaGateway,
                disponibilidadCaffeineGateway,
                proveedorModoConsistencia,
                disponibilidadPresenter
        );
    }

    @Bean
    public ListarOficinasInputBoundary listarOficinasInputBoundary(OficinaGateway oficinaGateway,
                                                                   OficinaPresenter oficinaPresenter) {
        return new ListarOficinasInteractor(oficinaGateway, oficinaPresenter);
    }
}

