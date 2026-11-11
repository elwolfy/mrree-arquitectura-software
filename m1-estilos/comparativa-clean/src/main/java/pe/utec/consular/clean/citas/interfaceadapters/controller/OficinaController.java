package pe.utec.consular.clean.citas.interfaceadapters.controller;

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
import pe.utec.consular.clean.citas.interfaceadapters.presenter.DisponibilidadPresenter;
import pe.utec.consular.clean.citas.interfaceadapters.presenter.DisponibilidadViewModel;
import pe.utec.consular.clean.citas.interfaceadapters.presenter.OficinaPresenter;
import pe.utec.consular.clean.citas.interfaceadapters.presenter.OficinaViewModel;
import pe.utec.consular.clean.citas.usecase.disponibilidad.ConsultarDisponibilidadInputBoundary;
import pe.utec.consular.clean.citas.usecase.disponibilidad.ConsultarDisponibilidadRequestModel;
import pe.utec.consular.clean.citas.usecase.oficinas.ListarOficinasInputBoundary;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para el catálogo de oficinas y consulta de disponibilidad de cupos (Círculo 3: Interface Adapters).
 * <p>Concepto: Clean Architecture – Controller + Presenter e interacción con modo CAP (secciones 1.3 y 1.4 del plan).
 * <p>Sesión: S2. Compárese con {@code pe.utec.consular.monolito.citas.adapter.in.web.OficinaController}.
 */
@RestController
@RequestMapping("/api/oficinas")
@Tag(name = "Oficinas", description = "Catálogo de oficinas consulares y disponibilidad de cupos")
public class OficinaController {

    private final ListarOficinasInputBoundary listarOficinasInputBoundary;
    private final ConsultarDisponibilidadInputBoundary consultarDisponibilidadInputBoundary;
    private final OficinaPresenter oficinaPresenter;
    private final DisponibilidadPresenter disponibilidadPresenter;

    public OficinaController(ListarOficinasInputBoundary listarOficinasInputBoundary,
                             ConsultarDisponibilidadInputBoundary consultarDisponibilidadInputBoundary,
                             OficinaPresenter oficinaPresenter,
                             DisponibilidadPresenter disponibilidadPresenter) {
        this.listarOficinasInputBoundary = listarOficinasInputBoundary;
        this.consultarDisponibilidadInputBoundary = consultarDisponibilidadInputBoundary;
        this.oficinaPresenter = oficinaPresenter;
        this.disponibilidadPresenter = disponibilidadPresenter;
    }

    @GetMapping
    @Operation(summary = "Lista todas las oficinas consulares activas")
    public ResponseEntity<List<OficinaViewModel>> listarOficinas() {
        listarOficinasInputBoundary.listar();
        return ResponseEntity.ok(oficinaPresenter.obtenerViewModels());
    }

    @GetMapping("/{oficinaId}/disponibilidad")
    @Operation(summary = "Consulta la disponibilidad de cupos para una oficina y fecha específica")
    public ResponseEntity<DisponibilidadViewModel> consultarDisponibilidad(
            @Parameter(description = "Identificador de la oficina consular", example = "MAD", required = true)
            @PathVariable String oficinaId,
            @Parameter(description = "Fecha de consulta en formato YYYY-MM-DD", example = "2026-11-30", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        consultarDisponibilidadInputBoundary.consultar(new ConsultarDisponibilidadRequestModel(oficinaId, fecha));
        return ResponseEntity.ok(disponibilidadPresenter.obtenerViewModel());
    }
}

