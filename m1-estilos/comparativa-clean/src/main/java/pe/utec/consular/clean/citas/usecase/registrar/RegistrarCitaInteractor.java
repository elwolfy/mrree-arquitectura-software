package pe.utec.consular.clean.citas.usecase.registrar;

import pe.utec.consular.clean.citas.entities.Cita;
import pe.utec.consular.clean.citas.entities.CodigoCita;
import pe.utec.consular.clean.citas.entities.Cupo;
import pe.utec.consular.clean.citas.entities.Dni;
import pe.utec.consular.clean.citas.entities.Email;
import pe.utec.consular.clean.citas.entities.ReglasDeReserva;
import pe.utec.consular.clean.citas.entities.exception.RecursoNoEncontradoException;
import pe.utec.consular.clean.citas.usecase.CitaGateway;
import pe.utec.consular.clean.citas.usecase.CupoGateway;
import pe.utec.consular.clean.citas.usecase.NotificadorGateway;
import pe.utec.consular.clean.citas.usecase.OficinaGateway;

import java.time.LocalDate;

/**
 * Interactor del caso de uso para registrar una nueva cita consular.
 * <p>Concepto: Clean Architecture – Use Case Interactor (Círculo 2, sección 1.3 del plan).
 * <ul>
 *   <li><b>Sin anotaciones de Spring:</b> No lleva {@code @Service} ni {@code @Transactional}; se ensambla
 *       explícitamente en {@code frameworks.config.CleanArchitectureBeansConfig}.</li>
 *   <li><b>Flujo Output Boundary -> Presenter -> ViewModel:</b> En lugar de retornar {@code Cita} al controlador
 *       (como hace {@code RegistrarCitaService} en la versión hexagonal), construye un {@link RegistrarCitaResponseModel}
 *       y lo entrega a {@link RegistrarCitaOutputBoundary#presentar(RegistrarCitaResponseModel)}.</li>
 * </ul>
 * <p>Sesión: S2. Compárese lado a lado con {@code pe.utec.consular.monolito.citas.application.service.RegistrarCitaService}.
 */
public class RegistrarCitaInteractor implements RegistrarCitaInputBoundary {

    private final CitaGateway citaGateway;
    private final CupoGateway cupoGateway;
    private final OficinaGateway oficinaGateway;
    private final NotificadorGateway notificadorGateway;
    private final RegistrarCitaOutputBoundary outputBoundary;

    public RegistrarCitaInteractor(CitaGateway citaGateway,
                                   CupoGateway cupoGateway,
                                   OficinaGateway oficinaGateway,
                                   NotificadorGateway notificadorGateway,
                                   RegistrarCitaOutputBoundary outputBoundary) {
        this.citaGateway = citaGateway;
        this.cupoGateway = cupoGateway;
        this.oficinaGateway = oficinaGateway;
        this.notificadorGateway = notificadorGateway;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void ejecutar(RegistrarCitaRequestModel request) {
        // 1. Validar existencia de la oficina consular
        if (!oficinaGateway.existe(request.oficinaId())) {
            throw new RecursoNoEncontradoException("No existe la oficina consular con identificador: " + request.oficinaId());
        }

        // 2. Construir y validar objetos de valor de la capa entities
        Dni dni = new Dni(request.dni());
        Email email = new Email(request.email());

        // 3. Validar ventana temporal (mañana a 30 días)
        ReglasDeReserva.validarVentanaTemporal(request.fecha(), LocalDate.now());

        // 4. Validar duplicidad de cita activa (misma fecha, misma oficina, mismo DNI)
        boolean duplicado = citaGateway.existeCitaActiva(dni.valor(), request.oficinaId(), request.fecha());
        ReglasDeReserva.validarNoDuplicada(duplicado, dni.valor(), request.oficinaId(), request.fecha());

        // 5. Adquirir el cupo con bloqueo pesimista
        Cupo cupo = cupoGateway.buscarPorOficinaFechaHoraConBloqueo(
                        request.oficinaId(), request.fecha(), request.hora())
                .orElseThrow(() -> new RecursoNoEncontradoException(String.format(
                        "No existe franja horaria configurada para la oficina %s en fecha %s a las %s",
                        request.oficinaId(), request.fecha(), request.hora())));

        // 6. Validar y decrementar capacidad del cupo
        cupo.reservar();
        cupoGateway.guardar(cupo);

        // 7. Generar código correlativo de cita (CIT-YYYY-NNNNNN)
        long secuencia = citaGateway.siguienteSecuencia();
        CodigoCita codigo = CodigoCita.generar(request.fecha().getYear(), secuencia);

        // 8. Crear y persistir la entidad Cita
        Cita cita = Cita.crearReserva(
                codigo,
                dni,
                request.nombres(),
                email,
                request.oficinaId(),
                cupo.getId(),
                request.fecha(),
                request.hora(),
                request.tipoTramite()
        );
        Cita citaGuardada = citaGateway.guardar(cita);

        // 9. Invocación síncrona del gateway de notificación (acoplamiento deliberado idéntico al monolito)
        notificadorGateway.notificarReserva(citaGuardada);

        // 10. Entregar el ResponseModel al OutputBoundary (Presenter)
        RegistrarCitaResponseModel responseModel = new RegistrarCitaResponseModel(
                citaGuardada.getId(),
                citaGuardada.getCodigo().valor(),
                citaGuardada.getDni().valor(),
                citaGuardada.getNombres(),
                citaGuardada.getOficinaId(),
                citaGuardada.getFecha(),
                citaGuardada.getHora(),
                citaGuardada.getTipoTramite(),
                citaGuardada.getEstado(),
                citaGuardada.getNotificacion(),
                citaGuardada.getCreadaEn()
        );
        outputBoundary.presentar(responseModel);
    }
}

