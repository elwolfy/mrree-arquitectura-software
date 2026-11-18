package pe.utec.consular.pagos.domain.tarifa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.utec.consular.pagos.domain.TipoTramite;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias de {@link CalculadoraDeTarifas} con los tres tipos de estrategia.
 */
@DisplayName("CalculadoraDeTarifas – Cálculo con las 3 estrategias (Strategy)")
class CalculadoraDeTarifasTest {

    private final CalculadoraDeTarifas calculadora = new CalculadoraDeTarifas(List.of(
            new LegalizacionTarifaStrategy(),
            new ApostillaTarifaStrategy(),
            new RegistroConsularTarifaStrategy()
    ));

    @Test
    @DisplayName("Debe calcular tarifa de LEGALIZACION con y sin recargo exterior")
    void debeCalcularLegalizacion() {
        Tarifa enSudamerica = calculadora.calcular(TipoTramite.LEGALIZACION, "AR");
        assertEquals(new BigDecimal("20.00"), enSudamerica.monto());
        assertEquals("LegalizacionTarifaStrategy", enSudamerica.estrategia());

        Tarifa enEuropa = calculadora.calcular(TipoTramite.LEGALIZACION, "ES");
        assertEquals(new BigDecimal("22.00"), enEuropa.monto());
    }

    @Test
    @DisplayName("Debe calcular tarifa de APOSTILLA con y sin recargo exterior")
    void debeCalcularApostilla() {
        Tarifa enSudamerica = calculadora.calcular(TipoTramite.APOSTILLA, "CL");
        assertEquals(new BigDecimal("30.00"), enSudamerica.monto());
        assertEquals("ApostillaTarifaStrategy", enSudamerica.estrategia());

        Tarifa enEuropa = calculadora.calcular(TipoTramite.APOSTILLA, "ES");
        assertEquals(new BigDecimal("33.00"), enEuropa.monto());
    }

    @Test
    @DisplayName("Debe calcular tarifa de REGISTRO_CONSULAR con y sin recargo exterior")
    void debeCalcularRegistroConsular() {
        Tarifa enSudamerica = calculadora.calcular(TipoTramite.REGISTRO_CONSULAR, "AR");
        assertEquals(new BigDecimal("25.00"), enSudamerica.monto());
        assertEquals("RegistroConsularTarifaStrategy", enSudamerica.estrategia());

        Tarifa enEuropa = calculadora.calcular(TipoTramite.REGISTRO_CONSULAR, "ES");
        assertEquals(new BigDecimal("27.50"), enEuropa.monto());
    }
}

