package pe.utec.consular.serverless.funciones;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.Message;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pe.utec.consular.serverless.dominio.Aviso;
import pe.utec.consular.serverless.dominio.CanalNotificacion;
import pe.utec.consular.serverless.dominio.Cita;
import pe.utec.consular.serverless.dominio.CitaDuplicadaException;
import pe.utec.consular.serverless.dominio.CitaNoEncontradaException;
import pe.utec.consular.serverless.dominio.CodigoCita;
import pe.utec.consular.serverless.dominio.Cupo;
import pe.utec.consular.serverless.dominio.CupoAgotadoException;
import pe.utec.consular.serverless.dominio.Disponibilidad;
import pe.utec.consular.serverless.dominio.Dni;
import pe.utec.consular.serverless.dominio.Email;
import pe.utec.consular.serverless.dominio.EstadoNotificacion;
import pe.utec.consular.serverless.dominio.OficinaNoEncontradaException;
import pe.utec.consular.serverless.dominio.ReglasDeReserva;
import pe.utec.consular.serverless.infraestructura.AlmacenDatosServerless;

@Configuration
public class CatalogoFuncionesConfig {

    private static final Logger log = LoggerFactory.getLogger(CatalogoFuncionesConfig.class);
    private static final long TIMEOUT_NOTIFICACION_MS = 300L;

    private final AlmacenDatosServerless almacen;
    private final JavaMailSender mailSender;
    private final RestClient restClient;
    private final String fnNotificarUrl;
    private final String modoConsistencia;
    private final String canalNotificacion;
    private final boolean simularFallaNotificacion;

    @Autowired
    public CatalogoFuncionesConfig(
            AlmacenDatosServerless almacen,
            @Autowired(required = false) JavaMailSender mailSender,
            @Value("${consular.serverless.fn-notificar-url:http://localhost:8080/notificar}") String fnNotificarUrl,
            @Value("${consular.consistencia.modo:CP}") String modoConsistencia,
            @Value("${consular.notificaciones.canal:CONSOLA}") String canalNotificacion,
            @Value("${consular.notificaciones.simular-falla:false}") boolean simularFallaNotificacion
    ) {
        this(
                almacen,
                mailSender,
                RestClient.create(),
                fnNotificarUrl,
                modoConsistencia,
                canalNotificacion,
                simularFallaNotificacion
        );
    }

    public CatalogoFuncionesConfig(
            AlmacenDatosServerless almacen,
            JavaMailSender mailSender,
            RestClient restClient,
            String fnNotificarUrl,
            String modoConsistencia,
            String canalNotificacion,
            boolean simularFallaNotificacion
    ) {
        this.almacen = almacen;
        this.mailSender = mailSender;
        this.restClient = restClient;
        this.fnNotificarUrl = fnNotificarUrl;
        this.modoConsistencia = modoConsistencia;
        this.canalNotificacion = canalNotificacion;
        this.simularFallaNotificacion = simularFallaNotificacion;
    }

    @Bean
    public Supplier<List<OficinaDto>> listarOficinas() {
        return () -> almacen.listarOficinas().stream()
                .map(OficinaDto::desdeDominio)
                .toList();
    }

    @Bean
    public Function<Message<?>, DisponibilidadDto> consultarDisponibilidad() {
        return message -> {
            String oficinaId = extraerParametro(message, "X-Oficina-Id", "oficinaId");
            String fechaTexto = extraerParametro(message, "X-Fecha", "fecha");

            if (oficinaId == null || oficinaId.isBlank()) {
                throw new IllegalArgumentException("El parámetro oficinaId es obligatorio");
            }
            if (fechaTexto == null || fechaTexto.isBlank()) {
                throw new IllegalArgumentException("El parámetro fecha es obligatorio (formato YYYY-MM-DD)");
            }

            LocalDate fecha;
            try {
                fecha = LocalDate.parse(fechaTexto.trim());
            } catch (Exception ex) {
                throw new IllegalArgumentException("Formato de fecha inválido (se espera YYYY-MM-DD): " + fechaTexto);
            }

            if (almacen.buscarOficinaPorId(oficinaId).isEmpty()) {
                throw new OficinaNoEncontradaException(oficinaId);
            }

            List<Cupo> cupos = almacen.obtenerCupos(oficinaId, fecha);
            return DisponibilidadDto.desdeDominio(new Disponibilidad(oficinaId, fecha, modoConsistencia, cupos));
        };
    }

    @Bean
    public Function<Message<?>, Object> consultarCita() {
        return message -> {
            String citaIdTexto = extraerParametro(message, "X-Cita-Id", "id");
            String dniTexto = extraerParametro(message, "X-Dni", "dni");

            if (citaIdTexto != null && !citaIdTexto.isBlank()) {
                UUID citaId;
                try {
                    citaId = UUID.fromString(citaIdTexto.trim());
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("Identificador de cita inválido: " + citaIdTexto);
                }
                Cita cita = almacen.buscarCitaPorId(citaId)
                        .orElseThrow(() -> new CitaNoEncontradaException(citaId));
                return CitaDto.desdeDominio(cita);
            }

            if (dniTexto != null && !dniTexto.isBlank()) {
                Dni dni = new Dni(dniTexto.trim());
                return almacen.buscarCitasPorDni(dni).stream()
                        .map(CitaDto::desdeDominio)
                        .toList();
            }

            throw new IllegalArgumentException("Debe especificar el ID de la cita o el parámetro dni");
        };
    }

    @Bean
    public Function<RegistrarCitaRequest, CitaDto> reservarCita() {
        return req -> {
            if (req == null) {
                throw new IllegalArgumentException("El cuerpo de la solicitud de reserva es obligatorio");
            }
            if (req.oficinaId() == null || req.oficinaId().isBlank()) {
                throw new IllegalArgumentException("La oficina consular es obligatoria");
            }
            if (req.tipoTramite() == null) {
                throw new IllegalArgumentException("El tipo de trámite es obligatorio");
            }
            if (req.fecha() == null || req.hora() == null) {
                throw new IllegalArgumentException("La fecha y hora de la cita son obligatorias");
            }

            Dni dni = new Dni(req.dni());
            Email email = new Email(req.email());

            if (almacen.buscarOficinaPorId(req.oficinaId()).isEmpty()) {
                throw new OficinaNoEncontradaException(req.oficinaId());
            }

            ReglasDeReserva.validarFechaYHora(req.fecha(), req.hora(), LocalDate.now(ZoneOffset.UTC));

            if (almacen.existeCitaActiva(dni, req.tipoTramite())) {
                throw new CitaDuplicadaException(dni.valor(), req.tipoTramite());
            }

            Cupo cupo = almacen.buscarYObtenerCupo(req.oficinaId(), req.fecha(), req.hora())
                    .orElseThrow(() -> new CupoAgotadoException(req.oficinaId(), req.fecha(), req.hora()));

            cupo.ocuparPlaza();
            almacen.guardarCupo(cupo);

            long correlativo = almacen.siguienteCorrelativoCodigo();
            CodigoCita codigo = CodigoCita.generar(req.oficinaId(), req.fecha().getYear(), correlativo);

            Cita cita = Cita.crearNueva(codigo, dni, req.nombres(), email, req.oficinaId(), cupo, req.tipoTramite());
            almacen.guardarCita(cita);

            AvisoRequest avisoRequest = new AvisoRequest(
                    email.valor(),
                    "Confirmación de cita consular " + codigo.valor(),
                    "Estimado(a) %s, su cita %s para %s en la oficina %s fue programada para el %s a las %s."
                            .formatted(
                                    cita.getNombres(),
                                    codigo.valor(),
                                    cita.getTipoTramite(),
                                    cita.getOficinaId(),
                                    cita.getFecha(),
                                    cita.getHora()
                            ),
                    codigo.valor()
            );

            boolean notificadaATiempo = invocarNotificarAsincrono(avisoRequest);
            if (notificadaATiempo) {
                cita.actualizarEstadoNotificacion(EstadoNotificacion.ENVIADA);
                almacen.guardarCita(cita);
            }

            return CitaDto.desdeDominio(cita);
        };
    }

    @Bean
    public Consumer<Message<?>> cancelarCita() {
        return message -> {
            String citaIdTexto = extraerParametro(message, "X-Cita-Id", "id");
            if ((citaIdTexto == null || citaIdTexto.isBlank()) && message != null && message.getPayload() instanceof String s && !s.isBlank() && !"{}".equals(s.trim())) {
                citaIdTexto = s.replace("\"", "").trim();
            }
            if (citaIdTexto == null || citaIdTexto.isBlank()) {
                throw new IllegalArgumentException("El identificador de la cita es obligatorio para cancelar");
            }

            UUID citaId;
            try {
                citaId = UUID.fromString(citaIdTexto.trim());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Identificador de cita inválido: " + citaIdTexto);
            }

            Cita cita = almacen.buscarCitaPorId(citaId)
                    .orElseThrow(() -> new CitaNoEncontradaException(citaId));

            cita.cancelar();
            almacen.guardarCita(cita);

            almacen.buscarCupoPorId(cita.getCupoId()).ifPresent(cupo -> {
                cupo.liberarPlaza();
                almacen.guardarCupo(cupo);
            });
        };
    }

    @Bean
    public Consumer<AvisoRequest> notificar() {
        return req -> {
            if (req == null || req.destinatario() == null || req.destinatario().isBlank()) {
                throw new IllegalArgumentException("Los datos del aviso son obligatorios");
            }
            if (simularFallaNotificacion) {
                throw new IllegalStateException("Falla simulada en la función notificar");
            }

            CanalNotificacion canal = "CORREO".equalsIgnoreCase(canalNotificacion)
                    ? CanalNotificacion.CORREO
                    : CanalNotificacion.CONSOLA;
            EstadoNotificacion estado = EstadoNotificacion.ENVIADA;

            try {
                if (canal == CanalNotificacion.CORREO && mailSender != null) {
                    SimpleMailMessage mail = new SimpleMailMessage();
                    mail.setFrom("no-reply@consulado.gob.pe");
                    mail.setTo(req.destinatario());
                    mail.setSubject(req.asunto());
                    mail.setText(req.cuerpo());
                    mailSender.send(mail);
                } else {
                    log.info("[FUNCIÓN NOTIFICAR - CONSOLA] Para: {} | Asunto: {} | Ref: {}",
                            req.destinatario(), req.asunto(), req.referencia());
                }
            } catch (Exception ex) {
                log.warn("Error enviando notificación por {}: {}", canal, ex.getMessage());
                estado = EstadoNotificacion.FALLIDA;
            }

            Aviso aviso = new Aviso(
                    UUID.randomUUID(),
                    req.destinatario(),
                    req.asunto() != null ? req.asunto() : "Aviso Consular",
                    req.cuerpo() != null ? req.cuerpo() : "",
                    canal,
                    estado,
                    Instant.now(),
                    req.referencia()
            );
            almacen.guardarAviso(aviso);

            if (estado == EstadoNotificacion.FALLIDA) {
                throw new IllegalStateException("No fue posible enviar la notificación por el canal " + canal);
            }
        };
    }

    @Bean
    public Supplier<InfoSistemaDto> infoSistema() {
        return () -> new InfoSistemaDto(
                "m1-estilos",
                "SERVERLESS",
                "HEXAGONAL",
                modoConsistencia,
                "1.0.0"
        );
    }

    private boolean invocarNotificarAsincrono(AvisoRequest avisoRequest) {
        var executor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            CompletableFuture<Boolean> futuro = CompletableFuture.supplyAsync(() -> {
                restClient.post()
                        .uri(fnNotificarUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(avisoRequest)
                        .retrieve()
                        .toBodilessEntity();
                return true;
            }, executor);

            return Boolean.TRUE.equals(futuro.get(TIMEOUT_NOTIFICACION_MS, TimeUnit.MILLISECONDS));
        } catch (Exception ex) {
            log.warn("Invocación asíncrona a función notificar superó {} ms o falló ({}). La cita queda con notificación PENDIENTE.",
                    TIMEOUT_NOTIFICACION_MS, ex.getMessage());
            return false;
        }
    }

    private String extraerParametro(Message<?> message, String nombreCabecera, String nombreQuery) {
        if (message != null && message.getHeaders() != null) {
            for (Map.Entry<String, Object> entry : message.getHeaders().entrySet()) {
                if (entry.getKey().equalsIgnoreCase(nombreCabecera) && entry.getValue() != null) {
                    return String.valueOf(entry.getValue());
                }
            }
            Object paramsObj = message.getHeaders().get("http_request_param");
            if (paramsObj instanceof Map<?, ?> paramsMap) {
                Object val = paramsMap.get(nombreQuery);
                if (val != null) {
                    return String.valueOf(val);
                }
            }
        }

        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            HttpServletRequest request = servletAttrs.getRequest();
            String headerVal = request.getHeader(nombreCabecera);
            if (headerVal != null && !headerVal.isBlank()) {
                return headerVal;
            }
            String paramVal = request.getParameter(nombreQuery);
            if (paramVal != null && !paramVal.isBlank()) {
                return paramVal;
            }
        }
        return null;
    }
}

