package pe.utec.consular.monolito.citas.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pe.utec.consular.monolito.citas.application.port.in.ConsultarDisponibilidadUseCase;
import pe.utec.consular.monolito.citas.application.port.out.CupoRepositoryPort;
import pe.utec.consular.monolito.citas.application.port.out.DisponibilidadCachePort;
import pe.utec.consular.monolito.citas.application.port.out.OficinaRepositoryPort;
import pe.utec.consular.monolito.citas.domain.Cupo;
import pe.utec.consular.monolito.citas.domain.Disponibilidad;
import pe.utec.consular.monolito.citas.domain.Disponibilidad.CupoDisponibilidad;
import pe.utec.consular.monolito.compartido.domain.ModoConsistencia;
import pe.utec.consular.monolito.compartido.domain.exception.BaseDeDatosNoDisponibleException;
import pe.utec.consular.monolito.compartido.domain.exception.RecursoNoEncontradoException;
import pe.utec.consular.monolito.compartido.infraestructura.ConsistenciaProperties;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Servicio de aplicación para consultar disponibilidad implementando el Teorema CAP.
 * <p>Concepto: Teorema CAP / PACELC (sección 1.4 del plan y regla 6 del contrato global).
 * <ul>
 *   <li><b>Modo CP:</b> La consulta va directo a la BD. Si la BD no responde (partición de red o timeout),
 *       falla inmediatamente con HTTP 503 {@code base-de-datos-no-disponible}.</li>
 *   <li><b>Modo AP:</b> Si la BD no responde, recurre a la memoria caché local (Caffeine), respondiendo
 *       HTTP 200 con {@code origen: CACHE} y la antigüedad en segundos.</li>
 * </ul>
 * <p>Sesión: S1.
 */
@Service
public class ConsultarDisponibilidadService implements ConsultarDisponibilidadUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConsultarDisponibilidadService.class);

    private final CupoRepositoryPort cupoRepositoryPort;
    private final OficinaRepositoryPort oficinaRepositoryPort;
    private final DisponibilidadCachePort disponibilidadCachePort;
    private final ConsistenciaProperties consistenciaProperties;

    public ConsultarDisponibilidadService(CupoRepositoryPort cupoRepositoryPort,
                                          OficinaRepositoryPort oficinaRepositoryPort,
                                          DisponibilidadCachePort disponibilidadCachePort,
                                          ConsistenciaProperties consistenciaProperties) {
        this.cupoRepositoryPort = cupoRepositoryPort;
        this.oficinaRepositoryPort = oficinaRepositoryPort;
        this.disponibilidadCachePort = disponibilidadCachePort;
        this.consistenciaProperties = consistenciaProperties;
    }

    @Override
    public Disponibilidad consultar(String oficinaId, LocalDate fecha) {
        ModoConsistencia modo = consistenciaProperties.getModo();
        log.info("Consultando disponibilidad para oficina {} en fecha {} bajo modo {}", oficinaId, fecha, modo);

        try {
            // Intentar consulta a la base de datos primaria
            if (!oficinaRepositoryPort.existe(oficinaId)) {
                throw new RecursoNoEncontradoException("No existe la oficina consular con identificador: " + oficinaId);
            }

            List<Cupo> cuposBd = cupoRepositoryPort.buscarPorOficinaYFecha(oficinaId, fecha);
            List<CupoDisponibilidad> items = cuposBd.stream()
                    .sorted(Comparator.comparing(Cupo::getHora))
                    .map(c -> new CupoDisponibilidad(c.getHora(), c.getCapacidad(), c.getOcupados(), c.cuposLibres()))
                    .toList();

            Disponibilidad dispBd = new Disponibilidad(oficinaId, fecha, items, "BASE_DE_DATOS", 0);

            // Refrescar caché local
            disponibilidadCachePort.guardar(oficinaId, fecha, dispBd);
            return dispBd;

        } catch (RecursoNoEncontradoException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Error al acceder a la base de datos para oficina {} fecha {}: {}", oficinaId, fecha, ex.getMessage());

            if (modo == ModoConsistencia.AP) {
                log.info("Modo AP activo: buscando disponibilidad en caché local para oficina {} fecha {}", oficinaId, fecha);
                return disponibilidadCachePort.obtener(oficinaId, fecha)
                        .orElseThrow(() -> new BaseDeDatosNoDisponibleException(
                                "Base de datos no disponible y no se hallaron registros en caché para oficina " + oficinaId + " fecha " + fecha, ex));
            }

            // Modo CP: rechazo estricto ante falla de consistencia
            throw new BaseDeDatosNoDisponibleException("La base de datos no responde o la conexión fue interrumpida (modo CP)", ex);
        }
    }
}
