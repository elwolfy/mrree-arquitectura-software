package pe.utec.consular.tramites.application.port.in;

/**
 * Puerto de entrada para reconstruir la proyección CQRS {@code tramites.vista_tramite}
 * a partir de los eventos almacenados en {@code tramites.eventos_tramite}.
 */
public interface ReconstruirVistaTramiteUseCase {

    int reconstruir();
}

