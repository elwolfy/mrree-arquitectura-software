package pe.utec.consular.clean.tarifas.interfaceadapters.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.utec.consular.clean.citas.entities.TipoTramite;
import pe.utec.consular.clean.tarifas.interfaceadapters.presenter.TarifaPresenter;
import pe.utec.consular.clean.tarifas.interfaceadapters.presenter.TarifaViewModel;
import pe.utec.consular.clean.tarifas.usecase.CalcularTarifaInputBoundary;
import pe.utec.consular.clean.tarifas.usecase.CalcularTarifaRequestModel;

/**
 * Controlador REST para cotización y consulta de tarifas consulares (Círculo 3: Interface Adapters).
 * <p>Concepto: Clean Architecture – Controller + Presenter y patrón Strategy (secciones 1.3 y 2.1 del plan).
 * <p>Sesión: S2. Compárese con {@code pe.utec.consular.monolito.tramites.adapter.in.web.TarifaController}.
 */
@RestController
@RequestMapping("/api/tarifas")
@Tag(name = "Tarifas", description = "Cálculo de tarifas consulares con patrón Strategy")
public class TarifaController {

    private final CalcularTarifaInputBoundary calcularTarifaInputBoundary;
    private final TarifaPresenter tarifaPresenter;

    public TarifaController(CalcularTarifaInputBoundary calcularTarifaInputBoundary,
                            TarifaPresenter tarifaPresenter) {
        this.calcularTarifaInputBoundary = calcularTarifaInputBoundary;
        this.tarifaPresenter = tarifaPresenter;
    }

    @GetMapping
    @Operation(summary = "Calcula la tarifa según el tipo de trámite y país de atención")
    public ResponseEntity<TarifaViewModel> obtenerTarifa(
            @Parameter(description = "Tipo de trámite consular", example = "APOSTILLA", required = true)
            @RequestParam TipoTramite tipoTramite,
            @Parameter(description = "Código ISO del país de atención", example = "ES", required = true)
            @RequestParam String pais) {

        calcularTarifaInputBoundary.calcular(new CalcularTarifaRequestModel(tipoTramite, pais));
        return ResponseEntity.ok(tarifaPresenter.obtenerViewModel());
    }
}

