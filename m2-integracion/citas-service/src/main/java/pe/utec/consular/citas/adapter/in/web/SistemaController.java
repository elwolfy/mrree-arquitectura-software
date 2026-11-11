package pe.utec.consular.citas.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para introspección y metadatos del servicio en ejecución.
 */
@RestController
@RequestMapping("/api/sistema")
@Tag(name = "Sistema", description = "Información del sistema y variante arquitectónica")
public class SistemaController {

    @GetMapping("/info")
    @Operation(summary = "Obtiene los metadatos de la variante arquitectónica activa")
    public ResponseEntity<InfoSistemaDto> obtenerInfo() {
        InfoSistemaDto info = new InfoSistemaDto(
                "MICROSERVICIOS",
                "HEXAGONAL",
                "CP",
                "1.0.0"
        );
        return ResponseEntity.ok(info);
    }
}

