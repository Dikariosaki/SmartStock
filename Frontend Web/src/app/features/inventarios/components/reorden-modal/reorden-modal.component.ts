import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormsModule,
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  FormGroup,
} from '@angular/forms';
import { Inventario } from '../../models/inventario.models';
import { ProveedorService } from '@features/proveedores/services/proveedores.service';
import { ProductoService } from '@features/productos/services/productos.service';
import { Producto } from '@features/productos/models/productos.models';
import { Proveedor } from '@features/proveedores/models/proveedor.models';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-reorden-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './reorden-modal.component.html',
  styleUrls: ['./reorden-modal.component.css'],
})
export class ReordenModalComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  @Input() show = false;
  @Input() inventario!: Inventario;
  @Output() success = new EventEmitter<{
    proveedor: Proveedor;
    cantidad: number;
    precioUnitario: number;
    total: number;
  }>();
  @Output() modalClosed = new EventEmitter<void>();

  proveedores: Proveedor[] = [];
  loading = false;
  error: string | null = null;
  precioUnitario = 0;
  showConfirm = false;
  selectedProveedor: Proveedor | null = null;

  form!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private proveedorService: ProveedorService,
    private productoService: ProductoService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      proveedorId: [null as number | null, [Validators.required]],
      cantidad: [1, [Validators.required, Validators.min(1)]],
      precio: [0, [Validators.required, Validators.min(0)]],
    });
    this.loadProveedores();
    this.loadPrecioProducto();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadProveedores(): void {
    this.loading = true;
    this.proveedorService
      .getProveedores(1, 50)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          this.proveedores = res.data;
          this.loading = false;
        },
        error: () => {
          this.error = 'Error al cargar proveedores';
          this.loading = false;
        },
      });
  }

  private loadPrecioProducto(): void {
    if (!this.inventario) return;
    this.productoService
      .getProductoById(this.inventario.productoId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (p: Producto) => {
          this.precioUnitario = p.precioUnitario;
        },
        error: () => {
          this.precioUnitario = 0;
        },
      });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.selectedProveedor =
      this.proveedores.find(
        p => p.proveedor_id === this.form.value.proveedorId!
      ) || null;
    this.showConfirm = true;
  }

  close(): void {
    this.modalClosed.emit();
  }

  confirmReorden(): void {
    if (!this.selectedProveedor) return;
    const cantidad = this.form.value.cantidad!;
    const total = cantidad * this.precioUnitario;
    this.success.emit({
      proveedor: this.selectedProveedor,
      cantidad,
      precioUnitario: this.precioUnitario,
      total,
    });
    this.showConfirm = false;
  }

  cancelConfirm(): void {
    this.showConfirm = false;
  }
}
