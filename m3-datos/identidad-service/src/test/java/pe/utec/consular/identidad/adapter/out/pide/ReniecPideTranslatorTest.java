package pe.utec.consular.identidad.adapter.out.pide;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.utec.consular.identidad.domain.Ciudadano;
import pe.utec.consular.identidad.domain.Dni;
import pe.utec.consular.identidad.domain.exception.DniNoEncontradoException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias del traductor de la Capa Anticorrupción ({@link ReniecPideTranslator}).
 */
@DisplayName("ReniecPideTranslator – Traducción ACL de modelo externo PIDE a Ciudadano")
class ReniecPideTranslatorTest {

    private final ReniecPideTranslator translator = new ReniecPideTranslator();

    @Test
    @DisplayName("Debe traducir una respuesta exitosa de persona viva a Ciudadano con vigente=true")
    void debeTraducirPersonaViva() {
        ReniecRespuesta respuesta = new ReniecRespuesta(
                "0000",
                "CONSULTA EXITOSA",
                new ReniecRespuesta.DatosPersonaPide(
                        "40000001", "ANA MARIA", "PEREZ", "MENDOZA", "SOLTERO", "N"
                )
        );

        Ciudadano ciudadano = translator.aDominio(new Dni("40000001"), respuesta);

        assertEquals("40000001", ciudadano.dni());
        assertEquals("ANA MARIA", ciudadano.nombres());
        assertEquals("PEREZ MENDOZA", ciudadano.apellidos());
        assertTrue(ciudadano.vigente());
    }

    @Test
    @DisplayName("Debe traducir una respuesta con inFallecido=S a Ciudadano con vigente=false")
    void debeTraducirPersonaFallecida() {
        ReniecRespuesta respuesta = new ReniecRespuesta(
                "0000",
                "CONSULTA EXITOSA",
                new ReniecRespuesta.DatosPersonaPide(
                        "49999999", "ROBERTO CARLOS", "TORRES", "SILVA", "CASADO", "S"
                )
        );

        Ciudadano ciudadano = translator.aDominio(new Dni("49999999"), respuesta);

        assertEquals("49999999", ciudadano.dni());
        assertFalse(ciudadano.vigente());
    }

    @Test
    @DisplayName("Debe lanzar DniNoEncontradoException cuando coResultado indica error")
    void debeLanzarExcepcionCuandoCoResultadoEsError() {
        ReniecRespuesta respuesta = new ReniecRespuesta(
                "0999",
                "NUMERO DE DNI NO EXISTE EN EL PADRON ELECTORAL",
                null
        );

        assertThrows(DniNoEncontradoException.class,
                () -> translator.aDominio(new Dni("48888888"), respuesta));
    }
}

