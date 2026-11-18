package pe.utec.consular.bff.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pe.utec.consular.bff.client.IdentidadClient;
import pe.utec.consular.bff.client.PagosClient;
import pe.utec.consular.bff.client.TramitesClient;

import java.util.Map;

/**
 * Fachada de aplicación en el BFF para la operación compuesta "Iniciar trámite" ({@code POST /bff/tramites}).
 * <p>Concepto: <b>Patrón Estructural Facade (GoF – sección 2.1)</b> combinado con <b>BFF (sección 2.2)</b>.
 * <ul>
 *   <li><b>Qué hace la fachada:</b> Ofrece al frontend web un único endpoint de alto nivel que
 *       coordina tres servicios de backend en secuencia: (1) valida la identidad del ciudadano en
 *       {@code identidad-service}, (2) calcula la tarifa aplicable en {@code pagos-service}, y
 *       (3) registra el trámite en {@code tramites-service}, devolviendo un DTO adaptado a la pantalla.</li>
 *   <li><b>Frontera arquitectónica:</b> La fachada <b>solo coordina</b> llamadas y ensambla la vista;
 *       todas las reglas de negocio (vigencia del DNI en RENIEC, estrategia de arancel y reglas de
 *       folios/idioma por subtipo de trámite) permanecen dentro de cada microservicio dueño.</li>
 * </ul>
 * <p>Sesión: S3.
 */
@Service
public class TramiteFacade {

    private static final Logger log = LoggerFactory.getLogger(TramiteFacade.class);

    private final IdentidadClient identidadClient;
    private final PagosClient pagosClient;
    private final TramitesClient tramitesClient;

    public TramiteFacade(IdentidadClient identidadClient,
                         PagosClient pagosClient,
                         TramitesClient tramitesClient) {
        this.identidadClient = identidadClient;
        this.pagosClient = pagosClient;
        this.tramitesClient = tramitesClient;
    }

    public TramiteIniciadoResponse iniciarTramite(IniciarTramiteBffRequest request) {
        log.info("TramiteFacade: iniciando flujo coordinado para DNI {} (tipo={}, país={})",
                request.dni(), request.tipoTramite(), request.pais());

        // Paso 1: Validar identidad en identidad-service
        Map<String, Object> ciudadano = identidadClient.validarIdentidad(Map.of("dni", request.dni()));
        String nombresCompletos = construirNombreCompleto(ciudadano);

        // Paso 2: Calcular tarifa en pagos-service
        Map<String, Object> tarifa = pagosClient.calcularTarifa(request.tipoTramite(), request.pais());

        // Paso 3: Registrar trámite en tramites-service
        Map<String, Object> tramite = tramitesClient.registrarTramite(Map.of(
                "dni", request.dni(),
                "nombres", nombresCompletos,
                "email", request.email(),
                "tipoTramite", request.tipoTramite(),
                "pais", request.pais(),
                "folios", request.folios(),
                "idioma", request.idioma()
        ));

        log.info("TramiteFacade: trámite {} creado satisfactoriamente para {}",
                tramite.get("codigo"), nombresCompletos);

        return new TramiteIniciadoResponse(
                ciudadano,
                tarifa,
                tramite
        );
    }

    private String construirNombreCompleto(Map<String, Object> ciudadano) {
        String nombres = String.valueOf(ciudadano.getOrDefault("nombres", "")).trim();
        String apellidos = String.valueOf(ciudadano.getOrDefault("apellidos", "")).trim();
        String combinado = (nombres + " " + apellidos).trim();
        return combinado.isEmpty() ? "Ciudadano Consular" : combinado;
    }

    public record IniciarTramiteBffRequest(
            String dni,
            String email,
            String tipoTramite,
            String pais,
            int folios,
            String idioma
    ) {
    }

    public record TramiteIniciadoResponse(
            Map<String, Object> ciudadano,
            Map<String, Object> tarifa,
            Map<String, Object> tramite
    ) {
    }
}

