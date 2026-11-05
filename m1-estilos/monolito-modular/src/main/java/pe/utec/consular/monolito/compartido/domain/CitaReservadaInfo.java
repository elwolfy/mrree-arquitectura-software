package pe.utec.consular.monolito.compartido.domain;

import java.util.UUID;

/**
 * Contrato común para eventos de dominio de citas reservadas.
 * Permite la observación reactiva (patrón Observer) desde otros módulos
 * sin introducir ciclos de dependencia directa hacia el dominio de citas.
 * <p>Concepto: Patrón Observer y diseño modular desacoplado.
 * <p>Sesión: S2 (2.1).
 */
public interface CitaReservadaInfo {
    UUID citaId();
    String codigo();
    String dni();
    String email();
    String oficinaId();
}
