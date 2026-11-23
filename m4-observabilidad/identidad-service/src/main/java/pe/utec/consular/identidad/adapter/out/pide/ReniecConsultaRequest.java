package pe.utec.consular.identidad.adapter.out.pide;

/**
 * Cuerpo de consulta exigido por el contrato externo simulado de RENIEC en la PIDE.
 * Encapsulado estrictamente dentro del paquete {@code adapter.out.pide}.
 */
public record ReniecConsultaRequest(
        String nuDniConsulta
) {
}

