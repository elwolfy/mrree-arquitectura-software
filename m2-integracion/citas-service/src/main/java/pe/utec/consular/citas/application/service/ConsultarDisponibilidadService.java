package pe.utec.consular.citas.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.citas.application.port.in.ConsultarDisponibilidadUseCase;
import pe.utec.consular.citas.application.port.out.CupoRepositoryPort;
import pe.utec.consular.citas.application.port.out.OficinaRepositoryPort;
import pe.utec.consular.citas.domain.Cupo;
import pe.utec.consular.citas.domain.Disponibilidad;
import pe.utec.consular.citas.domain.Disponibilidad.CupoDisponibilidad;
import pe.utec.consular.citas.domain.exception.BaseDeDatosNoDisponibleException;
import pe.utec.consular.citas.domain.exception.RecursoNoEncontradoException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Servicio de aplicación para consultar disponibilidad de cupos en una oficina consular.
 * <p>Concepto: Arquitectura hexagonal – caso de uso de consulta (sección 1.3).
 * <p>Sesión: S3.
 */
@Service
@Transactional(readOnly = true)
public class ConsultarDisponibilidadService implements ConsultarDisponibilidadUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConsultarDisponibilidadService.class);

    private final CupoRepositoryPort cupoRepositoryPort;
    private final OficinaRepositoryPort oficinaRepositoryPort;

    public ConsultarDisponibilidadService(CupoRepositoryPort cupoRepositoryPort,
                                          OficinaRepositoryPort oficinaRepositoryPort) {
        this.cupoRepositoryPort = cupoRepositoryPort;
        this.oficinaRepositoryPort = oficinaRepositoryPort;
    }

    @Override
    public Disponibilidad consultar(String oficinaId, LocalDate fecha) {
        log.info("Consultando disponibilidad para oficina {} en fecha {}", oficinaId, fecha);
        try {
            if (!oficinaRepositoryPort.existe(oficinaId)) {
                throw new RecursoNoEncontradoException("No existe la oficina consular con identificador: " + oficinaId);
            }

            List<Cupo> cuposBd = cupoRepositoryPort.buscarPorOficinaYFecha(oficinaId, fecha);
            List<CupoDisponibilidad> items = cuposBd.stream()
                    .sorted(Comparator.comparing(Cupo::getHora))
                    .map(c -> new CupoDisponibilidad(c.getHora(), c.getCapacidad(), c.getOcupados(), c.cuposLibres()))
                    .toList();

            return new Disponibilidad(oficinaId, fecha, items, "BASE_DE_DATOS", 0);
        } catch (RecursoNoEncontradoException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Error al acceder a la base de datos para oficina {} fecha {}: {}", oficinaId, fecha, ex.getMessage());
            throw new BaseDeDatosNoDisponibleException("La base de datos no responde o la conexión fue interrumpida", ex);
        }
    }
}

