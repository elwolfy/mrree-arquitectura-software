package pe.utec.consular.clean.citas.interfaceadapters.presenter;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * ViewModel de disponibilidad de cupos horarios para una oficina y fecha (Círculo 3: Interface Adapters).
 * <p>Concepto: Clean Architecture – ViewModel y Teorema CAP (secciones 1.3 y 1.4 del plan).
 * <p>Sesión: S2. Compárese con {@code DisponibilidadDto} en {@code monolito-modular}.
 */
@Schema(description = "Disponibilidad de cupos horarios para una oficina y fecha")
public record DisponibilidadViewModel(
        @Schema(description = "Identificador de la oficina consular", example = "MAD")
        String oficinaId,

        @Schema(description = "Fecha consultada", example = "2026-11-30")
        LocalDate fecha,

        @Schema(description = "Lista de franjas horarias y capacidad disponible")
        List<CupoDisponibilidadViewModel> cupos,

        @Schema(description = "Fuente de los datos (BASE_DE_DATOS en CP/normal, CACHE en modo AP con partición)", example = "BASE_DE_DATOS")
        String origen,

        @Schema(description = "Segundos transcurridos desde la última actualización en caché", example = "0")
        long actualizadoHaceSegundos
) {
    @Schema(description = "Detalle de disponibilidad de una franja horaria específica")
    public record CupoDisponibilidadViewModel(
            @Schema(description = "Hora de inicio de la franja", example = "08:00")
            LocalTime hora,

            @Schema(description = "Capacidad total del cupo", example = "2")
            int capacidad,

            @Schema(description = "Cantidad de citas ya ocupadas", example = "1")
            int ocupados,

            @Schema(description = "Cantidad de espacios libres", example = "1")
            int libres
    ) {
    }
}

