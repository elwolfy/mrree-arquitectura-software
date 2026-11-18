package pe.utec.consular.pagos.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.utec.consular.pagos.domain.TipoTramite;
import pe.utec.consular.pagos.domain.tarifa.CalculadoraDeTarifas;
import pe.utec.consular.pagos.domain.tarifa.Tarifa;

/**
 * Controlador REST para el cálculo de tarifas consulares mediante el patrón Strategy (:8083).
 */
@RestController
@RequestMapping("/api/tarifas")
@Tag(name = "Tarifas", description = "Cálculo de tarifas consulares por tipo de trámite y país (Patrón Strategy)")
public class TarifaController {

    private final CalculadoraDeTarifas calculadoraDeTarifas;

    public TarifaController(CalculadoraDeTarifas calculadoraDeTarifas) {
        this.calculadoraDeTarifas = calculadoraDeTarifas;
    }

    @GetMapping
    @Operation(summary = "Calcula la tarifa aplicable según el tipo de trámite y el país destino")
    public ResponseEntity<TarifaDto> calcularTarifa(
            @Parameter(description = "Tipo de trámite (LEGALIZACION, APOSTILLA, REGISTRO_CONSULAR)", required = true)
            @RequestParam TipoTramite tipoTramite,
            @Parameter(description = "Código ISO de 2 letras del país (p. ej. ES, AR)", required = true)
            @RequestParam String pais) {
        Tarifa tarifa = calculadoraDeTarifas.calcular(tipoTramite, pais);
        return ResponseEntity.ok(TarifaDto.desdeDominio(tarifa));
    }
}

