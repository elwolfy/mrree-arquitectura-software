package pe.utec.consular.monolito.citas.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.utec.consular.monolito.compartido.infraestructura.ConsistenciaProperties;

/**
 * Controlador REST para introspección y metadatos del sistema en ejecución.
 * <p>Concepto: Introspección arquitectónica y comparación de estilos (sección 4.1 del contrato global).
 */
@RestController
@RequestMapping("/api/sistema")
@Tag(name = "Sistema", description = "Información del sistema y estado de consistencia arquitectónica")
public class SistemaController {

    private final ConsistenciaProperties consistenciaProperties;

    public SistemaController(ConsistenciaProperties consistenciaProperties) {
        this.consistenciaProperties = consistenciaProperties;
    }

    @GetMapping("/info")
    @Operation(summary = "Obtiene los metadatos de la variante arquitectónica y modo de consistencia activo")
    public ResponseEntity<InfoSistemaDto> obtenerInfo() {
        InfoSistemaDto info = new InfoSistemaDto(
                "MONOLITO_MODULAR",
                "HEXAGONAL",
                consistenciaProperties.getModo().name(),
                "1.0.0"
        );
        return ResponseEntity.ok(info);
    }
}
