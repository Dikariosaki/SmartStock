import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-menu-reporte',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './menu-reporte.component.html',
  styleUrl: './menu-reporte.component.css',
})
export class MenuReporteComponent {
  toggleDarkMode() {
    document.documentElement.classList.toggle('dark');
  }
}
