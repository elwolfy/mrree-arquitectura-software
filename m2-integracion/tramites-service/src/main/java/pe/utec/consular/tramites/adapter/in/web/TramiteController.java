package pe.utec.consular.tramites.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.utec.consular.tramites.application.port.in.ConsultarTramiteUseCase;
import pe.utec.consular.tramites.application.port.in.ListarTramitesPorDniUseCase;
import pe.utec.consular.tramites.application.port.in.RegistrarTramiteCommand;
import pe.utec.consular.tramites.application.port.in.RegistrarTramiteUseCase;
import pe.utec.consular.tramites.domain.Tramite;
import pe.utec.consular.tramites.domain.exception.RecursoNoEncontradoException;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para el registro y consulta de trámites consulares (:8082).
 * <p>Concepto: Adaptador de entrada web en arquitectura hexagonal (sección 1.3)
 * y punto de comparación frente al servicio gRPC (:9082).
 * <p>Sesión: S3.
 */
@RestController
@RequestMapping("/api/tramites")
@Tag(name = "Trámites", description = "Registro y consulta de trámites consulares (Legalización, Apostilla y Registro Consular)")
public class TramiteController {

    private final RegistrarTramiteUseCase registrarTramiteUseCase;
    private final ConsultarTramiteUseCase consultarTramiteUseCase;
    private final ListarTramitesPorDniUseCase listarTramitesPorDniUseCase;
    private final TramiteWebMapper mapper;

    public TramiteController(RegistrarTramiteUseCase registrarTramiteUseCase,
                             ConsultarTramiteUseCase consultarTramiteUseCase,
                             ListarTramitesPorDniUseCase listarTramitesPorDniUseCase,
                             TramiteWebMapper mapper) {
        this.registrarTramiteUseCase = registrarTramiteUseCase;
        this.consultarTramiteUseCase = consultarTramiteUseCase;
        this.listarTramitesPorDniUseCase = listarTramitesPorDniUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Registra un nuevo trámite consular utilizando Builder y Factory Method, dejándolo en PENDIENTE_PAGO")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Trámite registrado satisfactoriamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "422", description = "Incoherencia de negocio en la solicitud por tipo de trámite")
    })
    public ResponseEntity<TramiteDto> registrarTramite(@Valid @RequestBody RegistrarTramiteRequest request) {
        RegistrarTramiteCommand comando = mapper.aComando(request);
        Tramite tramite = registrarTramiteUseCase.registrar(comando);
        TramiteDto dto = mapper.aDto(tramite);
        URI location = URI.create("/api/tramites/" + tramite.getId());
        return ResponseEntity.created(location).body(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta los detalles de un trámite por su identificador UUID")
    public ResponseEntity<TramiteDto> consultarPorId(
            @Parameter(description = "Identificador único UUID del trámite", required = true)
            @PathVariable UUID id) {
        return consultarTramiteUseCase.consultarPorId(id)
                .map(mapper::aDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el trámite con identificador: " + id));
    }

    @GetMapping
    @Operation(summary = "Lista todos los trámites registrados para un ciudadano por DNI")
    public ResponseEntity<List<TramiteDto>> listarPorDni(
            @Parameter(description = "Número de DNI de 8 dígitos", required = true)
            @RequestParam String dni) {
        List<Tramite> tramites = listarTramitesPorDniUseCase.listarPorDni(dni);
        return ResponseEntity.ok(mapper.aListaDto(tramites));
    }
}

