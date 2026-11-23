package pe.utec.consular.tramites.adapter.out.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pe.utec.consular.tramites.application.port.out.AnulacionPagoPort;

import java.util.Map;
import java.util.UUID;

/**
 * Adaptador HTTP para solicitar la anulación compensatoria de un pago hacia {@code pagos-service}
 * ({@code POST /api/pagos/{id}/anulacion}).
 * <p>Utilizado en la compensación de la saga de la Asignación A3.
 */
@Component
public class AnulacionPagoHttpAdapter implements AnulacionPagoPort {

    private static final Logger log = LoggerFactory.getLogger(AnulacionPagoHttpAdapter.class);

    private final RestClient restClient;
    private volatile String pagosServiceUrl;

    public AnulacionPagoHttpAdapter(@Value("${pagos.service.url:http://pagos-service:8083}") String pagosServiceUrl) {
        this.pagosServiceUrl = pagosServiceUrl;
        this.restClient = RestClient.create();
    }

    public void setPagosServiceUrl(String pagosServiceUrl) {
        this.pagosServiceUrl = pagosServiceUrl;
    }

    @Override
    public void solicitarAnulacion(UUID pagoId, String motivo) {
        if (pagoId == null) {
            log.warn("No se puede solicitar anulación de pago porque pagoId es nulo");
            return;
        }
        String url = pagosServiceUrl + "/api/pagos/" + pagoId + "/anulacion";
        log.info("Solicitando anulación compensatoria de pago {} en {} por motivo: {}", pagoId, url, motivo);
        restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("motivo", motivo))
                .retrieve()
                .toBodilessEntity();
    }
}

