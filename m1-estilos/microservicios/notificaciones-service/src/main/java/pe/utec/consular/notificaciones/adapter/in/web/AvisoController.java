package pe.utec.consular.notificaciones.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.utec.consular.notificaciones.application.port.in.ConsultarAvisosUseCase;
import pe.utec.consular.notificaciones.application.port.in.EnviarAvisoCommand;
import pe.utec.consular.notificaciones.application.port.in.EnviarAvisoUseCase;
import pe.utec.consular.notificaciones.domain.Aviso;

import java.util.List;

/**
 * Controlador REST para la recepción y consulta de avisos en {@code notificaciones-service}.
 * <p>Concepto: Adaptador primario en arquitectura hexagonal y comunicación entre microservicios (secciones 1.2 y 1.3).
 * <p>Sesión: S1.
 */
@RestController
@RequestMapping("/api/avisos")
@Tag(name = "Avisos", description = "Envío y consulta de avisos a ciudadanos")
public class AvisoController {

    private final EnviarAvisoUseCase enviarAvisoUseCase;
    private final ConsultarAvisosUseCase consultarAvisosUseCase;

    public AvisoController(EnviarAvisoUseCase enviarAvisoUseCase,
                           ConsultarAvisosUseCase consultarAvisosUseCase) {
        this.enviarAvisoUseCase = enviarAvisoUseCase;
        this.consultarAvisosUseCase = consultarAvisosUseCase;
    }

    @PostMapping
    @Operation(summary = "Recibe y procesa una solicitud de aviso consular (202 Accepted)")
    public ResponseEntity<AvisoDto> enviar(@Valid @RequestBody AvisoRequest request) {
        EnviarAvisoCommand comando = new EnviarAvisoCommand(
                request.destinatario(),
                request.asunto(),
                request.cuerpo(),
                request.referencia()
        );
        Aviso aviso = enviarAvisoUseCase.enviar(comando);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(AvisoDto.desdeDominio(aviso));
    }

    @GetMapping
    @Operation(summary = "Consulta los avisos registrados opcionalmente filtrados por referencia")
    public ResponseEntity<List<AvisoDto>> consultarPorReferencia(
            @RequestParam(name = "referencia", required = false) String referencia) {
        List<AvisoDto> respuesta = consultarAvisosUseCase.consultarPorReferencia(referencia)
                .stream()
                .map(AvisoDto::desdeDominio)
                .toList();
        return ResponseEntity.ok(respuesta);
    }
}

