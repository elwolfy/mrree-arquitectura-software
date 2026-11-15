package pe.utec.consular.serverless.funciones;

public record InfoSistemaDto(
        String modulo,
        String variante,
        String arquitecturaInterna,
        String modoConsistencia,
        String version
) {
}

