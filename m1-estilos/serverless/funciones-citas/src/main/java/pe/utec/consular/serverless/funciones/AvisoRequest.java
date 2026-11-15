package pe.utec.consular.serverless.funciones;

public record AvisoRequest(
        String destinatario,
        String asunto,
        String cuerpo,
        String referencia
) {
}

