// Environment base para desarrollo local
// Este archivo debe versionarse para que el frontend web compile despues de clonar.
declare const process: {
  env: { [key: string]: string | undefined };
};

const getEnvVar = (key: string, defaultValue: string = ''): string => {
  if (typeof process !== 'undefined' && process.env) {
    return process.env[key] || defaultValue;
  }
  return defaultValue;
};

export const config = {
  environment: {
    production: false,
    development: true,
    name: 'development',
    version: '1.0.0-dev',
    buildDate: new Date().toISOString(),
  },

  api: {
    // Vacio para usar proxy local o el mismo host en despliegue
    baseUrl: getEnvVar('API_BASE_URL', ''),
    timeout: 30000,
    retries: 3,

    endpoints: {
      auth: '/api/auth',
      usuarios: '/api/usuarios',
      clientes: '/api/clientes',
      proveedores: '/api/proveedores',
      subcategorias: '/api/subcategorias',
      categorias: '/api/categorias',
      movimientos: '/api/movimientos',
      reportes: '/api/reportes',
      roles: '/api/roles',
    },

    defaultHeaders: {
      'Content-Type': 'application/json',
    },
  },

  security: {
    jwtSecret: new TextEncoder().encode('CHANGE_THIS_TO_A_LONG_RANDOM_SECRET_KEY'),
  },
};
