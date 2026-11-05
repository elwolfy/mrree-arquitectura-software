package pe.utec.consular.monolito.citas.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import pe.utec.consular.monolito.citas.application.port.out.CupoRepositoryPort;
import pe.utec.consular.monolito.citas.application.port.out.DisponibilidadCachePort;
import pe.utec.consular.monolito.citas.application.port.out.OficinaRepositoryPort;
import pe.utec.consular.monolito.citas.domain.Cupo;
import pe.utec.consular.monolito.citas.domain.Disponibilidad;
import pe.utec.consular.monolito.citas.domain.Disponibilidad.CupoDisponibilidad;
import pe.utec.consular.monolito.compartido.domain.ModoConsistencia;
import pe.utec.consular.monolito.compartido.domain.exception.BaseDeDatosNoDisponibleException;
import pe.utec.consular.monolito.compartido.infraestructura.ConsistenciaProperties;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias del teorema CAP en ConsultarDisponibilidadService (CP vs AP)")
class ConsultarDisponibilidadServiceTest {

    @Mock
    private CupoRepositoryPort cupoRepositoryPort;

    @Mock
    private OficinaRepositoryPort oficinaRepositoryPort;

    @Mock
    private DisponibilidadCachePort disponibilidadCachePort;

    private ConsistenciaProperties consistenciaProperties;
    private ConsultarDisponibilidadService service;

    private final LocalDate fecha = LocalDate.of(2026, 11, 30);
    private final String oficinaId = "MAD";

    @BeforeEach
    void setUp() {
        consistenciaProperties = new ConsistenciaProperties();
        service = new ConsultarDisponibilidadService(
                cupoRepositoryPort,
                oficinaRepositoryPort,
                disponibilidadCachePort,
                consistenciaProperties
        );
    }

    @Test
    @DisplayName("Con BD disponible en modo CP, retorna datos con origen BASE_DE_DATOS")
    void consultaExitosaEnModoCP() {
        consistenciaProperties.setModo(ModoConsistencia.CP);
        when(oficinaRepositoryPort.existe(oficinaId)).thenReturn(true);

        Cupo cupo = new Cupo(UUID.randomUUID(), oficinaId, fecha, LocalTime.of(8, 0), 2, 1, 0);
        when(cupoRepositoryPort.buscarPorOficinaYFecha(oficinaId, fecha)).thenReturn(List.of(cupo));

        Disponibilidad resultado = service.consultar(oficinaId, fecha);

        assertNotNull(resultado);
        assertEquals("BASE_DE_DATOS", resultado.origen());
        assertEquals(0, resultado.actualizadoHaceSegundos());
        assertEquals(1, resultado.cupos().size());
        verify(disponibilidadCachePort).guardar(eq(oficinaId), eq(fecha), any(Disponibilidad.class));
    }

    @Test
    @DisplayName("Con BD no disponible en modo CP, lanza BaseDeDatosNoDisponibleException (503)")
    void fallaBdEnModoCP_lanzaExcepcion() {
        consistenciaProperties.setModo(ModoConsistencia.CP);
        when(oficinaRepositoryPort.existe(oficinaId)).thenThrow(new DataAccessResourceFailureException("Timeout"));

        assertThrows(BaseDeDatosNoDisponibleException.class, () -> service.consultar(oficinaId, fecha));
    }

    @Test
    @DisplayName("Con BD no disponible en modo AP, responde desde caché local con origen CACHE")
    void fallaBdEnModoAP_respondeDesdeCache() {
        consistenciaProperties.setModo(ModoConsistencia.AP);
        when(oficinaRepositoryPort.existe(oficinaId)).thenThrow(new DataAccessResourceFailureException("Timeout"));

        Disponibilidad dispCache = new Disponibilidad(
                oficinaId,
                fecha,
                List.of(new CupoDisponibilidad(LocalTime.of(8, 0), 2, 0, 2)),
                "CACHE",
                5
        );
        when(disponibilidadCachePort.obtener(oficinaId, fecha)).thenReturn(Optional.of(dispCache));

        Disponibilidad resultado = service.consultar(oficinaId, fecha);

        assertNotNull(resultado);
        assertEquals("CACHE", resultado.origen());
        assertEquals(5, resultado.actualizadoHaceSegundos());
        assertEquals(1, resultado.cupos().size());
    }

    @Test
    @DisplayName("Con BD no disponible en modo AP pero sin datos en caché, lanza BaseDeDatosNoDisponibleException")
    void fallaBdEnModoAP_sinCache_lanzaExcepcion() {
        consistenciaProperties.setModo(ModoConsistencia.AP);
        when(oficinaRepositoryPort.existe(oficinaId)).thenThrow(new DataAccessResourceFailureException("Timeout"));
        when(disponibilidadCachePort.obtener(oficinaId, fecha)).thenReturn(Optional.empty());

        assertThrows(BaseDeDatosNoDisponibleException.class, () -> service.consultar(oficinaId, fecha));
    }
}
