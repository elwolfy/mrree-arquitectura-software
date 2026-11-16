import { TestBed } from '@angular/core/testing';
import { CONFIG_POR_DEFECTO } from './app-config.model';
import { ConfigService } from './config.service';

describe('ConfigService', () => {
  let service: ConfigService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ConfigService);
  });

  it('debe iniciar con la configuración por defecto (MODULO=m1)', () => {
    expect(service.modulo()).toBe('m1');
    expect(service.apiBaseUrl()).toBe('http://localhost:8081');
    expect(service.funcionalidades().citas).toBeTrue();
    expect(service.funcionalidades().tramites).toBeFalse();
  });

  it('debe cargar y combinar la configuración desde fetch', async () => {
    spyOn(window, 'fetch').and.resolveTo(
      new Response(
        JSON.stringify({
          apiBaseUrl: 'http://localhost:8080/',
          modulo: 'm2',
          funcionalidades: {
            citas: true,
            tramites: true,
            pagos: false,
            bffInicio: true,
            historial: false,
          },
        }),
        { status: 200 }
      )
    );

    const cargada = await service.cargarConfiguracion('/assets/config.json');

    expect(cargada.modulo).toBe('m2');
    expect(service.apiBaseUrl()).toBe('http://localhost:8080');
    expect(service.funcionalidades().tramites).toBeTrue();
    expect(service.funcionalidades().bffInicio).toBeTrue();
  });

  it('debe conservar la configuración por defecto si fetch falla', async () => {
    spyOn(window, 'fetch').and.rejectWith(new Error('Red caída'));

    const cargada = await service.cargarConfiguracion('/assets/config.json');

    expect(cargada).toEqual(CONFIG_POR_DEFECTO);
  });
});

