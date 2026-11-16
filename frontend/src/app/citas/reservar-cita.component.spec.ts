import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { CitasService } from './citas.service';
import { ReservarCitaComponent } from './reservar-cita.component';

describe('ReservarCitaComponent', () => {
  let component: ReservarCitaComponent;
  let fixture: ComponentFixture<ReservarCitaComponent>;
  let citasServiceSpy: jasmine.SpyObj<CitasService>;

  beforeEach(async () => {
    citasServiceSpy = jasmine.createSpyObj<CitasService>('CitasService', [
      'listarOficinas',
      'consultarDisponibilidad',
      'reservarCita',
      'calcularTarifa',
    ]);

    citasServiceSpy.listarOficinas.and.returnValue(
      of([
        {
          id: 'MAD',
          nombre: 'Consulado General del Perú en Madrid (laboratorio)',
          pais: 'ES',
          ciudad: 'Madrid',
          zonaHoraria: 'Europe/Madrid',
        },
      ])
    );

    citasServiceSpy.consultarDisponibilidad.and.returnValue(
      of({
        oficinaId: 'MAD',
        fecha: '2026-11-30',
        origen: 'CACHE',
        actualizadoHaceSegundos: 7,
        cupos: [{ hora: '08:00', capacidad: 2, ocupados: 1, libres: 1 }],
      })
    );

    citasServiceSpy.calcularTarifa.and.returnValue(
      of({
        tipoTramite: 'LEGALIZACION',
        pais: 'ES',
        moneda: 'PEN',
        monto: 22.0,
        estrategia: 'LegalizacionTarifaStrategy',
      })
    );

    citasServiceSpy.reservarCita.and.returnValue(
      of({
        id: '6f1c7d24-3f11-4f9e-bc43-2287da23a2a1',
        codigo: 'CIT-2026-000001',
        dni: '40000001',
        nombres: 'Ana Pérez',
        oficinaId: 'MAD',
        fecha: '2026-11-30',
        hora: '08:00',
        tipoTramite: 'LEGALIZACION',
        estado: 'RESERVADA',
        notificacion: 'PENDIENTE',
        creadaEn: '2026-11-16T10:00:00Z',
      })
    );

    await TestBed.configureTestingModule({
      imports: [ReservarCitaComponent],
      providers: [
        provideRouter([]),
        provideNoopAnimations(),
        { provide: CitasService, useValue: citasServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ReservarCitaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debe cargar las oficinas al iniciar y permitir avanzar paso a paso hasta confirmar la cita', () => {
    expect(component.oficinas().length).toBe(1);

    component.seleccionarOficina(component.oficinas()[0]);
    component.irAPasoFecha();
    expect(component.pasoActual()).toBe(1);

    component.cargarDisponibilidad();
    expect(component.pasoActual()).toBe(2);
    expect(component.etiquetaOrigen()).toBe('Caché – actualizado hace 7 s');

    component.seleccionarCupo(component.disponibilidad()!.cupos[0]);
    component.irAPasoDatos();
    expect(component.pasoActual()).toBe(3);

    component.confirmarReserva();
    expect(component.pasoActual()).toBe(4);
    expect(component.citaConfirmada()?.codigo).toBe('CIT-2026-000001');
    expect(component.citaConfirmada()?.notificacion).toBe('PENDIENTE');
  });
});

