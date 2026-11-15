package pe.utec.consular.serverless.funciones;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.utec.consular.serverless.dominio.EstadoCita;
import pe.utec.consular.serverless.dominio.TipoTramite;

@Tag("integracion")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservarCitaIntegracionTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Ciclo completo de funciones sobre PostgreSQL 16 (infoSistema, listarOficinas, consultarDisponibilidad, reservarCita, consultarCita, cancelarCita)")
    void deberiaEjecutarCicloCompletoFuncionesServerless() {
        ResponseEntity<InfoSistemaDto> infoRes = restTemplate.getForEntity("/infoSistema", InfoSistemaDto.class);
        assertThat(infoRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(infoRes.getBody()).isNotNull();
        assertThat(infoRes.getBody().variante()).isEqualTo("SERVERLESS");

        ResponseEntity<OficinaDto[]> oficinasRes = restTemplate.getForEntity("/listarOficinas", OficinaDto[].class);
        assertThat(oficinasRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(oficinasRes.getBody()).hasSize(6);

        LocalDate fecha = proximoDiaHabil();
        HttpHeaders dispHeaders = new HttpHeaders();
        dispHeaders.set("X-Oficina-Id", "MAD");
        dispHeaders.set("X-Fecha", fecha.toString());
        ResponseEntity<DisponibilidadDto> dispRes = restTemplate.exchange(
                "/consultarDisponibilidad",
                HttpMethod.GET,
                new HttpEntity<>(dispHeaders),
                DisponibilidadDto.class
        );
        assertThat(dispRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(dispRes.getBody()).isNotNull();
        assertThat(dispRes.getBody().cupos()).hasSize(14);

        RegistrarCitaRequest req = new RegistrarCitaRequest(
                "45678912",
                "Ana Quispe Rojas",
                "ana.quispe@ejemplo.pe",
                "MAD",
                TipoTramite.PASAPORTE,
                fecha,
                LocalTime.of(10, 30)
        );
        ResponseEntity<CitaDto> reservaRes = restTemplate.postForEntity("/reservarCita", req, CitaDto.class);
        assertThat(reservaRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(reservaRes.getHeaders().getLocation()).isNotNull();
        CitaDto citaCreada = reservaRes.getBody();
        assertThat(citaCreada).isNotNull();
        assertThat(citaCreada.estado()).isEqualTo(EstadoCita.RESERVADA);

        HttpHeaders consultaHeaders = new HttpHeaders();
        consultaHeaders.set("X-Cita-Id", citaCreada.id().toString());
        ResponseEntity<CitaDto> consultaRes = restTemplate.exchange(
                "/consultarCita",
                HttpMethod.GET,
                new HttpEntity<>(consultaHeaders),
                CitaDto.class
        );
        assertThat(consultaRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(consultaRes.getBody()).isNotNull();
        assertThat(consultaRes.getBody().codigo()).isEqualTo(citaCreada.codigo());

        ResponseEntity<Void> cancelarRes = restTemplate.exchange(
                "/cancelarCita",
                HttpMethod.DELETE,
                new HttpEntity<>(consultaHeaders),
                Void.class
        );
        assertThat(cancelarRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private LocalDate proximoDiaHabil() {
        LocalDate fecha = LocalDate.now(ZoneOffset.UTC).plusDays(3);
        while (fecha.getDayOfWeek() == DayOfWeek.SATURDAY || fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
            fecha = fecha.plusDays(1);
        }
        return fecha;
    }
}

