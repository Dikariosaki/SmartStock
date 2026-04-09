// En: src/app/features/auth/components/login/login.component.ts

// --- IMPORTACIONES ---
import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService, LoginCredentials } from '@core/services/auth.service';

// --- DECORADOR DEL COMPONENTE ---
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent implements OnInit {
  // Propiedades para almacenar los datos del formulario
  email: string = '';
  password: string = '';

  // Estados del componente
  isLoading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  // URL de retorno después del login
  private returnUrl: string = '/menu';
  currentYear = new Date().getFullYear();

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Obtener la URL de retorno de los query params
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/menu';

    // Si el usuario ya está autenticado, redirigir
    if (this.authService.isAuthenticated()) {
      this.router.navigate([this.returnUrl]);
    }

    // Inicializar dark mode si está guardado
    if (
      localStorage.getItem('theme') === 'dark' ||
      (!('theme' in localStorage) &&
        window.matchMedia('(prefers-color-scheme: dark)').matches)
    ) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }

  toggleDarkMode(): void {
    document.documentElement.classList.toggle('dark');
    if (document.documentElement.classList.contains('dark')) {
      localStorage.setItem('theme', 'dark');
    } else {
      localStorage.setItem('theme', 'light');
    }
  }

  // Este método se llamará cuando el formulario se envíe
  onSubmit(): void {
    // Limpiar mensajes previos
    this.errorMessage = '';
    this.successMessage = '';

    // Validar campos
    if (!this.email || !this.password) {
      this.errorMessage = 'Por favor, completa todos los campos.';
      return;
    }

    // Validar formato de email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.email)) {
      this.errorMessage = 'Por favor, ingresa un email válido.';
      return;
    }

    // Mostrar estado de carga
    this.isLoading = true;

    // Preparar credenciales
    const credentials: LoginCredentials = {
      email: this.email,
      password: this.password,
    };

    // Llamar al servicio de autenticación
    this.authService.login(credentials).subscribe({
      next: response => {
        this.isLoading = false;

        if (response.success) {
          // Login exitoso
          this.successMessage = '¡Login exitoso! Ingresando al sistema';
          console.log('Login exitoso:', response);
          console.log('Token generado:', response.token);

          // Esperar un momento para mostrar el mensaje de éxito antes de redirigir
          setTimeout(() => {
            this.router.navigate([this.returnUrl]);
          }, 1500);
        } else {
          // Login fallido
          this.errorMessage =
            response.message ||
            'Credenciales incorrectas. Verifica tu email y contraseña.';
        }
      },
      error: error => {
        this.isLoading = false;
        this.errorMessage = 'Error de conexión. Intenta de nuevo.';
        console.error('Error en login:', error);
      },
    });
  }

  // Método para limpiar el formulario
  clearForm(): void {
    this.email = '';
    this.password = '';
    this.errorMessage = '';
    this.successMessage = '';
  }
}
