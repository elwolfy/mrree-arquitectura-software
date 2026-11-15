package pe.utec.consular.serverless.funciones;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.client.RestClient;
import pe.utec.consular.serverless.dominio.Cita;
import pe.utec.consular.serverless.dominio.CodigoCita;
import pe.utec.consular.serverless.dominio.Cupo;
import pe.utec.consular.serverless.dominio.Dni;
import pe.utec.consular.serverless.dominio.Email;
import pe.utec.consular.serverless.dominio.EstadoCita;
import pe.utec.consular.serverless.dominio.EstadoNotificacion;
import pe.utec.consular.serverless.dominio.OficinaConsular;
import pe.utec.consular.serverless.dominio.OficinaNoEncontradaException;
import pe.utec.consular.serverless.dominio.TipoTramite;
import pe.utec.consular.serverless.infraestructura.AlmacenDatosServerless;

class FuncionesUnitariasTest {

    private AlmacenDatosServerless almacen;
    private RestClient restClient;
    private CatalogoFuncionesConfig config;

    @BeforeEach
    void setUp() {
        almacen = mock(AlmacenDatosServerless.class);
        restClient = mock(RestClient.class);
        config = new CatalogoFuncionesConfig(
                almacen,
                null,
                restClient,
                "http://localhost:8080/notificar",
                "CP",
                "CONSOLA",
                false
        );
    }

    @Test
    @DisplayName("listarOficinas devuelve el catálogo de oficinas consulares")
    void deberiaListarOficinas() {
        when(almacen.listarOficinas()).thenReturn(List.of(
                new OficinaConsular("MAD", "Consulado Madrid", "ES", "Madrid", "Europe/Madrid")
        ));

        List<OficinaDto> resultado = config.listarOficinas().get();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().id()).isEqualTo("MAD");
    }

    @Test
    @DisplayName("consultarDisponibilidad devuelve los cupos de una oficina y fecha")
    void deberiaConsultarDisponibilidad() {
        LocalDate fecha = proximoDiaHabil();
        OficinaConsular mad = new OficinaConsular("MAD", "Consulado Madrid", "ES", "Madrid", "Europe/Madrid");
        Cupo cupo = new Cupo(UUID.randomUUID(), "MAD", fecha, LocalTime.of(9, 0), 5, 1, 0);

        when(almacen.buscarOficinaPorId("MAD")).thenReturn(Optional.of(mad));
        when(almacen.obtenerCupos("MAD", fecha)).thenReturn(List.of(cupo));

        Message<String> msg = MessageBuilder.withPayload("{}")
                .setHeader("X-Oficina-Id", "MAD")
                .setHeader("X-Fecha", fecha.toString())
                .build();

        DisponibilidadDto dto = config.consultarDisponibilidad().apply(msg);

        assertThat(dto.oficinaId()).isEqualTo("MAD");
        assertThat(dto.fecha()).isEqualTo(fecha);
        assertThat(dto.cupos()).hasSize(1);
        assertThat(dto.cupos().getFirst().disponibles()).isEqualTo(4);
    }

    @Test
    @DisplayName("consultarDisponibilidad lanza OficinaNoEncontradaException cuando la oficina no existe")
    void deberiaFallarDisponibilidadSiOficinaNoExiste() {
        when(almacen.buscarOficinaPorId("XXX")).thenReturn(Optional.empty());

        Message<String> msg = MessageBuilder.withPayload("{}")
                .setHeader("X-Oficina-Id", "XXX")
                .setHeader("X-Fecha", proximoDiaHabil().toString())
                .build();

        assertThatThrownBy(() -> config.consultarDisponibilidad().apply(msg))
                .isInstanceOf(OficinaNoEncontradaException.class);
    }

    @Test
    @DisplayName("consultarCita obtiene detalle por ID o lista por DNI")
    void deberiaConsultarCitaPorIdYPorDni() {
        UUID citaId = UUID.randomUUID();
        LocalDate fecha = proximoDiaHabil();
        Cita cita = new Cita(
                citaId,
                new CodigoCita("CITA-MAD-2026-000001"),
                new Dni("45678912"),
                "Ana Quispe Rojas",
                new Email("ana.quispe@ejemplo.pe"),
                "MAD",
                UUID.randomUUID(),
                fecha,
                LocalTime.of(10, 30),
                TipoTramite.PASAPORTE,
                EstadoCita.RESERVADA,
                EstadoNotificacion.ENVIADA,
                Instant.now()
        );

        when(almacen.buscarCitaPorId(citaId)).thenReturn(Optional.of(cita));
        when(almacen.buscarCitasPorDni(new Dni("45678912"))).thenReturn(List.of(cita));

        Message<String> msgPorId = MessageBuilder.withPayload("{}")
                .setHeader("X-Cita-Id", citaId.toString())
                .build();
        Object resPorId = config.consultarCita().apply(msgPorId);
        assertThat(resPorId).isInstanceOf(CitaDto.class);
        assertThat(((CitaDto) resPorId).codigo()).isEqualTo("CITA-MAD-2026-000001");

        Message<String> msgPorDni = MessageBuilder.withPayload("{}")
                .setHeader("X-Dni", "45678912")
                .build();
        Object resPorDni = config.consultarCita().apply(msgPorDni);
        assertThat(resPorDni).isInstanceOf(List.class);
        assertThat((List<?>) resPorDni).hasSize(1);
    }

    @Test
    @DisplayName("reservarCita marca notificación ENVIADA cuando notificar responde en < 300 ms")
    void deberiaReservarCitaConNotificacionEnviadaCuandoRespondeRapido() {
        LocalDate fecha = proximoDiaHabil();
        OficinaConsular mad = new OficinaConsular("MAD", "Consulado Madrid", "ES", "Madrid", "Europe/Madrid");
        Cupo cupo = new Cupo(UUID.randomUUID(), "MAD", fecha, LocalTime.of(10, 30), 5, 0, 0);

        when(almacen.buscarOficinaPorId("MAD")).thenReturn(Optional.of(mad));
        when(almacen.existeCitaActiva(new Dni("45678912"), TipoTramite.PASAPORTE)).thenReturn(false);
        when(almacen.buscarYObtenerCupo("MAD", fecha, LocalTime.of(10, 30))).thenReturn(Optional.of(cupo));
        when(almacen.guardarCupo(any())).thenAnswer(inv -> inv.getArgument(0));
        when(almacen.siguienteCorrelativoCodigo()).thenReturn(1L);
        when(almacen.guardarCita(any())).thenAnswer(inv -> inv.getArgument(0));

        RestClient.RequestBodyUriSpec uriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
        when(restClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(any(String.class))).thenReturn(bodySpec);
        when(bodySpec.contentType(any())).thenReturn(bodySpec);
        when(bodySpec.body(any(AvisoRequest.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.accepted().build());

        RegistrarCitaRequest req = new RegistrarCitaRequest(
                "45678912",
                "Ana Quispe Rojas",
                "ana.quispe@ejemplo.pe",
                "MAD",
                TipoTramite.PASAPORTE,
                fecha,
                LocalTime.of(10, 30)
        );

        CitaDto dto = config.reservarCita().apply(req);

        assertThat(dto.estado()).isEqualTo(EstadoCita.RESERVADA);
        assertThat(dto.notificacion()).isEqualTo(EstadoNotificacion.ENVIADA);
    }

    @Test
    @DisplayName("reservarCita deja notificación PENDIENTE cuando la función notificar falla o tarda >= 300 ms")
    void deberiaReservarCitaConNotificacionPendienteSiNotificarFalla() {
        LocalDate fecha = proximoDiaHabil();
        OficinaConsular mad = new OficinaConsular("MAD", "Consulado Madrid", "ES", "Madrid", "Europe/Madrid");
        Cupo cupo = new Cupo(UUID.randomUUID(), "MAD", fecha, LocalTime.of(11, 0), 5, 0, 0);

        when(almacen.buscarOficinaPorId("MAD")).thenReturn(Optional.of(mad));
        when(almacen.existeCitaActiva(new Dni("45678912"), TipoTramite.PASAPORTE)).thenReturn(false);
        when(almacen.buscarYObtenerCupo("MAD", fecha, LocalTime.of(11, 0))).thenReturn(Optional.of(cupo));
        when(almacen.guardarCupo(any())).thenAnswer(inv -> inv.getArgument(0));
        when(almacen.siguienteCorrelativoCodigo()).thenReturn(2L);
        when(almacen.guardarCita(any())).thenAnswer(inv -> inv.getArgument(0));
        when(restClient.post()).thenThrow(new RuntimeException("Conexión rechazada a m1-fn-notificar"));

        RegistrarCitaRequest req = new RegistrarCitaRequest(
                "45678912",
                "Ana Quispe Rojas",
                "ana.quispe@ejemplo.pe",
                "MAD",
                TipoTramite.PASAPORTE,
                fecha,
                LocalTime.of(11, 0)
        );

        CitaDto dto = config.reservarCita().apply(req);

        assertThat(dto.estado()).isEqualTo(EstadoCita.RESERVADA);
        assertThat(dto.notificacion()).isEqualTo(EstadoNotificacion.PENDIENTE);
    }

    @Test
    @DisplayName("cancelarCita cambia el estado a CANCELADA y libera el cupo")
    void deberiaCancelarCita() {
        UUID citaId = UUID.randomUUID();
        UUID cupoId = UUID.randomUUID();
        LocalDate fecha = proximoDiaHabil();
        Cupo cupo = new Cupo(cupoId, "MAD", fecha, LocalTime.of(10, 30), 5, 1, 0);
        Cita cita = new Cita(
                citaId,
                new CodigoCita("CITA-MAD-2026-000001"),
                new Dni("45678912"),
                "Ana Quispe Rojas",
                new Email("ana.quispe@ejemplo.pe"),
                "MAD",
                cupoId,
                fecha,
                LocalTime.of(10, 30),
                TipoTramite.PASAPORTE,
                EstadoCita.RESERVADA,
                EstadoNotificacion.ENVIADA,
                Instant.now()
        );

        when(almacen.buscarCitaPorId(citaId)).thenReturn(Optional.of(cita));
        when(almacen.buscarCupoPorId(cupoId)).thenReturn(Optional.of(cupo));

        Message<String> msg = MessageBuilder.withPayload("{}")
                .setHeader("X-Cita-Id", citaId.toString())
                .build();

        config.cancelarCita().accept(msg);

        assertThat(cita.getEstado()).isEqualTo(EstadoCita.CANCELADA);
        assertThat(cupo.getOcupados()).isZero();
        verify(almacen).guardarCita(cita);
        verify(almacen).guardarCupo(cupo);
    }

    @Test
    @DisplayName("notificar registra el aviso en el almacén de datos")
    void deberiaNotificarYGuardarAviso() {
        AvisoRequest req = new AvisoRequest(
                "ana.quispe@ejemplo.pe",
                "Confirmación CITA-MAD-2026-000001",
                "Detalle de su cita",
                "CITA-MAD-2026-000001"
        );

        config.notificar().accept(req);

        verify(almacen).guardarAviso(any());
    }

    @Test
    @DisplayName("infoSistema devuelve variante SERVERLESS")
    void deberiaDevolverInfoSistemaServerless() {
        InfoSistemaDto info = config.infoSistema().get();

        assertThat(info.modulo()).isEqualTo("m1-estilos");
        assertThat(info.variante()).isEqualTo("SERVERLESS");
        assertThat(info.arquitecturaInterna()).isEqualTo("HEXAGONAL");
        assertThat(info.modoConsistencia()).isEqualTo("CP");
    }

    private LocalDate proximoDiaHabil() {
        LocalDate fecha = LocalDate.now(ZoneOffset.UTC).plusDays(2);
        while (fecha.getDayOfWeek() == DayOfWeek.SATURDAY || fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
            fecha = fecha.plusDays(1);
        }
        return fecha;
    }
}

