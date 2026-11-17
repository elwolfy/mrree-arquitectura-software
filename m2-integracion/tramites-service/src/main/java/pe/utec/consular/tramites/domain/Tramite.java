package pe.utec.consular.tramites.domain;

import pe.utec.consular.tramites.domain.exception.ReglaNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Abstracción raíz de dominio que modela un trámite consular y su ciclo de vida.
 * <p>Concepto: Polimorfismo en el dominio y jerarquía creada mediante Factory Method (sección 2.1).
 * <p>Sesión: S2 y S3.
 */
public abstract class Tramite {

    private static final Set<String> PAISES_SUDAMERICA = Set.of("PE", "AR", "BO", "BR", "CL", "CO", "EC", "GY", "PY", "SR", "UY", "VE");
    private static final BigDecimal RECARGO_EXTERIOR = new BigDecimal("1.10");

    private final UUID id;
    private final CodigoTramite codigo;
    private final Dni dni;
    private final String nombres;
    private final Email email;
    private final TipoTramite tipoTramite;
    private final String pais;
    private final int folios;
    private final String idioma;
    private EstadoTramite estado;
    private final BigDecimal monto;
    private final Instant creadoEn;

    protected Tramite(UUID id,
                      CodigoTramite codigo,
                      Dni dni,
                      String nombres,
                      Email email,
                      TipoTramite tipoTramite,
                      String pais,
                      int folios,
                      String idioma,
                      EstadoTramite estado,
                      BigDecimal monto,
                      Instant creadoEn) {
        this.id = Objects.requireNonNull(id, "El identificador del trámite no puede ser nulo");
        this.codigo = Objects.requireNonNull(codigo, "El código del trámite no puede ser nulo");
        this.dni = Objects.requireNonNull(dni, "El DNI no puede ser nulo");
        this.nombres = Objects.requireNonNull(nombres, "Los nombres no pueden ser nulos");
        this.email = Objects.requireNonNull(email, "El correo no puede ser nulo");
        this.tipoTramite = Objects.requireNonNull(tipoTramite, "El tipo de trámite no puede ser nulo");
        this.pais = Objects.requireNonNull(pais, "El país no puede ser nulo");
        if (folios <= 0) {
            throw new IllegalArgumentException("El número de folios debe ser positivo");
        }
        this.folios = folios;
        this.idioma = Objects.requireNonNull(idioma, "El idioma no puede ser nulo");
        this.estado = Objects.requireNonNull(estado, "El estado no puede ser nulo");
        this.monto = Objects.requireNonNull(monto, "El monto no puede ser nulo").setScale(2, RoundingMode.HALF_UP);
        this.creadoEn = creadoEn != null ? creadoEn : Instant.now();
    }

    protected static BigDecimal aplicarRecargoExteriorSiCorresponde(BigDecimal montoBase, String pais) {
        if (PAISES_SUDAMERICA.contains(pais.toUpperCase())) {
            return montoBase.setScale(2, RoundingMode.HALF_UP);
        }
        return montoBase.multiply(RECARGO_EXTERIOR).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Transiciona el trámite desde {@code REGISTRADO} hacia {@code PENDIENTE_PAGO}
     * tras completar su registro inicial en M2.
     */
    public void pasarAPendientePago() {
        if (this.estado != EstadoTramite.REGISTRADO) {
            throw new ReglaNegocioException(
                    "Solo un trámite en estado REGISTRADO puede pasar a PENDIENTE_PAGO (estado actual: " + this.estado + ")");
        }
        this.estado = EstadoTramite.PENDIENTE_PAGO;
    }

    /**
     * Nombre concreto del subtipo de dominio ({@code Legalizacion}, {@code Apostilla} o {@code RegistroConsular}).
     */
    public abstract String getSubtipo();

    /**
     * Descripción de la modalidad de verificación o procesamiento propia del subtipo.
     */
    public abstract String getModalidadProcesamiento();

    public UUID getId() {
        return id;
    }

    public CodigoTramite getCodigo() {
        return codigo;
    }

    public Dni getDni() {
        return dni;
    }

    public String getNombres() {
        return nombres;
    }

    public Email getEmail() {
        return email;
    }

    public TipoTramite getTipoTramite() {
        return tipoTramite;
    }

    public String getPais() {
        return pais;
    }

    public int getFolios() {
        return folios;
    }

    public String getIdioma() {
        return idioma;
    }

    public EstadoTramite getEstado() {
        return estado;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}

