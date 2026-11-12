package pe.utec.consular.notificaciones.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Pruebas unitarias de la entidad Aviso")
class AvisoTest {

    @Test
    @DisplayName("Crea un aviso con estado inicial ENVIADA y permite marcarlo como FALLIDA")
    void creaAvisoYMarcaFallida() {
        Aviso aviso = Aviso.nuevo(
                "ana@correo.lab",
                "Confirmación de Cita",
                "Su cita ha sido reservada.",
                CanalNotificacion.CONSOLA,
                "CIT-2026-000001"
        );

        assertThat(aviso.getId()).isNotNull();
        assertThat(aviso.getDestinatario()).isEqualTo("ana@correo.lab");
        assertThat(aviso.getCanal()).isEqualTo(CanalNotificacion.CONSOLA);
        assertThat(aviso.getEstado()).isEqualTo(EstadoNotificacion.ENVIADA);
        assertThat(aviso.getReferencia()).isEqualTo("CIT-2026-000001");

        aviso.marcarComoFallida();
        assertThat(aviso.getEstado()).isEqualTo(EstadoNotificacion.FALLIDA);
    }
}

