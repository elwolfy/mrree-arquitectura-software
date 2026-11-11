package pe.utec.consular.clean.citas.usecase.disponibilidad;

import pe.utec.consular.clean.citas.entities.Cupo;
import pe.utec.consular.clean.citas.entities.Disponibilidad;
import pe.utec.consular.clean.citas.entities.Disponibilidad.CupoDisponibilidad;
import pe.utec.consular.clean.citas.entities.ModoConsistencia;
import pe.utec.consular.clean.citas.entities.exception.BaseDeDatosNoDisponibleException;
import pe.utec.consular.clean.citas.entities.exception.RecursoNoEncontradoException;
import pe.utec.consular.clean.citas.usecase.CupoGateway;
import pe.utec.consular.clean.citas.usecase.DisponibilidadCacheGateway;
import pe.utec.consular.clean.citas.usecase.OficinaGateway;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * Interactor para consultar disponibilidad implementando el Teorema CAP (CP vs AP).
 * <p>Concepto: Clean Architecture – Interactor sin dependencias de Spring (secciones 1.3 y 1.4 del plan).
 * Recibe un {@code Supplier<ModoConsistencia>} para consultar el modo activo sin acoplarse
 * a {@code @ConfigurationProperties} de Spring Boot.
 * <p>Sesión: S2. Compárese con {@code ConsultarDisponibilidadService} en {@code monolito-modular}.
 */
public class ConsultarDisponibilidadInteractor implements ConsultarDisponibilidadInputBoundary {

    private final CupoGateway cupoGateway;
    private final OficinaGateway oficinaGateway;
    private final DisponibilidadCacheGateway disponibilidadCacheGateway;
    private final Supplier<ModoConsistencia> proveedorModoConsistencia;
    private final ConsultarDisponibilidadOutputBoundary outputBoundary;

    public ConsultarDisponibilidadInteractor(CupoGateway cupoGateway,
                                             OficinaGateway oficinaGateway,
                                             DisponibilidadCacheGateway disponibilidadCacheGateway,
                                             Supplier<ModoConsistencia> proveedorModoConsistencia,
                                             ConsultarDisponibilidadOutputBoundary outputBoundary) {
        this.cupoGateway = cupoGateway;
        this.oficinaGateway = oficinaGateway;
        this.disponibilidadCacheGateway = disponibilidadCacheGateway;
        this.proveedorModoConsistencia = proveedorModoConsistencia;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void consultar(ConsultarDisponibilidadRequestModel request) {
        String oficinaId = request.oficinaId();
        var fecha = request.fecha();
        ModoConsistencia modo = proveedorModoConsistencia.get();

        try {
            if (!oficinaGateway.existe(oficinaId)) {
                throw new RecursoNoEncontradoException("No existe la oficina consular con identificador: " + oficinaId);
            }

            List<Cupo> cuposBd = cupoGateway.buscarPorOficinaYFecha(oficinaId, fecha);
            List<CupoDisponibilidad> items = cuposBd.stream()
                    .sorted(Comparator.comparing(Cupo::getHora))
                    .map(c -> new CupoDisponibilidad(c.getHora(), c.getCapacidad(), c.getOcupados(), c.cuposLibres()))
                    .toList();

            Disponibilidad dispBd = new Disponibilidad(oficinaId, fecha, items, "BASE_DE_DATOS", 0);
            disponibilidadCacheGateway.guardar(oficinaId, fecha, dispBd);

            outputBoundary.presentar(new ConsultarDisponibilidadResponseModel(
                    dispBd.oficinaId(),
                    dispBd.fecha(),
                    dispBd.cupos(),
                    dispBd.origen(),
                    dispBd.actualizadoHaceSegundos()
            ));
        } catch (RecursoNoEncontradoException ex) {
            throw ex;
        } catch (Exception ex) {
            if (modo == ModoConsistencia.AP) {
                Disponibilidad dispCache = disponibilidadCacheGateway.obtener(oficinaId, fecha)
                        .orElseThrow(() -> new BaseDeDatosNoDisponibleException(
                                "Base de datos no disponible y no se hallaron registros en caché para oficina "
                                        + oficinaId + " fecha " + fecha, ex));

                outputBoundary.presentar(new ConsultarDisponibilidadResponseModel(
                        dispCache.oficinaId(),
                        dispCache.fecha(),
                        dispCache.cupos(),
                        dispCache.origen(),
                        dispCache.actualizadoHaceSegundos()
                ));
                return;
            }
            throw new BaseDeDatosNoDisponibleException("La base de datos no responde o la conexión fue interrumpida (modo CP)", ex);
        }
    }
}

