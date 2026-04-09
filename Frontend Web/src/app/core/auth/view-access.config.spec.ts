import { canAccessAction, canAccessRoute, canAccessView } from './view-access.config';

describe('view-access.config', () => {
  it('allows admin to access usuarios view and create action', () => {
    expect(canAccessView('administrador', 'usuarios')).toBeTrue();
    expect(canAccessAction('administrador', 'usuarios', 'create')).toBeTrue();
  });

  it('denies supervisor creating usuarios', () => {
    expect(canAccessAction('supervisor', 'usuarios', 'create')).toBeFalse();
  });

  it('allows supervisor editing clientes and denies auxiliar', () => {
    expect(canAccessAction('supervisor', 'clientes', 'edit')).toBeTrue();
    expect(canAccessAction('auxiliar', 'clientes', 'edit')).toBeFalse();
  });

  it('allows only admin deleting categorias', () => {
    expect(canAccessAction('administrador', 'categorias', 'delete')).toBeTrue();
    expect(canAccessAction('supervisor', 'categorias', 'delete')).toBeFalse();
  });

  it('does not allow deactivate action for categorias', () => {
    expect(canAccessAction('administrador', 'categorias', 'deactivate')).toBeFalse();
  });

  it('allows only admin deleting productos', () => {
    expect(canAccessAction('administrador', 'productos', 'delete')).toBeTrue();
    expect(canAccessAction('supervisor', 'productos', 'delete')).toBeFalse();
  });

  it('allows supervisor completing provisiones and denies auxiliar', () => {
    expect(canAccessAction('supervisor', 'provisiones', 'completeOrden')).toBeTrue();
    expect(canAccessAction('auxiliar', 'provisiones', 'completeOrden')).toBeFalse();
  });

  it('allows report download only for reportes roles', () => {
    expect(canAccessAction('administrador', 'reportes', 'download')).toBeTrue();
    expect(canAccessAction('supervisor', 'reportes', 'download')).toBeTrue();
    expect(canAccessAction('auxiliar', 'reportes', 'download')).toBeFalse();
  });

  it('denies unknown routes by default', () => {
    expect(canAccessRoute('administrador', '/ruta-desconocida')).toBeFalse();
    expect(canAccessRoute('auxiliar', '/ruta-desconocida')).toBeFalse();
  });
});
