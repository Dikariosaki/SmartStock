import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MovimientosService } from '../../services/movimientos.service';
import { Movimiento } from '../../models/movimiento.models';
import { ActivatedRoute } from '@angular/router';
import { ReportesService } from '../../services/reportes.service';
import { ReporteResumen } from '../../models/reportes-evidencia.models';
import { SidebarComponent } from '../../../../shared/ui/layout/sidebar/sidebar.component';
import { BellNotificationComponent } from '@shared/ui/notifications/bell-notification.component';
import { PermissionService } from '@core/auth/permission.service';

@Component({
  selector: 'app-reportes-movimientos',
  standalone: true,
  imports: [
    CommonModule,
    SidebarComponent,
    FormsModule,
    BellNotificationComponent,
  ],
  templateUrl: './reportes-movimientos.component.html',
  styleUrls: ['./reportes-movimientos.component.css'],
})
export class ReportesMovimientosComponent implements OnInit {
  tipo: string = 'all';
  selectedTipo: string = 'all';
  allMovimientos: Movimiento[] = [];
  filteredMovimientos: Movimiento[] = [];

  fechaInicio: string = '';
  fechaFin: string = '';
  searchTerm: string = '';
  limit: number = 10;

  // Paginación
  currentPage: number = 1;
  totalPages: number = 1;
  totalItems: number = 0;

  // Estados de carga
  isLoading: boolean = false;
  error: string | null = null;
  showFilterMenu: boolean = false;
  evidenceReports: ReporteResumen[] = [];
  evidenceLoading: boolean = false;
  evidenceError: string | null = null;
  evidenceViewerOpen: boolean = false;
  evidenceViewerImages: string[] = [];
  evidenceViewerIndex: number = 0;

  Math = Math;

  constructor(
    private movimientosService: MovimientosService,
    private route: ActivatedRoute,
    private reportesService: ReportesService,
    private permissionService: PermissionService
  ) {}

  get canDownloadReportes(): boolean {
    return this.permissionService.canAction('reportes', 'download');
  }

  toggleFilterMenu(): void {
    this.showFilterMenu = !this.showFilterMenu;
  }

  toggleDarkMode(): void {
    document.documentElement.classList.toggle('dark');
  }

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.tipo = data['tipo'] || 'all';
      this.selectedTipo = this.tipo;
      this.cargarMovimientos();
      this.cargarReportesConEvidencia();
    });
  }

  filtrarPorTipo(tipo: string): void {
    this.selectedTipo = tipo;
    this.currentPage = 1;
    this.cargarMovimientos();
  }

  cargarMovimientos(): void {
    this.isLoading = true;
    this.error = null;

    const filters: any = {
      pageNumber: this.currentPage,
      pageSize: this.limit,
    };

    if (this.selectedTipo !== 'all') {
      filters.tipo = this.selectedTipo;
    }
    
    if (this.searchTerm) {
        filters.search = this.searchTerm;
    }
    if (this.fechaInicio) {
        filters.fechaInicio = this.fechaInicio;
    }
    if (this.fechaFin) {
        filters.fechaFin = this.fechaFin;
    }

    this.movimientosService.getMovimientos(filters).subscribe({
      next: (data: any) => {
        // data es PagedResponse
        this.allMovimientos = data.data;
        this.filteredMovimientos = data.data;
        this.totalPages = data.totalPages;
        this.totalItems = data.totalCount;
        this.isLoading = false;
      },
      error: err => {
        console.error('Error al cargar movimientos:', err);
        this.error =
          'Error al cargar los movimientos. Por favor, intenta de nuevo.';
        this.isLoading = false;
        this.allMovimientos = [];
        this.filteredMovimientos = [];
      },
    });
  }

  cargarReportesConEvidencia(): void {
    this.evidenceLoading = true;
    this.evidenceError = null;

    this.reportesService.getReportes(1, 6).subscribe({
      next: reports => {
        this.evidenceReports = reports;
        this.evidenceLoading = false;
      },
      error: err => {
        console.error('Error al cargar reportes con evidencia:', err);
        this.evidenceError =
          'No se pudieron cargar los reportes con evidencia en este momento.';
        this.evidenceLoading = false;
        this.evidenceReports = [];
      },
    });
  }

  aplicarFiltros(): void {
    // Los filtros ahora se aplican en el backend al llamar a cargarMovimientos
    this.currentPage = 1;
    this.cargarMovimientos();
  }

  updatePagination(): void {
    // Deprecated: Backend provides totalPages
    // this.totalPages = Math.ceil(this.filteredMovimientos.length / this.limit) || 1;
  }

  get paginatedMovimientos(): Movimiento[] {
    // Como ya traemos la página específica del backend, no necesitamos hacer slice
    return this.filteredMovimientos;
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.cargarMovimientos();
    }
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.cargarMovimientos();
    }
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.cargarMovimientos();
    }
  }

  descargarReporte(): void {
    if (
      !this.permissionService.guardAction(
        'reportes',
        'download',
        'No tienes permisos para descargar reportes.'
      )
    ) {
      return;
    }

    const header = [
      'ID',
      'Producto',
      'Cantidad',
      'Tipo',
      'Fecha',
      'Lote',
      'Asignado A',
    ];
    
    if (this.selectedTipo === 'entrada' || this.selectedTipo === 'all') {
      header.push('Proveedor');
    }
    if (this.selectedTipo === 'salida' || this.selectedTipo === 'all') {
      header.push('Cliente');
    }

    const csvContent = [
      header.join(','),
      ...this.filteredMovimientos.map(m => {
        const row = [
          m.id,
          `"${m.productoNombre}"`,
          m.cantidad,
          m.tipo,
          new Date(m.fecha).toLocaleString(),
          m.lote || '',
          m.asignadoA || '',
        ];
        
        if (this.selectedTipo === 'entrada' || this.selectedTipo === 'all') {
          row.push(m.proveedor || '');
        }
        if (this.selectedTipo === 'salida' || this.selectedTipo === 'all') {
          row.push(m.cliente || '');
        }
        
        return row.join(',');
      }),
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `reporte-${this.selectedTipo}s-${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  getFillerRows(): number[] {
    const currentItems = this.paginatedMovimientos.length;
    const fillerCount = Math.max(0, this.limit - currentItems);
    return Array(fillerCount)
      .fill(0)
      .map((_, i) => i);
  }

  getPageNumbers(): number[] {
    const pages = [];
    const maxVisiblePages = 5;
    
    if (this.totalPages <= maxVisiblePages) {
      for (let i = 1; i <= this.totalPages; i++) {
        pages.push(i);
      }
    } else {
      if (this.currentPage <= 3) {
        for (let i = 1; i <= 5; i++) {
          pages.push(i);
        }
      } else if (this.currentPage >= this.totalPages - 2) {
        for (let i = this.totalPages - 4; i <= this.totalPages; i++) {
          pages.push(i);
        }
      } else {
        for (let i = this.currentPage - 2; i <= this.currentPage + 2; i++) {
          pages.push(i);
        }
      }
    }
    return pages;
  }

  onEvidenceImageError(event: Event): void {
    const target = event.target as HTMLImageElement | null;
    if (target) {
      target.style.display = 'none';
    }
  }

  openEvidenceViewer(images: string[], index: number): void {
    if (!images.length) {
      return;
    }

    this.evidenceViewerImages = [...images];
    this.evidenceViewerIndex = Math.min(Math.max(index, 0), images.length - 1);
    this.evidenceViewerOpen = true;
  }

  closeEvidenceViewer(): void {
    this.evidenceViewerOpen = false;
    this.evidenceViewerImages = [];
    this.evidenceViewerIndex = 0;
  }

  showPreviousEvidenceImage(): void {
    if (!this.evidenceViewerImages.length) {
      return;
    }

    this.evidenceViewerIndex =
      (this.evidenceViewerIndex - 1 + this.evidenceViewerImages.length) %
      this.evidenceViewerImages.length;
  }

  showNextEvidenceImage(): void {
    if (!this.evidenceViewerImages.length) {
      return;
    }

    this.evidenceViewerIndex =
      (this.evidenceViewerIndex + 1) % this.evidenceViewerImages.length;
  }

  get currentEvidenceViewerImage(): string | null {
    return this.evidenceViewerImages[this.evidenceViewerIndex] ?? null;
  }

  @HostListener('document:keydown', ['$event'])
  handleViewerKeyboard(event: KeyboardEvent): void {
    if (!this.evidenceViewerOpen) {
      return;
    }

    if (event.key === 'Escape') {
      event.preventDefault();
      this.closeEvidenceViewer();
      return;
    }

    if (event.key === 'ArrowLeft') {
      event.preventDefault();
      this.showPreviousEvidenceImage();
      return;
    }

    if (event.key === 'ArrowRight') {
      event.preventDefault();
      this.showNextEvidenceImage();
    }
  }
}
