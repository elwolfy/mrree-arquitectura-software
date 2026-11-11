package pe.utec.consular.clean.citas.usecase.registrar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.utec.consular.clean.citas.entities.Cita;
import pe.utec.consular.clean.citas.entities.Cupo;
import pe.utec.consular.clean.citas.entities.EstadoCita;
import pe.utec.consular.clean.citas.entities.OficinaConsular;
import pe.utec.consular.clean.citas.entities.TipoTramite;
import pe.utec.consular.clean.citas.entities.exception.CitaDuplicadaException;
import pe.utec.consular.clean.citas.entities.exception.CupoSinCapacidadException;
import pe.utec.consular.clean.citas.entities.exception.NotificacionFallidaException;
import pe.utec.consular.clean.citas.interfaceadapters.presenter.CitaPresenter;
import pe.utec.consular.clean.citas.interfaceadapters.presenter.CitaViewModel;
import pe.utec.consular.clean.citas.usecase.CitaGateway;
import pe.utec.consular.clean.citas.usecase.CupoGateway;
import pe.utec.consular.clean.citas.usecase.NotificadorGateway;
import pe.utec.consular.clean.citas.usecase.OficinaGateway;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias puras (sin Spring) del {@link RegistrarCitaInteractor} utilizando dobles de prueba
 * en memoria para los Gateways y el {@link CitaPresenter}.
 * <p>Concepto: Testabilidad de Clean Architecture – el caso de uso y el presentador se prueban sin contenedor IoC ni base de datos.
 * <p>Sesión: S2.
 */
@DisplayName("Pruebas unitarias de RegistrarCitaInteractor con dobles de prueba y CitaPresenter")
class RegistrarCitaInteractorTest {

    private FakeCitaGateway citaGateway;
    private FakeCupoGateway cupoGateway;
    private FakeOficinaGateway oficinaGateway;
    private SpyNotificadorGateway notificadorGateway;
    private CitaPresenter citaPresenter;
    private RegistrarCitaInteractor interactor;

    private final LocalDate fechaValida = LocalDate.now().plusDays(5);
    private final LocalTime horaValida = LocalTime.of(9, 0);

    @BeforeEach
    void setUp() {
        citaGateway = new FakeCitaGateway();
        cupoGateway = new FakeCupoGateway();
        oficinaGateway = new FakeOficinaGateway();
        notificadorGateway = new SpyNotificadorGateway();
        citaPresenter = new CitaPresenter();

        oficinaGateway.agregar(new OficinaConsular(
                "MAD",
                "Consulado General del Perú en Madrid (laboratorio)",
                "ES",
                "Madrid",
                "Europe/Madrid"
        ));
        cupoGateway.guardar(Cupo.nuevo("MAD", fechaValida, horaValida, 2));

        interactor = new RegistrarCitaInteractor(
                citaGateway,
                cupoGateway,
                oficinaGateway,
                notificadorGateway,
                citaPresenter
        );
    }

    @Test
    @DisplayName("Registra una cita válida, notifica al ciudadano y entrega el ViewModel a través del CitaPresenter")
    void registraCitaExitosamenteYPresentaViewModel() {
        RegistrarCitaRequestModel request = new RegistrarCitaRequestModel(
                "40000001",
                "Ana Pérez",
                "ana@correo.lab",
                "MAD",
                fechaValida,
                horaValida,
                TipoTramite.LEGALIZACION
        );

        interactor.ejecutar(request);

        CitaViewModel viewModel = citaPresenter.obtenerViewModel();
        assertNotNull(viewModel, "El CitaPresenter debe contener el CitaViewModel producido por el OutputBoundary");
        assertEquals(String.format("CIT-%04d-000001", fechaValida.getYear()), viewModel.codigo());
        assertEquals("40000001", viewModel.dni());
        assertEquals("Ana Pérez", viewModel.nombres());
        assertEquals("MAD", viewModel.oficinaId());
        assertEquals(EstadoCita.RESERVADA, viewModel.estado());
        assertEquals("ENVIADA", viewModel.notificacion());
        assertTrue(notificadorGateway.notificado, "El NotificadorGateway debió ser invocado");
        assertEquals(1, cupoGateway.buscarPorOficinaFechaHoraConBloqueo("MAD", fechaValida, horaValida).orElseThrow().getOcupados());
    }

    @Test
    @DisplayName("Rechaza una cita duplicada para el mismo DNI, oficina y fecha")
    void rechazaCitaDuplicada() {
        RegistrarCitaRequestModel request = new RegistrarCitaRequestModel(
                "40000001",
                "Ana Pérez",
                "ana@correo.lab",
                "MAD",
                fechaValida,
                horaValida,
                TipoTramite.LEGALIZACION
        );

        interactor.ejecutar(request);
        citaPresenter.obtenerViewModel();

        assertThrows(CitaDuplicadaException.class, () -> interactor.ejecutar(request));
        assertNull(citaPresenter.obtenerViewModel(), "Ante error no debe presentarse un nuevo ViewModel");
    }

    @Test
    @DisplayName("Rechaza la reserva cuando el cupo ya no tiene capacidad disponible")
    void rechazaCuandoCupoSinCapacidad() {
        interactor.ejecutar(new RegistrarCitaRequestModel(
                "40000001", "Ana Pérez", "ana@correo.lab", "MAD", fechaValida, horaValida, TipoTramite.LEGALIZACION));
        citaPresenter.obtenerViewModel();

        interactor.ejecutar(new RegistrarCitaRequestModel(
                "40000002", "Carlos Gómez", "carlos@correo.lab", "MAD", fechaValida, horaValida, TipoTramite.APOSTILLA));
        citaPresenter.obtenerViewModel();

        RegistrarCitaRequestModel terceraSolicitud = new RegistrarCitaRequestModel(
                "40000003", "Beatriz Ramos", "beatriz@correo.lab", "MAD", fechaValida, horaValida, TipoTramite.LEGALIZACION);

        assertThrows(CupoSinCapacidadException.class, () -> interactor.ejecutar(terceraSolicitud));
    }

    @Test
    @DisplayName("Propaga NotificacionFallidaException y no invoca al Presenter cuando falla el NotificadorGateway")
    void propagaErrorCuandoFallaNotificador() {
        notificadorGateway.simularFalla = true;
        RegistrarCitaRequestModel request = new RegistrarCitaRequestModel(
                "40000004",
                "Diana Salazar",
                "diana@correo.lab",
                "MAD",
                fechaValida,
                horaValida,
                TipoTramite.APOSTILLA
        );

        assertThrows(NotificacionFallidaException.class, () -> interactor.ejecutar(request));
        assertNull(citaPresenter.obtenerViewModel());
    }

    // =========================================================================
    // Dobles de prueba en memoria (Fakes / Spies)
    // =========================================================================

    private static class FakeCitaGateway implements CitaGateway {
        private final List<Cita> citas = new ArrayList<>();
        private long secuencia = 0;

        @Override
        public Cita guardar(Cita cita) {
            citas.removeIf(c -> c.getId().equals(cita.getId()));
            citas.add(cita);
            return cita;
        }

        @Override
        public Optional<Cita> buscarPorId(UUID id) {
            return citas.stream().filter(c -> c.getId().equals(id)).findFirst();
        }

        @Override
        public List<Cita> buscarPorDni(String dni) {
            return citas.stream().filter(c -> c.getDni().valor().equals(dni)).toList();
        }

        @Override
        public boolean existeCitaActiva(String dni, String oficinaId, LocalDate fecha) {
            return citas.stream().anyMatch(c ->
                    c.getDni().valor().equals(dni)
                            && c.getOficinaId().equals(oficinaId)
                            && c.getFecha().equals(fecha)
                            && c.getEstado() == EstadoCita.RESERVADA);
        }

        @Override
        public long siguienteSecuencia() {
            return ++secuencia;
        }
    }

    private static class FakeCupoGateway implements CupoGateway {
        private final List<Cupo> cupos = new ArrayList<>();

        @Override
        public Optional<Cupo> buscarPorOficinaFechaHoraConBloqueo(String oficinaId, LocalDate fecha, LocalTime hora) {
            return cupos.stream()
                    .filter(c -> c.getOficinaId().equals(oficinaId) && c.getFecha().equals(fecha) && c.getHora().equals(hora))
                    .findFirst();
        }

        @Override
        public List<Cupo> buscarPorOficinaYFecha(String oficinaId, LocalDate fecha) {
            return cupos.stream()
                    .filter(c -> c.getOficinaId().equals(oficinaId) && c.getFecha().equals(fecha))
                    .toList();
        }

        @Override
        public boolean existenCuposParaOficinaYFecha(String oficinaId, LocalDate fecha) {
            return !buscarPorOficinaYFecha(oficinaId, fecha).isEmpty();
        }

        @Override
        public Cupo guardar(Cupo cupo) {
            cupos.removeIf(c -> c.getId().equals(cupo.getId()));
            cupos.add(cupo);
            return cupo;
        }

        @Override
        public void guardarTodos(List<Cupo> nuevos) {
            nuevos.forEach(this::guardar);
        }
    }

    private static class FakeOficinaGateway implements OficinaGateway {
        private final List<OficinaConsular> oficinas = new ArrayList<>();

        void agregar(OficinaConsular oficina) {
            oficinas.add(oficina);
        }

        @Override
        public List<OficinaConsular> listarTodas() {
            return List.copyOf(oficinas);
        }

        @Override
        public Optional<OficinaConsular> buscarPorId(String id) {
            return oficinas.stream().filter(o -> o.getId().equals(id)).findFirst();
        }

        @Override
        public boolean existe(String id) {
            return buscarPorId(id).isPresent();
        }
    }

    private static class SpyNotificadorGateway implements NotificadorGateway {
        boolean simularFalla = false;
        boolean notificado = false;

        @Override
        public void notificarReserva(Cita cita) {
            if (simularFalla) {
                throw new NotificacionFallidaException("Falla simulada en doble de prueba");
            }
            this.notificado = true;
        }
    }
}

