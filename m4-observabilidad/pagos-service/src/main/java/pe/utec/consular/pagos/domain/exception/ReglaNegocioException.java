package pe.utec.consular.pagos.domain.exception;

/**
 * Excepción base para reglas de negocio en el dominio de pagos.
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

