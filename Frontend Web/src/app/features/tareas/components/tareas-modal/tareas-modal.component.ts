import { Component, EventEmitter, Input, OnInit, Output, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
  FormsModule,
} from '@angular/forms';
import {
  Subject,
  debounceTime,
  distinctUntilChanged,
  takeUntil,
} from 'rxjs';

import { TareaService } from '../../services/tareas.service';
import { UsuarioService } from '@features/usuarios/services/usuarios.service';
import { Usuario } from '@features/usuarios/models/usuario.models';
import {
  Tarea,
  CreateTareaRequest,
  UpdateTareaRequest,
} from '../../models/tarea.models';
import { PermissionService } from '@core/auth/permission.service';

@Component({
  selector: 'app-tareas-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './tareas-modal.component.html',
  styleUrls: ['./tareas-modal.component.css'],
})
export class TareasModalComponent implements OnInit, OnDestroy {
  @Input() tarea: Tarea | null = null;
  @Input() mode: 'create' | 'edit' | 'view' = 'create';
  @Output() success = new EventEmitter<void>();
  @Output() modalClosed = new EventEmitter<void>();

  private destroy$ = new Subject<void>();
  form!: FormGroup;
  loading = false;
  error: string | null = null;
  showCloseConfirm = false;

  // Usuario Dropdown
  usuarios: Usuario[] = [];
  filteredUsuarios: Usuario[] = [];
  usuarioSearchTerm = '';
  showUsuarioDropdown = false;
  usuarioLoading = false;
  private usuarioSearch$ = new Subject<string>();

  constructor(
    private fb: FormBuilder,
    private tareaService: TareaService,
    private usuarioService: UsuarioService,
    private permissionService: PermissionService
  ) {}

  canSubmitByPermission(): boolean {
    if (this.mode === 'create') {
      return this.permissionService.canAction('tareas', 'create');
    }

    if (this.mode === 'edit') {
      return this.permissionService.canAction('tareas', 'edit');
    }

    return true;
  }

  ngOnInit(): void {
    this.initForm();
    this.cargarUsuarios();
    
    // Setup local filtering
    this.usuarioSearch$
      .pipe(
        debounceTime(250),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe((term) => {
        const t = (term || '').toLowerCase();
        this.filteredUsuarios = t
          ? this.usuarios.filter(u => 
              (u.nombre || '').toLowerCase().includes(t) || 
              (u.correo || '').toLowerCase().includes(t) ||
              (u.identificacion || '').toLowerCase().includes(t)
            )
          : this.usuarios;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  initForm(): void {
    const isViewMode = this.mode === 'view';

    this.form = this.fb.group({
      titulo: [
        { value: this.tarea?.titulo || '', disabled: isViewMode },
        [Validators.required, Validators.maxLength(200)],
      ],
      descripcion: [
        { value: this.tarea?.descripcion || '', disabled: isViewMode },
        [Validators.maxLength(500)],
      ],
      asignadoA: [
        { value: this.tarea?.asignadoA || null, disabled: isViewMode },
      ],
      fechaFin: [
        {
          value: this.tarea?.fechaFin
            ? this.formatDateForInput(this.tarea.fechaFin)
            : '',
          disabled: isViewMode,
        },
      ],
      estado: [{ value: this.tarea?.estado ?? true, disabled: isViewMode }],
    });
  }

  formatDateForInput(date: Date): string {
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  get title(): string {
    switch (this.mode) {
      case 'create':
        return 'Nueva Tarea';
      case 'edit':
        return 'Editar Tarea';
      case 'view':
        return 'Detalles de Tarea';
      default:
        return '';
    }
  }

  get submitButtonText(): string {
    return this.mode === 'create' ? 'Guardar' : 'Actualizar';
  }

  onSubmit(): void {
    if (this.mode === 'create' || this.mode === 'edit') {
      const action = this.mode === 'edit' ? 'edit' : 'create';
      const deniedMessage =
        this.mode === 'edit'
          ? 'No tienes permisos para editar tareas.'
          : 'No tienes permisos para crear tareas.';

      if (!this.permissionService.guardAction('tareas', action, deniedMessage)) {
        return;
      }
    }

    if (this.form.invalid || this.mode === 'view') return;

    this.loading = true;
    this.error = null;

    const formValue = this.form.getRawValue();
    const request = {
      titulo: formValue.titulo,
      descripcion: formValue.descripcion,
      asignadoA: formValue.asignadoA,
      fechaFin: formValue.fechaFin || null,
      estado: formValue.estado
    };

    const operation = this.mode === 'create'
      ? this.tareaService.createTarea(request as CreateTareaRequest)
      : this.tareaService.updateTarea(this.tarea!.tareaId, request as UpdateTareaRequest);

    (operation as any).subscribe({
      next: () => {
        this.success.emit();
        this.loading = false;
      },
      error: (err: any) => {
        this.error = typeof err === 'string' ? err : 'Error al guardar la tarea';
        this.loading = false;
      }
    });
  }

  // Métodos para el dropdown de usuarios
  private cargarUsuarios(): void {
    this.usuarioLoading = true;
    // Cargamos una cantidad razonable de usuarios para el dropdown
    // Idealmente el backend debería soportar búsqueda, pero por ahora filtramos localmente
    this.usuarioService.getUsuarios(1, 100).subscribe({
      next: (response) => {
        // Manejar PagedResponse o array directo
        let data: Usuario[] = [];
        if (Array.isArray(response)) {
            data = response;
        } else if (response && response.data) {
            data = response.data;
        }
        
        // Filtrar solo usuarios activos si es necesario
        this.usuarios = data.filter(u => u.activo);
        this.filteredUsuarios = this.usuarios;
        this.usuarioLoading = false;
      },
      error: (err) => {
        console.error('Error al cargar usuarios:', err);
        this.usuarioLoading = false;
      }
    });
  }

  toggleUsuarioDropdown(): void {
    if (this.mode === 'view') return;
    this.showUsuarioDropdown = !this.showUsuarioDropdown;
  }

  closeUsuarioDropdown(): void {
    this.showUsuarioDropdown = false;
  }

  filterUsuarios(): void {
    this.usuarioSearch$.next(this.usuarioSearchTerm);
    this.showUsuarioDropdown = true;
  }

  onSelectUsuario(usuario: Usuario): void {
    this.form.get('asignadoA')?.setValue(usuario.usuario_id);
    this.closeUsuarioDropdown();
  }

  getSelectedUsuarioText(): string {
    const id = this.form.get('asignadoA')?.value;
    if (!id) return 'Seleccione un usuario';
    
    // Si tenemos el usuario en la lista cargada
    const usuario = this.usuarios.find(u => u.usuario_id === id);
    if (usuario) return usuario.nombre;
    
    // Si estamos editando y el usuario no está en la lista (ej. paginación), 
    // intentamos usar el nombre que viene en la tarea (si existe) o el ID
    if (this.tarea && this.tarea.asignadoA === id && this.tarea.usuarioNombre) {
      return this.tarea.usuarioNombre;
    }
    
    return `Usuario ID: ${id}`;
  }

  onBackdropClick(event: any): void {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.onCancel();
    }
  }

  onCancel(): void {
    if (this.form.dirty && !this.showCloseConfirm && this.mode !== 'view') {
      this.showCloseConfirm = true;
    } else {
      this.modalClosed.emit();
    }
  }

  confirmClose(): void {
    this.modalClosed.emit();
  }

  cancelClose(): void {
    this.showCloseConfirm = false;
  }

  getFieldError(field: string): string | null {
    const control = this.form.get(field);
    if (control?.invalid && control?.touched) {
      if (control.errors?.['required']) return 'Este campo es obligatorio';
      if (control.errors?.['maxlength'])
        return `Máximo ${control.errors?.['maxlength'].requiredLength} caracteres`;
    }
    return null;
  }
}
