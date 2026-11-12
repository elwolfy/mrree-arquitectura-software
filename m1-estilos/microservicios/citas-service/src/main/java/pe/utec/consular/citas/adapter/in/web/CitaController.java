package pe.utec.consular.citas.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.utec.consular.citas.application.port.in.CancelarCitaUseCase;
import pe.utec.consular.citas.application.port.in.ConsultarCitaUseCase;
import pe.utec.consular.citas.application.port.in.RegistrarCitaUseCase;
import pe.utec.consular.citas.domain.Cita;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para la gestión de citas consulares en {@code citas-service}.
 * <p>Concepto: Adaptador primario web en arquitectura hexagonal (secciones 1.2, 1.3 y 4 del contrato global).
 * <p>Sesión: S1.
 */
@RestController
@RequestMapping("/api/citas")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Citas", description = "Reserva, consulta y cancelación de citas consulares")
public class CitaController {

    private final RegistrarCitaUseCase registrarCitaUseCase;
    private final ConsultarCitaUseCase consultarCitaUseCase;
    private final CancelarCitaUseCase cancelarCitaUseCase;

    public CitaController(RegistrarCitaUseCase registrarCitaUseCase,
                          ConsultarCitaUseCase consultarCitaUseCase,
                          CancelarCitaUseCase cancelarCitaUseCase) {
        this.registrarCitaUseCase = registrarCitaUseCase;
        this.consultarCitaUseCase = consultarCitaUseCase;
        this.cancelarCitaUseCase = cancelarCitaUseCase;
    }

    @PostMapping
    @Operation(summary = "Reserva una nueva cita consular")
    public ResponseEntity<CitaDto> registrar(@Valid @RequestBody RegistrarCitaRequest request) {
        Cita cita = registrarCitaUseCase.registrar(CitaWebMapper.aComando(request));
        CitaDto dto = CitaWebMapper.aDto(cita);
        return ResponseEntity
                .created(URI.create("/api/citas/" + dto.id()))
                .body(dto);
    }

    @GetMapping("/{citaId}")
    @Operation(summary = "Consulta una cita consular por su identificador único")
    public ResponseEntity<CitaDto> obtenerPorId(@PathVariable UUID citaId) {
        Cita cita = consultarCitaUseCase.buscarPorId(citaId);
        return ResponseEntity.ok(CitaWebMapper.aDto(cita));
    }

    @GetMapping
    @Operation(summary = "Lista las citas registradas de un ciudadano por su DNI")
    public ResponseEntity<List<CitaDto>> listarPorDni(@RequestParam("dni") String dni) {
        List<CitaDto> respuesta = consultarCitaUseCase.buscarPorDni(dni).stream()
                .map(CitaWebMapper::aDto)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @DeleteMapping("/{citaId}")
    @Operation(summary = "Cancela una cita activa y libera su cupo")
    public ResponseEntity<Void> cancelar(@PathVariable UUID citaId) {
        cancelarCitaUseCase.cancelar(citaId);
        return ResponseEntity.noContent().build();
    }
}

