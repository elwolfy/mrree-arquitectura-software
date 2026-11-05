package pe.utec.consular.monolito.tramites.domain.tarifa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.utec.consular.monolito.compartido.domain.TipoTramite;
import pe.utec.consular.monolito.compartido.domain.exception.TarifaNoDisponibleException;
import pe.utec.consular.monolito.tramites.application.port.out.TarifaBaseRepositoryPort;
import pe.utec.consular.monolito.tramites.domain.tarifa.CalculadoraDeTarifas.ResultadoTarifa;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Pruebas unitarias del cálculo de tarifas con patrón Strategy")
class CalculadoraDeTarifasTest {

    private CalculadoraDeTarifas calculadora;

    @BeforeEach
    void setUp() {
        // Mock sencillo de repositorio de tarifas base
        TarifaBaseRepositoryPort repoMock = tipo -> switch (tipo) {
            case LEGALIZACION -> Optional.of(new BigDecimal("20.00"));
            case APOSTILLA -> Optional.of(new BigDecimal("30.00"));
            default -> Optional.empty();
        };

        TarifaStrategy legalizacionStrategy = new LegalizacionTarifaStrategy(repoMock);
        TarifaStrategy apostillaStrategy = new ApostillaTarifaStrategy(repoMock);

        calculadora = new CalculadoraDeTarifas(List.of(legalizacionStrategy, apostillaStrategy));
    }

    @Test
    @DisplayName("Apostilla en país fuera de Sudamérica (ES) aplica 10% de recargo -> 33.00 PEN")
    void apostillaFueraDeSudamerica() {
        ResultadoTarifa resultado = calculadora.calcular(TipoTramite.APOSTILLA, "ES");

        assertEquals(new BigDecimal("33.00"), resultado.monto());
        assertEquals("PEN", resultado.moneda());
        assertEquals("ApostillaTarifaStrategy", resultado.estrategia());
        assertEquals("ES", resultado.pais());
    }

    @Test
    @DisplayName("Apostilla en país de Sudamérica (AR) mantiene tarifa base -> 30.00 PEN")
    void apostillaEnSudamerica() {
        ResultadoTarifa resultado = calculadora.calcular(TipoTramite.APOSTILLA, "AR");

        assertEquals(new BigDecimal("30.00"), resultado.monto());
        assertEquals("ApostillaTarifaStrategy", resultado.estrategia());
    }

    @Test
    @DisplayName("Legalización en país fuera de Sudamérica (US) aplica 10% de recargo -> 22.00 PEN")
    void legalizacionFueraDeSudamerica() {
        ResultadoTarifa resultado = calculadora.calcular(TipoTramite.LEGALIZACION, "US");

        assertEquals(new BigDecimal("22.00"), resultado.monto());
        assertEquals("LegalizacionTarifaStrategy", resultado.estrategia());
    }

    @Test
    @DisplayName("Legalización en Perú (PE) mantiene tarifa base -> 20.00 PEN")
    void legalizacionEnPeru() {
        ResultadoTarifa resultado = calculadora.calcular(TipoTramite.LEGALIZACION, "PE");

        assertEquals(new BigDecimal("20.00"), resultado.monto());
    }

    @Test
    @DisplayName("REGISTRO_CONSULAR lanza TarifaNoDisponibleException (se agregará en asignación S2)")
    void registroConsularSinEstrategiaLanzaExcepcion() {
        assertThrows(TarifaNoDisponibleException.class,
                () -> calculadora.calcular(TipoTramite.REGISTRO_CONSULAR, "ES"));
    }
}
