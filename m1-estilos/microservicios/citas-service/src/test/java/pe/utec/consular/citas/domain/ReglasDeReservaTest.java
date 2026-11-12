package pe.utec.consular.citas.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.utec.consular.citas.domain.exception.CitaDuplicadaException;
import pe.utec.consular.citas.domain.exception.ReglaNegocioException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Pruebas unitarias de ReglasDeReserva")
class ReglasDeReservaTest {

    @Test
    @DisplayName("Valida que la fecha de la cita esté entre mañana y 30 días")
    void validaVentanaTemporal() {
        LocalDate hoy = LocalDate.of(2026, 11, 1);

        assertThatCode(() -> ReglasDeReserva.validarVentanaTemporal(hoy.plusDays(1), hoy))
                .doesNotThrowAnyException();
        assertThatCode(() -> ReglasDeReserva.validarVentanaTemporal(hoy.plusDays(30), hoy))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> ReglasDeReserva.validarVentanaTemporal(hoy, hoy))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> ReglasDeReserva.validarVentanaTemporal(hoy.plusDays(31), hoy))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("Lanza CitaDuplicadaException cuando ya existe una cita activa el mismo día en la oficina")
    void validaNoDuplicada() {
        LocalDate fecha = LocalDate.of(2026, 11, 10);

        assertThatCode(() -> ReglasDeReserva.validarNoDuplicada(false, "40000001", "MAD", fecha))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> ReglasDeReserva.validarNoDuplicada(true, "40000001", "MAD", fecha))
                .isInstanceOf(CitaDuplicadaException.class);
    }
}

