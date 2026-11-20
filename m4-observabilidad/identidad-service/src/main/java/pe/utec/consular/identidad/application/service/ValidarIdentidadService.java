package pe.utec.consular.identidad.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pe.utec.consular.identidad.application.port.in.ValidarIdentidadUseCase;
import pe.utec.consular.identidad.application.port.out.ValidarIdentidadPort;
import pe.utec.consular.identidad.domain.Ciudadano;
import pe.utec.consular.identidad.domain.Dni;
import pe.utec.consular.identidad.domain.exception.IdentidadNoVigenteException;

/**
 * Caso de uso para validar la identidad y vigencia de un ciudadano peruano.
 */
@Service
public class ValidarIdentidadService implements ValidarIdentidadUseCase {

    private static final Logger log = LoggerFactory.getLogger(ValidarIdentidadService.class);

    private final ValidarIdentidadPort validarIdentidadPort;

    public ValidarIdentidadService(ValidarIdentidadPort validarIdentidadPort) {
        this.validarIdentidadPort = validarIdentidadPort;
    }

    @Override
    public Ciudadano validar(String dniTexto) {
        Dni dni = new Dni(dniTexto);
        log.info("Validando identidad del ciudadano con DNI {}", dni.enmascarado());

        Ciudadano ciudadano = validarIdentidadPort.consultarPorDni(dni);
        if (!ciudadano.vigente()) {
            log.warn("Rechazo por identidad no vigente para DNI {}", dni.enmascarado());
            throw new IdentidadNoVigenteException(
                    "La identidad del titular con DNI " + dni.valor() + " no se encuentra vigente (registro de defunción activo)");
        }

        return ciudadano;
    }
}

