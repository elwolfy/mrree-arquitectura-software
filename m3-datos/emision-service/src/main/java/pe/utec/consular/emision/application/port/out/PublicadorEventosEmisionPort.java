package pe.utec.consular.emision.application.port.out;

import pe.utec.consular.emision.domain.EmisionCompletada;
import pe.utec.consular.emision.domain.EmisionFallida;

/**
 * Puerto secundario para publicar el resultado de la emisión en RabbitMQ ({@code consular.eventos}).
 */
public interface PublicadorEventosEmisionPort {

    void publicarEmisionCompletada(EmisionCompletada evento);

    void publicarEmisionFallida(EmisionFallida evento);
}

