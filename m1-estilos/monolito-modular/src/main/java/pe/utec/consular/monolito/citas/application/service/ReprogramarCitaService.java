package pe.utec.consular.monolito.citas.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.monolito.citas.application.port.in.ReprogramarCitaCommand;
import pe.utec.consular.monolito.citas.application.port.in.ReprogramarCitaUseCase;
import pe.utec.consular.monolito.citas.application.port.out.CitaRepositoryPort;
import pe.utec.consular.monolito.citas.application.port.out.CupoRepositoryPort;
import pe.utec.consular.monolito.citas.application.port.out.NotificacionPort;
import pe.utec.consular.monolito.citas.domain.Cita;
import pe.utec.consular.monolito.citas.domain.Cupo;
import pe.utec.consular.monolito.citas.domain.ReglaDeReprogramacion;
import pe.utec.consular.monolito.citas.domain.ReglasDeReserva;
import pe.utec.consular.monolito.compartido.domain.exception.RecursoNoEncontradoException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Caso de uso para reprogramar una cita consular hacia una nueva fecha y hora.
 * <p>Concepto: Arquitectura hexagonal y consistencia CP (secciones 1.3 y 1.4).
 * <p>En una misma transacción carga la cita actual, valida la regla de negocio del plazo mínimo,
 * libera el cupo anterior y bloquea/ocupa el nuevo cupo con el mismo método de bloqueo pesimista
 * ({@code SELECT ... FOR UPDATE}) que utiliza la reserva (evidencia de que es una operación CP),
 * actualiza la cita al estado {@code REPROGRAMADA}, la persiste y notifica igual que la reserva.
 * <p><b>Asignación A1:</b> Este servicio de aplicación ya se entrega completo; la implementación
 * de la regla de las 24 horas en {@link ReglaDeReprogramacion} es la tarea del participante.
 * <p>Sesión: S2 (Asignación A1).
 */
@Service
public class ReprogramarCitaService implements ReprogramarCitaUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReprogramarCitaService.class);
    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    private final CitaRepositoryPort citaRepositoryPort;
    private final CupoRepositoryPort cupoRepositoryPort;
    private final NotificacionPort notificacionPort;

    public ReprogramarCitaService(CitaRepositoryPort citaRepositoryPort,
                                  CupoRepositoryPort cupoRepositoryPort,
                                  NotificacionPort notificacionPort) {
        this.citaRepositoryPort = citaRepositoryPort;
        this.cupoRepositoryPort = cupoRepositoryPort;
        this.notificacionPort = notificacionPort;
    }

    @Override
    @Transactional
    public Cita reprogramar(ReprogramarCitaCommand comando) {
        log.info("Iniciando reprogramación de cita {} hacia fecha {} hora {}",
                comando.citaId(), comando.fecha(), comando.hora());

        // 1. Cargar la cita existente
        Cita citaActual = citaRepositoryPort.buscarPorId(comando.citaId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la cita con identificador: " + comando.citaId()));

        // 2. Validar regla de las 24 h en el dominio (Tarea 2 del participante en Asignación A1)
        ReglaDeReprogramacion.validar(citaActual, LocalDateTime.now(ZONA_LIMA));

        // 3. Validar ventana temporal de la nueva fecha solicitada (mañana a 30 días)
        ReglasDeReserva.validarVentanaTemporal(comando.fecha(), LocalDate.now(ZONA_LIMA));

        // 4. Liberar el cupo anterior con bloqueo pesimista (SELECT ... FOR UPDATE)
        cupoRepositoryPort.buscarPorOficinaFechaHoraConBloqueo(
                citaActual.getOficinaId(), citaActual.getFecha(), citaActual.getHora())
                .ifPresent(cupoAnterior -> {
                    cupoAnterior.liberar();
                    cupoRepositoryPort.guardar(cupoAnterior);
                });

        // 5. Adquirir y ocupar el nuevo cupo con el mismo bloqueo pesimista que la reserva (operación CP)
        Cupo nuevoCupo = cupoRepositoryPort.buscarPorOficinaFechaHoraConBloqueo(
                citaActual.getOficinaId(), comando.fecha(), comando.hora())
                .orElseThrow(() -> new RecursoNoEncontradoException(String.format(
                        "No existe franja horaria configurada para la oficina %s en fecha %s a las %s",
                        citaActual.getOficinaId(), comando.fecha(), comando.hora())));

        nuevoCupo.reservar();
        cupoRepositoryPort.guardar(nuevoCupo);

        // 6. Actualizar el estado de la cita a REPROGRAMADA y persistir
        citaActual.reprogramar(nuevoCupo.getId(), comando.fecha(), comando.hora());
        Cita citaGuardada = citaRepositoryPort.guardar(citaActual);

        log.info("Cita {} reprogramada satisfactoriamente a {} {}",
                citaGuardada.getCodigo().valor(), citaGuardada.getFecha(), citaGuardada.getHora());

        // 7. Notificar igual que en la reserva
        notificacionPort.notificarReserva(citaGuardada);

        return citaGuardada;
    }
}

