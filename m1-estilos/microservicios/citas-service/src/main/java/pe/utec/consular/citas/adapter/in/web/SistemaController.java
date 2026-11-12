package pe.utec.consular.citas.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para introspección del estilo arquitectónico activo en ejecución.
 * <p>Concepto: En la variante de microservicios devuelve {@code variante: "MICROSERVICIOS"},
 * {@code arquitecturaInterna: "HEXAGONAL"}, {@code modoConsistencia: "CP"}.
 */
@RestController
@RequestMapping("/api/sistema")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Sistema", description = "Información de la variante arquitectónica activa")
public class SistemaController {

    @GetMapping("/info")
    @Operation(summary = "Obtiene los metadatos de la variante arquitectónica en ejecución")
    public ResponseEntity<InfoSistemaDto> obtenerInfo() {
        return ResponseEntity.ok(new InfoSistemaDto(
                "MICROSERVICIOS",
                "HEXAGONAL",
                "CP",
                "1.0.0"
        ));
    }
}

