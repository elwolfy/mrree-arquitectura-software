package pe.utec.consular.monolito.citas.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.utec.consular.monolito.citas.application.port.in.CancelarCitaUseCase;
import pe.utec.consular.monolito.citas.application.port.in.ConsultarCitaUseCase;
import pe.utec.consular.monolito.citas.application.port.in.RegistrarCitaCommand;
import pe.utec.consular.monolito.citas.application.port.in.RegistrarCitaUseCase;
import pe.utec.consular.monolito.citas.domain.Cita;
import pe.utec.consular.monolito.compartido.domain.exception.RecursoNoEncontradoException;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para la gestión y reserva de citas consulares.
 * <p>Concepto: Adaptador de entrada web en arquitectura hexagonal (sección 1.3).
 * <p>Sesión: S1.
 */
@RestController
@RequestMapping("/api/citas")
@Tag(name = "Citas", description = "Operaciones de reserva, consulta y cancelación de citas consulares")
public class CitaController {

    private final RegistrarCitaUseCase registrarCitaUseCase;
    private final ConsultarCitaUseCase consultarCitaUseCase;
    private final CancelarCitaUseCase cancelarCitaUseCase;
    private final CitaWebMapper mapper;

    public CitaController(RegistrarCitaUseCase registrarCitaUseCase,
                          ConsultarCitaUseCase consultarCitaUseCase,
                          CancelarCitaUseCase cancelarCitaUseCase,
                          CitaWebMapper mapper) {
        this.registrarCitaUseCase = registrarCitaUseCase;
        this.consultarCitaUseCase = consultarCitaUseCase;
        this.cancelarCitaUseCase = cancelarCitaUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Reserva una nueva cita consular con validación pesimista de cupo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cita reservada satisfactoriamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Cita duplicada o cupo sin capacidad"),
            @ApiResponse(responseCode = "500", description = "Falla síncrona en servicio de notificación"),
            @ApiResponse(responseCode = "503", description = "Base de datos no disponible (modo CP)")
    })
    public ResponseEntity<CitaDto> registrarCita(@Valid @RequestBody RegistrarCitaRequest request) {
        RegistrarCitaCommand comando = mapper.aComando(request);
        Cita cita = registrarCitaUseCase.registrar(comando);
        CitaDto dto = mapper.aDto(cita);
        URI location = URI.create("/api/citas/" + cita.getId());
        return ResponseEntity.created(location).body(dto);
    }

    @GetMapping("/{citaId}")
    @Operation(summary = "Consulta los detalles de una cita por su identificador")
    public ResponseEntity<CitaDto> consultarPorId(
            @Parameter(description = "Identificador único UUID de la cita", required = true)
            @PathVariable UUID citaId) {
        return consultarCitaUseCase.consultarPorId(citaId)
                .map(mapper::aDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la cita con identificador: " + citaId));
    }

    @GetMapping
    @Operation(summary = "Lista todas las citas registradas para un ciudadano por DNI")
    public ResponseEntity<List<CitaDto>> consultarPorDni(
            @Parameter(description = "Número de DNI de 8 dígitos", required = true)
            @RequestParam String dni) {
        List<Cita> citas = consultarCitaUseCase.consultarPorDni(dni);
        return ResponseEntity.ok(mapper.aListaCitaDto(citas));
    }

    @DeleteMapping("/{citaId}")
    @Operation(summary = "Cancela una cita activa y libera el cupo")
    public ResponseEntity<Void> cancelarCita(
            @Parameter(description = "Identificador único UUID de la cita", required = true)
            @PathVariable UUID citaId) {
        cancelarCitaUseCase.cancelar(citaId);
        return ResponseEntity.noContent().build();
    }
}
