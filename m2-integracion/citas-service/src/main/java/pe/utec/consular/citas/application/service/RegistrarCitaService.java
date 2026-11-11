package pe.utec.consular.citas.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.citas.application.port.in.RegistrarCitaCommand;
import pe.utec.consular.citas.application.port.in.RegistrarCitaUseCase;
import pe.utec.consular.citas.application.port.out.CitaRepositoryPort;
import pe.utec.consular.citas.application.port.out.CupoRepositoryPort;
import pe.utec.consular.citas.application.port.out.OficinaRepositoryPort;
import pe.utec.consular.citas.application.port.out.PublicadorDeEventosPort;
import pe.utec.consular.citas.domain.Cita;
import pe.utec.consular.citas.domain.CitaReservada;
import pe.utec.consular.citas.domain.CodigoCita;
import pe.utec.consular.citas.domain.Cupo;
import pe.utec.consular.citas.domain.Dni;
import pe.utec.consular.citas.domain.Email;
import pe.utec.consular.citas.domain.ReglasDeReserva;
import pe.utec.consular.citas.domain.exception.RecursoNoEncontradoException;

import java.time.LocalDate;

/**
 * Caso de uso para registrar una nueva cita consular y publicar el evento {@code cita.reservada}.
 * <p>Concepto: Arquitectura hexagonal y comunicación asíncrona mediante eventos (secciones 1.3 y 2.3).
 * A diferencia del monolito síncrono de M1, en el Módulo 2 {@code citas-service} solo publica el evento
 * {@code cita.reservada} hacia el broker y el envío real del aviso lo realiza de forma asíncrona
 * {@code notificaciones-service}. Por esta razón, la cita se registra con {@code notificacion = PENDIENTE}
 * (según {@code EstadoNotificacion} del contrato) y no {@code ENVIADA}: al momento de confirmar la transacción
 * local en el contexto Citas, el correo aún está pendiente de procesamiento en la cola de mensajería.
 * <p>Sesión: S3.
 */
@Service
public class RegistrarCitaService implements RegistrarCitaUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarCitaService.class);

    private final CitaRepositoryPort citaRepositoryPort;
    private final CupoRepositoryPort cupoRepositoryPort;
    private final OficinaRepositoryPort oficinaRepositoryPort;
    private final PublicadorDeEventosPort publicadorDeEventosPort;

    public RegistrarCitaService(CitaRepositoryPort citaRepositoryPort,
                                CupoRepositoryPort cupoRepositoryPort,
                                OficinaRepositoryPort oficinaRepositoryPort,
                                PublicadorDeEventosPort publicadorDeEventosPort) {
        this.citaRepositoryPort = citaRepositoryPort;
        this.cupoRepositoryPort = cupoRepositoryPort;
        this.oficinaRepositoryPort = oficinaRepositoryPort;
        this.publicadorDeEventosPort = publicadorDeEventosPort;
    }

    @Override
    @Transactional
    public Cita registrar(RegistrarCitaCommand comando) {
        // 1. Construir y validar tipos de dominio (Value Objects)
        Dni dni = new Dni(comando.dni());
        Email email = new Email(comando.email());

        log.info("Iniciando registro de cita para DNI {} en oficina {}", dni.enmascarado(), comando.oficinaId());

        // 2. Validar existencia de la oficina consular
        if (!oficinaRepositoryPort.existe(comando.oficinaId())) {
            throw new RecursoNoEncontradoException("No existe la oficina consular con identificador: " + comando.oficinaId());
        }

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

        // 9. Publicar evento de integración hacia RabbitMQ (sección 6.1 del contrato global)
        publicadorDeEventosPort.publicarCitaReservada(new CitaReservada(
                citaGuardada.getId(),
                citaGuardada.getCodigo().valor(),
                citaGuardada.getDni().valor(),
                citaGuardada.getEmail().valor(),
                citaGuardada.getOficinaId(),
                citaGuardada.getFecha(),
                citaGuardada.getHora()
        ));

        return citaGuardada;
    }
}

