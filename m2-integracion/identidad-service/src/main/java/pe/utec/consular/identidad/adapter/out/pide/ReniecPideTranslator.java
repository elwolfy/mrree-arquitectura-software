package pe.utec.consular.identidad.adapter.out.pide;

import org.springframework.stereotype.Component;
import pe.utec.consular.identidad.domain.Ciudadano;
import pe.utec.consular.identidad.domain.Dni;
import pe.utec.consular.identidad.domain.exception.DniNoEncontradoException;

/**
 * Traductor de la Capa Anticorrupción (ACL) que convierte la respuesta externa
 * {@link ReniecRespuesta} al modelo propio del dominio consular ({@link Ciudadano}).
 * <p>Concepto: Anti-Corruption Layer (ACL) en el Context Map de DDD (sección 2.4).
 * Traduce códigos crípticos ({@code coResultado = "0000"} / {@code "0999"}) y banderas
 * de texto ({@code inFallecido = "S" / "N"}) a conceptos semánticos e invariantes del dominio.
 */
@Component
public class ReniecPideTranslator {

    private static final String CODIGO_EXITO_RENIEC = "0000";
    private static final String INDICADOR_FALLECIDO_SI = "S";

    public Ciudadano aDominio(Dni dniSolicitado, ReniecRespuesta respuesta) {
        if (respuesta == null
                || !CODIGO_EXITO_RENIEC.equals(respuesta.coResultado())
                || respuesta.datosPersona() == null) {
            String detalleExterno = respuesta != null && respuesta.deResultado() != null
                    ? respuesta.deResultado()
                    : "Sin respuesta del padrón externo";
            throw new DniNoEncontradoException(
                    "DNI no encontrado en el padrón de identificación (" + dniSolicitado.valor() + "): " + detalleExterno);
        }

        ReniecRespuesta.DatosPersonaPide datos = respuesta.datosPersona();
        String nombres = datos.prenombres() != null ? datos.prenombres().trim() : "";
        String primerApellido = datos.apPrimer() != null ? datos.apPrimer().trim() : "";
        String segundoApellido = datos.apSegundo() != null ? datos.apSegundo().trim() : "";
        String apellidos = (primerApellido + " " + segundoApellido).trim();
        boolean vigente = !INDICADOR_FALLECIDO_SI.equalsIgnoreCase(datos.inFallecido());

        return new Ciudadano(
                dniSolicitado.valor(),
                nombres,
                apellidos,
                vigente
        );
    }
}

