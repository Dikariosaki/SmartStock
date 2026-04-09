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
import { FormsModule } from '@angular/forms';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import {
  Subject,
  debounceTime,
  distinctUntilChanged,
  switchMap,
  takeUntil,
  tap,
  map,
} from 'rxjs';
import { Inventario } from '../../models/inventario.models';
import { InventarioService } from '../../services/inventarios.service';
import { ProductoService } from '@features/productos/services/productos.service';
import { Producto } from '@features/productos/models/productos.models';
import { PermissionService } from '@core/auth/permission.service';

@Component({
  selector: 'app-inventario-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './inventario-modal.component.html',
  styleUrl: './inventario-modal.component.css',
})
export class InventarioModalComponent implements OnInit, OnChanges, OnDestroy {
  private destroy$ = new Subject<void>();
  @Input() show = false;
  @Input() mode: 'create' | 'edit' | 'view' = 'create';
  @Input() inventario: Inventario | null = null;
  @Output() inventarioSaved = new EventEmitter<Inventario>();
  @Output() modalClosed = new EventEmitter<void>();
  inventarioForm!: FormGroup;
  showConfirmation = false;
  showSuccess = false;
  loading = false;
  error: string | null = null;
  productos: Producto[] = [];
  filteredProductos: Producto[] = [];
  productoSearchTerm = '';
  showProductoDropdown = false;
  private productoSearch$ = new Subject<string>();
  productoLoading = false;
  confirmationMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private inventarioService: InventarioService,
    private productoService: ProductoService,
    private permissionService: PermissionService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.cargarProductos();
    this.productoSearch$
      .pipe(
        debounceTime(250),
        distinctUntilChanged(),
        tap(() => (this.productoLoading = true)),
        switchMap(term =>
          this.productoService
            .getProductos(term ? { search: term } : undefined)
            .pipe(map(response => ({ term, list: response.data })))
        ),
        takeUntil(this.destroy$)
      )
      .subscribe(({ term, list }) => {
        const t = (term || '').toLowerCase();
        this.filteredProductos = t
          ? list.filter(p => (p.nombre || '').toLowerCase().includes(t))
          : list;
        this.productoLoading = false;
      });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['inventario'] && this.inventarioForm) this.initializeForm();
    if (changes['show'] && this.show && this.inventarioForm)
      this.initializeForm();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private cargarProductos(): void {
    this.productoService.getProductos().subscribe({
      next: (response: any) => {
        // Handle PagedResponse
        const data = response.data || response;
        this.productos = data;
        this.filteredProductos = data;
      },
      error: (err: any) => {
        console.error('Error al cargar productos:', err);
        this.error = 'No se pudieron cargar los productos';
      },
    });
  }

  private initForm(): void {
    this.inventarioForm = this.fb.group({
      productoId: [null, Validators.required],
      ubicacion: ['', [Validators.required, Validators.maxLength(100)]],
      cantidad: [0, [Validators.required, Validators.min(0)]],
      puntoReorden: [0, [Validators.required, Validators.min(0)]],
      estado: [true],
    });
    this.initializeForm();
  }

  private initializeForm(): void {
    if (this.inventario && (this.mode === 'edit' || this.mode === 'view')) {
      this.inventarioForm.patchValue({
        productoId: this.inventario.productoId,
        ubicacion: this.inventario.ubicacion,
        cantidad: this.inventario.cantidad,
        puntoReorden: this.inventario.puntoReorden,
        estado: this.inventario.estado,
      });
    } else {
      this.inventarioForm.reset({ estado: true, cantidad: 0, puntoReorden: 0 });
    }
    if (this.mode === 'view') this.inventarioForm.disable();
    else this.inventarioForm.enable();
    this.error = null;
    this.loading = false;
    this.showConfirmation = false;
    this.showSuccess = false;
    this.productoSearchTerm = '';
    this.filteredProductos = this.productos;
  }

  getModalTitle(): string {
    return this.mode === 'edit'
      ? 'Modificar Inventario'
      : this.mode === 'view'
        ? 'Ver Inventario'
        : 'Nuevo Inventario';
  }
  getButtonText(): string {
    return this.mode === 'edit'
      ? 'Modificar'
      : this.mode === 'view'
        ? 'Cerrar'
        : 'Registrar';
  }
  canSave(): boolean {
    return (
      this.inventarioForm.valid &&
      !this.loading &&
      (this.mode === 'create' || this.mode === 'edit') &&
      this.canSubmitByPermission()
    );
  }

  canSubmitByPermission(): boolean {
    if (this.mode === 'create') {
      return this.permissionService.canAction('inventarios', 'create');
    }

    if (this.mode === 'edit') {
      return this.permissionService.canAction('inventarios', 'edit');
    }

    return true;
  }

  toggleProductoDropdown(): void {
    this.showProductoDropdown = !this.showProductoDropdown;
  }
  closeProductoDropdown(): void {
    this.showProductoDropdown = false;
  }
  filterProductos(): void {
    this.productoSearch$.next(this.productoSearchTerm || '');
    this.showProductoDropdown = true;
  }
  onSelectProducto(p: Producto): void {
    this.inventarioForm.get('productoId')?.setValue(p.productoId);
    this.closeProductoDropdown();
  }
  getSelectedProductoText(): string {
    const id = this.inventarioForm.get('productoId')?.value;
    const p = this.productos.find(x => x.productoId === id);
    return p ? `${p.codigo} - ${p.nombre}` : 'Seleccione un producto';
  }

  confirmarCierre(): void {
    if (
      this.inventarioForm.dirty &&
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
      this.executeGuardar();
    }
  }

  guardarInventario(): void {
    if (this.mode === 'create' || this.mode === 'edit') {
      const action = this.mode === 'edit' ? 'edit' : 'create';
      const deniedMessage =
        this.mode === 'edit'
          ? 'No tienes permisos para editar inventarios.'
          : 'No tienes permisos para crear inventarios.';

      if (!this.permissionService.guardAction('inventarios', action, deniedMessage)) {
        return;
      }
    }

    if (!this.canSave()) return;
    this.showConfirmation = true;
    this.confirmationMessage =
      this.mode === 'create'
        ? '¿Está seguro que desea registrar este inventario?'
        : '¿Está seguro que desea modificar este inventario?';
  }

  private executeGuardar(): void {
    if (this.mode === 'create' || this.mode === 'edit') {
      const action = this.mode === 'edit' ? 'edit' : 'create';
      const deniedMessage =
        this.mode === 'edit'
          ? 'No tienes permisos para editar inventarios.'
          : 'No tienes permisos para crear inventarios.';

      if (!this.permissionService.guardAction('inventarios', action, deniedMessage)) {
        this.showConfirmation = false;
        return;
      }
    }

    this.loading = true;
    this.error = '';
    const form = this.inventarioForm.value;
    if (this.mode === 'create') {
      this.inventarioService
        .createInventario({
          productoId: form.productoId,
          ubicacion: form.ubicacion,
          cantidad: form.cantidad,
          puntoReorden: form.puntoReorden,
          estado: form.estado ?? true,
        })
        .subscribe({
          next: response => {
            this.loading = false;
            this.showConfirmation = false;
            this.showSuccess = true;
            this.successMessage = 'El Inventario fue registrado con éxito';
            this.inventarioSaved.emit(response);
          },
          error: (error: any) => {
            this.loading = false;
            this.showConfirmation = false;
            this.error =
              error?.error?.message ||
              error.message ||
              'Error al crear el inventario';
          },
        });
    } else if (this.mode === 'edit') {
      this.inventarioService
        .updateInventario(this.inventario!.inventarioId, {
          productoId: form.productoId,
          ubicacion: form.ubicacion,
          cantidad: form.cantidad,
          puntoReorden: form.puntoReorden,
          estado: form.estado,
        })
        .subscribe({
          next: () => {
            this.loading = false;
            this.showConfirmation = false;
            this.showSuccess = true;
            this.successMessage = 'El Inventario fue modificado con éxito';
            this.inventarioSaved.emit(this.inventario!);
          },
          error: (error: any) => {
            this.loading = false;
            this.showConfirmation = false;
            this.error =
              error?.error?.message ||
              error.message ||
              'Error al actualizar el inventario';
          },
        });
    }
  }

  cerrarModal(): void {
    if (this.loading) return;
    this.inventarioForm.reset();
    this.show = false;
    this.showConfirmation = false;
    this.showSuccess = false;
    this.error = null;
    this.modalClosed.emit();
  }

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal-overlay'))
      this.confirmarCierre();
  }
  getConfirmationTitle(): string {
    return this.mode === 'create'
      ? 'Ingresar Inventario'
      : this.mode === 'edit'
        ? 'Modificar Inventario'
        : 'Confirmación';
  }
  getSuccessTitle(): string {
    return this.mode === 'create'
      ? 'Registrado'
      : this.mode === 'edit'
        ? 'Modificado'
        : 'Éxito';
  }
  hasFieldError(fieldName: string): boolean {
    const field = this.inventarioForm.get(fieldName);
    return !!(field?.errors && field.touched);
  }

  getFieldError(fieldName: string): string {
    const field = this.inventarioForm.get(fieldName);
    if (!field?.errors || !field.touched) return '';
    if (field.errors['required']) return 'Campo requerido';
    if (field.errors['min'])
      return `El valor mínimo es ${field.errors['min'].min}`;
    if (field.errors['maxlength'])
      return `Máximo ${field.errors['maxlength'].requiredLength} caracteres`;
    return 'Campo inválido';
  }
}
