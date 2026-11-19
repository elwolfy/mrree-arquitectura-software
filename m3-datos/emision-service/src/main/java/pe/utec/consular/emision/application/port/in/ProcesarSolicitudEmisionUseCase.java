package pe.utec.consular.emision.application.port.in;

/**
 * Puerto de entrada para procesar comandos de emisión documental de forma idempotente.
 */
public interface ProcesarSolicitudEmisionUseCase {

    void procesar(ProcesarSolicitudEmisionCommand comando);
}

