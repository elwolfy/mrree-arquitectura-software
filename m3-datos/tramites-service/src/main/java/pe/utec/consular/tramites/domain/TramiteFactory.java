package pe.utec.consular.tramites.domain;

import pe.utec.consular.tramites.domain.exception.SolicitudInvalidaException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fábrica de dominio para la creación polimórfica de subtipos de {@link Tramite}.
 * <p>Concepto: <b>Patrón Creacional Factory Method</b> (sección 2.1).
 * <ul>
 *   <li><b>Patrón aplicado:</b> Factory Method estático de dominio que encapsula la decisión
 *       de qué subclase concreta ({@link Legalizacion}, {@link Apostilla} o {@link RegistroConsular})
 *       instanciar a partir del {@link TipoTramite} y de una {@link Solicitud} previamente validada.</li>
 *   <li><b>Problema que resuelve:</b> Desacopla los casos de uso de la capa de aplicación
 *       ({@code RegistrarTramiteService}) y los adaptadores de las clases concretas de trámite.
 *       Evita que existan bloques {@code switch} o verificaciones {@code instanceof} dispersos
 *       en la capa de aplicación cada vez que se inicia o reconstruye un trámite.</li>
 *   <li><b>Costo / Trade-off:</b> Introduce una jerarquía de subclases en el dominio; cuando se
 *       incorpora una nueva familia de trámite consular, es necesario definir su subclase concreta
 *       y registrar su rama de creación en esta única fábrica.</li>
 * </ul>
 * <p>Sesión: S2 y S3.
 */
public final class TramiteFactory {

    private static final AtomicLong SECUENCIA_MEMORIA = new AtomicLong(1L);

    private TramiteFactory() {
    }

    /**
     * Crea la instancia concreta de {@link Tramite} correspondiente al {@code tipo} indicado,
     * en estado inicial {@link EstadoTramite#REGISTRADO}.
     *
     * @param tipo      tipo de trámite consular solicitado
     * @param solicitud solicitud inmutable construida con {@link Solicitud#builder()}
     * @return instancia de {@link Legalizacion}, {@link Apostilla} o {@link RegistroConsular}
     * @throws SolicitudInvalidaException si {@code tipo} o {@code solicitud} son nulos o incompatibles
     */
    public static Tramite crear(TipoTramite tipo, Solicitud solicitud) {
        return crear(tipo, solicitud, CodigoTramite.generar(Year.now().getValue(), SECUENCIA_MEMORIA.getAndIncrement()));
    }

    /**
     * Crea la instancia concreta de {@link Tramite} utilizando un {@link CodigoTramite} correlativo
     * suministrado por la secuencia de persistencia.
     */
    public static Tramite crear(TipoTramite tipo, Solicitud solicitud, CodigoTramite codigo) {
        if (tipo == null) {
            throw new SolicitudInvalidaException("El tipo de trámite no puede ser nulo al invocar TramiteFactory.crear");
        }
        if (solicitud == null) {
            throw new SolicitudInvalidaException("La solicitud no puede ser nula al invocar TramiteFactory.crear");
        }
        if (tipo != solicitud.getTipo()) {
            throw new SolicitudInvalidaException(String.format(
                    "El tipo solicitado en la fábrica (%s) no coincide con el tipo de la solicitud (%s)",
                    tipo, solicitud.getTipo()));
        }

        return switch (tipo) {
            case LEGALIZACION -> Legalizacion.crearNueva(codigo, solicitud);
            case APOSTILLA -> Apostilla.crearNueva(codigo, solicitud);
            case REGISTRO_CONSULAR -> RegistroConsular.crearNuevo(codigo, solicitud);
        };
    }

    /**
     * Reconstruye la subclase concreta de {@link Tramite} desde el almacenamiento persistente.
     */
    public static Tramite reconstruir(UUID id,
                                      CodigoTramite codigo,
                                      Dni dni,
                                      String nombres,
                                      Email email,
                                      TipoTramite tipo,
                                      String pais,
                                      int folios,
                                      String idioma,
                                      EstadoTramite estado,
                                      BigDecimal monto,
                                      Instant creadoEn) {
        if (tipo == null) {
            throw new SolicitudInvalidaException("El tipo de trámite almacenado no puede ser nulo");
        }
        return switch (tipo) {
            case LEGALIZACION -> new Legalizacion(id, codigo, dni, nombres, email, pais, folios, idioma, estado, monto, creadoEn);
            case APOSTILLA -> new Apostilla(id, codigo, dni, nombres, email, pais, folios, idioma, estado, monto, creadoEn);
            case REGISTRO_CONSULAR -> new RegistroConsular(id, codigo, dni, nombres, email, pais, folios, idioma, estado, monto, creadoEn);
        };
    }
}

