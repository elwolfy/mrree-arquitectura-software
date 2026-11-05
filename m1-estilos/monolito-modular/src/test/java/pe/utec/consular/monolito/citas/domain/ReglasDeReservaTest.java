package pe.utec.consular.monolito.citas.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.utec.consular.monolito.compartido.domain.exception.CitaDuplicadaException;
import pe.utec.consular.monolito.compartido.domain.exception.ReglaNegocioException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Pruebas unitarias de reglas de reserva de citas (ReglasDeReserva)")
class ReglasDeReservaTest {

    private final LocalDate hoy = LocalDate.of(2026, 11, 4);

    @Test
    @DisplayName("Permite fecha a partir de mañana y hasta 30 días")
    void fechaValidaDentroDeVentana() {
        assertDoesNotThrow(() -> ReglasDeReserva.validarVentanaTemporal(hoy.plusDays(1), hoy));
        assertDoesNotThrow(() -> ReglasDeReserva.validarVentanaTemporal(hoy.plusDays(15), hoy));
        assertDoesNotThrow(() -> ReglasDeReserva.validarVentanaTemporal(hoy.plusDays(30), hoy));
    }

    @Test
    @DisplayName("Rechaza reserva para el mismo día de hoy o fechas pasadas")
    void rechazaFechaHoyOPasada() {
        assertThrows(ReglaNegocioException.class,
                () -> ReglasDeReserva.validarVentanaTemporal(hoy, hoy));
        assertThrows(ReglaNegocioException.class,
                () -> ReglasDeReserva.validarVentanaTemporal(hoy.minusDays(1), hoy));
    }

    @Test
    @DisplayName("Rechaza reserva con más de 30 días de anticipación")
    void rechazaFechaMayorA30Dias() {
        assertThrows(ReglaNegocioException.class,
                () -> ReglasDeReserva.validarVentanaTemporal(hoy.plusDays(31), hoy));
    }

    @Test
    @DisplayName("Lanza CitaDuplicadaException si ya existe una cita activa en la misma fecha y oficina")
    void lanzaExcepcionAnteCitaDuplicada() {
        assertThrows(CitaDuplicadaException.class,
                () -> ReglasDeReserva.validarNoDuplicada(true, "40000001", "MAD", hoy.plusDays(5)));
    }

    @Test
    @DisplayName("No lanza excepción si no existe cita previa duplicada")
    void permiteReservaSinDuplicados() {
        assertDoesNotThrow(
                () -> ReglasDeReserva.validarNoDuplicada(false, "40000001", "MAD", hoy.plusDays(5)));
    }
}
