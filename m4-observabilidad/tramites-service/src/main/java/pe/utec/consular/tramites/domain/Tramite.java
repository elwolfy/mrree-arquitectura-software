package pe.utec.consular.tramites.domain;

import pe.utec.consular.tramites.domain.evento.EmisionConError;
import pe.utec.consular.tramites.domain.evento.EmisionSolicitada;
import pe.utec.consular.tramites.domain.evento.EventoDominioTramite;
import pe.utec.consular.tramites.domain.evento.EventoVersionadoTramite;
import pe.utec.consular.tramites.domain.evento.PagoAnulacionSolicitada;
import pe.utec.consular.tramites.domain.evento.PagoAsociado;
import pe.utec.consular.tramites.domain.evento.TramiteEmitido;
import pe.utec.consular.tramites.domain.evento.TramiteRechazado;
import pe.utec.consular.tramites.domain.evento.TramiteRegistrado;
import pe.utec.consular.tramites.domain.exception.ConflictoVersionException;
import pe.utec.consular.tramites.domain.exception.TransicionEstadoInvalidaException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Agregado raíz de dominio que modela un trámite consular y su ciclo de vida mediante <b>Event Sourcing</b>.
 *
 * <h3>¿Qué es Event Sourcing y por qué se aplica en {@code Tramite}?</h3>
 * <p>En un modelo CRUD tradicional solo se persiste una instantánea mutable del estado actual
 * (por ejemplo, {@code UPDATE tramite SET estado = 'EMITIDO'}), perdiendo la historia de cómo y cuándo
 * ocurrieron las transiciones intermedias. Con <b>Event Sourcing</b>, la fuente de la verdad del agregado
 * es una secuencia ordenada e inmutable de eventos de dominio ({@code tramites.eventos_tramite},
 * <i>append-only</i>):
 * <ol>
 *   <li>{@link TramiteRegistrado} → crea el trámite y lo sitúa en {@link EstadoTramite#PENDIENTE_PAGO}.</li>
 *   <li>{@link PagoAsociado} → asocia el pago confirmado y avanza a {@link EstadoTramite#PAGADO}.</li>
 *   <li>{@link EmisionSolicitada} → registra la orden de emisión y avanza a {@link EstadoTramite#EN_EMISION}.</li>
 *   <li>{@link TramiteEmitido} → registra el número de documento emitido y culmina en {@link EstadoTramite#EMITIDO}.</li>
 *   <li>{@link EmisionConError} / {@link PagoAnulacionSolicitada} / {@link TramiteRechazado} → registran fallas
 *       de emisión y pasos de compensación de la saga hacia {@link EstadoTramite#RECHAZADO}.</li>
 * </ol>
 * <p>Para ejecutar un comando sobre un trámite existente, el repositorio lee todos sus eventos pasados
 * ordenados por {@code version} y llama a {@link #reconstruirDesdeEventos(List)} (o
 * {@link #reconstruirDesdeEventosVersionados(List)}). Cada nuevo cambio valida las invariantes del estado
 * actual, emite un nuevo {@link EventoDominioTramite}, incrementa la {@code version} del agregado y
 * utiliza control de concurrencia optimista ({@code UNIQUE(tramite_id, version)}) al persistir.
 *
 * <p>Conceptos: Polimorfismo + Factory Method (S2), Event Sourcing + Concurrencia Optimista + CQRS (S4 y S5).
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

    private int versionBase;
    private int version;
    private UUID pagoId;
    private String numeroDocumento;
    private String motivoRechazo;
    private String ultimoErrorEmision;
    private Instant actualizadoEn;
    private final List<EventoVersionadoTramite> eventosPendientes = new ArrayList<>();

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
        this.actualizadoEn = this.creadoEn;
        this.versionBase = 0;
        this.version = 0;
    }

    protected static BigDecimal aplicarRecargoExteriorSiCorresponde(BigDecimal montoBase, String pais) {
        if (PAISES_SUDAMERICA.contains(pais.toUpperCase())) {
            return montoBase.setScale(2, RoundingMode.HALF_UP);
        }
        return montoBase.multiply(RECARGO_EXTERIOR).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Reconstruye determinísticamente un agregado {@link Tramite} aplicando en orden su secuencia
     * de eventos de dominio desde la versión 1.
     */
    public static Tramite reconstruirDesdeEventos(List<EventoDominioTramite> historial) {
        if (historial == null || historial.isEmpty()) {
            throw new IllegalArgumentException("No se puede reconstruir un trámite a partir de un historial de eventos vacío");
        }
        List<EventoVersionadoTramite> versionados = new ArrayList<>(historial.size());
        for (int i = 0; i < historial.size(); i++) {
            versionados.add(new EventoVersionadoTramite(i + 1, historial.get(i)));
        }
        return reconstruirDesdeEventosVersionados(versionados);
    }

    /**
     * Reconstruye un agregado {@link Tramite} validando que las versiones de los eventos almacenados
     * formen una secuencia estricta y continua {@code 1..N}.
     *
     * @throws ConflictoVersionException si la secuencia de versiones presenta saltos o colisiones
     */
    public static Tramite reconstruirDesdeEventosVersionados(List<EventoVersionadoTramite> historialVersionado) {
        if (historialVersionado == null || historialVersionado.isEmpty()) {
            throw new IllegalArgumentException("No se puede reconstruir un trámite a partir de un historial de eventos vacío");
        }
        EventoVersionadoTramite primero = historialVersionado.get(0);
        if (primero.version() != 1) {
            throw new ConflictoVersionException(
                    "El primer evento del flujo debe tener versión 1, pero se encontró versión " + primero.version());
        }
        if (!(primero.evento() instanceof TramiteRegistrado evInicial)) {
            throw new TransicionEstadoInvalidaException(
                    "El flujo de eventos de un trámite debe iniciar con TramiteRegistrado, pero inició con " + primero.evento().tipo());
        }

        Tramite tramite = TramiteFactory.reconstruir(
                evInicial.tramiteId(),
                new CodigoTramite(evInicial.codigo()),
                new Dni(evInicial.dni()),
                evInicial.nombres(),
                new Email(evInicial.email()),
                evInicial.tipoTramite(),
                evInicial.pais(),
                evInicial.folios(),
                evInicial.idioma(),
                EstadoTramite.REGISTRADO,
                evInicial.monto(),
                evInicial.ocurridoEn()
        );

        int versionEsperada = 1;
        for (EventoVersionadoTramite item : historialVersionado) {
            if (item.version() != versionEsperada) {
                throw new ConflictoVersionException(String.format(
                        "Conflicto de versión al reconstruir trámite %s: se esperaba versión %d pero se recibió versión %d",
                        tramite.getId(), versionEsperada, item.version()));
            }
            tramite.aplicarEventoHistorico(item.version(), item.evento());
            versionEsperada++;
        }
        tramite.marcarCambiosConfirmados();
        return tramite;
    }

    /**
     * Transiciona el trámite recién creado desde {@code REGISTRADO} hacia {@code PENDIENTE_PAGO}
     * emitiendo el primer evento de dominio {@link TramiteRegistrado} (versión 1).
     */
    public void pasarAPendientePago() {
        if (this.estado != EstadoTramite.REGISTRADO) {
            throw new TransicionEstadoInvalidaException(
                    "Solo un trámite en estado REGISTRADO puede pasar a PENDIENTE_PAGO (estado actual: " + this.estado + ")");
        }
        registrarNuevoEvento(new TramiteRegistrado(
                this.id,
                this.codigo.valor(),
                this.dni.valor(),
                this.nombres,
                this.email.valor(),
                this.tipoTramite,
                this.pais,
                this.folios,
                this.idioma,
                this.monto,
                this.creadoEn
        ));
    }

    /**
     * Asocia un pago confirmado al trámite y transiciona de {@code PENDIENTE_PAGO} a {@code PAGADO}.
     */
    public void asociarPago(UUID pagoId, BigDecimal montoPagado) {
        Objects.requireNonNull(pagoId, "El identificador del pago no puede ser nulo");
        Objects.requireNonNull(montoPagado, "El monto pagado no puede ser nulo");
        if (this.estado != EstadoTramite.PENDIENTE_PAGO) {
            throw new TransicionEstadoInvalidaException(
                    "Solo se puede asociar un pago a un trámite en estado PENDIENTE_PAGO (estado actual: " + this.estado + ")");
        }
        registrarNuevoEvento(new PagoAsociado(this.id, pagoId, montoPagado, Instant.now()));
    }

    /**
     * Registra la solicitud de emisión del documento consular y transiciona de {@code PAGADO} a {@code EN_EMISION}.
     */
    public void solicitarEmision() {
        if (this.estado != EstadoTramite.PAGADO) {
            throw new TransicionEstadoInvalidaException(
                    "Solo se puede solicitar emisión para un trámite en estado PAGADO (estado actual: " + this.estado + ")");
        }
        registrarNuevoEvento(new EmisionSolicitada(this.id, Instant.now()));
    }

    /**
     * Registra la emisión exitosa del documento consular y transiciona de {@code EN_EMISION} a {@code EMITIDO}.
     */
    public void marcarEmitido(String numeroDocumento) {
        if (this.estado != EstadoTramite.EN_EMISION) {
            throw new TransicionEstadoInvalidaException(
                    "Solo un trámite en estado EN_EMISION puede marcarse como EMITIDO (estado actual: " + this.estado + ")");
        }
        registrarNuevoEvento(new TramiteEmitido(this.id, numeroDocumento, Instant.now()));
    }

    /**
     * Registra un error durante la emisión manteniendo el trámite en {@code EN_EMISION}
     * (comportamiento en {@code main} antes de la compensación de A3).
     */
    public void registrarErrorEmision(String motivo) {
        if (this.estado != EstadoTramite.EN_EMISION) {
            throw new TransicionEstadoInvalidaException(
                    "Solo un trámite en estado EN_EMISION puede registrar un error de emisión (estado actual: " + this.estado + ")");
        }
        registrarNuevoEvento(new EmisionConError(this.id, motivo, Instant.now()));
    }

    /**
     * Registra la solicitud de anulación del pago como paso de compensación de la saga (Asignación A3).
     */
    public void solicitarAnulacionPago(UUID pagoIdAnular, String motivo) {
        if (this.estado != EstadoTramite.EN_EMISION && this.estado != EstadoTramite.PAGADO) {
            throw new TransicionEstadoInvalidaException(
                    "Solo se puede solicitar anulación de pago desde EN_EMISION o PAGADO (estado actual: " + this.estado + ")");
        }
        UUID idPago = pagoIdAnular != null ? pagoIdAnular : this.pagoId;
        registrarNuevoEvento(new PagoAnulacionSolicitada(this.id, idPago, motivo, Instant.now()));
    }

    /**
     * Rechaza el trámite con un motivo explícito, transicionando a {@code RECHAZADO} (Asignación A3).
     */
    public void rechazar(String motivo) {
        if (this.estado == EstadoTramite.EMITIDO || this.estado == EstadoTramite.RECHAZADO) {
            throw new TransicionEstadoInvalidaException(
                    "No se puede rechazar un trámite que ya se encuentra en estado " + this.estado);
        }
        registrarNuevoEvento(new TramiteRechazado(this.id, motivo, Instant.now()));
    }

    /**
     * Verifica el control de concurrencia optimista comparando la versión base cargada
     * con la versión esperada por el llamador.
     */
    public void verificarVersionEsperada(int versionEsperada) {
        if (this.versionBase != versionEsperada) {
            throw new ConflictoVersionException(String.format(
                    "Conflicto de concurrencia optimista en trámite %s: versión esperada %d pero la versión actual es %d",
                    this.id, versionEsperada, this.versionBase));
        }
    }

    private void registrarNuevoEvento(EventoDominioTramite evento) {
        int nuevaVersion = this.version + 1;
        aplicar(evento);
        this.version = nuevaVersion;
        this.eventosPendientes.add(new EventoVersionadoTramite(nuevaVersion, evento));
    }

    private void aplicarEventoHistorico(int versionEvento, EventoDominioTramite evento) {
        aplicar(evento);
        this.version = versionEvento;
    }

    private void aplicar(EventoDominioTramite evento) {
        switch (evento) {
            case TramiteRegistrado ev -> {
                if (this.estado != EstadoTramite.REGISTRADO) {
                    throw new TransicionEstadoInvalidaException(
                            "No se puede aplicar TramiteRegistrado sobre estado " + this.estado);
                }
                this.estado = EstadoTramite.PENDIENTE_PAGO;
                this.actualizadoEn = ev.ocurridoEn();
            }
            case PagoAsociado ev -> {
                if (this.estado != EstadoTramite.PENDIENTE_PAGO) {
                    throw new TransicionEstadoInvalidaException(
                            "No se puede aplicar PagoAsociado desde el estado " + this.estado);
                }
                this.pagoId = ev.pagoId();
                this.estado = EstadoTramite.PAGADO;
                this.actualizadoEn = ev.ocurridoEn();
            }
            case EmisionSolicitada ev -> {
                if (this.estado != EstadoTramite.PAGADO) {
                    throw new TransicionEstadoInvalidaException(
                            "No se puede aplicar EmisionSolicitada desde el estado " + this.estado);
                }
                this.estado = EstadoTramite.EN_EMISION;
                this.actualizadoEn = ev.ocurridoEn();
            }
            case TramiteEmitido ev -> {
                if (this.estado != EstadoTramite.EN_EMISION) {
                    throw new TransicionEstadoInvalidaException(
                            "No se puede aplicar TramiteEmitido desde el estado " + this.estado);
                }
                this.numeroDocumento = ev.numeroDocumento();
                this.estado = EstadoTramite.EMITIDO;
                this.actualizadoEn = ev.ocurridoEn();
            }
            case EmisionConError ev -> {
                if (this.estado != EstadoTramite.EN_EMISION) {
                    throw new TransicionEstadoInvalidaException(
                            "No se puede aplicar EmisionConError desde el estado " + this.estado);
                }
                this.ultimoErrorEmision = ev.motivo();
                this.actualizadoEn = ev.ocurridoEn();
            }
            case PagoAnulacionSolicitada ev -> {
                if (this.estado != EstadoTramite.EN_EMISION && this.estado != EstadoTramite.PAGADO) {
                    throw new TransicionEstadoInvalidaException(
                            "No se puede aplicar PagoAnulacionSolicitada desde el estado " + this.estado);
                }
                this.actualizadoEn = ev.ocurridoEn();
            }
            case TramiteRechazado ev -> {
                if (this.estado == EstadoTramite.EMITIDO || this.estado == EstadoTramite.RECHAZADO) {
                    throw new TransicionEstadoInvalidaException(
                            "No se puede aplicar TramiteRechazado desde el estado " + this.estado);
                }
                this.motivoRechazo = ev.motivo();
                this.estado = EstadoTramite.RECHAZADO;
                this.actualizadoEn = ev.ocurridoEn();
            }
        }
    }

    /**
     * Marca los eventos pendientes como confirmados en el Event Store y actualiza {@code versionBase}.
     */
    public void marcarCambiosConfirmados() {
        this.versionBase = this.version;
        this.eventosPendientes.clear();
    }

    public List<EventoVersionadoTramite> getEventosPendientes() {
        return Collections.unmodifiableList(eventosPendientes);
    }

    public VistaTramite aVista() {
        return new VistaTramite(
                this.id,
                this.codigo.valor(),
                this.dni.valor(),
                this.nombres,
                this.email.valor(),
                this.tipoTramite,
                getSubtipo(),
                getModalidadProcesamiento(),
                this.pais,
                this.folios,
                this.idioma,
                this.estado,
                this.monto,
                this.motivoRechazo,
                this.pagoId,
                this.numeroDocumento,
                this.creadoEn,
                this.actualizadoEn
        );
    }

    /**
     * Nombre concreto del subtipo de dominio ({@code Legalizacion}, {@code Apostilla} o {@code RegistroConsular}).
     */
    public abstract String getSubtipo();

    /**
     * Descripción de la modalidad de verificación o procesamiento propia del subtipo.
     */
    public abstract String getModalidadProcesamiento();

    /**
     * Construye el documento variable de metadatos del expediente según el subtipo de trámite
     * (sección 6.3 del contrato global: modelo documental JSONB).
     */
    public abstract Map<String, Object> construirMetadatosExpediente();

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

    public int getVersionBase() {
        return versionBase;
    }

    public int getVersion() {
        return version;
    }

    public UUID getPagoId() {
        return pagoId;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public String getUltimoErrorEmision() {
        return ultimoErrorEmision;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }
}
