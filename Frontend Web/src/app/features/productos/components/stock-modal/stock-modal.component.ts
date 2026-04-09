import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Producto } from '../../models/productos.models';
import { ProveedorService } from '../../../proveedores/services/proveedores.service';
import { ClienteService } from '../../../clientes/services/cliente.service';

interface Proveedor {
  proveedorId: number;
  nombre: string;
}

interface Cliente {
  clienteId: number;
  nombre: string;
}

@Component({
  selector: 'app-stock-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './stock-modal.component.html',
  styleUrls: ['./stock-modal.component.css'],
})
export class StockModalComponent implements OnInit {
  @Input() show = false;
  @Input() mode: 'entrada' | 'salida' = 'entrada';
  @Input() producto: Producto | null = null;

  @Output() stockConfirm = new EventEmitter<{
    cantidad: number;
    proveedorId?: number;
    clienteId?: number;
    lote?: string;
  }>();
  @Output() stockCancel = new EventEmitter<void>();

  cantidad: number = 1;
  selectedProveedorId?: number;
  selectedClienteId?: number;
  lote: string = '';

  proveedores: Proveedor[] = [];
  clientes: Cliente[] = [];
  loading = false;

  constructor(
    private proveedoresService: ProveedorService,
    private clientesService: ClienteService
  ) {}

  ngOnInit(): void {
    this.cargarProveedores();
    this.cargarClientes();
  }

  cargarProveedores(): void {
    this.loading = true;
    this.proveedoresService.getProveedores(1, 1000).subscribe({
      next: response => {
        this.proveedores = response.data.map((p: any) => ({
          proveedorId: p.proveedorId,
          nombre: p.nombre || `Proveedor #${p.proveedorId}`,
        }));
        this.loading = false;
      },
      error: () => {
        this.proveedores = [];
        this.loading = false;
      },
    });
  }

  cargarClientes(): void {
    this.loading = true;
    this.clientesService.getClientes({ pageNumber: 1, pageSize: 1000 }).subscribe({
      next: response => {
        this.clientes = response.data.map((c: any) => ({
          clienteId: c.clienteId || c.cliente_id,
          nombre: c.nombre || `Cliente #${c.clienteId || c.cliente_id}`,
        }));
        this.loading = false;
      },
      error: () => {
        this.clientes = [];
        this.loading = false;
      },
    });
  }

  get title(): string {
    return this.mode === 'entrada' ? 'Entrada Producto' : 'Salida Producto';
  }

  onConfirm(): void {
    if (this.cantidad > 0) {
      this.stockConfirm.emit({
        cantidad: this.cantidad,
        proveedorId:
          this.mode === 'entrada' ? this.selectedProveedorId : undefined,
        clienteId: this.mode === 'salida' ? this.selectedClienteId : undefined,
        lote: this.lote || undefined,
      });
      this.resetForm();
    }
  }

  onCancel(): void {
    this.stockCancel.emit();
    this.resetForm();
  }

  private resetForm(): void {
    this.cantidad = 1;
    this.selectedProveedorId = undefined;
    this.selectedClienteId = undefined;
    this.lote = '';
  }
}
