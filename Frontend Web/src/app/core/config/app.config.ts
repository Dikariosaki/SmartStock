export const AppConfig = {
  api: {
    baseUrl: 'http://localhost:3000/api',
    timeout: 30000,
    retryAttempts: 3,
  },
  auth: {
    tokenKey: 'smartstock_token',
    userKey: 'currentUser',
    sessionTimeout: 3600000, // 1 hour in milliseconds
  },
  app: {
    name: 'SmartStock',
    version: '1.0.0',
    description: 'Sistema de gestión de inventario inteligente',
  },
  pagination: {
    defaultPageSize: 10,
    pageSizeOptions: [5, 10, 25, 50, 100],
  },
  dateFormat: {
    display: 'dd/MM/yyyy',
    api: 'yyyy-MM-dd',
  },
};

export type AppConfigType = typeof AppConfig;
