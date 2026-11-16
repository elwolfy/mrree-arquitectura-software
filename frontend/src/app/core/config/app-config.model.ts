export interface FuncionalidadesConfig {
  citas: boolean;
  tramites: boolean;
  pagos: boolean;
  bffInicio: boolean;
  historial: boolean;
}

export interface AppConfig {
  apiBaseUrl: string;
  modulo: string;
  funcionalidades: FuncionalidadesConfig;
}

export const CONFIG_POR_DEFECTO: AppConfig = {
  apiBaseUrl: 'http://localhost:8081',
  modulo: 'm1',
  funcionalidades: {
    citas: true,
    tramites: false,
    pagos: false,
    bffInicio: false,
    historial: false,
  },
};

