package pe.utec.consular.citas.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.utec.consular.citas.application.port.in.ConsultarDisponibilidadUseCase;
import pe.utec.consular.citas.application.port.in.ListarOficinasUseCase;
import pe.utec.consular.citas.domain.Disponibilidad;
import pe.utec.consular.citas.domain.OficinaConsular;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para el catálogo de oficinas y consulta de disponibilidad de cupos.
 * <p>Concepto: Adaptador web primario en arquitectura hexagonal (sección 1.3).
 * <p>Sesión: S3.
 */
@RestController
@RequestMapping("/api/oficinas")
@Tag(name = "Oficinas", description = "Catálogo de oficinas consulares y disponibilidad de cupos")
public class OficinaController {

    private final ListarOficinasUseCase listarOficinasUseCase;
    private final ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase;
    private final CitaWebMapper mapper;

    public OficinaController(ListarOficinasUseCase listarOficinasUseCase,
                             ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase,
                             CitaWebMapper mapper) {
        this.listarOficinasUseCase = listarOficinasUseCase;
        this.consultarDisponibilidadUseCase = consultarDisponibilidadUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Lista todas las oficinas consulares activas")
    public ResponseEntity<List<OficinaDto>> listarOficinas() {
        List<OficinaConsular> oficinas = listarOficinasUseCase.listar();
        return ResponseEntity.ok(mapper.aListaOficinaDto(oficinas));
    }

    @GetMapping("/{oficinaId}/disponibilidad")
    @Operation(summary = "Consulta la disponibilidad de cupos para una oficina y fecha específica")
    public ResponseEntity<DisponibilidadDto> consultarDisponibilidad(
            @Parameter(description = "Identificador de la oficina consular", example = "MAD", required = true)
            @PathVariable String oficinaId,
            @Parameter(description = "Fecha de consulta en formato YYYY-MM-DD", example = "2026-11-30", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        Disponibilidad disponibilidad = consultarDisponibilidadUseCase.consultar(oficinaId, fecha);
        return ResponseEntity.ok(mapper.aDto(disponibilidad));
    }
}

