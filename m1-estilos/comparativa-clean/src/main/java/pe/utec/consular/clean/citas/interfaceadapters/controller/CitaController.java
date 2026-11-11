package pe.utec.consular.clean.citas.interfaceadapters.controller;

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
import pe.utec.consular.clean.citas.interfaceadapters.presenter.CitaPresenter;
import pe.utec.consular.clean.citas.interfaceadapters.presenter.CitaViewModel;
import pe.utec.consular.clean.citas.usecase.cancelar.CancelarCitaInputBoundary;
import pe.utec.consular.clean.citas.usecase.consultar.ConsultarCitaInputBoundary;
import pe.utec.consular.clean.citas.usecase.registrar.RegistrarCitaInputBoundary;
import pe.utec.consular.clean.citas.usecase.registrar.RegistrarCitaRequestModel;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Controlador REST de citas consulares (Círculo 3: Interface Adapters).
 * <p>Concepto: Clean Architecture – Controller + Presenter (sección 1.3 del plan).
 * <p><b>Diferencia clave con la versión Hexagonal ({@code monolito-modular}):</b>
 * <ol>
 *   <li>Construye el {@link RegistrarCitaRequestModel} a partir de la petición HTTP.</li>
 *   <li>Invoca {@link RegistrarCitaInputBoundary#ejecutar(RegistrarCitaRequestModel)} (retorno {@code void}).</li>
 *   <li>Obtiene el {@link CitaViewModel} ya formateado desde el {@link CitaPresenter}, que fue invocado
 *       por el {@code Interactor} a través del {@code OutputBoundary}.</li>
 * </ol>
 * <p>Sesión: S2. Compárese con {@code pe.utec.consular.monolito.citas.adapter.in.web.CitaController}.
 */
@RestController
@RequestMapping("/api/citas")
@Tag(name = "Citas", description = "Operaciones de reserva, consulta y cancelación de citas consulares (Clean Architecture)")
public class CitaController {

    private final RegistrarCitaInputBoundary registrarCitaInputBoundary;
    private final ConsultarCitaInputBoundary consultarCitaInputBoundary;
    private final CancelarCitaInputBoundary cancelarCitaInputBoundary;
    private final CitaPresenter citaPresenter;

    public CitaController(RegistrarCitaInputBoundary registrarCitaInputBoundary,
                          ConsultarCitaInputBoundary consultarCitaInputBoundary,
                          CancelarCitaInputBoundary cancelarCitaInputBoundary,
                          CitaPresenter citaPresenter) {
        this.registrarCitaInputBoundary = registrarCitaInputBoundary;
        this.consultarCitaInputBoundary = consultarCitaInputBoundary;
        this.cancelarCitaInputBoundary = cancelarCitaInputBoundary;
        this.citaPresenter = citaPresenter;
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
    public ResponseEntity<CitaViewModel> registrarCita(@Valid @RequestBody RegistrarCitaHttpRequest request) {
        RegistrarCitaRequestModel requestModel = new RegistrarCitaRequestModel(
                request.dni(),
                request.nombres(),
                request.email(),
                request.oficinaId(),
                request.fecha(),
                request.hora(),
                request.tipoTramite()
        );

        registrarCitaInputBoundary.ejecutar(requestModel);
        CitaViewModel viewModel = citaPresenter.obtenerViewModel();

        URI location = URI.create("/api/citas/" + viewModel.id());
        return ResponseEntity.created(location).body(viewModel);
    }

    @GetMapping("/{citaId}")
    @Operation(summary = "Consulta los detalles de una cita por su identificador")
    public ResponseEntity<CitaViewModel> consultarPorId(
            @Parameter(description = "Identificador único UUID de la cita", required = true)
            @PathVariable UUID citaId) {
        consultarCitaInputBoundary.consultarPorId(citaId);
        CitaViewModel viewModel = citaPresenter.obtenerViewModel();
        return ResponseEntity.ok(viewModel);
    }

    @GetMapping
    @Operation(summary = "Lista todas las citas registradas para un ciudadano por DNI")
    public ResponseEntity<List<CitaViewModel>> consultarPorDni(
            @Parameter(description = "Número de DNI de 8 dígitos", required = true)
            @RequestParam String dni) {
        consultarCitaInputBoundary.consultarPorDni(dni);
        List<CitaViewModel> viewModels = citaPresenter.obtenerListaViewModel();
        return ResponseEntity.ok(viewModels);
    }

    @DeleteMapping("/{citaId}")
    @Operation(summary = "Cancela una cita activa y libera el cupo")
    public ResponseEntity<Void> cancelarCita(
            @Parameter(description = "Identificador único UUID de la cita", required = true)
            @PathVariable UUID citaId) {
        cancelarCitaInputBoundary.cancelar(citaId);
        citaPresenter.obtenerCancelacion();
        return ResponseEntity.noContent().build();
    }
}

