// Configuración JWT para SmartStock
export const JWT_CONFIG = {
  // Claves para localStorage
  TOKEN_KEY: 'auth_token',
  USER_KEY: 'current_user',

  // Duración del token (en segundos)
  TOKEN_DURATION: 24 * 60 * 60, // 24 horas

  // Credenciales por defecto (solo para desarrollo)
  DEFAULT_CREDENTIALS: {
    email: 'admin@smartstock.com',
    password: '123456',
  },

  // Configuración del token
  TOKEN_CONFIG: {
    algorithm: 'HS256',
    type: 'JWT',
  },
};
