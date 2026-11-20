workspace "Plataforma Digital de Servicios Consulares y de Legalización" "Modelo de arquitectura C4 de la plataforma consular (UTEC Posgrado – MRREE)" {

    model {
        # ----------------------------------------------------------------------
        # 1. Personas (Actores)
        # ----------------------------------------------------------------------
        ciudadano = person "Ciudadano" "Peruano en el exterior o en el país que reserva citas, inicia trámites de apostilla, legalización o registro consular, realiza pagos en línea y consulta el estado de su trámite." "Persona"
        funcionario = person "Funcionario Consular" "Servidor consular que atiende citas en la sede exterior, revisa expedientes, registra observaciones sobre documentos y supervisa la emisión." "Persona"

        # ----------------------------------------------------------------------
        # 2. Sistemas Externos
        # ----------------------------------------------------------------------
        pideReniec = softwareSystem "PIDE / RENIEC" "Plataforma de Interoperabilidad del Estado Peruano para validación oficial de identidad ciudadana por DNI (simulada con WireMock en laboratorio)." "Externo"
        pasarelaPagos = softwareSystem "Pasarela de Pagos" "Procesador externo de cobros con tarjeta y medios electrónicos (simulado en laboratorio)." "Externo"
        servicioCorreo = softwareSystem "Servicio de Correo" "Servidor SMTP institucional para envío de avisos y notificaciones al ciudadano (simulado con Mailpit en laboratorio)." "Externo"

        # ----------------------------------------------------------------------
        # 3. Sistema Principal y sus Contenedores
        # ----------------------------------------------------------------------
        plataformaConsular = softwareSystem "Plataforma Consular" "Sistema distribuido para reserva de citas consulares, gestión de trámites (apostilla, legalización, registro consular), cobro arancelario idempotente y emisión electrónica de documentos." {

            frontend = container "Frontend Ciudadano" "Aplicación web SPA que permite reservar citas, iniciar trámites, pagar con Idempotency-Key y consultar el estado con sondeo asíncrono." "Angular 19 + PrimeNG (Nginx :4200)" "WebBrowser"
            gateway = container "API Gateway" "Punto único de entrada HTTP (:8080): enrutamiento centralizado, propagación de X-Correlation-Id y traceparent, CORS, Rate Limiting y Swagger agregado." "Spring Cloud Gateway MVC (Java 21)"
            bffWeb = container "BFF Web" "Backend For Frontend (:8085) para la experiencia ciudadana: vista agregada (/bff/inicio) con degradación elegante y fachada de inicio de trámite (/bff/tramites)." "Spring Boot 3.4 (Java 21)"

            citasService = container "citas-service" "Microservicio (:8081) dueño del contexto de citas y cupos consulares con bloqueo pesimista y publicación de cita.reservada." "Spring Boot 3.4 (Java 21)"
            identidadService = container "identidad-service" "Microservicio (:8086) que valida el DNI del ciudadano contra PIDE/RENIEC mediante una Capa Anticorrupción (ACL), Circuit Breaker y Retry." "Spring Boot 3.4 + Resilience4j (Java 21)"
            pagosService = container "pagos-service" "Microservicio (:8083) de cálculo de tarifas (Strategy), cobro idempotente (Idempotency-Key) y Transactional Outbox propio." "Spring Boot 3.4 (Java 21)"
            emisionService = container "emision-service" "Microservicio (:8087) consumidor idempotente de emision.solicitar que genera y firma electrónicamente el documento consular." "Spring Boot 3.4 (Java 21)"
            notificacionesService = container "notificaciones-service" "Microservicio (:8084) consumidor idempotente de eventos de negocio con reintentos exponenciales, DLQ y envío SMTP." "Spring Boot 3.4 (Java 21)"

            tramitesService = container "tramites-service" "Microservicio (:8082 HTTP / :9082 gRPC) dueño del ciclo de vida del trámite con Event Sourcing, proyección CQRS, Transactional Outbox y orquestador de la Saga." "Spring Boot 3.4 + gRPC (Java 21)" {
                tramiteController = component "TramiteController" "Adaptador primario REST que expone POST /api/tramites, GET /api/tramites/{id}, GET /api/tramites/{id}/historial y reconstrucción CQRS." "Spring MVC @RestController"
                tramiteGrpcAdapter = component "TramitesGrpcService" "Adaptador primario gRPC (:9082) para consulta binaria de baja latencia (ConsultarTramite)." "gRPC Server Service"
                sagaEventosConsumer = component "SagaEventosConsumer" "Adaptador primario AMQP que escucha pago.confirmado, emision.completada y emision.fallida en RabbitMQ." "Spring AMQP @RabbitListener"
                registrarTramiteService = component "RegistrarTramiteService" "Caso de uso de registro de trámite: aplica Factory Method + Builder, persiste eventos, actualiza proyección y abre la Saga." "Application Service"
                consultarTramiteService = component "ConsultarTramiteService" "Caso de uso de lectura CQRS que consulta la proyección vista_tramite a través del puerto de caché." "Application Service"
                sagaSolicitudTramite = component "SagaSolicitudTramite" "Orquestador central de la Saga solicitud -> pago -> emisión; coordina los pasos y compensaciones e instrumenta spans (@WithSpan)." "Saga Orchestrator"
                proyectorVistaTramite = component "ProyectorVistaTramite" "Proyector CQRS que materializa el modelo de lectura tramites.vista_tramite e invalida el caché al cambiar el estado." "CQRS Projector"
                relayOutbox = component "RelayOutbox" "Publicador periódico (@Scheduled 500 ms) que lee registros pendientes de tramites.outbox y los envía a RabbitMQ con traceparent." "Transactional Outbox Relay"
                eventoStoreAdapter = component "EventoTramiteStoreAdapter" "Adaptador secundario de persistencia sobre la bitácora append-only tramites.eventos_tramite con control optimista de versión." "Spring Data JPA Adapter"
                vistaJpaAdapter = component "VistaTramiteJpaAdapter" "Adaptador secundario de persistencia para la proyección CQRS tramites.vista_tramite y el expediente JSONB." "Spring Data JPA Adapter"
                outboxPersistenceAdapter = component "OutboxPersistenceAdapter" "Adaptador secundario que inserta mensajes en tramites.outbox dentro de la misma transacción ACID local." "Spring Data JPA Adapter"
                anulacionPagoHttpAdapter = component "AnulacionPagoHttpAdapter" "Adaptador secundario HTTP que invoca POST /api/pagos/{id}/anulacion para compensar la Saga (Asignación A3)." "Spring RestClient Adapter"
                cacheConsultaAdapter = component "CacheConsultaTramiteAdapter" "Adaptador secundario del puerto CacheConsultaTramitePort (nulo en main; Redis cache-aside en Asignación A4)." "Cache Adapter"
            }

            postgres = container "PostgreSQL (Esquemas por Servicio)" "Motor relacional y documental JSONB (:5432, base consular) con aislamiento estricto Database-per-Service mediante esquemas y usuarios separados (citas, tramites, pagos, emision, notificaciones)." "PostgreSQL 16" "Database"
            rabbitmq = container "RabbitMQ Broker" "Broker de mensajería asíncrona (:5672 / :15672) con exchange topic consular.eventos, colas por consumidor y Dead Letter Queues (DLQ)." "RabbitMQ 3 Management" "Queue"

            # NOTA DIDÁCTICA — ASIGNACIÓN A4 (Tarea 1 Conceptual):
            # El contenedor Redis (:6379) NO se incluye en la rama main.
            # En la Asignación A4, el participante agrega aquí el contenedor Redis y su relación cache-aside con tramites-service.
        }

        # ----------------------------------------------------------------------
        # 4. Relaciones de Contexto y Contenedores
        # ----------------------------------------------------------------------
        ciudadano -> frontend "Reserva citas, inicia trámites, paga y consulta estados desde su navegador" "HTTPS"
        funcionario -> gateway "Consulta expedientes y gestiona observaciones consulares" "HTTPS / JSON"

        frontend -> gateway "Consume las APIs públicas de negocio y del BFF" "HTTP/JSON (:8080)"
        gateway -> bffWeb "Enruta solicitudes de vista agregada e inicio de trámite (/bff/**)" "HTTP/JSON"
        gateway -> citasService "Enruta operaciones de citas y oficinas (/api/citas/**, /api/oficinas/**)" "HTTP/JSON"
        gateway -> tramitesService "Enruta consultas e inicio directo de trámites (/api/tramites/**)" "HTTP/JSON"
        gateway -> pagosService "Enruta cálculo de tarifas y pagos idempotentes (/api/tarifas/**, /api/pagos/**)" "HTTP/JSON"
        gateway -> identidadService "Enruta validaciones de DNI (/api/identidad/**)" "HTTP/JSON"
        gateway -> notificacionesService "Enruta consulta de avisos (/api/avisos/**)" "HTTP/JSON"

        bffWeb -> identidadService "1. Valida identidad del ciudadano por DNI" "HTTP/JSON"
        bffWeb -> pagosService "2. Calcula tarifa arancelaria según tipo y país" "HTTP/JSON"
        bffWeb -> tramitesService "3. Registra trámite o consulta trámites del ciudadano" "HTTP/JSON"
        bffWeb -> citasService "Consulta citas del ciudadano para /bff/inicio" "HTTP/JSON"
        bffWeb -> notificacionesService "Consulta avisos del ciudadano para /bff/inicio" "HTTP/JSON"

        identidadService -> pideReniec "Consulta padrón de identidad mediante Capa Anticorrupción (ACL)" "HTTP/JSON"
        pagosService -> pasarelaPagos "Procesa autorización de cobro electrónico" "HTTPS"
        notificacionesService -> servicioCorreo "Envía correos electrónicos de confirmación y avisos" "SMTP (:1025)"

        citasService -> postgres "Lee y escribe en esquema citas (bloqueo pesimista de cupos)" "JDBC (:5432)"
        tramitesService -> postgres "Lee y escribe en esquema tramites (eventos_tramite, vista_tramite, expediente JSONB, outbox, saga_instancia)" "JDBC (:5432)"
        pagosService -> postgres "Lee y escribe en esquema pagos (pago idempotente y outbox)" "JDBC (:5432)"
        emisionService -> postgres "Lee y escribe en esquema emision (documento_emitido y mensaje_procesado)" "JDBC (:5432)"
        notificacionesService -> postgres "Lee y escribe en esquema notificaciones (aviso y mensaje_procesado)" "JDBC (:5432)"

        citasService -> rabbitmq "Publica evento cita.reservada" "AMQP (:5672)"
        tramitesService -> rabbitmq "Publica tramite.registrado y comando emision.solicitar vía Outbox; consume pago.confirmado, emision.completada y emision.fallida" "AMQP (:5672)"
        pagosService -> rabbitmq "Publica pago.confirmado y pago.anulado vía Outbox" "AMQP (:5672)"
        emisionService -> rabbitmq "Consume emision.solicitar y publica emision.completada o emision.fallida" "AMQP (:5672)"
        notificacionesService -> rabbitmq "Consume cita.reservada, tramite.registrado, pago.confirmado y pago.anulado" "AMQP (:5672)"
        tramitesService -> pagosService "Solicita anulación de pago al compensar la Saga (Asignación A3)" "HTTP/JSON"

        # ----------------------------------------------------------------------
        # 5. Relaciones Internas de Componentes en tramites-service
        # ----------------------------------------------------------------------
        gateway -> tramiteController "Enruta peticiones REST /api/tramites/**" "HTTP/JSON"
        bffWeb -> tramiteController "Invoca registro y consulta de trámites" "HTTP/JSON"
        tramiteController -> registrarTramiteService "Ejecuta registro de nuevo trámite"
        tramiteController -> consultarTramiteService "Consulta estado o historial del trámite"
        tramiteController -> proyectorVistaTramite "Dispara reconstrucción de proyección CQRS"
        tramiteGrpcAdapter -> consultarTramiteService "Consulta trámite vía gRPC (:9082)"

        registrarTramiteService -> eventoStoreAdapter "Persiste eventos de dominio (append-only)"
        registrarTramiteService -> proyectorVistaTramite "Actualiza proyección de lectura síncrona"
        registrarTramiteService -> sagaSolicitudTramite "Inicia instancia de Saga en ESPERANDO_PAGO"
        registrarTramiteService -> outboxPersistenceAdapter "Encola tramite.registrado en tramites.outbox"

        sagaEventosConsumer -> sagaSolicitudTramite "Entrega eventos pago.confirmado, emision.completada y emision.fallida"
        sagaSolicitudTramite -> eventoStoreAdapter "Reconstruye agregado Tramite y anexa nuevos eventos"
        sagaSolicitudTramite -> proyectorVistaTramite "Actualiza vista_tramite en cada transición de la Saga"
        sagaSolicitudTramite -> outboxPersistenceAdapter "Encola comando emision.solicitar en tramites.outbox"
        sagaSolicitudTramite -> anulacionPagoHttpAdapter "Compensa pago ante emision.fallida (Asignación A3)"

        proyectorVistaTramite -> vistaJpaAdapter "Guarda fila desnormalizada en tramites.vista_tramite"
        proyectorVistaTramite -> cacheConsultaAdapter "Invalida entrada cacheada al cambiar estado del trámite"
        consultarTramiteService -> cacheConsultaAdapter "Consulta vista cacheada (HIT/MISS)"
        consultarTramiteService -> vistaJpaAdapter "Lee tramites.vista_tramite en caso de MISS"

        relayOutbox -> outboxPersistenceAdapter "Sondea registros con publicado_en IS NULL cada 500 ms"
        relayOutbox -> rabbitmq "Publica mensajes en exchange consular.eventos con cabecera traceparent" "AMQP"
        eventoStoreAdapter -> postgres "INSERT/SELECT en tramites.eventos_tramite" "JDBC"
        vistaJpaAdapter -> postgres "SELECT/UPSERT en tramites.vista_tramite y tramites.expediente" "JDBC"
        outboxPersistenceAdapter -> postgres "INSERT/UPDATE en tramites.outbox" "JDBC"
        anulacionPagoHttpAdapter -> pagosService "POST /api/pagos/{id}/anulacion" "HTTP/JSON"
    }

    views {
        systemContext plataformaConsular "ContextoSistema" {
            include *
            autoLayout lr
            title "1. Diagrama de Contexto del Sistema (C1) — Plataforma Consular"
        }

        container plataformaConsular "Contenedores" {
            include *
            autoLayout lr
            title "2. Diagrama de Contenedores (C2) — Plataforma Consular"
        }

        component tramitesService "ComponentesTramites" {
            include *
            autoLayout tb
            title "3. Diagrama de Componentes (C3) — tramites-service (Event Sourcing, CQRS, Saga y Outbox)"
        }

        dynamic plataformaConsular "FlujoSagaSolicitudPagoEmision" "Flujo de la Saga Orquestada: Solicitud de Trámite -> Pago Idempotente -> Emisión Documental" {
            ciudadano -> frontend "1. Solicita iniciar trámite consular (DNI, tipo, país, folios)"
            frontend -> gateway "2. POST /bff/tramites"
            gateway -> bffWeb "3. Enruta solicitud a la fachada del BFF"
            bffWeb -> identidadService "4. POST /api/identidad/validaciones (valida DNI en PIDE)"
            identidadService -> pideReniec "5. Consulta DNI en padrón RENIEC y traduce vía ACL"
            bffWeb -> pagosService "6. GET /api/tarifas (calcula arancel con patrón Strategy)"
            bffWeb -> tramitesService "7. POST /api/tramites (persiste evento TramiteRegistrado, vista CQRS y Outbox en PENDIENTE_PAGO)"
            tramitesService -> postgres "8. Transacción ACID local: eventos_tramite + vista_tramite + saga_instancia + outbox"
            ciudadano -> frontend "9. Confirma pago en línea con Idempotency-Key"
            frontend -> gateway "10. POST /api/pagos (Header: Idempotency-Key)"
            gateway -> pagosService "11. Enruta cobro idempotente"
            pagosService -> postgres "12. Persiste pago CONFIRMADO y evento pago.confirmado en pagos.outbox"
            pagosService -> rabbitmq "13. RelayOutbox publica pago.confirmado"
            rabbitmq -> tramitesService "14. SagaEventosConsumer recibe pago.confirmado -> Saga avanza a EN_EMISION y encola emision.solicitar"
            tramitesService -> rabbitmq "15. RelayOutbox publica comando emision.solicitar"
            rabbitmq -> emisionService "16. Consume emision.solicitar, genera documento DOC-YYYY-NNNNNN y publica emision.completada"
            emisionService -> rabbitmq "17. Publica evento emision.completada"
            rabbitmq -> tramitesService "18. SagaEventosConsumer recibe emision.completada -> Saga culmina trámite en estado EMITIDO"
            autoLayout lr
            title "4. Vista Dinámica — Flujo Solicitud -> Pago -> Emisión (Saga Orquestada)"
        }

        styles {
            element "Element" {
                fontSize 22
            }
            element "Persona" {
                shape Person
                background "#1e3a5f"
                color "#ffffff"
            }
            element "Software System" {
                background "#2b547e"
                color "#ffffff"
            }
            element "Externo" {
                background "#6c757d"
                color "#ffffff"
            }
            element "Container" {
                background "#336699"
                color "#ffffff"
            }
            element "WebBrowser" {
                shape WebBrowser
                background "#2a6f97"
                color "#ffffff"
            }
            element "Database" {
                shape Cylinder
                background "#2d6a4f"
                color "#ffffff"
            }
            element "Queue" {
                shape Pipe
                background "#b5651d"
                color "#ffffff"
            }
            element "Component" {
                background "#e9f1f7"
                color "#1b263b"
            }
        }
    }
}

