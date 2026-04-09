import {
  Component,
  EventEmitter,
  Input,
  Output,
  OnInit,
  OnDestroy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Producto } from '../../models/productos.models'; // Revisa si tu archivo es singular o plural

@Component({
  selector: 'app-averia-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './averia-modal.component.html',
  styleUrls: ['./averia-modal.component.css'],
})
export class AveriaModalComponent implements OnInit, OnDestroy {
  @Input() show = false;
  @Input() producto: Producto | null = null;

  // Emitimos un objeto con todos los datos del reporte
  @Output() confirmAveria = new EventEmitter<{
    tipo: string;
    cantidad: number;
    descripcion: string;
  }>();
  @Output() cancelAveria = new EventEmitter<void>();

  tipoReporte: string = 'Entrada'; // Valor por defecto
  cantidad: number = 1;
  descripcion: string = '';

  ngOnInit(): void {
    document.body.style.overflow = 'hidden';
  }
  ngOnDestroy(): void {
    document.body.style.overflow = 'auto';
  }

  onConfirm(): void {
    if (this.cantidad > 0 && this.descripcion.trim()) {
      this.confirmAveria.emit({
        tipo: this.tipoReporte,
        cantidad: this.cantidad,
        descripcion: this.descripcion,
      });
      this.resetForm();
    } else {
      alert('Por favor complete la cantidad y la descripción');
    }
  }

  onCancel(): void {
    this.cancelAveria.emit();
    this.resetForm();
  }

  resetForm(): void {
    this.cantidad = 1;
    this.descripcion = '';
    this.tipoReporte = 'Entrada';
  }
}
