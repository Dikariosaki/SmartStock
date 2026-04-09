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
import { Producto } from '../../models/productos.models';
import { ProductoService } from '../../services/productos.service';
import { SubcategoriaService } from '@features/subcategorias/services/subcategorias.service';
import { Subcategoria } from '@features/subcategorias/models/subcategoria.models';
import { PermissionService } from '@core/auth/permission.service';

@Component({
  selector: 'app-productos-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './productos-modal.component.html',
  styleUrls: ['./productos-modal.component.css'],
})
export class ProductosModalComponent implements OnInit, OnChanges, OnDestroy {
  @Input() show = false;
  @Input() mode: 'create' | 'edit' | 'view' = 'create';
  @Input() producto: Producto | null = null;
  @Output() productoSaved = new EventEmitter<Producto>();
  @Output() modalClosed = new EventEmitter<void>();

  productoForm!: FormGroup;
  showConfirmation = false;
  showSuccess = false;
  loading = false;
  error: string | null = null;
  subcategorias: Subcategoria[] = [];

  constructor(
    private fb: FormBuilder,
    private productoService: ProductoService,
    private subcategoriaService: SubcategoriaService,
    private permissionService: PermissionService
  ) {}

  canSubmitByPermission(): boolean {
    if (this.mode === 'create') {
      return this.permissionService.canAction('productos', 'create');
    }

    if (this.mode === 'edit') {
      return this.permissionService.canAction('productos', 'edit');
    }

    return true;
  }

  ngOnInit(): void {
    this.initForm();
    this.cargarSubcategorias();
    document.body.style.overflow = 'hidden';
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['producto'] && this.productoForm && this.producto) {
      this.productoForm.patchValue({
        codigo: this.producto.codigo,
        nombre: this.producto.nombre,
        descripcion: this.producto.descripcion,
        precioUnitario: this.producto.precioUnitario,
        subcategoriaId: this.producto.subcategoriaId,
        estado: this.producto.estado,
      });
    }
  }

  ngOnDestroy(): void {
    document.body.style.overflow = 'auto';
  }

  private cargarSubcategorias(): void {
    this.subcategoriaService
      .getSubcategorias(1, 1000, { estado: true })
      .subscribe({
        next: (response: any) => {
          this.subcategorias = response.data;
        },
        error: (err: any) => {
          console.error('Error al cargar subcategorías:', err);
          this.error = 'No se pudieron cargar las subcategorías';
        },
      });
  }

  private initForm(): void {
    // En modo create, no incluimos código (se auto-genera en backend)
    if (this.mode === 'create') {
      this.productoForm = this.fb.group({
        nombre: ['', [Validators.required, Validators.maxLength(150)]],
        descripcion: ['', Validators.maxLength(500)],
        precioUnitario: [0, [Validators.required, Validators.min(0)]],
        subcategoriaId: [null, Validators.required],
        estado: [true],
      });
    } else {
      // En modo edit/view, incluimos código (readonly)
      this.productoForm = this.fb.group({
        codigo: [
          this.producto?.codigo || '',
          [Validators.required, Validators.maxLength(50)],
        ],
        nombre: [
          this.producto?.nombre || '',
          [Validators.required, Validators.maxLength(150)],
        ],
        descripcion: [
          this.producto?.descripcion || '',
          Validators.maxLength(500),
        ],
        precioUnitario: [
          this.producto?.precioUnitario || 0,
          [Validators.required, Validators.min(0)],
        ],
        subcategoriaId: [
          this.producto?.subcategoriaId || null,
          Validators.required,
        ],
        estado: [this.producto?.estado ?? true],
      });
    }
  }

  getModalTitle(): string {
    if (this.mode === 'view') return 'Ver Producto';
    return this.mode === 'edit' ? 'Editar Producto' : 'Nuevo Producto';
  }

  confirmarCierre(): void {
    if (this.productoForm.dirty && !this.showSuccess) {
      this.showConfirmation = true;
    } else {
      this.cerrarModal();
    }
  }

  cancelarConfirmacion(): void {
    this.showConfirmation = false;
  }

  confirmarAccion(): void {
    this.showConfirmation = false;
    this.cerrarModal();
  }

  cerrarModal(): void {
    this.show = false;
    this.showConfirmation = false;
    this.showSuccess = false;
    this.error = null;
    this.productoForm.reset();
    this.modalClosed.emit();
  }

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.confirmarCierre();
    }
  }

  guardarProducto(): void {
    if (this.mode === 'create' || this.mode === 'edit') {
      const action = this.mode === 'edit' ? 'edit' : 'create';
      const deniedMessage =
        this.mode === 'edit'
          ? 'No tienes permisos para editar productos.'
          : 'No tienes permisos para crear productos.';

      if (!this.permissionService.guardAction('productos', action, deniedMessage)) {
        return;
      }
    }

    if (!this.productoForm.valid) {
      return;
    }

    this.loading = true;
    this.error = null;
    const formValue = this.productoForm.value;

    const productoData: any = {
      subcategoriaId: Number(formValue.subcategoriaId),
      nombre: formValue.nombre,
      descripcion: formValue.descripcion || '',
      precioUnitario: Number(formValue.precioUnitario),
      estado: formValue.estado ?? true,
    };

    // Solo en modo edit incluimos el código
    if (this.mode === 'edit' && formValue.codigo) {
      productoData.codigo = formValue.codigo;
    }

    if (this.mode === 'edit' && this.producto) {
      // Modo edición: updateProducto retorna Observable<void>
      this.productoService
        .updateProducto(this.producto.productoId, productoData)
        .subscribe({
          next: () => {
            this.loading = false;
            this.showSuccess = true;
            this.productoSaved.emit(this.producto!);
            setTimeout(() => {
              if (this.showSuccess) this.cerrarModal();
            }, 2000);
          },
          error: (err: any) => {
            console.error('Error al guardar producto:', err);
            this.loading = false;
            this.error =
              err?.error?.message ||
              err.message ||
              'Error al guardar el producto';
          },
        });
    } else {
      // Modo creación: createProducto retorna Observable<Producto>
      this.productoService.createProducto(productoData).subscribe({
        next: (res: Producto) => {
          this.loading = false;
          this.showSuccess = true;
          this.productoSaved.emit(res);
          setTimeout(() => {
            if (this.showSuccess) this.cerrarModal();
          }, 2000);
        },
        error: (err: any) => {
          console.error('Error al guardar producto:', err);
          this.loading = false;
          this.error =
            err?.error?.message ||
            err.message ||
            'Error al guardar el producto';
        },
      });
    }
  }
}
