import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  OnChanges,
  SimpleChanges,
  OnDestroy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { of, Subject, switchMap } from 'rxjs';
import { Proveedor } from '../../models/proveedor.models';
import { ProveedorService } from '../../services/proveedores.service';
import { UsuarioService } from '@features/usuarios/services/usuarios.service';
import { PermissionService } from '@core/auth/permission.service';

@Component({
  selector: 'app-proveedores-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './proveedores-modal.component.html',
  styleUrls: ['./proveedores-modal.component.css'],
})
export class ProveedoresModalComponent implements OnInit, OnChanges, OnDestroy {
  private destroy$ = new Subject<void>();

  @Input() show = false;
  @Input() mode: 'create' | 'edit' | 'view' = 'create';
  @Input() proveedor: Proveedor | null = null;

  @Output() proveedorSaved = new EventEmitter<Proveedor>();
  @Output() modalClosed = new EventEmitter<void>();

  proveedorForm!: FormGroup;

  showConfirmation = false;
  showSuccess = false;
  loading = false;
  error: string | null = null;

  // Mensajes dinámicos
  confirmationMessage = '¿Seguro que quieres cerrar sin guardar?';
  successMessage = '¡El proveedor se guardó correctamente!';

  constructor(
    private fb: FormBuilder,
    private proveedorService: ProveedorService,
    private usuarioService: UsuarioService,
    private permissionService: PermissionService
  ) {}

  ngOnInit(): void {
    this.initForm();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['proveedor'] && this.proveedorForm) {
      this.initializeForm();
    }
    if (changes['show'] && this.show && this.proveedorForm) {
      this.initializeForm();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initForm(): void {
    this.proveedorForm = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(150)]],
      cedula: ['', [Validators.required]],
      contacto: ['', [Validators.maxLength(150)]],
      telefono: ['', [Validators.maxLength(50)]],
      email: [
        '',
        [Validators.required, Validators.email, Validators.maxLength(150)],
      ],
    });

    this.initializeForm();
  }

  private initializeForm(): void {
    if (this.proveedor && (this.mode === 'edit' || this.mode === 'view')) {
      this.proveedorForm.patchValue({
        nombre: this.proveedor.nombre || '',
        cedula: this.proveedor.usuario?.cedula || '',
        contacto: this.proveedor.contacto || '',
        telefono: this.proveedor.telefono || '',
        email: this.proveedor.email || '',
      });
    } else {
      this.proveedorForm.reset();
    }

    // Deshabilitar formulario en modo vista
    if (this.mode === 'view') {
      this.proveedorForm.disable();
    } else {
      this.proveedorForm.enable();
    }

    // Limpiar estados
    this.error = null;
    this.loading = false;
    this.showConfirmation = false;
    this.showSuccess = false;
  }

  // === Métodos para el HTML ===
  onBackdropClick(event: Event): void {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.confirmarCierre();
    }
  }

  getModalTitle(): string {
    switch (this.mode) {
      case 'edit':
        return 'Modificar Proveedor';
      case 'view':
        return 'Ver Proveedor';
      default:
        return 'Ingresar Proveedor';
    }
  }

  getButtonText(): string {
    switch (this.mode) {
      case 'edit':
        return 'Modificar';
      case 'view':
        return 'Cerrar';
      default:
        return 'Registrar';
    }
  }

  canSave(): boolean {
    return (
      this.proveedorForm.valid &&
      !this.loading &&
      (this.mode === 'create' || this.mode === 'edit') &&
      this.canSubmitByPermission()
    );
  }

  canSubmitByPermission(): boolean {
    if (this.mode === 'create') {
      return this.permissionService.canAction('proveedores', 'create');
    }

    if (this.mode === 'edit') {
      return this.permissionService.canAction('proveedores', 'edit');
    }

    return true;
  }

  confirmarCierre(): void {
    if (
      this.proveedorForm.dirty &&
      !this.showSuccess &&
      !this.showConfirmation
    ) {
      this.showConfirmation = true;
      this.confirmationMessage =
        '¿Está seguro de cerrar? Los cambios no guardados se perderán.';
    } else {
      this.cerrarModal();
    }
  }

  cancelarConfirmacion(): void {
    this.showConfirmation = false;
  }

  confirmarAccion(): void {
    if (
      this.confirmationMessage.includes('cerrar') ||
      this.confirmationMessage.includes('guardar')
    ) {
      this.showConfirmation = false;
      this.cerrarModal();
    } else {
      // Es una confirmación de guardar
      this.executeGuardar();
    }
  }

  guardarProveedor(): void {
    if (this.mode === 'create' || this.mode === 'edit') {
      const action = this.mode === 'edit' ? 'edit' : 'create';
      const deniedMessage =
        this.mode === 'edit'
          ? 'No tienes permisos para editar proveedores.'
          : 'No tienes permisos para crear proveedores.';

      if (
        !this.permissionService.guardAction('proveedores', action, deniedMessage)
      ) {
        return;
      }
    }

    if (!this.canSave()) {
      return;
    }

    // Mostrar modal de confirmación
    this.showConfirmation = true;
    this.confirmationMessage = this.getConfirmationMessage();
  }

  private getConfirmationMessage(): string {
    switch (this.mode) {
      case 'create':
        return '¿Está seguro que desea registrar este proveedor?';
      case 'edit':
        return '¿Está seguro que desea modificar este proveedor?';
      default:
        return '¿Está seguro que desea realizar esta acción?';
    }
  }

  private executeGuardar(): void {
    if (this.mode === 'create' || this.mode === 'edit') {
      const action = this.mode === 'edit' ? 'edit' : 'create';
      const deniedMessage =
        this.mode === 'edit'
          ? 'No tienes permisos para editar proveedores.'
          : 'No tienes permisos para crear proveedores.';

      if (
        !this.permissionService.guardAction('proveedores', action, deniedMessage)
      ) {
        this.showConfirmation = false;
        return;
      }
    }

    this.loading = true;
    this.error = '';

    const form = this.proveedorForm.value;

    if (this.mode === 'create') {
      const datosProveedor = {
        nombre: form.nombre,
        cedula: parseInt(form.cedula) || Math.floor(Math.random() * 1000000000),
        email: form.email,
        telefono: form.telefono || '',
        contacto: form.contacto || undefined,
      };

      this.proveedorService
        .createProveedorConUsuario(datosProveedor)
        .subscribe({
          next: response => {
            this.loading = false;
            this.showConfirmation = false;
            this.showSuccess = true;
            this.successMessage = 'El Proveedor fue registrado con éxito';
            this.proveedorSaved.emit(response);
          },
          error: error => {
            this.loading = false;
            this.showConfirmation = false;
            this.error =
              error?.error?.message ||
              error.message ||
              'Error al crear el proveedor';
          },
        });
    } else if (this.mode === 'edit') {
      const updateReq = {
        usuarioId: this.proveedor?.usuarioId,
        contacto: form.contacto || undefined,
      };

      const usuarioId = this.proveedor?.usuario?.usuarioId;
      const usuarioUpdate = usuarioId
        ? {
            rolId: this.proveedor!.usuario!.rolId,
            nombre: form.nombre || this.proveedor!.usuario!.nombre,
            cedula: parseInt(form.cedula) || this.proveedor!.usuario!.cedula,
            email: form.email || this.proveedor!.usuario!.email,
            telefono: form.telefono || '',
            estado: this.proveedor!.usuario!.estado,
          }
        : null;

      this.proveedorService
        .updateProveedor(this.proveedor!.proveedor_id, updateReq)
        .pipe(
          switchMap(() => {
            if (usuarioUpdate && usuarioId) {
              return this.usuarioService.updateUsuario(usuarioId, usuarioUpdate);
            }
            return of(null);
          }),
          switchMap(() =>
            this.proveedorService.getProveedorById(this.proveedor!.proveedor_id)
          )
        )
        .subscribe({
          next: response => {
            this.loading = false;
            this.showConfirmation = false;
            this.showSuccess = true;
            this.successMessage = 'El Proveedor fue modificado con éxito';
            this.proveedorSaved.emit(response);
          },
          error: error => {
            this.loading = false;
            this.showConfirmation = false;
            this.error =
              error?.error?.message ||
              error.message ||
              'Error al actualizar el proveedor';
          },
        });
    }
  }

  cerrarModal(): void {
    if (this.loading) {
      return;
    }

    this.proveedorForm.reset();
    this.show = false;
    this.showConfirmation = false;
    this.showSuccess = false;
    this.error = null;
    this.modalClosed.emit();
  }

  getConfirmationTitle(): string {
    switch (this.mode) {
      case 'create':
        return 'Ingresar Proveedor';
      case 'edit':
        return 'Modificar Proveedor';
      default:
        return 'Confirmación';
    }
  }

  getSuccessTitle(): string {
    switch (this.mode) {
      case 'create':
        return 'Registrado';
      case 'edit':
        return 'Modificado';
      default:
        return 'Éxito';
    }
  }

  // Marcar todos los campos como tocados para mostrar errores
  private markFormGroupTouched(): void {
    Object.keys(this.proveedorForm.controls).forEach(key => {
      const control = this.proveedorForm.get(key);
      control?.markAsTouched();
    });
  }

  // Verificar si un campo específico tiene errores
  hasFieldError(fieldName: string): boolean {
    const field = this.proveedorForm.get(fieldName);
    return !!(field?.errors && field.touched);
  }

  // Obtener mensaje de error específico de un campo
  getFieldError(fieldName: string): string {
    const field = this.proveedorForm.get(fieldName);
    if (!field?.errors || !field.touched) return '';

    if (field.errors['required']) {
      return `Campo requerido`;
    }
    if (field.errors['minlength']) {
      return `Mínimo ${field.errors['minlength'].requiredLength} caracteres`;
    }
    if (field.errors['maxlength']) {
      return `Máximo ${field.errors['maxlength'].requiredLength} caracteres`;
    }
    if (field.errors['email']) {
      return `Email inválido`;
    }

    return 'Campo inválido';
  }
}
