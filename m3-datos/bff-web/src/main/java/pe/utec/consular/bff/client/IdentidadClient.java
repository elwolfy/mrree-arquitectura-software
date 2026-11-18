package pe.utec.consular.bff.client;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.Map;

/**
 * Cliente HTTP declarativo hacia {@code identidad-service} (por nombre de contenedor).
 */
@HttpExchange
public interface IdentidadClient {

    @PostExchange("/api/identidad/validaciones")
    Map<String, Object> validarIdentidad(@RequestBody Map<String, String> solicitudDni);
}

