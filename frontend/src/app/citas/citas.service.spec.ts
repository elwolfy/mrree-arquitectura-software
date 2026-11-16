import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ConfigService } from '../core/config/config.service';
import { CitasService } from './citas.service';

describe('CitasService', () => {
  let service: CitasService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), ConfigService, CitasService],
    });
    service = TestBed.inject(CitasService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('debe listar las oficinas consulares desde /api/oficinas', () => {
    service.listarOficinas().subscribe((oficinas) => {
      expect(oficinas.length).toBe(1);
      expect(oficinas[0].id).toBe('MAD');
    });

    const req = httpMock.expectOne('http://localhost:8081/api/oficinas');
    expect(req.request.method).toBe('GET');
    req.flush([
      {
        id: 'MAD',
        nombre: 'Consulado en Madrid',
        pais: 'ES',
        ciudad: 'Madrid',
        zonaHoraria: 'Europe/Madrid',
      },
    ]);
  });

  it('debe consultar la disponibilidad de una oficina y fecha', () => {
    service.consultarDisponibilidad('MAD', '2026-11-30').subscribe((disp) => {
      expect(disp.oficinaId).toBe('MAD');
      expect(disp.origen).toBe('BASE_DE_DATOS');
      expect(disp.cupos.length).toBe(1);
    });

    const req = httpMock.expectOne(
      'http://localhost:8081/api/oficinas/MAD/disponibilidad?fecha=2026-11-30'
    );
    expect(req.request.method).toBe('GET');
    req.flush({
      oficinaId: 'MAD',
      fecha: '2026-11-30',
      origen: 'BASE_DE_DATOS',
      actualizadoHaceSegundos: 0,
      cupos: [{ hora: '08:00', capacidad: 2, ocupados: 1, libres: 1 }],
    });
  });

  it('debe reservar una cita mediante POST /api/citas', () => {
    service
      .reservarCita({
        dni: '40000001',
        nombres: 'Ana Pérez',
        email: 'ana@correo.lab',
        oficinaId: 'MAD',
        fecha: '2026-11-30',
        hora: '08:00',
        tipoTramite: 'LEGALIZACION',
      })
      .subscribe((cita) => {
        expect(cita.codigo).toBe('CIT-2026-000001');
        expect(cita.notificacion).toBe('ENVIADA');
      });

    const req = httpMock.expectOne('http://localhost:8081/api/citas');
    expect(req.request.method).toBe('POST');
    req.flush({
      id: '6f1c7d24-3f11-4f9e-bc43-2287da23a2a1',
      codigo: 'CIT-2026-000001',
      dni: '40000001',
      nombres: 'Ana Pérez',
      oficinaId: 'MAD',
      fecha: '2026-11-30',
      hora: '08:00',
      tipoTramite: 'LEGALIZACION',
      estado: 'RESERVADA',
      notificacion: 'ENVIADA',
      creadaEn: '2026-11-16T10:00:00Z',
    });
  });
});

