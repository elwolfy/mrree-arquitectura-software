package pe.utec.consular.monolito.tramites.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.utec.consular.monolito.compartido.domain.TipoTramite;
import pe.utec.consular.monolito.tramites.application.port.in.CalcularTarifaUseCase;
import pe.utec.consular.monolito.tramites.domain.tarifa.CalculadoraDeTarifas.ResultadoTarifa;

/**
 * Controlador REST para cotización y consulta de tarifas consulares.
 * <p>Concepto: Adaptador primario web en arquitectura hexagonal (sección 1.3).
 */
@RestController
@RequestMapping("/api/tarifas")
@Tag(name = "Tarifas", description = "Cálculo de tarifas consulares con patrón Strategy")
public class TarifaController {

    private final CalcularTarifaUseCase calcularTarifaUseCase;

    public TarifaController(CalcularTarifaUseCase calcularTarifaUseCase) {
        this.calcularTarifaUseCase = calcularTarifaUseCase;
    }

    @GetMapping
    @Operation(summary = "Calcula la tarifa según el tipo de trámite y país de atención")
    public ResponseEntity<TarifaDto> obtenerTarifa(
            @Parameter(description = "Tipo de trámite consular", example = "APOSTILLA", required = true)
            @RequestParam TipoTramite tipoTramite,
            @Parameter(description = "Código ISO del país de atención", example = "ES", required = true)
            @RequestParam String pais) {

        ResultadoTarifa resultado = calcularTarifaUseCase.calcular(tipoTramite, pais);
        TarifaDto dto = new TarifaDto(
                resultado.tipoTramite(),
                resultado.pais(),
                resultado.moneda(),
                resultado.monto(),
                resultado.estrategia()
        );
        return ResponseEntity.ok(dto);
    }
}
