package pe.utec.consular.monolito.citas.adapter.out.notificaciones;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pe.utec.consular.monolito.citas.application.port.out.NotificacionPort;
import pe.utec.consular.monolito.citas.domain.Cita;
import pe.utec.consular.monolito.notificaciones.AvisoCommand;
import pe.utec.consular.monolito.notificaciones.NotificacionesApi;

/**
 * Adaptador de salida que conecta el módulo citas con la API pública del módulo notificaciones.
 * <p>Concepto: Adaptador hexagonal de salida inter-módulos y acoplamiento deliberado (secciones 1.2 y 1.3).
 * <p>Sesión: S1 y S2.
 */
@Component
public class NotificacionModuloAdapter implements NotificacionPort {

    private static final Logger log = LoggerFactory.getLogger(NotificacionModuloAdapter.class);

    private final NotificacionesApi notificacionesApi;

    public NotificacionModuloAdapter(NotificacionesApi notificacionesApi) {
        this.notificacionesApi = notificacionesApi;
    }


    @Override
    public void notificarReserva(Cita cita) {
        log.info("Despachando aviso síncrono al módulo notificaciones para cita {}", cita.getCodigo().valor());

        String asunto = "Confirmación de Cita Consular - " + cita.getCodigo().valor();
        String cuerpo = String.format(
                "Estimado(a) %s, su cita para el trámite de %s ha sido reservada para el día %s a las %s en la oficina %s.",
                cita.getNombres(),
                cita.getTipoTramite(),
                cita.getFecha(),
                cita.getHora(),
                cita.getOficinaId()
        );

        AvisoCommand comando = new AvisoCommand(
                cita.getEmail().valor(),
                asunto,
                cuerpo,
                cita.getCodigo().valor()
        );

        notificacionesApi.enviar(comando);
    }
}
