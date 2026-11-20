package pe.utec.consular.bff.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.utec.consular.bff.application.InicioCiudadanoService;
import pe.utec.consular.bff.application.InicioCiudadanoService.InicioCiudadanoResponse;
import pe.utec.consular.bff.application.TramiteFacade;
import pe.utec.consular.bff.application.TramiteFacade.IniciarTramiteBffRequest;
import pe.utec.consular.bff.application.TramiteFacade.TramiteIniciadoResponse;

/**
 * Controlador REST del BFF Web (:8085).
 */
@RestController
@RequestMapping("/bff")
@Tag(name = "BFF Web", description = "Endpoints orientados a la interfaz ciudadana: inicio agregado y fachada de trámites")
public class BffController {

    private final InicioCiudadanoService inicioCiudadanoService;
    private final TramiteFacade tramiteFacade;

    public BffController(InicioCiudadanoService inicioCiudadanoService,
                         TramiteFacade tramiteFacade) {
        this.inicioCiudadanoService = inicioCiudadanoService;
        this.tramiteFacade = tramiteFacade;
    }

    @GetMapping("/inicio")
    @Operation(summary = "Agrega en paralelo citas, trámites y avisos del ciudadano con degradación elegante")
    public ResponseEntity<InicioCiudadanoResponse> obtenerInicio(@RequestParam String dni) {
        return ResponseEntity.ok(inicioCiudadanoService.obtenerResumenInicio(dni));
    }

    @PostMapping("/tramites")
    @Operation(summary = "Fachada 'Iniciar trámite': valida identidad + calcula tarifa + registra trámite")
    public ResponseEntity<TramiteIniciadoResponse> iniciarTramite(@RequestBody IniciarTramiteBffRequest request) {
        TramiteIniciadoResponse respuesta = tramiteFacade.iniciarTramite(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}

