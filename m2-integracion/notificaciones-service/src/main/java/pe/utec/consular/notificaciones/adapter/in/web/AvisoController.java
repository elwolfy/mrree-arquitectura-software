package pe.utec.consular.notificaciones.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.utec.consular.notificaciones.application.port.in.ConsultarAvisosUseCase;

import java.util.List;

/**
 * Controlador REST para consultar los avisos de un ciudadano (:8084).
 */
@RestController
@RequestMapping("/api/avisos")
@Tag(name = "Avisos", description = "Consulta de avisos y notificaciones enviadas al ciudadano")
public class AvisoController {

    private final ConsultarAvisosUseCase consultarAvisosUseCase;

    public AvisoController(ConsultarAvisosUseCase consultarAvisosUseCase) {
        this.consultarAvisosUseCase = consultarAvisosUseCase;
    }

    @GetMapping
    @Operation(summary = "Lista los avisos enviados a un ciudadano por DNI")
    public ResponseEntity<List<AvisoDto>> listarPorDni(
            @Parameter(description = "Número de DNI de 8 dígitos", required = true)
            @RequestParam String dni) {
        List<AvisoDto> respuesta = consultarAvisosUseCase.listarPorDni(dni).stream()
                .map(AvisoDto::desdeDominio)
                .toList();
        return ResponseEntity.ok(respuesta);
    }
}

