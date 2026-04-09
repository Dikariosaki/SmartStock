import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { SidebarService, SidebarItem } from './sidebar.service';
import { ProfileModalComponent } from '../../../../features/menu/components/profile-modal/profile-modal.component';
import { MenuService } from '../../../../features/menu/services/menu.service';
import { UserProfile } from '../../../../features/menu/models/menu.models';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, ProfileModalComponent],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
})
export class SidebarComponent implements OnInit {
  sidebarItems: SidebarItem[] = [];
  currentRoute: string = '';
  showProfileModal = false;
  userProfile: UserProfile | null = null;

  constructor(
    private sidebarService: SidebarService,
    private router: Router,
    private menuService: MenuService
  ) {}

  ngOnInit(): void {
    this.sidebarItems = this.sidebarService.getSidebarItems();
    this.currentRoute = this.router.url;

    // Escuchar cambios de ruta
    this.router.events.subscribe(() => {
      this.currentRoute = this.router.url;
    });
  }

  onNavigate(route: string): void {
    this.sidebarService.navigateTo(route);
  }

  isActiveRoute(route: string): boolean {
    return this.sidebarService.isCurrentRoute(route);
  }

  onProfileClick(): void {
    this.loadUserProfile();
    this.showProfileModal = true;
  }

  onProfileModalClose(): void {
    this.showProfileModal = false;
  }

  onProfileUpdate(updatedProfile: Partial<UserProfile>): void {
    this.menuService.updateUserProfile(updatedProfile).subscribe({
      next: profile => {
        this.userProfile = profile;
        this.showProfileModal = false;
      },
      error: error => {
        console.error('Error updating profile:', error);
      },
    });
  }

  private loadUserProfile(): void {
    this.menuService.getUserProfile().subscribe({
      next: profile => {
        this.userProfile = profile;
      },
      error: error => {
        console.error('Error loading user profile:', error);
      },
    });
  }

  onHomeClick(): void {
    this.sidebarService.navigateTo(this.sidebarService.getHomeRoute());
  }

  toggleDarkMode(): void {
    document.documentElement.classList.toggle('dark');
  }
}
