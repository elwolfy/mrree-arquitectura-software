package pe.utec.consular.citas.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.utec.consular.citas.domain.exception.CupoSinCapacidadException;
import pe.utec.consular.citas.domain.exception.ReglaNegocioException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Pruebas unitarias de las entidades Cita y Cupo en citas-service")
class CitaTest {

    @Test
    @DisplayName("Cita creada inicia en estado RESERVADA y puede cancelarse")
    void cicloDeVidaCita() {
        Cita cita = Cita.crearReserva(
                CodigoCita.generar(2026, 123),
                new Dni("40000001"),
                "Ana Pérez",
                new Email("ana@correo.lab"),
                "MAD",
                UUID.randomUUID(),
                LocalDate.of(2026, 11, 30),
                LocalTime.of(8, 0),
                TipoTramite.LEGALIZACION
        );

        assertEquals(EstadoCita.RESERVADA, cita.getEstado());
        assertEquals("PENDIENTE", cita.getNotificacion());
        assertEquals("CIT-2026-000123", cita.getCodigo().valor());

        cita.cancelar();
        assertEquals(EstadoCita.CANCELADA, cita.getEstado());

        assertThrows(ReglaNegocioException.class, cita::cancelar);
    }

    @Test
    @DisplayName("Cupo controla capacidad y rechaza reservas cuando se satura")
    void controlCapacidadCupo() {
        Cupo cupo = Cupo.nuevo("MAD", LocalDate.of(2026, 11, 30), LocalTime.of(8, 0), 2);
        assertTrue(cupo.tieneCapacidadLibre());
        assertEquals(2, cupo.cuposLibres());

        cupo.reservar();
        assertEquals(1, cupo.cuposLibres());
        assertTrue(cupo.tieneCapacidadLibre());

        cupo.reservar();
        assertEquals(0, cupo.cuposLibres());
        assertFalse(cupo.tieneCapacidadLibre());

        assertThrows(CupoSinCapacidadException.class, cupo::reservar);

        cupo.liberar();
        assertEquals(1, cupo.cuposLibres());
        assertTrue(cupo.tieneCapacidadLibre());
    }

    @Test
    @DisplayName("Dni valida formato de 8 dígitos y enmascara correctamente")
    void validacionDni() {
        Dni dni = new Dni("40000001");
        assertEquals("4000****", dni.enmascarado());

        assertThrows(IllegalArgumentException.class, () -> new Dni("123"));
        assertThrows(IllegalArgumentException.class, () -> new Dni("4000000A"));
    }
}

