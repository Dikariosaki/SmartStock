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
// CAMBIO: Importaciones para Subcategoría
import { Subcategoria } from '../../models/subcategoria.models';
import { SubcategoriaService } from '../../services/subcategorias.service';
import { CategoriaService } from '@features/categorias/services/categorias.service';
import { Categoria } from '@features/categorias/models/categoria.models';
import { PermissionService } from '@core/auth/permission.service';

@Component({
  selector: 'app-subcategorias-modal',
  standalone: true, // <-- 1. MARCA EL COMPONENTE COMO STANDALONE
  imports: [
    CommonModule, // <-- 2. AÑADE CommonModule (para ngIf, ngFor, etc.)
    ReactiveFormsModule, // <-- 3. AÑADE ReactiveFormsModule (para formGroup, formControlName)
  ],
  templateUrl: './subcategorias-modal.component.html',
  styleUrls: ['./subcategorias-modal.component.css'],
})
// CAMBIO
export class SubcategoriasModalComponent implements OnInit, OnChanges {
  @Input() show = false;
  @Input() mode: 'create' | 'edit' | 'view' = 'create';
  @Input() subcategoria: Subcategoria | null = null; // CAMBIO

  @Output() subcategoriaSaved = new EventEmitter<Subcategoria>(); // CAMBIO
  @Output() modalClosed = new EventEmitter<void>();

  subcategoriaForm!: FormGroup; // CAMBIO

  showConfirmation = false;
  showSuccess = false;
  loading = false;
  error: string | null = null;

  confirmationMessage = '¿Seguro que quieres cerrar sin guardar?';
  successMessage = '¡La subcategoría se guardó correctamente!'; // CAMBIO

  categorias: Categoria[] = [];

  constructor(
    private fb: FormBuilder,
    private subcategoriaService: SubcategoriaService, // CAMBIO
    private categoriaService: CategoriaService,
    private permissionService: PermissionService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.cargarCategorias();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['subcategoria'] && this.subcategoriaForm) {
      this.subcategoriaForm.patchValue(this.subcategoria || {});
    }
  }

  private initForm(): void {
    // CAMBIO: Formulario para Subcategoría
    this.subcategoriaForm = this.fb.group({
      nombre: [
        this.subcategoria?.nombre || '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(100),
        ],
      ],
      categoriaId: [
        this.subcategoria?.categoriaId || null,
        Validators.required,
      ],
      estado: [this.subcategoria?.estado ?? true],
    });
  }

  private cargarCategorias(): void {
    this.categoriaService.getCategorias(1, 100).subscribe({
      next: (response: any) => {
        this.categorias = response.data.filter((c: Categoria) => c.estado);
      },
      error: (err: any) => {
        console.error('Error al cargar categorías:', err);
      },
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
        return 'Editar Subcategoría';
      case 'view':
        return 'Ver Subcategoría';
      default:
        return 'Nueva Subcategoría';
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
      this.subcategoriaForm.valid &&
      !this.loading &&
      this.mode !== 'view' &&
      this.canSubmitByPermission()
    );
  }

  canSubmitByPermission(): boolean {
    if (this.mode === 'create') {
      return this.permissionService.canAction('subcategorias', 'create');
    }

    if (this.mode === 'edit') {
      return this.permissionService.canAction('subcategorias', 'edit');
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
        return '¿Está seguro que desea crear esta subcategoría?';
      case 'edit':
        return '¿Está seguro que desea modificar esta subcategoría?';
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
          ? 'No tienes permisos para editar subcategorias.'
          : 'No tienes permisos para crear subcategorias.';

      if (
        !this.permissionService.guardAction('subcategorias', action, deniedMessage)
      ) {
        return;
      }
    }

    this.loading = true;
    this.error = null;

    const formData = this.subcategoriaForm.value;

    if (this.mode === 'edit' && this.subcategoria) {
      // Actualizar subcategoría existente - retorna void
      this.subcategoriaService
        .updateSubcategoria(this.subcategoria.subcategoriaId, formData)
        .subscribe({
          next: () => {
            this.loading = false;
            this.showSuccess = true;
            this.subcategoriaSaved.emit(this.subcategoria!);
            setTimeout(() => this.cerrarModalExitoso(), 2000);
          },
          error: () => {
            this.loading = false;
            this.error = 'Error al actualizar la subcategoría.';
          },
        });
    } else {
      // Crear nueva subcategoría
      this.subcategoriaService.createSubcategoria(formData).subscribe({
        next: nuevaSubcategoria => {
          this.loading = false;
          this.showSuccess = true;
          this.subcategoriaSaved.emit(nuevaSubcategoria);
          setTimeout(() => this.cerrarModalExitoso(), 2000);
        },
        error: () => {
          this.loading = false;
          this.error = 'Error al crear la subcategoría.';
        },
      });
    }
  }

  guardarSubcategoria(): void {
    if (this.mode === 'create' || this.mode === 'edit') {
      const action = this.mode === 'edit' ? 'edit' : 'create';
      const deniedMessage =
        this.mode === 'edit'
          ? 'No tienes permisos para editar subcategorias.'
          : 'No tienes permisos para crear subcategorias.';

      if (
        !this.permissionService.guardAction('subcategorias', action, deniedMessage)
      ) {
        return;
      }
    }

    if (this.subcategoriaForm.valid) {
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
    Object.values(this.subcategoriaForm.controls).forEach(control =>
      control.markAsTouched()
    );
  }

  private getFormErrors(): string {
    const errors: string[] = [];
    Object.keys(this.subcategoriaForm.controls).forEach(key => {
      const control = this.subcategoriaForm.get(key);
      if (control?.errors && control.touched) {
        errors.push(`${this.getFieldName(key)} es inválido`);
      }
    });
    return errors.length > 0
      ? errors.join('. ')
      : 'Por favor complete los campos requeridos.';
  }

  private getFieldName(fieldKey: string): string {
    const fieldNames: { [key: string]: string } = {
      nombre: 'Nombre',
      categoriaId: 'Categoría',
    };
    return fieldNames[fieldKey] || fieldKey;
  }

  hasFieldError(fieldName: string): boolean {
    const field = this.subcategoriaForm.get(fieldName);
    return !!(field?.invalid && field.touched);
  }

  getFieldError(fieldName: string): string {
    const field = this.subcategoriaForm.get(fieldName);
    if (!field?.errors || !field.touched) return '';

    if (field.errors['required'])
      return `${this.getFieldName(fieldName)} es requerido`;
    if (field.errors['minlength'])
      return `Mínimo ${field.errors['minlength'].requiredLength} caracteres`;
    if (field.errors['maxlength'])
      return `Máximo ${field.errors['maxlength'].requiredLength} caracteres`;
    return 'Campo inválido';
  }
}
