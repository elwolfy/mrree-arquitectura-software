package pe.utec.consular.tramites.domain;

/**
 * Resultado de una consulta de trámite por identificador que indica si la lectura
 * provino del caché ({@code desdeCache = true} → {@code X-Cache: HIT}) o de la base
 * de datos ({@code desdeCache = false} → {@code X-Cache: MISS}).
 */
public record ResultadoConsultaTramite(
        VistaTramite vista,
        boolean desdeCache
) {
}

