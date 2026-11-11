package pe.utec.consular.clean.citas.interfaceadapters.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.utec.consular.clean.citas.entities.ModoConsistencia;
import pe.utec.consular.clean.citas.interfaceadapters.presenter.InfoSistemaViewModel;

import java.util.function.Supplier;

/**
 * Controlador REST para introspección y metadatos del sistema en ejecución.
 * Devuelve {@code arquitecturaInterna: CLEAN} y {@code variante: MONOLITO_MODULAR}.
 * <p>Concepto: Introspección arquitectónica y comparación de estilos (sección 4.1 del contrato global).
 * <p>Sesión: S2. Compárese con {@code pe.utec.consular.monolito.citas.adapter.in.web.SistemaController}.
 */
@RestController
@RequestMapping("/api/sistema")
@Tag(name = "Sistema", description = "Información del sistema y estado de consistencia arquitectónica")
public class SistemaController {

    private final Supplier<ModoConsistencia> proveedorModoConsistencia;

    public SistemaController(Supplier<ModoConsistencia> proveedorModoConsistencia) {
        this.proveedorModoConsistencia = proveedorModoConsistencia;
    }

    @GetMapping("/info")
    @Operation(summary = "Obtiene los metadatos de la variante arquitectónica y modo de consistencia activo")
    public ResponseEntity<InfoSistemaViewModel> obtenerInfo() {
        InfoSistemaViewModel info = new InfoSistemaViewModel(
                "MONOLITO_MODULAR",
                "CLEAN",
                proveedorModoConsistencia.get().name(),
                "1.0.0"
        );
        return ResponseEntity.ok(info);
    }
}

