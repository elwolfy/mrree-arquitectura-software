package pe.utec.consular.monolito.citas.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.monolito.citas.application.port.in.RegistrarCitaCommand;
import pe.utec.consular.monolito.citas.application.port.in.RegistrarCitaUseCase;
import pe.utec.consular.monolito.citas.application.port.out.CitaRepositoryPort;
import pe.utec.consular.monolito.citas.application.port.out.CupoRepositoryPort;
import pe.utec.consular.monolito.citas.application.port.out.NotificacionPort;
import pe.utec.consular.monolito.citas.application.port.out.OficinaRepositoryPort;
import pe.utec.consular.monolito.citas.domain.Cita;
import pe.utec.consular.monolito.citas.domain.CitaReservada;
import pe.utec.consular.monolito.citas.domain.CodigoCita;
import pe.utec.consular.monolito.citas.domain.Cupo;
import pe.utec.consular.monolito.citas.domain.ReglasDeReserva;
import pe.utec.consular.monolito.compartido.domain.Dni;
import pe.utec.consular.monolito.compartido.domain.Email;
import pe.utec.consular.monolito.compartido.domain.exception.RecursoNoEncontradoException;

import java.time.LocalDate;

/**
 * Caso de uso para registrar una nueva cita consular.
 * <p>Concepto: Arquitectura hexagonal – caso de uso e implementación de puerto primario (sección 1.3).
 * <p>Acoplamiento deliberado para la comparación de estilos (1.2):
 * La reserva de cupo (con bloqueo pesimista), la persistencia de la cita y el envío síncrono
 * de la notificación ocurren dentro de la misma transacción. Si el servicio de notificación falla,
 * toda la transacción sufre rollback y no se confirma la reserva.
 * <p>Sesión: S1 y S2.
 */
@Service
public class RegistrarCitaService implements RegistrarCitaUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarCitaService.class);

    private final CitaRepositoryPort citaRepositoryPort;
    private final CupoRepositoryPort cupoRepositoryPort;
    private final OficinaRepositoryPort oficinaRepositoryPort;
    private final NotificacionPort notificacionPort;
    private final ApplicationEventPublisher eventPublisher;

    public RegistrarCitaService(CitaRepositoryPort citaRepositoryPort,
                                CupoRepositoryPort cupoRepositoryPort,
                                OficinaRepositoryPort oficinaRepositoryPort,
                                NotificacionPort notificacionPort,
                                ApplicationEventPublisher eventPublisher) {
        this.citaRepositoryPort = citaRepositoryPort;
        this.cupoRepositoryPort = cupoRepositoryPort;
        this.oficinaRepositoryPort = oficinaRepositoryPort;
        this.notificacionPort = notificacionPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Cita registrar(RegistrarCitaCommand comando) {
        log.info("Iniciando registro de cita para DNI {} en oficina {}", comando.dni(), comando.oficinaId());

        // 1. Validar existencia de la oficina consular
        if (!oficinaRepositoryPort.existe(comando.oficinaId())) {
            throw new RecursoNoEncontradoException("No existe la oficina consular con identificador: " + comando.oficinaId());
        }

        // 2. Construir y validar tipos de dominio (Value Objects)
        Dni dni = new Dni(comando.dni());
        Email email = new Email(comando.email());

        // 3. Validar ventana temporal (mañana a 30 días)
        ReglasDeReserva.validarVentanaTemporal(comando.fecha(), LocalDate.now());

        // 4. Validar duplicidad de cita activa (misma fecha, misma oficina, mismo DNI)
        boolean duplicado = citaRepositoryPort.existeCitaActiva(dni.valor(), comando.oficinaId(), comando.fecha());
        ReglasDeReserva.validarNoDuplicada(duplicado, dni.valor(), comando.oficinaId(), comando.fecha());

        // 5. Adquirir el cupo con bloqueo pesimista (SELECT ... FOR UPDATE)
        Cupo cupo = cupoRepositoryPort.buscarPorOficinaFechaHoraConBloqueo(
                comando.oficinaId(), comando.fecha(), comando.hora())
                .orElseThrow(() -> new RecursoNoEncontradoException(String.format(
                        "No existe franja horaria configurada para la oficina %s en fecha %s a las %s",
                        comando.oficinaId(), comando.fecha(), comando.hora())));

        // 6. Validar y decrementar capacidad del cupo
        cupo.reservar();
        cupoRepositoryPort.guardar(cupo);

        // 7. Generar código correlativo de cita (CIT-YYYY-NNNNNN)
        long secuencia = citaRepositoryPort.siguienteSecuencia();
        CodigoCita codigo = CodigoCita.generar(comando.fecha().getYear(), secuencia);

        // 8. Crear y persistir la entidad Cita
        Cita cita = Cita.crearReserva(
                codigo,
                dni,
                comando.nombres(),
                email,
                comando.oficinaId(),
                cupo.getId(),
                comando.fecha(),
                comando.hora(),
                comando.tipoTramite()
        );
        Cita citaGuardada = citaRepositoryPort.guardar(cita);

        log.info("Cita {} reservada para DNI {} en {}",
                citaGuardada.getCodigo().valor(), dni.enmascarado(), comando.oficinaId());

        // 9. Publicar evento de dominio (Observer S2)
        eventPublisher.publishEvent(new CitaReservada(
                citaGuardada.getId(),
                citaGuardada.getCodigo().valor(),
                citaGuardada.getDni().valor(),
                citaGuardada.getEmail().valor(),
                citaGuardada.getOficinaId(),
                citaGuardada.getFecha(),
                citaGuardada.getHora()
        ));

        // 10. Invocación síncrona y acoplada del puerto de notificación (Rollback completo ante falla)
        notificacionPort.notificarReserva(citaGuardada);

        return citaGuardada;
    }
}
