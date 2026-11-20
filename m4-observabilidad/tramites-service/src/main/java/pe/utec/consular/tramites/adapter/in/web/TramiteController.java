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
import pe.utec.consular.tramites.application.port.in.BuscarTramitesPorPaisUseCase;
import pe.utec.consular.tramites.application.port.in.ConsultarHistorialTramiteUseCase;
import pe.utec.consular.tramites.application.port.in.ConsultarTramiteUseCase;
import pe.utec.consular.tramites.application.port.in.ListarTramitesPorDniUseCase;
import pe.utec.consular.tramites.application.port.in.RegistrarTramiteCommand;
import pe.utec.consular.tramites.application.port.in.RegistrarTramiteUseCase;
import pe.utec.consular.tramites.domain.Tramite;
import pe.utec.consular.tramites.domain.VistaTramite;
import pe.utec.consular.tramites.domain.evento.EventoVersionadoTramite;
import pe.utec.consular.tramites.domain.exception.RecursoNoEncontradoException;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para el registro (Event Sourcing + Outbox), consulta (proyección CQRS)
 * e historial de eventos de trámites consulares (:8082).
 */
@RestController
@RequestMapping("/api/tramites")
@Tag(name = "Trámites", description = "Registro por Event Sourcing + Outbox, lectura CQRS desde vista_tramite e historial de eventos")
public class TramiteController {

    private final RegistrarTramiteUseCase registrarTramiteUseCase;
    private final ConsultarTramiteUseCase consultarTramiteUseCase;
    private final ConsultarHistorialTramiteUseCase consultarHistorialTramiteUseCase;
    private final ListarTramitesPorDniUseCase listarTramitesPorDniUseCase;
    private final BuscarTramitesPorPaisUseCase buscarTramitesPorPaisUseCase;
    private final TramiteWebMapper mapper;

    public TramiteController(RegistrarTramiteUseCase registrarTramiteUseCase,
                             ConsultarTramiteUseCase consultarTramiteUseCase,
                             ConsultarHistorialTramiteUseCase consultarHistorialTramiteUseCase,
                             ListarTramitesPorDniUseCase listarTramitesPorDniUseCase,
                             BuscarTramitesPorPaisUseCase buscarTramitesPorPaisUseCase,
                             TramiteWebMapper mapper) {
        this.registrarTramiteUseCase = registrarTramiteUseCase;
        this.consultarTramiteUseCase = consultarTramiteUseCase;
        this.consultarHistorialTramiteUseCase = consultarHistorialTramiteUseCase;
        this.listarTramitesPorDniUseCase = listarTramitesPorDniUseCase;
        this.buscarTramitesPorPaisUseCase = buscarTramitesPorPaisUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Registra un nuevo trámite consular con Event Sourcing, actualiza vista_tramite y encola tramite.registrado en outbox")
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

    @GetMapping("/buscar")
    @Operation(summary = "Busca trámites por país de destino utilizando el operador JSONB @> sobre tramites.expediente.metadatos")
    public ResponseEntity<List<TramiteDto>> buscarPorPais(
            @Parameter(description = "Código ISO-2 del país de destino en el expediente JSONB (p. ej. ES)", required = true)
            @RequestParam String pais) {
        List<VistaTramite> tramites = buscarTramitesPorPaisUseCase.buscarPorPais(pais);
        return ResponseEntity.ok(mapper.aListaDto(tramites));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta los detalles de un trámite por su identificador UUID aplicando Cache-Aside (cabecera X-Cache: HIT|MISS)")
    public ResponseEntity<TramiteDto> consultarPorId(
            @Parameter(description = "Identificador único UUID del trámite", required = true)
            @PathVariable UUID id) {
        var resultado = consultarTramiteUseCase.consultarConOrigenCache(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el trámite en vista_tramite con identificador: " + id));
        return ResponseEntity.ok()
                .header("X-Cache", resultado.desdeCache() ? "HIT" : "MISS")
                .body(mapper.aDto(resultado.vista()));
    }

    @GetMapping("/{id}/historial")
    @Operation(summary = "Devuelve la secuencia ordenada de eventos de dominio (Event Sourcing) del trámite")
    public ResponseEntity<List<EventoHistorialDto>> consultarHistorial(
            @Parameter(description = "Identificador único UUID del trámite", required = true)
            @PathVariable UUID id) {
        List<EventoVersionadoTramite> historial = consultarHistorialTramiteUseCase.consultarHistorial(id);
        return ResponseEntity.ok(mapper.aListaHistorialDto(historial));
    }

    @GetMapping
    @Operation(summary = "Lista todos los trámites de un ciudadano por DNI leyendo exclusivamente de la vista CQRS")
    public ResponseEntity<List<TramiteDto>> listarPorDni(
            @Parameter(description = "Número de DNI de 8 dígitos", required = true)
            @RequestParam String dni) {
        List<VistaTramite> tramites = listarTramitesPorDniUseCase.listarPorDni(dni);
        return ResponseEntity.ok(mapper.aListaDto(tramites));
    }
}
