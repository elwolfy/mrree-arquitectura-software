import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate } from 'k6/metrics';

// Un 409 (cita duplicada o cupo agotado por concurrencia) es una respuesta esperada de negocio
http.setResponseCallback(http.expectedStatuses(200, 201, 204, 409));

export const tasaErrorReal = new Rate('tasa_error_real');
export const reservasCreadas = new Counter('reservas_creadas_201');
export const conflictosEsperados = new Counter('conflictos_esperados_409');
export const consultasExitosas = new Counter('consultas_exitosas_200');

export const options = {
  vus: 50,
  duration: '60s',
  thresholds: {
    http_req_duration: [{ threshold: 'p(95)<2000', abortOnFail: false }],
    tasa_error_real: [{ threshold: 'rate<0.05', abortOnFail: false }],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
};

const OBJETIVO = (__ENV.OBJETIVO || 'http://host.docker.internal:8081').replace(/\/+$/, '');
const OFICINAS = ['MAD', 'BUE', 'SCL', 'MIA', 'MIL', 'TYO'];
const TIPOS_TRAMITE = ['LEGALIZACION', 'APOSTILLA'];

function calcularFechasHabiles(cantidad = 20) {
  const fechas = [];
  const cursor = new Date();
  cursor.setUTCDate(cursor.getUTCDate() + 2);

  while (fechas.length < cantidad) {
    const diaSemana = cursor.getUTCDay();
    if (diaSemana !== 0 && diaSemana !== 6) {
      const yyyy = cursor.getUTCFullYear();
      const mm = String(cursor.getUTCMonth() + 1).padStart(2, '0');
      const dd = String(cursor.getUTCDate()).padStart(2, '0');
      fechas.push(`${yyyy}-${mm}-${dd}`);
    }
    cursor.setUTCDate(cursor.getUTCDate() + 1);
  }
  return fechas;
}

const FECHAS_HABILES = calcularFechasHabiles(20);

function elegirAleatorio(arreglo) {
  return arreglo[Math.floor(Math.random() * arreglo.length)];
}

function dniAleatorio() {
  const numero = Math.floor(Math.random() * 50) + 1;
  return String(40000000 + numero);
}

export default function () {
  const oficinaId = elegirAleatorio(OFICINAS);
  const fecha = elegirAleatorio(FECHAS_HABILES);

  // 80 % consultas (oficinas y disponibilidad) y 20 % reservas
  if (Math.random() < 0.8) {
    const resOficinas = http.get(`${OBJETIVO}/api/oficinas`, {
      tags: { operacion: 'listar_oficinas' },
    });
    const okOficinas = check(resOficinas, {
      'GET /api/oficinas responde 200': (r) => r.status === 200,
    });
    tasaErrorReal.add(!okOficinas);
    if (okOficinas) {
      consultasExitosas.add(1);
    }

    const resDisp = http.get(
      `${OBJETIVO}/api/oficinas/${oficinaId}/disponibilidad?fecha=${fecha}`,
      { tags: { operacion: 'consultar_disponibilidad' } }
    );
    const okDisp = check(resDisp, {
      'GET /disponibilidad responde 200': (r) => r.status === 200,
    });
    tasaErrorReal.add(!okDisp);
    if (okDisp) {
      consultasExitosas.add(1);
    }
  } else {
    // 20 % reservas: primero obtener un cupo libre de la disponibilidad
    const resDisp = http.get(
      `${OBJETIVO}/api/oficinas/${oficinaId}/disponibilidad?fecha=${fecha}`,
      { tags: { operacion: 'consultar_disponibilidad_previa' } }
    );

    if (resDisp.status !== 200) {
      tasaErrorReal.add(true);
      sleep(0.2);
      return;
    }
    tasaErrorReal.add(false);
    consultasExitosas.add(1);

    let cupos = [];
    try {
      const cuerpo = resDisp.json();
      cupos = Array.isArray(cuerpo?.cupos) ? cuerpo.cupos : [];
    } catch {
      cupos = [];
    }

    const cuposLibres = cupos.filter((c) => {
      const libres =
        typeof c.libres === 'number'
          ? c.libres
          : typeof c.disponibles === 'number'
            ? c.disponibles
            : (c.capacidad || 0) - (c.ocupados || 0);
      return libres > 0;
    });

    if (cuposLibres.length === 0) {
      // Cupo lleno en esa fecha/oficina: condición normal bajo concurrencia
      conflictosEsperados.add(1);
      tasaErrorReal.add(false);
      sleep(0.2);
      return;
    }

    const cupoElegido = elegirAleatorio(cuposLibres);
    const horaCorta = String(cupoElegido.hora).substring(0, 5);
    const dni = dniAleatorio();
    const tipoTramite = elegirAleatorio(TIPOS_TRAMITE);

    const payload = JSON.stringify({
      dni: dni,
      nombres: `Ciudadano Prueba ${dni.substring(5)}`,
      email: `ciudadano.${dni}@correo.lab`,
      oficinaId: oficinaId,
      fecha: fecha,
      hora: horaCorta,
      tipoTramite: tipoTramite,
    });

    const resReserva = http.post(`${OBJETIVO}/api/citas`, payload, {
      headers: { 'Content-Type': 'application/json' },
      tags: { operacion: 'reservar_cita' },
    });

    const esRespuestaEsperada = resReserva.status === 201 || resReserva.status === 409;
    check(resReserva, {
      'POST /api/citas responde 201 (creada) o 409 (conflicto esperado)': () => esRespuestaEsperada,
    });

    tasaErrorReal.add(!esRespuestaEsperada);
    if (resReserva.status === 201) {
      reservasCreadas.add(1);
    } else if (resReserva.status === 409) {
      conflictosEsperados.add(1);
    }
  }

  sleep(0.2);
}

export function handleSummary(data) {
  const p95 = data.metrics.http_req_duration?.values?.['p(95)'] ?? 0;
  const promedio = data.metrics.http_req_duration?.values?.avg ?? 0;
  const reqPorSeg = data.metrics.http_reqs?.values?.rate ?? 0;
  const totalReqs = data.metrics.http_reqs?.values?.count ?? 0;
  const errRate = (data.metrics.tasa_error_real?.values?.rate ?? 0) * 100;
  const creadas = data.metrics.reservas_creadas_201?.values?.count ?? 0;
  const conflictos = data.metrics.conflictos_esperados_409?.values?.count ?? 0;

  const estadoP95 = p95 < 2000 ? 'CUMPLE (< 2000 ms)' : 'SUPERA UMBRAL INFORMATIVO (>= 2000 ms)';
  const estadoErr = errRate < 5 ? 'CUMPLE (< 5 %)' : 'SUPERA UMBRAL INFORMATIVO (>= 5 %)';

  const resumen = [
    '',
    '====================================================================',
    ' RESUMEN DE PRUEBA DE CARGA k6 - M1 (Escenario: citas.js - 50 VUs)',
    '====================================================================',
    `  * Objetivo evaluado           : ${OBJETIVO}`,
    `  * Latencia p95                : ${p95.toFixed(2)} ms  [${estadoP95}]`,
    `  * Latencia promedio           : ${promedio.toFixed(2)} ms`,
    `  * Solicitudes por segundo     : ${reqPorSeg.toFixed(2)} req/s`,
    `  * Tasa de error real          : ${errRate.toFixed(2)} %   [${estadoErr}]`,
    '--------------------------------------------------------------------',
    `  * Total solicitudes HTTP      : ${totalReqs}`,
    `  * Reservas confirmadas (201)  : ${creadas}`,
    `  * Conflictos esperados (409)  : ${conflictos} (duplicados/cupo lleno)`,
    '====================================================================',
    '',
  ].join('\n');

  return {
    stdout: resumen,
  };
}

