package pe.utec.consular.monolito.compartido.domain;

/**
 * Modo de consistencia según el Teorema CAP (sección 1.4 del plan).
 * <ul>
 *   <li>{@link #CP}: Consistencia y tolerancia a particiones. Ante partición, rechaza disponibilidad con 503.</li>
 *   <li>{@link #AP}: Disponibilidad y tolerancia a particiones. Ante partición, responde con datos en caché.</li>
 * </ul>
 * <p>Concepto: Teorema CAP / PACELC.
 * <p>Sesión: S1.
 */
public enum ModoConsistencia {
    CP,
    AP
}
