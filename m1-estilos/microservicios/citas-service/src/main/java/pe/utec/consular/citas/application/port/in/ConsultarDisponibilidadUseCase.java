package pe.utec.consular.citas.application.port.in;

import pe.utec.consular.citas.domain.Disponibilidad;

import java.time.LocalDate;

/**
 * Puerto de entrada para consultar la disponibilidad de cupos en una oficina y fecha.
 */
public interface ConsultarDisponibilidadUseCase {

    Disponibilidad consultar(String oficinaId, LocalDate fecha);
}

