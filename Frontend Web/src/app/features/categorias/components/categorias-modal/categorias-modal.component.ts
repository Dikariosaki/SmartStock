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
import { Categoria } from '../../models/categoria.models';
import { CategoriaService } from '../../services/categorias.service';
import { PermissionService } from '@core/auth/permission.service';

@Component({
  selector: 'app-categorias-modal', // CAMBIO
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './categorias-modal.component.html',
  styleUrls: ['./categorias-modal.component.css'],
})
export class CategoriasModalComponent implements OnInit, OnChanges {
  @Input() show = false;
  @Input() mode: 'create' | 'edit' | 'view' = 'create';
  @Input() categoria: Categoria | null = null; // CAMBIO

  @Output() categoriaSaved = new EventEmitter<Categoria>(); // CAMBIO
  @Output() modalClosed = new EventEmitter<void>();

  categoriaForm!: FormGroup;

  showConfirmation = false;
  showSuccess = false;
  loading = false;
  error: string | null = null;

  confirmationMessage = '¿Seguro que quieres cerrar sin guardar?';
  successMessage = '¡La categoría se guardó correctamente!'; // CAMBIO

  constructor(
    private fb: FormBuilder,
    private categoriaService: CategoriaService, // CAMBIO
    private permissionService: PermissionService
  ) {}

  ngOnInit(): void {
    this.initForm();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['categoria'] && this.categoriaForm) {
      this.categoriaForm.patchValue(this.categoria || {});
    }
  }

  private initForm(): void {
    this.categoriaForm = this.fb.group({
      nombre: [
        this.categoria?.nombre || '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(100),
        ],
      ],
      estado: [this.categoria?.estado ?? true],
    });
  }

  onBackdropClick(event: Event): void {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.confirmarCierre();
    }
  }

  getModalTitle(): string {
    switch (this.mode) {
      case 'edit':
        return 'Editar Categoría'; // CAMBIO
      case 'view':
        return 'Ver Categoría';
      default:
        return 'Nueva Categoría';
    }
  }

  getButtonText(): string {
    switch (this.mode) {
      case 'edit':
        return 'Guardar cambios';
      case 'view':
        return 'Cerrar';
      default:
        return 'Guardar';
    }
  }

  canSave(): boolean {
    return (
      this.categoriaForm.valid &&
      !this.loading &&
      this.mode !== 'view' &&
      this.canSubmitByPermission()
    );
  }

  canSubmitByPermission(): boolean {
    if (this.mode === 'create') {
      return this.permissionService.canAction('categorias', 'create');
    }

    if (this.mode === 'edit') {
      return this.permissionService.canAction('categorias', 'edit');
    }

    return true;
  }

  confirmarCierre(): void {
    this.showConfirmation = true;
    this.confirmationMessage = '¿Seguro que quieres cerrar sin guardar?';
  }
  cancelarConfirmacion(): void {
    this.showConfirmation = false;
  }

  getConfirmationMessage(): string {
    switch (this.mode) {
      case 'create':
        return '¿Está seguro que desea crear esta categoría?';
      case 'edit':
        return '¿Está seguro que desea modificar esta categoría?';
      default:
        return '¿Está seguro que desea realizar esta acción?';
    }
  }

  confirmarAccion(): void {
    if (this.confirmationMessage.includes('cerrar')) {
      this.showConfirmation = false;
      this.cerrarModal();
      return;
    }

    this.showConfirmation = false;

    // Lógica de guardado movida aquí
    if (this.mode === 'create' || this.mode === 'edit') {
      const action = this.mode === 'edit' ? 'edit' : 'create';
      const deniedMessage =
        this.mode === 'edit'
          ? 'No tienes permisos para editar categorias.'
          : 'No tienes permisos para crear categorias.';

      if (!this.permissionService.guardAction('categorias', action, deniedMessage)) {
        return;
      }
    }

    this.loading = true;
    this.error = null;
    const formData = this.categoriaForm.value;

    if (this.mode === 'edit' && this.categoria) {
      // Actualizar - retorna void
      this.categoriaService
        .updateCategoria(this.categoria.categoriaId, formData)
        .subscribe({
          next: () => {
            this.loading = false;
            this.showSuccess = true;
            this.categoriaSaved.emit(this.categoria!);
            setTimeout(() => this.cerrarModalExitoso(), 2000);
          },
          error: () => {
            this.loading = false;
            this.error = 'Error al actualizar la categoría.';
          },
        });
    } else {
      // Crear
      this.categoriaService.createCategoria(formData).subscribe({
        next: nuevaData => {
          this.loading = false;
          this.showSuccess = true;
          this.categoriaSaved.emit(nuevaData);
          setTimeout(() => this.cerrarModalExitoso(), 2000);
        },
        error: () => {
          this.loading = false;
          this.error = 'Error al crear la categoría.';
        },
      });
    }
  }

  guardarCategoria(): void {
    if (this.mode === 'create' || this.mode === 'edit') {
      const action = this.mode === 'edit' ? 'edit' : 'create';
      const deniedMessage =
        this.mode === 'edit'
          ? 'No tienes permisos para editar categorias.'
          : 'No tienes permisos para crear categorias.';

      if (!this.permissionService.guardAction('categorias', action, deniedMessage)) {
        return;
      }
    }

    if (this.categoriaForm.valid) {
      this.showConfirmation = true;
      this.confirmationMessage = this.getConfirmationMessage();
    } else {
      this.markFormGroupTouched();
      this.error = this.getFormErrors();
    }
  }

  cerrarModal(): void {
    this.show = false;
    this.showConfirmation = false;
    this.showSuccess = false;
    this.error = null;
    this.modalClosed.emit();
  }

  private cerrarModalExitoso(): void {
    if (this.showSuccess) {
      this.showSuccess = false;
      this.cerrarModal();
    }
  }

  getConfirmationTitle(): string {
    return 'Confirmación';
  }
  getSuccessTitle(): string {
    return 'Éxito';
  }

  private markFormGroupTouched(): void {
    Object.values(this.categoriaForm.controls).forEach(control =>
      control.markAsTouched()
    );
  }

  private getFormErrors(): string {
    // Simplificado
    return 'Por favor complete los campos requeridos.';
  }

  hasFieldError(fieldName: string): boolean {
    const field = this.categoriaForm.get(fieldName);
    return !!(field?.invalid && field.touched);
  }

  getFieldError(fieldName: string): string {
    const field = this.categoriaForm.get(fieldName);
    if (!field?.errors || !field.touched) return '';
    if (field.errors['required']) return 'Campo requerido';
    return 'Campo inválido';
  }
}
