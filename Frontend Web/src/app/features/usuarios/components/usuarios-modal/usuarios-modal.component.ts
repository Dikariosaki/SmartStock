import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { Usuario, Rol } from '../../models/usuario.models';
import { UsuarioService } from '../../services/usuarios.service';
import { RolesService } from '../../services/roles.service';
import { ActionKey } from '@core/auth/view-access.config';
import { PermissionService } from '@core/auth/permission.service';

@Component({
  selector: 'app-usuarios-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './usuarios-modal.component.html',
  styleUrls: ['./usuarios-modal.component.css'],
})
export class UsuariosModalComponent implements OnInit, OnChanges {
  @Input() show = false;
  @Input() mode: 'create' | 'edit' | 'view' = 'create';
  @Input() usuario: Usuario | null = null;
  @Output() usuarioSaved = new EventEmitter<Usuario>();
  @Output() modalClosed = new EventEmitter<void>();

  usuarioForm!: FormGroup;
  showConfirmation = false;
  showSuccess = false;
  loading = false;
  error: string | null = null;
  roles: Rol[] = [];

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService,
    private rolesService: RolesService,
    private permissionService: PermissionService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadRoles();
    this.setupRolListener();
  }

  private setupRolListener(): void {
    this.usuarioForm.get('rolId')?.valueChanges.subscribe(() => {
      this.updatePasswordValidators();
    });
  }

  private updatePasswordValidators(): void {
    const rolId = this.usuarioForm.get('rolId')?.value;
    const passwordControl = this.usuarioForm.get('password');
    const isInternalRole = rolId && [1, 2, 3].includes(Number(rolId));

    if (isInternalRole) {
      if (this.mode === 'create' || (this.mode === 'edit' && !this.usuario?.passwordHash)) {
        passwordControl?.setValidators([Validators.required, Validators.minLength(6)]);
      } else {
        passwordControl?.setValidators([Validators.minLength(6)]);
      }
    } else {
      passwordControl?.setValidators([Validators.minLength(6)]);
    }
    passwordControl?.updateValueAndValidity();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['usuario'] && this.usuarioForm) {
      this.usuarioForm.patchValue(this.usuario || {});
      this.updatePasswordValidators();
    }
  }

  private initForm(): void {
    this.usuarioForm = this.fb.group({
      rolId: [this.usuario?.rolId || '', [Validators.required]],
      nombre: [
        this.usuario?.nombre || '',
        [Validators.required, Validators.minLength(3)],
      ],
      identificacion: [
        this.usuario?.identificacion || '',
        [Validators.required],
      ],
      correo: [
        this.usuario?.correo || '',
        [Validators.required, Validators.email],
      ],
      telefono: [this.usuario?.telefono || '', [Validators.required]],
      password: [
        '',
        [Validators.minLength(6)],
      ],
    });
  }

  private loadRoles(): void {
    this.rolesService.getRoles().subscribe({
      next: roles => {
        this.roles = roles;
      },
      error: err => {
        console.error('Error al cargar roles:', err);
        this.error = 'Error al cargar roles';
      },
    });
  }

  getModalTitle(): string {
    return this.mode === 'edit'
      ? 'Editar Usuario'
      : this.mode === 'view'
        ? 'Ver Usuario'
        : 'Agregar Usuario';
  }

  getButtonText(): string {
    return this.mode === 'edit' ? 'Guardar cambios' : 'Guardar';
  }

  canSave(): boolean {
    return (
      this.usuarioForm.valid &&
      !this.loading &&
      this.mode !== 'view' &&
      this.canSubmitByPermission()
    );
  }

  canSubmitByPermission(): boolean {
    if (this.mode === 'create') {
      return this.permissionService.canAction('usuarios', 'create');
    }

    if (this.mode === 'edit') {
      return this.permissionService.canAction('usuarios', 'edit');
    }

    return true;
  }

  confirmarCierre(): void {
    this.showConfirmation = true;
  }
  cancelarConfirmacion(): void {
    this.showConfirmation = false;
  }
  confirmarAccion(): void {
    this.showConfirmation = false;
    this.cerrarModal();
  }

  onBackdropClick(event: Event): void {
    if ((event.target as HTMLElement).classList.contains('modal-overlay'))
      this.confirmarCierre();
  }

  guardarUsuario(): void {
    const action: ActionKey = this.mode === 'edit' ? 'edit' : 'create';
    if (
      !this.permissionService.guardAction(
        'usuarios',
        action,
        this.mode === 'edit'
          ? 'No tienes permisos para editar usuarios.'
          : 'No tienes permisos para crear usuarios.'
      )
    ) {
      return;
    }

    if (this.usuarioForm.valid) {
      this.loading = true;
      const formData = this.usuarioForm.value;

      // Mapear datos del formulario al formato del backend
      const data = {
        rolId: formData.rolId,
        nombre: formData.nombre,
        cedula: parseInt(formData.identificacion), // Backend espera cedula (number)
        email: formData.correo, // Backend espera email
        telefono: formData.telefono,
        estado: true,
      };

      // Agregar password solo si está presente (crear o cambiar)
      if (formData.password && formData.password.trim() !== '') {
        (data as any).password = formData.password;
      }

      const request$ =
        this.mode === 'edit' && this.usuario
          ? this.usuarioService.updateUsuarioObservable(this.usuario.usuario_id, data)
          : this.usuarioService.createUsuario(data as any);

      request$.subscribe({
        next: (res: Usuario) => {
          this.loading = false;
          this.showSuccess = true;
          this.usuarioSaved.emit(res);
          setTimeout(() => {
            if (this.showSuccess) {
              this.showSuccess = false;
              this.cerrarModal();
            }
          }, 2000);
        },
        error: (err: any) => {
          this.loading = false;
          console.error('Error al guardar usuario:', err);
          this.error = err.error?.message || 'Error al guardar usuario';
        },
      });
    } else {
      this.usuarioForm.markAllAsTouched();
    }
  }

  cerrarModal(): void {
    this.show = false;
    this.showConfirmation = false;
    this.showSuccess = false;
    this.error = null;
    this.modalClosed.emit();
  }

  hasFieldError(field: string): boolean {
    const control = this.usuarioForm.get(field);
    return !!(control?.invalid && control.touched);
  }

  getFieldError(field: string): string {
    const control = this.usuarioForm.get(field);
    if (control?.hasError('required')) return 'Campo requerido';
    if (control?.hasError('email')) return 'Correo inválido';
    if (control?.hasError('minlength')) {
      const minLength = control.errors?.['minlength']?.requiredLength;
      return `Mínimo ${minLength} caracteres`;
    }
    return '';
  }
}
