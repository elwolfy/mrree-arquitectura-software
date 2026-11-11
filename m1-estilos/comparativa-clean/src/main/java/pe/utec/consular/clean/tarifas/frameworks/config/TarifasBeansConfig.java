package pe.utec.consular.clean.tarifas.frameworks.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.utec.consular.clean.tarifas.entities.ApostillaTarifaStrategy;
import pe.utec.consular.clean.tarifas.entities.CalculadoraDeTarifas;
import pe.utec.consular.clean.tarifas.entities.LegalizacionTarifaStrategy;
import pe.utec.consular.clean.tarifas.entities.TarifaStrategy;
import pe.utec.consular.clean.tarifas.interfaceadapters.gateway.TarifaBaseJpaGateway;
import pe.utec.consular.clean.tarifas.interfaceadapters.gateway.TarifaBasePersistenceDataSource;
import pe.utec.consular.clean.tarifas.interfaceadapters.presenter.TarifaPresenter;
import pe.utec.consular.clean.tarifas.usecase.CalcularTarifaInputBoundary;
import pe.utec.consular.clean.tarifas.usecase.CalcularTarifaInteractor;
import pe.utec.consular.clean.tarifas.usecase.TarifaBaseGateway;

import java.util.List;

/**
 * Ensamblado explícito de beans de Clean Architecture para el módulo de Tarifas.
 * <p>Concepto: Clean Architecture – Círculo 4 ({@code frameworks.config}) y Patrón Strategy (secciones 1.3 y 2.1).
 * Sin anotaciones de Spring en {@code entities} ni {@code usecase}.
 * <p>Sesión: S2.
 */
@Configuration
public class TarifasBeansConfig {

    @Bean
    public TarifaStrategy legalizacionTarifaStrategy() {
        return new LegalizacionTarifaStrategy();
    }

    @Bean
    public TarifaStrategy apostillaTarifaStrategy() {
        return new ApostillaTarifaStrategy();
    }

    @Bean
    public CalculadoraDeTarifas calculadoraDeTarifas(List<TarifaStrategy> estrategias) {
        return new CalculadoraDeTarifas(estrategias);
    }

    @Bean
    public TarifaBaseGateway tarifaBaseGateway(TarifaBasePersistenceDataSource dataSource) {
        return new TarifaBaseJpaGateway(dataSource);
    }

    @Bean
    public TarifaPresenter tarifaPresenter() {
        return new TarifaPresenter();
    }

    @Bean
    public CalcularTarifaInputBoundary calcularTarifaInputBoundary(CalculadoraDeTarifas calculadoraDeTarifas,
                                                                   TarifaBaseGateway tarifaBaseGateway,
                                                                   TarifaPresenter tarifaPresenter) {
        return new CalcularTarifaInteractor(calculadoraDeTarifas, tarifaBaseGateway, tarifaPresenter);
    }
}

