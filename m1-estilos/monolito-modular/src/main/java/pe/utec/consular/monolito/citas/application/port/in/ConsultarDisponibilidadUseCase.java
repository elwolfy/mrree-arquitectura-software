package pe.utec.consular.monolito.citas.application.port.in;

import pe.utec.consular.monolito.citas.domain.Disponibilidad;

import java.time.LocalDate;

/**
 * Puerto de entrada primario para consultar la disponibilidad de cupos en una oficina consular.
 * <p>Concepto: Arquitectura hexagonal y Teorema CAP (secciones 1.3 y 1.4).
 * Su comportamiento depende del modo activo CP vs. AP.
 */
public interface ConsultarDisponibilidadUseCase {

    Disponibilidad consultar(String oficinaId, LocalDate fecha);
}
