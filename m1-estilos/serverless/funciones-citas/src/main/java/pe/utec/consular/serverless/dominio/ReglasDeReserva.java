package pe.utec.consular.serverless.dominio;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public final class ReglasDeReserva {

    private static final int ANTICIPACION_MINIMA_DIAS = 1;
    private static final int ANTICIPACION_MAXIMA_DIAS = 60;
    private static final LocalTime HORA_APERTURA = LocalTime.of(9, 0);
    private static final LocalTime HORA_ULTIMO_TURNO = LocalTime.of(15, 30);

    private ReglasDeReserva() {
    }

    public static void validarFechaYHora(LocalDate fecha, LocalTime hora, LocalDate fechaActual) {
        LocalDate fechaMinima = fechaActual.plusDays(ANTICIPACION_MINIMA_DIAS);
        LocalDate fechaMaxima = fechaActual.plusDays(ANTICIPACION_MAXIMA_DIAS);

        if (fecha.isBefore(fechaMinima) || fecha.isAfter(fechaMaxima)) {
            throw new ReglaReservaVioladaException(
                    "La fecha de la cita debe estar entre %s y %s (recibida: %s)"
                            .formatted(fechaMinima, fechaMaxima, fecha));
        }

        DayOfWeek dia = fecha.getDayOfWeek();
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
            throw new ReglaReservaVioladaException(
                    "No se atienden citas en fines de semana (fecha solicitada: %s)".formatted(fecha));
        }

        if (hora.isBefore(HORA_APERTURA)
                || hora.isAfter(HORA_ULTIMO_TURNO)
                || (hora.getMinute() != 0 && hora.getMinute() != 30)
                || hora.getSecond() != 0) {
            throw new ReglaReservaVioladaException(
                    "La hora de la cita debe estar en bloques de 30 minutos entre 09:00 y 15:30 (recibida: %s)"
                            .formatted(hora));
        }
    }
}

