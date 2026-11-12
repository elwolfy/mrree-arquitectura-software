package pe.utec.consular.citas.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.citas.application.port.in.RegistrarCitaCommand;
import pe.utec.consular.citas.application.port.in.RegistrarCitaUseCase;
import pe.utec.consular.citas.application.port.out.CitaRepositoryPort;
import pe.utec.consular.citas.application.port.out.CupoRepositoryPort;
import pe.utec.consular.citas.application.port.out.NotificacionPort;
import pe.utec.consular.citas.application.port.out.OficinaRepositoryPort;
import pe.utec.consular.citas.domain.Cita;
import pe.utec.consular.citas.domain.CodigoCita;
import pe.utec.consular.citas.domain.Cupo;
import pe.utec.consular.citas.domain.Dni;
import pe.utec.consular.citas.domain.Email;
import pe.utec.consular.citas.domain.EstadoNotificacion;
import pe.utec.consular.citas.domain.ReglasDeReserva;
import pe.utec.consular.citas.domain.exception.RecursoNoEncontradoException;

import java.time.LocalDate;

/**
 * Caso de uso para registrar una nueva cita consular en la variante de microservicios.
 * <p>Concepto: Estilo microservicios y degradación elegante con Circuit Breaker (secciones 1.2 y 4.3).
 * A diferencia del monolito modular (donde una falla en notificaciones revierte toda la transacción),
 * aquí {@link NotificacionPort} invoca a {@code notificaciones-service} con Circuit Breaker Resilience4j:
 * si el servicio de notificaciones está caído o responde con error/timeout, el fallback devuelve
 * {@link EstadoNotificacion#PENDIENTE} y la cita se confirma igualmente con HTTP 201.
 * <p>Sesión: S1.
 */
@Service
public class RegistrarCitaService implements RegistrarCitaUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarCitaService.class);

    private final CitaRepositoryPort citaRepositoryPort;
    private final CupoRepositoryPort cupoRepositoryPort;
    private final OficinaRepositoryPort oficinaRepositoryPort;
    private final NotificacionPort notificacionPort;

    public RegistrarCitaService(CitaRepositoryPort citaRepositoryPort,
                                CupoRepositoryPort cupoRepositoryPort,
                                OficinaRepositoryPort oficinaRepositoryPort,
                                NotificacionPort notificacionPort) {
        this.citaRepositoryPort = citaRepositoryPort;
        this.cupoRepositoryPort = cupoRepositoryPort;
        this.oficinaRepositoryPort = oficinaRepositoryPort;
        this.notificacionPort = notificacionPort;
    }

    @Override
    @Transactional
    public Cita registrar(RegistrarCitaCommand comando) {
        Dni dni = new Dni(comando.dni());
        Email email = new Email(comando.email());

        log.info("Iniciando registro de cita para DNI {} en oficina {}", dni.enmascarado(), comando.oficinaId());

        if (!oficinaRepositoryPort.existe(comando.oficinaId())) {
            throw new RecursoNoEncontradoException("No existe la oficina consular con identificador: " + comando.oficinaId());
        }

        ReglasDeReserva.validarVentanaTemporal(comando.fecha(), LocalDate.now());

        boolean duplicado = citaRepositoryPort.existeCitaActiva(dni.valor(), comando.oficinaId(), comando.fecha());
        ReglasDeReserva.validarNoDuplicada(duplicado, dni.valor(), comando.oficinaId(), comando.fecha());

        Cupo cupo = cupoRepositoryPort.buscarPorOficinaFechaHoraConBloqueo(
                        comando.oficinaId(), comando.fecha(), comando.hora())
                .orElseThrow(() -> new RecursoNoEncontradoException(String.format(
                        "No existe franja horaria configurada para la oficina %s en fecha %s a las %s",
                        comando.oficinaId(), comando.fecha(), comando.hora())));

        cupo.reservar();
        cupoRepositoryPort.guardar(cupo);

        long secuencia = citaRepositoryPort.siguienteSecuencia();
        CodigoCita codigo = CodigoCita.generar(comando.fecha().getYear(), secuencia);

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

        EstadoNotificacion estadoNotificacion = notificacionPort.notificarReserva(cita);
        cita.marcarNotificacion(estadoNotificacion.name());

        Cita citaGuardada = citaRepositoryPort.guardar(cita);

        log.info("Cita {} reservada para DNI {} en {} con notificación {}",
                citaGuardada.getCodigo().valor(), dni.enmascarado(), comando.oficinaId(), citaGuardada.getNotificacion());

        return citaGuardada;
    }
}

