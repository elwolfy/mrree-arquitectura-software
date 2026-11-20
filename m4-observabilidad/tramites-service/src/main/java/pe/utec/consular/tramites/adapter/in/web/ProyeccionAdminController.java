package pe.utec.consular.tramites.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.utec.consular.tramites.application.port.in.ReconstruirVistaTramiteUseCase;

import java.time.Instant;

/**
 * Controlador administrativo para la reconstrucción determinista de la proyección CQRS
 * {@code tramites.vista_tramite} a partir del Event Store {@code tramites.eventos_tramite}.
 */
@RestController
@Tag(name = "Proyecciones CQRS", description = "Operaciones administrativas de reconstrucción de vistas CQRS desde Event Sourcing")
public class ProyeccionAdminController {

    private final ReconstruirVistaTramiteUseCase reconstruirVistaTramiteUseCase;

    public ProyeccionAdminController(ReconstruirVistaTramiteUseCase reconstruirVistaTramiteUseCase) {
        this.reconstruirVistaTramiteUseCase = reconstruirVistaTramiteUseCase;
    }

    @PostMapping({
            "/admin/proyecciones/vista-tramite/reconstruir",
            "/api/tramites/proyecciones/vista-tramite/reconstruir"
    })
    @Operation(summary = "Borra tramites.vista_tramite y la reconstruye íntegramente desde tramites.eventos_tramite")
    public ResponseEntity<ReconstruccionProyeccionDto> reconstruirVistaTramite() {
        int reconstruidos = reconstruirVistaTramiteUseCase.reconstruir();
        return ResponseEntity.ok(new ReconstruccionProyeccionDto(reconstruidos, Instant.now()));
    }
}

