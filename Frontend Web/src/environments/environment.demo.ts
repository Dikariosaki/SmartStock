// ⚠️ ARCHIVO DE DEMOSTRACIÓN - NO USAR EN PRODUCCIÓN
// Este archivo muestra la estructura completa del sistema de configuración
// pero con valores de ejemplo/placeholder en lugar de credenciales reales

// Declaración para acceder a variables de entorno en el navegador
declare const process: {
  env: { [key: string]: string | undefined };
};

// Helper function para obtener variables de entorno con fallbacks
const getEnvVar = (key: string, defaultValue: string = ''): string => {
  if (typeof process !== 'undefined' && process.env) {
    return process.env[key] || defaultValue;
  }
  return defaultValue;
};

// 🔧 CONFIGURACIÓN PRINCIPAL DEL SISTEMA
export const config = {
  // 🌍 Configuración del entorno
  environment: {
    production: false,
    development: true,
    name: 'demo',
    version: '1.0.0-demo',
    buildDate: new Date().toISOString(),
  },

  // 🌐 Configuración de API Backend (.NET Core)
  api: {
    baseUrl: getEnvVar('API_BASE_URL', 'http://localhost:5000'),
    timeout: parseInt(getEnvVar('API_TIMEOUT', '30000')),
    retries: parseInt(getEnvVar('API_RETRIES', '3')),

    // Endpoints principales
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

    // Headers por defecto
    defaultHeaders: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
  },

  // 🔐 Configuración de seguridad (VALORES DE EJEMPLO)
  security: {
    jwtSecret: getEnvVar(
      'JWT_SECRET',
      'demo-jwt-secret-key-not-for-production'
    ),
    encryptionKey: getEnvVar(
      'ENCRYPTION_KEY',
      'demo-encryption-key-32-chars-long'
    ),

    // Configuración de sesiones
    session: {
      maxAge: parseInt(getEnvVar('SESSION_MAX_AGE', '86400')), // 24 horas
      secure: getEnvVar('SESSION_SECURE', 'false') === 'true',
      sameSite: 'lax' as const,
    },

    // Configuración de CORS
    cors: {
      origin: getEnvVar('CORS_ORIGIN', 'http://localhost:4200'),
      credentials: true,
    },

    // Rate limiting
    rateLimit: {
      windowMs: parseInt(getEnvVar('RATE_LIMIT_WINDOW', '900000')), // 15 minutos
      max: parseInt(getEnvVar('RATE_LIMIT_MAX', '100')),
    },
  },

  // 📊 Configuración de logging
  logging: {
    level: getEnvVar('LOG_LEVEL', 'debug'),
    enableConsole: getEnvVar('LOG_CONSOLE', 'true') === 'true',
    enableFile: getEnvVar('LOG_FILE', 'false') === 'true',

    // Configuración de archivos de log
    file: {
      path: getEnvVar('LOG_FILE_PATH', './logs'),
      maxSize: getEnvVar('LOG_MAX_SIZE', '10MB'),
      maxFiles: parseInt(getEnvVar('LOG_MAX_FILES', '5')),
    },

    // Configuración de servicios externos
    external: {
      enabled: getEnvVar('LOG_EXTERNAL', 'false') === 'true',
      service: getEnvVar('LOG_SERVICE', 'none'), // 'sentry', 'datadog', etc.
      apiKey: getEnvVar('LOG_API_KEY', 'demo-log-api-key'),
    },
  },

  // 🎛️ Feature Flags (Configuración de características)
  features: {
    enableRegistration: getEnvVar('FEATURE_REGISTRATION', 'true') === 'true',
    enableSocialLogin: getEnvVar('FEATURE_SOCIAL_LOGIN', 'false') === 'true',
    enableNotifications: getEnvVar('FEATURE_NOTIFICATIONS', 'true') === 'true',
    enableAnalytics: getEnvVar('FEATURE_ANALYTICS', 'false') === 'true',
    enableDarkMode: getEnvVar('FEATURE_DARK_MODE', 'true') === 'true',
    enableOfflineMode: getEnvVar('FEATURE_OFFLINE', 'false') === 'true',

    // Features experimentales
    experimental: {
      newDashboard: getEnvVar('FEATURE_NEW_DASHBOARD', 'false') === 'true',
      aiAssistant: getEnvVar('FEATURE_AI_ASSISTANT', 'false') === 'true',
      realTimeSync: getEnvVar('FEATURE_REALTIME_SYNC', 'false') === 'true',
    },
  },

  // 📱 Configuración de la aplicación
  app: {
    name: getEnvVar('APP_NAME', 'Mi Aplicación Demo'),
    description: getEnvVar('APP_DESCRIPTION', 'Aplicación de demostración'),
    version: getEnvVar('APP_VERSION', '1.0.0'),

    // Configuración de UI
    ui: {
      theme: getEnvVar('UI_THEME', 'light'),
      language: getEnvVar('UI_LANGUAGE', 'es'),
      timezone: getEnvVar('UI_TIMEZONE', 'America/Bogota'),
      dateFormat: getEnvVar('UI_DATE_FORMAT', 'DD/MM/YYYY'),
      currency: getEnvVar('UI_CURRENCY', 'COP'),
    },

    // Configuración de cache
    cache: {
      enabled: getEnvVar('CACHE_ENABLED', 'true') === 'true',
      ttl: parseInt(getEnvVar('CACHE_TTL', '3600')), // 1 hora
      maxSize: parseInt(getEnvVar('CACHE_MAX_SIZE', '100')),
    },
  },

  // 🔧 Configuración de desarrollo
  development: {
    enableDebugMode: getEnvVar('DEBUG_MODE', 'true') === 'true',
    enableMockData: getEnvVar('MOCK_DATA', 'false') === 'true',
    enableHotReload: getEnvVar('HOT_RELOAD', 'true') === 'true',

    // Configuración de testing
    testing: {
      enableE2E: getEnvVar('ENABLE_E2E', 'false') === 'true',
      testTimeout: parseInt(getEnvVar('TEST_TIMEOUT', '30000')),
      mockApiDelay: parseInt(getEnvVar('MOCK_API_DELAY', '500')),
    },
  },

  // 📈 Configuración de monitoreo
  monitoring: {
    enabled: getEnvVar('MONITORING_ENABLED', 'false') === 'true',

    // Configuración de métricas
    metrics: {
      endpoint: getEnvVar('METRICS_ENDPOINT', 'http://localhost:9090'),
      interval: parseInt(getEnvVar('METRICS_INTERVAL', '60000')), // 1 minuto
      enabled: getEnvVar('METRICS_ENABLED', 'false') === 'true',
    },

    // Configuración de health checks
    health: {
      endpoint: '/health',
      interval: parseInt(getEnvVar('HEALTH_CHECK_INTERVAL', '30000')), // 30 segundos
      timeout: parseInt(getEnvVar('HEALTH_CHECK_TIMEOUT', '5000')),
    },
  },
};

// 🏭 Configuración específica para producción (ejemplo)
export const productionConfig = {
  ...config,
  environment: {
    ...config.environment,
    production: true,
    development: false,
    name: 'production',
  },
  logging: {
    ...config.logging,
    level: 'error',
    enableConsole: false,
    enableFile: true,
  },
  security: {
    ...config.security,
    session: {
      ...config.security.session,
      secure: true,
    },
  },
};

// 🧪 Configuración específica para testing (ejemplo)
export const testingConfig = {
  ...config,
  environment: {
    ...config.environment,
    name: 'testing',
  },
  development: {
    ...config.development,
    enableMockData: true,
  },
};

// 📋 Validación de configuración
export const validateConfig = () => {
  const requiredVars = ['JWT_SECRET'];

  const missing = requiredVars.filter(varName => !getEnvVar(varName));

  if (missing.length > 0) {
    console.warn('⚠️ Variables de entorno faltantes:', missing);
    console.warn(
      '📝 Revisa el archivo .env.example para ver todas las variables necesarias'
    );
  }

  return missing.length === 0;
};

// 🔍 Función de debug para desarrollo
export const debugConfig = () => {
  if (config.development.enableDebugMode) {
    console.group('🔧 Configuración del Sistema');
    console.log('Environment:', config.environment.name);
    console.log('Version:', config.environment.version);
    console.log(
      'Features habilitadas:',
      Object.entries(config.features).filter(([, enabled]) => enabled)
    );
    console.groupEnd();
  }
};

// Exportar configuración por defecto
export default config;

// 📚 NOTAS DE USO:
// 1. Este es un archivo de DEMOSTRACIÓN - no contiene credenciales reales
// 2. Para usar en desarrollo, copia este archivo como environment.ts
// 3. Reemplaza todos los valores de ejemplo con tus credenciales reales
// 4. Nunca subas credenciales reales al repositorio
// 5. Usa variables de entorno (.env) para valores sensibles
