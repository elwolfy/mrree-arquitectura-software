import { Injectable, computed, signal } from '@angular/core';
import { AppConfig, CONFIG_POR_DEFECTO, FuncionalidadesConfig } from './app-config.model';

@Injectable({ providedIn: 'root' })
export class ConfigService {
  private readonly estadoConfig = signal<AppConfig>(CONFIG_POR_DEFECTO);

  readonly config = this.estadoConfig.asReadonly();
  readonly apiBaseUrl = computed(() => this.estadoConfig().apiBaseUrl.replace(/\/+$/, ''));
  readonly modulo = computed(() => this.estadoConfig().modulo);
  readonly funcionalidades = computed<FuncionalidadesConfig>(() => this.estadoConfig().funcionalidades);

  async cargarConfiguracion(url = '/assets/config.json'): Promise<AppConfig> {
    try {
      const respuesta = await fetch(url, { cache: 'no-store' });
      if (!respuesta.ok) {
        return this.estadoConfig();
      }
      const datos = (await respuesta.json()) as Partial<AppConfig>;
      const configCombinada: AppConfig = {
        apiBaseUrl: datos.apiBaseUrl ?? CONFIG_POR_DEFECTO.apiBaseUrl,
        modulo: datos.modulo ?? CONFIG_POR_DEFECTO.modulo,
        funcionalidades: {
          ...CONFIG_POR_DEFECTO.funcionalidades,
          ...(datos.funcionalidades ?? {}),
        },
      };
      this.estadoConfig.set(configCombinada);
      return configCombinada;
    } catch {
      return this.estadoConfig();
    }
  }

  establecerConfiguracion(config: AppConfig): void {
    this.estadoConfig.set(config);
  }
}

