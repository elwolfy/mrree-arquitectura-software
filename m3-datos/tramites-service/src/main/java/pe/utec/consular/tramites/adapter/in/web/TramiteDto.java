package pe.utec.consular.tramites.adapter.in.web;

import pe.utec.consular.tramites.domain.EstadoTramite;
import pe.utec.consular.tramites.domain.TipoTramite;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Representación JSON de un trámite consular expuesto por la API REST.
 * Incluye el {@code subtipo} concreto instanciado por {@code TramiteFactory}
 * ({@code Legalizacion}, {@code Apostilla} o {@code RegistroConsular}).
 */
public record TramiteDto(
        UUID id,
        String codigo,
        String dni,
        String nombres,
        String email,
        TipoTramite tipoTramite,
        String subtipo,
        String modalidadProcesamiento,
        String pais,
        int folios,
        String idioma,
        EstadoTramite estado,
        BigDecimal monto,
        Instant creadoEn
) {
}

