package pe.utec.consular.clean.citas.usecase.oficinas;

/**
 * Modelo de respuesta de una oficina consular en el círculo {@code usecase}.
 * <p>Concepto: Clean Architecture – Response Model (sección 1.3 del plan).
 */
public record ListarOficinasResponseModel(
        String id,
        String nombre,
        String pais,
        String ciudad,
        String zonaHoraria
) {
}

