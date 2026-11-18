package pe.utec.consular.bff.client;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.Map;

/**
 * Cliente HTTP declarativo hacia {@code pagos-service} (por nombre de contenedor).
 */
@HttpExchange
public interface PagosClient {

    @GetExchange("/api/tarifas")
    Map<String, Object> calcularTarifa(@RequestParam("tipoTramite") String tipoTramite,
                                       @RequestParam("pais") String pais);
}

