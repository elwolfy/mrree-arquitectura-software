# Lenguaje Ubicuo por Contexto Delimitado (DDD Estratégico — Sección 2.4)

En Diseño Guiado por el Dominio (DDD), el **Lenguaje Ubicuo** no pretende imponer un único diccionario global para toda la organización, sino hacer explícitos los términos, invariantes y significados **dentro de cada Contexto Delimitado (*Bounded Context*)**.

---

## 1. Catálogo de Términos del Dominio Consular

| Término | Definición en el negocio consular | Bounded Context | Clase / Artefacto en el código |
| --- | --- | --- | --- |
| **Oficina consular** | Sede diplomática en el exterior habilitada para atender ciudadanos con huso horario propio (`MAD`, `BUE`, `SCL`, `MIA`, `MIL`, `TYO`). | **Citas** | `citas-service/.../domain/OficinaConsular.java` |
| **Cupo** | Franja horaria (`fecha` + `hora`) con capacidad máxima limitada y bloqueo pesimista para evitar sobreasignación. | **Citas** | `citas-service/.../domain/Cupo.java` |
| **Cita** | Reserva confirmada de un cupo por un ciudadano para ser atendido presencialmente en una oficina consular. | **Citas** | `citas-service/.../domain/Cita.java` |
| **Solicitud** | Pedido inicial inmutable del ciudadano que especifica tipo de trámite, país destino, cantidad de folios e idioma documental. | **Trámites** | `tramites-service/.../domain/Solicitud.java` |
| **Trámite** | Proceso consular con ciclo de vida propio (`REGISTRADO` → `PENDIENTE_PAGO` → `PAGADO` → `EN_EMISION` → `EMITIDO`) y comportamiento polimórfico (`Legalizacion`, `Apostilla`, `RegistroConsular`). | **Trámites** | `tramites-service/.../domain/Tramite.java` |
| **Observación** | Estado de un trámite que requiere subsanación o corrección por parte del ciudadano antes de continuar su procesamiento (Asignación A2). | **Trámites** | `EstadoTramite.OBSERVADO` |
| **Tarifa** | Arancel consular calculado mediante una estrategia según el tipo de trámite y el país destino (con recargo exterior fuera de Sudamérica). | **Pagos / Tarifas** | `pagos-service/.../domain/tarifa/Tarifa.java` |
| **Orden de pago** | Obligación financiera generada para cobrar el arancel de un trámite, identificada por monto, moneda, estado (`PENDIENTE`, `CONFIRMADO`, `ANULADO`) y clave de idempotencia. | **Pagos** | `pagos-service/.../domain/OrdenDePago.java` |
| **Ciudadano** | Titular peruano identificado por DNI de 8 dígitos cuya identidad y vigencia han sido contrastadas y traducidas desde el padrón externo. | **Identidad** | `identidad-service/.../domain/Ciudadano.java` |
| **Aviso** | Notificación electrónica enviada al ciudadano (por correo SMTP) como reacción asíncrona a un hecho ocurrido en otro contexto. | **Notificaciones** | `notificaciones-service/.../domain/Aviso.java` |

---

## 2. Contraste Polisémico: `Solicitud` (Trámites) vs. `OrdenDePago` (Pagos)

Un error común al diseñar plataformas empresariales es crear una clase `Tramite` o `Solicitud` compartida en una librería común (`common-domain.jar`) que mezcle campos documentales y campos contables.

En nuestra arquitectura cada contexto modela únicamente lo que le concierne:

| Aspecto | En el contexto **Trámites** (`Solicitud` / `Tramite`) | En el contexto **Pagos** (`OrdenDePago`) |
| --- | --- | --- |
| **Pregunta central** | ¿El documento cumple las reglas consulares de folios, idioma y Convenio de La Haya? | ¿Cuánto debe cobrarse, en qué moneda y cómo evitamos un doble cobro? |
| **Atributos relevantes** | `dni`, `tipo`, `pais`, `folios`, `idioma`, `modalidadProcesamiento`, `estado` | `id`, `tramiteId`, `monto`, `moneda`, `estado`, `claveIdempotencia` |
| **Lo que ignora deliberadamente** | Medios de pago, pasarelas bancarias, claves de idempotencia de cobro o asientos contables. | Cantidad de folios, idioma del acta, nombre del funcionario consular o cadena de firmas. |
| **Vínculo entre ambos** | Referencia por identificador (`UUID tramiteId`) y eventos publicados (`pago.confirmado` / `pago.anulado` en M3). | Referencia únicamente `UUID tramiteId` sin acoplarse a la entidad `Tramite`. |

