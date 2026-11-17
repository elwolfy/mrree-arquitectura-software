package pe.utec.consular.tramites.domain.exception;

/**
 * Excepción base para incumplimientos de reglas de negocio en el dominio de trámites.
 */
public class ReglaNegocioException extends RuntimeException {

    private final String titulo;
    private final String codigo;

    public ReglaNegocioException(String mensaje) {
        this(mensaje, "Regla de negocio incumplida", "regla-negocio");
    }

    public ReglaNegocioException(String mensaje, String titulo, String codigo) {
        super(mensaje);
        this.titulo = titulo;
        this.codigo = codigo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getCodigo() {
        return codigo;
    }
}

