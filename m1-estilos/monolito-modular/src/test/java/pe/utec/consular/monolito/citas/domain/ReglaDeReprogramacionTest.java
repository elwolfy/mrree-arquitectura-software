package pe.utec.consular.monolito.citas.domain;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.utec.consular.monolito.citas.domain.exception.ReprogramacionFueraDePlazoException;
import pe.utec.consular.monolito.compartido.domain.Dni;
import pe.utec.consular.monolito.compartido.domain.Email;
import pe.utec.consular.monolito.compartido.domain.TipoTramite;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas unitarias de la regla de reprogramación (Asignación A1 – Tarea 2).
 * <p>En la rama {@code main} está anotada con {@code @Disabled("Asignación A1")} para que
 * el participante quite la anotación e implemente {@link ReglaDeReprogramacion#validar}.
 */
@Disabled("Asignación A1")
@DisplayName("ReglaDeReprogramacion – Validación del plazo mínimo de 24 horas (Asignación A1)")
class ReglaDeReprogramacionTest {

    private static final LocalDateTime AHORA_FIJO = LocalDateTime.of(2026, 11, 20, 10, 0);

    @Test
    @DisplayName("Debe permitir reprogramar cuando faltan 48 horas para la cita actual")
    void debePermitirReprogramarCuandoFaltan48Horas() {
        Cita cita = crearCitaEn(AHORA_FIJO.plusHours(48).toLocalDate(), AHORA_FIJO.plusHours(48).toLocalTime());

        assertDoesNotThrow(() -> ReglaDeReprogramacion.validar(cita, AHORA_FIJO));
    }

    @Test
    @DisplayName("Debe rechazar con ReprogramacionFueraDePlazoException cuando faltan solo 2 horas")
    void debeRechazarCuandoFaltan2Horas() {
        Cita cita = crearCitaEn(AHORA_FIJO.plusHours(2).toLocalDate(), AHORA_FIJO.plusHours(2).toLocalTime());

        assertThrows(ReprogramacionFueraDePlazoException.class,
                () -> ReglaDeReprogramacion.validar(cita, AHORA_FIJO));
    }

    @Test
    @DisplayName("Caso borde: debe permitir reprogramar cuando faltan exactamente 24 horas")
    void debePermitirReprogramarCuandoFaltanExactamente24Horas() {
        Cita cita = crearCitaEn(AHORA_FIJO.plusHours(24).toLocalDate(), AHORA_FIJO.plusHours(24).toLocalTime());

        assertDoesNotThrow(() -> ReglaDeReprogramacion.validar(cita, AHORA_FIJO));
    }

    private Cita crearCitaEn(LocalDate fechaCita, LocalTime horaCita) {
        return Cita.crearReserva(
                new CodigoCita("CIT-2026-000001"),
                new Dni("40000001"),
                "Ana Pérez",
                new Email("ana@correo.lab"),
                "MAD",
                UUID.randomUUID(),
                fechaCita,
                horaCita,
                TipoTramite.LEGALIZACION
        );
    }
}

