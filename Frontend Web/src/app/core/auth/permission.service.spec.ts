import { TestBed } from '@angular/core/testing';
import { PermissionService } from './permission.service';
import { AuthService } from '@core/services/auth.service';
import { ToastService } from '@shared/services/toast.service';

describe('PermissionService', () => {
  let service: PermissionService;
  let authServiceMock: { currentUserValue: { role: string } | null };
  let toastServiceMock: jasmine.SpyObj<ToastService>;

  beforeEach(() => {
    authServiceMock = {
      currentUserValue: { role: 'supervisor' },
    };

    toastServiceMock = jasmine.createSpyObj<ToastService>('ToastService', [
      'warning',
    ]);

    TestBed.configureTestingModule({
      providers: [
        PermissionService,
        { provide: AuthService, useValue: authServiceMock },
        { provide: ToastService, useValue: toastServiceMock },
      ],
    });

    service = TestBed.inject(PermissionService);
  });

  it('allows valid action for current role', () => {
    expect(service.canAction('clientes', 'edit')).toBeTrue();
  });

  it('blocks invalid action and emits warning toast', () => {
    const allowed = service.guardAction(
      'usuarios',
      'create',
      'No autorizado para crear usuarios'
    );

    expect(allowed).toBeFalse();
    expect(toastServiceMock.warning).toHaveBeenCalledWith(
      'No autorizado para crear usuarios'
    );
  });

  it('does not emit warning when action is allowed', () => {
    const allowed = service.guardAction('productos', 'edit');

    expect(allowed).toBeTrue();
    expect(toastServiceMock.warning).not.toHaveBeenCalled();
  });
});

