package pe.utec.consular.identidad.application.port.out;

import pe.utec.consular.identidad.domain.Ciudadano;
import pe.utec.consular.identidad.domain.Dni;

/**
 * Puerto de salida para consultar y validar la identidad de un ciudadano
 * frente al registro externo de identificación.
 * <p>Concepto: Puerto secundario en arquitectura hexagonal (sección 1.3), expresado
 * íntegramente en el lenguaje ubicuo del dominio consular ({@link Ciudadano} y {@link Dni}).
 * <p>Sesión: S3.
 */
public interface ValidarIdentidadPort {

    Ciudadano consultarPorDni(Dni dni);
}

