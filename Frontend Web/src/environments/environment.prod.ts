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
    production: true,
    development: false,
    name: 'production',
    version: '1.0.0',
    buildDate: new Date().toISOString(),
  },

  api: {
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
    jwtSecret: new TextEncoder().encode(
      'CHANGE_THIS_TO_A_LONG_RANDOM_SECRET_KEY'
    ),
  },
};
