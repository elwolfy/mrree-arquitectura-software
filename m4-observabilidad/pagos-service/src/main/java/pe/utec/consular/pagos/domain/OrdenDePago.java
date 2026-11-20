package pe.utec.consular.pagos.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio que modela una obligación de cobro arancelario para un trámite.
 * <p>Concepto: <b>Lenguaje Ubicuo por Bounded Context (DDD – sección 2.4)</b>.
 * Mientras que en el contexto de Trámites el pedido inicial del ciudadano se modela como
 * {@code Solicitud} (con folios, idioma y requisitos documentales), en el contexto de Pagos
 * el mismo proceso origina una {@code OrdenDePago} centrada exclusivamente en el monto,
 * moneda, estado financiero y clave de idempotencia.
 * <p>Véase {@code docs/ddd/lenguaje-ubicuo.md} para la comparativa de modelos por contexto delimitado.
 * <p>Sesión: S3 (en M2 solo existe el modelo de dominio y el cálculo de tarifas; la persistencia
 * y confirmación de pagos se activan en M3).
 */
public final class OrdenDePago {

    private final UUID id;
    private final UUID tramiteId;
    private final BigDecimal monto;
    private final String moneda;
    private EstadoPago estado;
    private final String claveIdempotencia;
    private final Instant creadaEn;

    public OrdenDePago(UUID id,
                       UUID tramiteId,
                       BigDecimal monto,
                       String moneda,
                       EstadoPago estado,
                       String claveIdempotencia,
                       Instant creadaEn) {
        this.id = Objects.requireNonNull(id, "El id de la orden de pago no puede ser nulo");
        this.tramiteId = Objects.requireNonNull(tramiteId, "El id del trámite no puede ser nulo");
        this.monto = Objects.requireNonNull(monto, "El monto no puede ser nulo");
        this.moneda = Objects.requireNonNull(moneda, "La moneda no puede ser nula");
        this.estado = Objects.requireNonNull(estado, "El estado de pago no puede ser nulo");
        this.claveIdempotencia = Objects.requireNonNull(claveIdempotencia, "La clave de idempotencia no puede ser nula");
        this.creadaEn = creadaEn != null ? creadaEn : Instant.now();
    }

    public static OrdenDePago nuevaPendiente(UUID tramiteId, BigDecimal monto, String claveIdempotencia) {
        return new OrdenDePago(
                UUID.randomUUID(),
                tramiteId,
                monto,
                "PEN",
                EstadoPago.PENDIENTE,
                claveIdempotencia,
                Instant.now()
        );
    }

    public void confirmar() {
        this.estado = EstadoPago.CONFIRMADO;
    }

    public void anular() {
        this.estado = EstadoPago.ANULADO;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTramiteId() {
        return tramiteId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getMoneda() {
        return moneda;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public String getClaveIdempotencia() {
        return claveIdempotencia;
    }

    public Instant getCreadaEn() {
        return creadaEn;
    }
}

