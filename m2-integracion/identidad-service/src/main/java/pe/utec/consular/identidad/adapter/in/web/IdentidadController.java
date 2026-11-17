package pe.utec.consular.identidad.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.utec.consular.identidad.application.port.in.ValidarIdentidadUseCase;
import pe.utec.consular.identidad.domain.Ciudadano;

/**
 * Controlador REST para la validación de identidad ciudadana (:8086).
 */
@RestController
@RequestMapping("/api/identidad")
@Tag(name = "Identidad", description = "Validación de identidad contra RENIEC mediante Capa Anticorrupción (PIDE simulada)")
public class IdentidadController {

    private final ValidarIdentidadUseCase validarIdentidadUseCase;

    public IdentidadController(ValidarIdentidadUseCase validarIdentidadUseCase) {
        this.validarIdentidadUseCase = validarIdentidadUseCase;
    }

    @PostMapping("/validaciones")
    @Operation(summary = "Valida la identidad y vigencia de un ciudadano peruano por DNI")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Identidad válida y vigente"),
            @ApiResponse(responseCode = "400", description = "Formato de DNI inválido"),
            @ApiResponse(responseCode = "404", description = "DNI no encontrado"),
            @ApiResponse(responseCode = "422", description = "Identidad no vigente (fallecido)"),
            @ApiResponse(responseCode = "503", description = "Servicio de identidad no disponible")
    })
    public ResponseEntity<CiudadanoDto> validarIdentidad(@Valid @RequestBody ValidarIdentidadRequest request) {
        Ciudadano ciudadano = validarIdentidadUseCase.validar(request.dni());
        return ResponseEntity.ok(CiudadanoDto.desdeDominio(ciudadano));
    }
}

