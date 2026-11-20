package pe.utec.consular.bff.client;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

/**
 * Cliente HTTP declarativo hacia {@code tramites-service} (por nombre de contenedor).
 */
@HttpExchange
public interface TramitesClient {

    @GetExchange("/api/tramites")
    List<Map<String, Object>> listarTramitesPorDni(@RequestParam("dni") String dni);

    @PostExchange("/api/tramites")
    Map<String, Object> registrarTramite(@RequestBody Map<String, Object> solicitud);
}

