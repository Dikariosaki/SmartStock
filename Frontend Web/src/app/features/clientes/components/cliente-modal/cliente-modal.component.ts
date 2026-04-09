import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  OnDestroy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { of, Subject, switchMap } from 'rxjs';
import { ClienteService } from '@features/clientes/services/cliente.service';
import { Cliente } from '@features/clientes/models/cliente.models';
import { UsuarioService } from '@features/usuarios/services/usuarios.service';
import { PermissionService } from '@core/auth/permission.service';

@Component({
  selector: 'app-cliente-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './cliente-modal.component.html',
  styleUrl: './cliente-modal.component.css',
})
export class ClienteModalComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  // Propiedades del modal
  @Input() show = false;
  @Input() mode: 'create' | 'edit' | 'view' = 'create';
  @Input() cliente: Cliente | null = null;

  @Output() clienteGuardado = new EventEmitter<Cliente>();
  @Output() modalCerrado = new EventEmitter<void>();

  // Estados del modal
  loading = false;
  error: string | null = null;
  showConfirmation = false;
  showSuccess = false;
  confirmationMessage = '';
  successMessage = '';

  // Formulario
  clienteForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private clienteService: ClienteService,
    private usuarioService: UsuarioService,
    private permissionService: PermissionService
  ) {
    this.clienteForm = this.createForm();
  }

  ngOnInit(): void {
    this.initializeForm();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Crea el formulario reactivo
   */
  private createForm(): FormGroup {
    return this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(150)]],
      cedula: ['', [Validators.required]], // Campo de cédula requerido
      contacto: ['', [Validators.maxLength(150)]],
      telefono: ['', [Validators.maxLength(50)]],
      email: [
        '',
        [Validators.required, Validators.email, Validators.maxLength(150)],
      ],
      direccion: ['', [Validators.maxLength(250)]],
      sucursal: ['', [Validators.maxLength(100)]],
      activo: [true],
    });
  }

  /**
   * Inicializa el formulario con los datos del cliente
   */
  private initializeForm(): void {
    if (this.cliente && (this.mode === 'edit' || this.mode === 'view')) {
      this.clienteForm.patchValue({
        nombre: this.cliente.nombre || '',
        cedula: this.cliente.usuario?.cedula || '',
        contacto: this.cliente.contacto || '',
        telefono: this.cliente.telefono || '',
        email: this.cliente.email || '',
        direccion: this.cliente.direccion || '',
        sucursal: this.cliente.sucursal || '',
      });
    } else {
      this.clienteForm.reset();
    }

    // Deshabilitar formulario en modo vista
    if (this.mode === 'view') {
      this.clienteForm.disable();
    } else {
      this.clienteForm.enable();
    }

    // Limpiar estados
    this.error = null;
    this.loading = false;
    this.showConfirmation = false;
    this.showSuccess = false;
  }

  /**
   * Obtiene el título del modal según el modo
   */
  getModalTitle(): string {
    switch (this.mode) {
      case 'create':
        return 'Ingresar Cliente';
      case 'edit':
        return 'Modificar Cliente';
      case 'view':
        return 'Ver Cliente';
      default:
        return 'Cliente';
    }
  }

  /**
   * Obtiene el título de confirmación según el modo
   */
  getConfirmationTitle(): string {
    // Si el mensaje es de cancelación (cerrar sin guardar)
    if (
      this.confirmationMessage &&
      this.confirmationMessage.includes('cerrar')
    ) {
      return '¿Cancelar cambios?';
    }

    switch (this.mode) {
      case 'create':
        return 'Ingresar Cliente';
      case 'edit':
        return 'Modificar Cliente';
      default:
        return 'Confirmar Acción';
    }
  }

  /**
   * Obtiene el título de éxito según el modo
   */
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

  /**
   * Obtiene el texto del botón según el modo
   */
  getButtonText(): string {
    switch (this.mode) {
      case 'create':
        return 'Registrar';
      case 'edit':
        return 'Modificar';
      default:
        return 'Guardar';
    }
  }

  /**
   * Verifica si se puede guardar
   */
  canSubmitByPermission(): boolean {
    if (this.mode === 'create') {
      return this.permissionService.canAction('clientes', 'create');
    }

    if (this.mode === 'edit') {
      return this.permissionService.canAction('clientes', 'edit');
    }

    return true;
  }

  get canManageEstadoCliente(): boolean {
    return (
      this.permissionService.canAction('clientes', 'activate') ||
      this.permissionService.canAction('clientes', 'deactivate')
    );
  }

  canSave(): boolean {
    return (
      !this.loading &&
      this.clienteForm.valid &&
      (this.mode === 'create' || this.mode === 'edit') &&
      this.canSubmitByPermission()
    );
  }

  /**
   * Guarda el cliente (crear o actualizar)
   */
  guardarCliente(): void {
    if (this.mode === 'create' || this.mode === 'edit') {
      const action = this.mode === 'edit' ? 'edit' : 'create';
      const deniedMessage =
        this.mode === 'edit'
          ? 'No tienes permisos para editar clientes.'
          : 'No tienes permisos para crear clientes.';

      if (!this.permissionService.guardAction('clientes', action, deniedMessage)) {
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

  /**
   * Obtiene el mensaje de confirmación según el modo
   */
  private getConfirmationMessage(): string {
    switch (this.mode) {
      case 'create':
        return '¿Está seguro que desea registrar este cliente?';
      case 'edit':
        return '¿Está seguro que desea modificar este cliente?';
      default:
        return '¿Está seguro que desea realizar esta acción?';
    }
  }

  /**
   * Verifica si un campo tiene error
   */
  hasFieldError(field: string): boolean {
    const control = this.clienteForm.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  /**
   * Obtiene el mensaje de error de un campo
   */
  getFieldError(field: string): string {
    const control = this.clienteForm.get(field);
    if (!control || !control.errors) return '';

    const errors = control.errors;

    if (errors['required']) return 'Este campo es obligatorio';
    if (errors['email']) return 'Correo electrónico inválido';
    if (errors['minlength'])
      return `Mínimo ${errors['minlength'].requiredLength} caracteres`;
    if (errors['maxlength'])
      return `Máximo ${errors['maxlength'].requiredLength} caracteres`;
    if (errors['pattern']) return 'Formato inválido';

    return 'Campo inválido';
  }

  /**
   * Marca todos los campos del formulario como tocados
   */
  private markFormGroupTouched(): void {
    Object.keys(this.clienteForm.controls).forEach(key => {
      this.clienteForm.get(key)?.markAsTouched();
    });
  }

  /**
   * Cierra el modal
   */
  cerrarModal(): void {
    if (this.loading) {
      return; // No permitir cerrar mientras se está guardando
    }

    this.clienteForm.reset();
    this.error = null;
    this.showConfirmation = false;
    this.showSuccess = false;
    this.modalCerrado.emit();
  }

  /**
   * Confirma el cierre del modal
   */
  confirmarCierre(): void {
    if (this.clienteForm.dirty && !this.showSuccess && !this.showConfirmation) {
      this.showConfirmation = true;
      this.confirmationMessage =
        '¿Está seguro de cerrar? Los cambios no guardados se perderán.';
    } else {
      this.cerrarModal();
    }
  }

  /**
   * Obtiene el texto del botón de confirmación
   */
  getConfirmationButtonText(): string {
    if (
      this.confirmationMessage &&
      this.confirmationMessage.includes('cerrar')
    ) {
      return 'Sí, cerrar';
    }
    return 'Confirmar';
  }

  getConfirmationButtonClass(): string {
    const baseClasses =
      'flex-1 px-4 py-2 text-sm font-medium text-white rounded-lg transition-colors shadow-lg';
    if (
      this.confirmationMessage &&
      this.confirmationMessage.includes('cerrar')
    ) {
      return `${baseClasses} bg-red-500 hover:bg-red-600 shadow-red-500/30`;
    }
    return `${baseClasses} bg-primary hover:bg-blue-600 shadow-blue-500/30`;
  }

  /**
   * Confirma la acción
   */
  confirmarAccion(): void {
    // Si es confirmación de cierre (cancelar cambios)
    if (
      this.confirmationMessage &&
      this.confirmationMessage.includes('cerrar')
    ) {
      this.clienteForm.markAsPristine(); // Marcar como no sucio para permitir cerrar
      this.cerrarModal();
      return;
    }

    if (this.mode === 'create' || this.mode === 'edit') {
      const action = this.mode === 'edit' ? 'edit' : 'create';
      const deniedMessage =
        this.mode === 'edit'
          ? 'No tienes permisos para editar clientes.'
          : 'No tienes permisos para crear clientes.';

      if (!this.permissionService.guardAction('clientes', action, deniedMessage)) {
        this.showConfirmation = false;
        return;
      }
    }

    this.loading = true;
    this.error = '';

    const form = this.clienteForm.value;

    if (this.mode === 'create') {
      const datosCliente = {
        nombre: form.nombre,
        cedula: parseInt(form.cedula) || Math.floor(Math.random() * 1000000000),
        email: form.email,
        telefono: form.telefono || '',
        contacto: form.contacto || undefined,
        direccion: form.direccion || undefined,
        sucursal: form.sucursal || undefined,
      };

      this.clienteService.createClienteConUsuario(datosCliente).subscribe({
        next: response => {
          this.loading = false;
          this.showConfirmation = false;
          this.showSuccess = true;
          this.successMessage = 'El Cliente fue registrado con éxito';
          this.clienteGuardado.emit(response);
        },
        error: error => {
          this.loading = false;
          this.showConfirmation = false;
          this.error =
            error?.error?.message ||
            error.message ||
            'Error al crear el cliente';
        },
      });
    } else if (this.mode === 'edit') {
      const updateReq = {
        usuarioId: this.cliente?.usuarioId,
        contacto: form.contacto || undefined,
        direccion: form.direccion || undefined,
        sucursal: form.sucursal || undefined,
      };

      const usuarioId = this.cliente?.usuario?.usuarioId;
      const usuarioUpdate = usuarioId
        ? {
            rolId: this.cliente!.usuario!.rolId,
            nombre: form.nombre || this.cliente!.usuario!.nombre,
            cedula: parseInt(form.cedula) || this.cliente!.usuario!.cedula,
            email: form.email || this.cliente!.usuario!.email,
            telefono: form.telefono || '',
            estado: this.canManageEstadoCliente
              ? form.activo
              : (this.cliente?.activo ?? this.cliente?.usuario?.estado ?? true),
          }
        : null;

      this.clienteService
        .updateCliente(this.cliente!.cliente_id, updateReq)
        .pipe(
          switchMap(() => {
            if (usuarioUpdate && usuarioId) {
              return this.usuarioService.updateUsuario(usuarioId, usuarioUpdate);
            }
            return of(null);
          }),
          switchMap(() =>
            this.clienteService.getClienteById(this.cliente!.cliente_id)
          )
        )
        .subscribe({
          next: response => {
            this.loading = false;
            this.showConfirmation = false;
            this.showSuccess = true;
            this.successMessage = 'El Cliente fue modificado con éxito';
            this.clienteGuardado.emit(response);
          },
          error: error => {
            this.loading = false;
            this.showConfirmation = false;
            this.error =
              error?.error?.message ||
              error.message ||
              'Error al actualizar el cliente';
          },
        });
    }
  }

  /**
   * Cancela la confirmación
   */
  cancelarConfirmacion(): void {
    this.showConfirmation = false;
    this.loading = false;
  }

  /**
   * Maneja clics en el backdrop
   */
  onBackdropClick(event: Event): void {
    if (event.target === event.currentTarget) {
      this.confirmarCierre();
    }
  }
}
