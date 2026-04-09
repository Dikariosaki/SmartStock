export const ROLE_ID_TO_NAME = {
  1: 'administrador',
  2: 'supervisor',
  3: 'auxiliar',
  4: 'proveedor',
  5: 'cliente',
} as const;

export const ROLE_ALIASES = {
  admin: 'administrador',
  manager: 'supervisor',
  employee: 'auxiliar',
  viewer: 'proveedor',
  supplier: 'proveedor',
  customer: 'cliente',
} as const;

export type AppRole = (typeof ROLE_ID_TO_NAME)[keyof typeof ROLE_ID_TO_NAME];

export const VIEW_ACCESS = {
  usuarios: ['administrador'],
  clientes: ['administrador', 'supervisor', 'auxiliar'],
  productos: ['administrador', 'supervisor', 'auxiliar'],
  'menu-reportes': ['administrador', 'supervisor'],
  reportes: ['administrador', 'supervisor'],
  proveedores: ['administrador', 'supervisor'],
  inventarios: ['administrador', 'supervisor'],
  categorias: ['administrador', 'supervisor', 'auxiliar'],
  subcategorias: ['administrador', 'supervisor', 'auxiliar'],
  tareas: ['administrador', 'supervisor', 'auxiliar'],
  provisiones: ['administrador', 'supervisor'],
} as const;

export type ViewKey = keyof typeof VIEW_ACCESS;
export type ModuleKey = ViewKey;

export const ACTION_KEYS = [
  'create',
  'edit',
  'delete',
  'activate',
  'deactivate',
  'stockEntrada',
  'stockSalida',
  'reportAveria',
  'completeOrden',
  'completeItem',
  'manageReorden',
  'download',
] as const;

export type ActionKey = (typeof ACTION_KEYS)[number];
export type ModuleAction = `${ModuleKey}:${ActionKey}`;

type ActionAccessMap = {
  [K in ModuleKey]: Partial<Record<ActionKey, readonly AppRole[]>>;
};

export const ACTION_ACCESS: ActionAccessMap = {
  usuarios: {
    create: ['administrador'],
    edit: ['administrador', 'supervisor', 'auxiliar'],
    delete: ['administrador'],
    activate: ['administrador'],
    deactivate: ['administrador'],
  },
  clientes: {
    create: ['administrador'],
    edit: ['administrador', 'supervisor'],
    delete: ['administrador'],
    activate: ['administrador'],
    deactivate: ['administrador'],
  },
  productos: {
    create: ['administrador', 'supervisor'],
    edit: ['administrador', 'supervisor'],
    delete: ['administrador'],
    activate: ['administrador', 'supervisor'],
    deactivate: ['administrador', 'supervisor'],
    stockEntrada: ['administrador', 'supervisor'],
    stockSalida: ['administrador', 'supervisor'],
    reportAveria: ['administrador', 'supervisor'],
  },
  'menu-reportes': {},
  reportes: {
    download: ['administrador', 'supervisor'],
  },
  proveedores: {
    create: ['administrador'],
    edit: ['administrador', 'supervisor'],
    delete: ['administrador'],
    deactivate: ['administrador'],
  },
  inventarios: {
    create: ['administrador'],
    edit: ['administrador', 'supervisor'],
    delete: ['administrador'],
    activate: ['administrador', 'supervisor'],
    deactivate: ['administrador', 'supervisor'],
  },
  categorias: {
    create: ['administrador', 'supervisor'],
    edit: ['administrador', 'supervisor'],
    delete: ['administrador'],
  },
  subcategorias: {
    create: ['administrador', 'supervisor'],
    edit: ['administrador', 'supervisor'],
    delete: ['administrador'],
    activate: ['administrador', 'supervisor'],
    deactivate: ['administrador', 'supervisor'],
  },
  tareas: {
    create: ['administrador', 'supervisor'],
    edit: ['administrador', 'supervisor'],
    delete: ['administrador'],
    activate: ['administrador', 'supervisor'],
    deactivate: ['administrador', 'supervisor'],
  },
  provisiones: {
    completeOrden: ['administrador', 'supervisor'],
    completeItem: ['administrador', 'supervisor'],
    manageReorden: ['administrador', 'supervisor'],
  },
};

const ROUTE_TO_VIEW_KEY: Record<string, ViewKey> = {
  '/usuarios': 'usuarios',
  '/clientes': 'clientes',
  '/productos': 'productos',
  '/menu-reportes': 'menu-reportes',
  '/reportes': 'reportes',
  '/proveedores': 'proveedores',
  '/inventarios': 'inventarios',
  '/categorias': 'categorias',
  '/subcategorias': 'subcategorias',
  '/tareas': 'tareas',
  '/provisiones': 'provisiones',
};

export function getRoleFromId(roleId: number): AppRole {
  return ROLE_ID_TO_NAME[roleId as keyof typeof ROLE_ID_TO_NAME] ?? 'auxiliar';
}

export function normalizeRole(role?: string | null): AppRole | null {
  if (!role) {
    return null;
  }

  const normalizedRole = role.trim().toLowerCase();
  const aliasRole =
    ROLE_ALIASES[normalizedRole as keyof typeof ROLE_ALIASES] ?? normalizedRole;

  if ((Object.values(ROLE_ID_TO_NAME) as string[]).includes(aliasRole)) {
    return aliasRole as AppRole;
  }

  return null;
}

export function hasAnyRole(
  role: string | null | undefined,
  allowedRoles: readonly AppRole[]
): boolean {
  const normalizedRole = normalizeRole(role);
  return normalizedRole ? allowedRoles.includes(normalizedRole) : false;
}

export function canAccessView(
  role: string | null | undefined,
  viewKey: ViewKey
): boolean {
  return hasAnyRole(role, VIEW_ACCESS[viewKey]);
}

export function canAccessRoute(
  role: string | null | undefined,
  route: string
): boolean {
  const viewKey = ROUTE_TO_VIEW_KEY[route];
  return viewKey ? canAccessView(role, viewKey) : false;
}

export function canAccessAction(
  role: string | null | undefined,
  moduleKey: ModuleKey,
  action: ActionKey
): boolean {
  const allowedRoles = ACTION_ACCESS[moduleKey][action];
  if (!allowedRoles?.length) {
    return false;
  }

  return hasAnyRole(role, allowedRoles);
}
