package pe.utec.consular.citas.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.utec.consular.citas.domain.exception.CupoSinCapacidadException;
import pe.utec.consular.citas.domain.exception.ReglaNegocioException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Pruebas unitarias de Cita y Cupo en citas-service")
class CitaTest {

    @Test
    @DisplayName("Cupo permite reservar hasta agotar su capacidad y rechaza reservas adicionales")
    void cupoControlaCapacidad() {
        Cupo cupo = Cupo.nuevo("MAD", LocalDate.now().plusDays(2), LocalTime.of(8, 0), 2);
        assertThat(cupo.cuposLibres()).isEqualTo(2);

        cupo.reservar();
        cupo.reservar();
        assertThat(cupo.cupoDisponible()).isFalse();

        assertThatThrownBy(cupo::reservar)
                .isInstanceOf(CupoSinCapacidadException.class);
    }

    @Test
    @DisplayName("Cita cambia de estado al cancelar y rechaza doble cancelación")
    void citaCancelaCorrectamente() {
        Cita cita = Cita.crearReserva(
                CodigoCita.generar(2026, 1),
                new Dni("40000001"),
                "Ana Pérez",
                new Email("ana@correo.lab"),
                "MAD",
                UUID.randomUUID(),
                LocalDate.now().plusDays(2),
                LocalTime.of(9, 0),
                TipoTramite.LEGALIZACION
        );

        cita.marcarNotificacion("ENVIADA");
        assertThat(cita.getNotificacion()).isEqualTo("ENVIADA");

        cita.cancelar();
        assertThat(cita.getEstado()).isEqualTo(EstadoCita.CANCELADA);

        assertThatThrownBy(cita::cancelar)
                .isInstanceOf(ReglaNegocioException.class);
    }
}

