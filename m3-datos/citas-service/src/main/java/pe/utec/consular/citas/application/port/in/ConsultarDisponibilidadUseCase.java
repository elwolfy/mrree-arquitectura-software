package pe.utec.consular.citas.application.port.in;

import pe.utec.consular.citas.domain.Disponibilidad;

import java.time.LocalDate;

/**
 * Puerto de entrada para consultar la disponibilidad de cupos en una oficina consular.
 * <p>Concepto: Arquitectura hexagonal – puerto primario (sección 1.3).
 * <p>Sesión: S3.
 */
public interface ConsultarDisponibilidadUseCase {

    Disponibilidad consultar(String oficinaId, LocalDate fecha);
}

