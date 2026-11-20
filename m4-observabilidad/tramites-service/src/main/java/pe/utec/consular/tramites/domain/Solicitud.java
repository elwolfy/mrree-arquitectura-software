package pe.utec.consular.tramites.domain;

import pe.utec.consular.tramites.domain.exception.SolicitudInvalidaException;

import java.util.Map;
import java.util.Set;

/**
 * Pedido inicial inmutable del ciudadano para iniciar un trámite consular.
 * <p>Concepto: <b>Patrón Creacional Builder</b> (sección 2.1).
 * <ul>
 *   <li><b>Problema que resuelve:</b> Evita constructores telescópicos con múltiples parámetros
 *       del mismo tipo ({@code String}, {@code int}) y centraliza en {@link Builder#build()}
 *       la validación de datos obligatorios y de coherencia cruzada según el {@link TipoTramite}
 *       antes de exponer una instancia inmutable.</li>
 *   <li><b>Inmutabilidad:</b> Todos sus atributos son {@code private final}, no existen métodos
 *       mutadores (*setters*) y los metadatos adicionales se encapsulan con {@link Map#copyOf(Map)}.</li>
 * </ul>
 * <p>Sesión: S2 y S3.
 */
public final class Solicitud {

    private static final Set<String> PAISES_EXCLUIDOS_LA_HAYA = Set.of("CU", "XX");

    private final Dni dni;
    private final String nombres;
    private final Email email;
    private final TipoTramite tipo;
    private final String pais;
    private final int folios;
    private final String idioma;
    private final Map<String, String> metadatos;

    private Solicitud(Builder builder) {
        this.dni = new Dni(builder.dni);
        this.nombres = builder.nombres != null && !builder.nombres.isBlank()
                ? builder.nombres.trim()
                : "Ciudadano Consular";
        this.email = new Email(builder.email != null && !builder.email.isBlank()
                ? builder.email.trim()
                : "ciudadano@correo.lab");
        this.tipo = builder.tipo;
        this.pais = builder.pais.trim().toUpperCase();
        this.folios = builder.folios;
        this.idioma = builder.idioma.trim().toUpperCase();
        this.metadatos = builder.metadatos != null ? Map.copyOf(builder.metadatos) : Map.of();
    }

    public static Builder builder() {
        return new Builder();
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

    public TipoTramite getTipo() {
        return tipo;
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

    public Map<String, String> getMetadatos() {
        return metadatos;
    }

    /**
     * Builder fluido para construir instancias válidas e inmutables de {@link Solicitud}.
     */
    public static final class Builder {

        private String dni;
        private String nombres;
        private String email;
        private TipoTramite tipo;
        private String pais;
        private Integer folios;
        private String idioma;
        private Map<String, String> metadatos = Map.of();

        private Builder() {
        }

        public Builder dni(String dni) {
            this.dni = dni;
            return this;
        }

        public Builder nombres(String nombres) {
            this.nombres = nombres;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder tipo(TipoTramite tipo) {
            this.tipo = tipo;
            return this;
        }

        public Builder pais(String pais) {
            this.pais = pais;
            return this;
        }

        public Builder folios(int folios) {
            this.folios = folios;
            return this;
        }

        public Builder idioma(String idioma) {
            this.idioma = idioma;
            return this;
        }

        public Builder metadatos(Map<String, String> metadatos) {
            this.metadatos = metadatos != null ? Map.copyOf(metadatos) : Map.of();
            return this;
        }

        /**
         * Valida los atributos obligatorios y las invariantes de coherencia por tipo
         * antes de instanciar {@link Solicitud}.
         *
         * @return instancia inmutable de {@link Solicitud}
         * @throws SolicitudInvalidaException si falta un dato obligatorio o hay incoherencia por tipo
         */
        public Solicitud build() {
            if (dni == null || dni.isBlank()) {
                throw new SolicitudInvalidaException("El DNI del solicitante es obligatorio");
            }
            if (tipo == null) {
                throw new SolicitudInvalidaException("El tipo de trámite es obligatorio");
            }
            if (pais == null || !pais.trim().matches("^[A-Za-z]{2}$")) {
                throw new SolicitudInvalidaException("El país debe ser un código ISO de 2 letras (por ejemplo ES, AR, US)");
            }
            if (folios == null || folios <= 0) {
                throw new SolicitudInvalidaException("El número de folios debe ser mayor a cero");
            }
            if (idioma == null || idioma.isBlank()) {
                throw new SolicitudInvalidaException("El idioma del documento es obligatorio");
            }

            String paisNormalizado = pais.trim().toUpperCase();
            String idiomaNormalizado = idioma.trim().toUpperCase();

            switch (tipo) {
                case LEGALIZACION -> {
                    if (folios > 50) {
                        throw new SolicitudInvalidaException(
                                "Una solicitud de LEGALIZACION no puede exceder los 50 folios (recibido: " + folios + ")");
                    }
                }
                case APOSTILLA -> {
                    if (folios > 20) {
                        throw new SolicitudInvalidaException(
                                "Una solicitud de APOSTILLA no puede exceder los 20 folios (recibido: " + folios + ")");
                    }
                    if (PAISES_EXCLUIDOS_LA_HAYA.contains(paisNormalizado)) {
                        throw new SolicitudInvalidaException(
                                "El país " + paisNormalizado + " no pertenece al Convenio de La Haya; debe solicitar LEGALIZACION");
                    }
                }
                case REGISTRO_CONSULAR -> {
                    if (folios != 1) {
                        throw new SolicitudInvalidaException(
                                "El REGISTRO_CONSULAR corresponde a un acta única y requiere exactamente 1 folio (recibido: " + folios + ")");
                    }
                    if (!"ES".equals(idiomaNormalizado)) {
                        throw new SolicitudInvalidaException(
                                "El REGISTRO_CONSULAR peruano debe asentarse en idioma español ('ES', recibido: '" + idiomaNormalizado + "')");
                    }
                }
            }

            return new Solicitud(this);
        }
    }
}

