package pe.utec.consular.bff.client;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;
import java.util.Map;

/**
 * Cliente HTTP declarativo hacia {@code citas-service} (por nombre de contenedor).
 */
@HttpExchange
public interface CitasClient {

    @GetExchange("/api/citas")
    List<Map<String, Object>> listarCitasPorDni(@RequestParam("dni") String dni);
}

