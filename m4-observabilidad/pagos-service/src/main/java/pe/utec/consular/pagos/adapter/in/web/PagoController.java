package pe.utec.consular.pagos.adapter.in.web;

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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.utec.consular.pagos.application.port.in.AnularPagoCommand;
import pe.utec.consular.pagos.application.port.in.AnularPagoUseCase;
import pe.utec.consular.pagos.application.port.in.ConsultarPagoUseCase;
import pe.utec.consular.pagos.application.port.in.RegistrarPagoCommand;
import pe.utec.consular.pagos.application.port.in.RegistrarPagoUseCase;
import pe.utec.consular.pagos.domain.OrdenDePago;
import pe.utec.consular.pagos.domain.exception.RecursoNoEncontradoException;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para el registro idempotente, consulta y anulación de pagos consulares (:8083).
 */
@RestController
@RequestMapping("/api/pagos")
@Tag(name = "Pagos", description = "Registro idempotente con Idempotency-Key, consulta y anulación de pagos consulares")
public class PagoController {

    private final RegistrarPagoUseCase registrarPagoUseCase;
    private final AnularPagoUseCase anularPagoUseCase;
    private final ConsultarPagoUseCase consultarPagoUseCase;

    public PagoController(RegistrarPagoUseCase registrarPagoUseCase,
                          AnularPagoUseCase anularPagoUseCase,
                          ConsultarPagoUseCase consultarPagoUseCase) {
        this.registrarPagoUseCase = registrarPagoUseCase;
        this.anularPagoUseCase = anularPagoUseCase;
        this.consultarPagoUseCase = consultarPagoUseCase;
    }

    @PostMapping
    @Operation(summary = "Registra un pago de manera idempotente exigiendo la cabecera Idempotency-Key y publica pago.confirmado vía Outbox")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago confirmado o respuesta idempotente devuelta"),
            @ApiResponse(responseCode = "400", description = "Falta la cabecera Idempotency-Key o datos inválidos")
    })
    public ResponseEntity<PagoDto> registrarPago(
            @Parameter(description = "Clave de idempotencia única por intención de pago (obligatoria)", required = true)
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody RegistrarPagoRequest request) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("La cabecera 'Idempotency-Key' es obligatoria en POST /api/pagos");
        }
        OrdenDePago orden = registrarPagoUseCase.registrarPago(new RegistrarPagoCommand(
                request.tramiteId(),
                request.monto(),
                idempotencyKey
        ));
        return ResponseEntity.ok(PagoDto.desdeDominio(orden));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta un pago por su identificador UUID")
    public ResponseEntity<PagoDto> consultarPorId(
            @Parameter(description = "Identificador único UUID del pago", required = true)
            @PathVariable UUID id) {
        return consultarPagoUseCase.consultarPorId(id)
                .map(PagoDto::desdeDominio)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el pago con identificador: " + id));
    }

    @GetMapping
    @Operation(summary = "Lista los pagos asociados a un trámite consular")
    public ResponseEntity<List<PagoDto>> listarPorTramiteId(
            @Parameter(description = "Identificador UUID del trámite", required = true)
            @RequestParam UUID tramiteId) {
        List<PagoDto> pagos = consultarPagoUseCase.listarPorTramiteId(tramiteId).stream()
                .map(PagoDto::desdeDominio)
                .toList();
        return ResponseEntity.ok(pagos);
    }

    @PostMapping("/{id}/anulacion")
    @Operation(summary = "Anula un pago confirmado y publica pago.anulado vía Outbox (Andamiaje de la Asignación A3)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago anulado satisfactoriamente"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado"),
            @ApiResponse(responseCode = "422", description = "Datos o estado inválido para anulación")
    })
    public ResponseEntity<PagoDto> anularPago(
            @Parameter(description = "Identificador único UUID del pago a anular", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody AnularPagoRequest request) {
        OrdenDePago orden = anularPagoUseCase.anularPago(new AnularPagoCommand(id, request.motivo()));
        return ResponseEntity.ok(PagoDto.desdeDominio(orden));
    }
}

