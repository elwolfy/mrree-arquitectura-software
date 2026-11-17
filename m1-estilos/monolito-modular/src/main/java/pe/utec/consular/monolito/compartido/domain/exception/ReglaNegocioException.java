package pe.utec.consular.monolito.compartido.domain.exception;

/**
 * Excepción base para violaciones de reglas de negocio en el dominio consular.
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

    public ReglaNegocioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.titulo = "Regla de negocio incumplida";
        this.codigo = "regla-negocio";
    }

    public String getTitulo() {
        return titulo;
    }

    public String getCodigo() {
        return codigo;
    }
}
