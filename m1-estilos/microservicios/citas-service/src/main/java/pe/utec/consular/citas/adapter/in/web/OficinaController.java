package pe.utec.consular.citas.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.utec.consular.citas.application.port.in.ConsultarDisponibilidadUseCase;
import pe.utec.consular.citas.application.port.in.ListarOficinasUseCase;
import pe.utec.consular.citas.domain.Disponibilidad;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para el catálogo de oficinas consulares y consulta de disponibilidad.
 */
@RestController
@RequestMapping("/api/oficinas")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Oficinas", description = "Catálogo de oficinas consulares y consulta de disponibilidad de cupos")
public class OficinaController {

    private final ListarOficinasUseCase listarOficinasUseCase;
    private final ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase;

    public OficinaController(ListarOficinasUseCase listarOficinasUseCase,
                             ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase) {
        this.listarOficinasUseCase = listarOficinasUseCase;
        this.consultarDisponibilidadUseCase = consultarDisponibilidadUseCase;
    }

    @GetMapping
    @Operation(summary = "Lista todas las oficinas consulares registradas")
    public ResponseEntity<List<OficinaDto>> listarOficinas() {
        List<OficinaDto> respuesta = listarOficinasUseCase.listarTodas().stream()
                .map(CitaWebMapper::aDto)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/{oficinaId}/disponibilidad")
    @Operation(summary = "Consulta los cupos disponibles para una oficina consular en una fecha determinada")
    public ResponseEntity<DisponibilidadDto> consultarDisponibilidad(
            @PathVariable String oficinaId,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        Disponibilidad disponibilidad = consultarDisponibilidadUseCase.consultar(oficinaId, fecha);
        return ResponseEntity.ok(CitaWebMapper.aDto(disponibilidad));
    }
}

