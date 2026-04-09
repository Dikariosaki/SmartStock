import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { MenuService } from '../../services/menu.service';
import { MenuItem, UserProfile } from '../../models/menu.models';
import { ProfileModalComponent } from '../profile-modal/profile-modal.component';

@Component({
  selector: 'app-menu-dashboard',
  standalone: true,
  imports: [CommonModule, ProfileModalComponent],
  templateUrl: './menu-dashboard.component.html',
  styleUrl: './menu-dashboard.component.css',
})
export class MenuDashboardComponent implements OnInit, OnDestroy {
  @ViewChild(ProfileModalComponent) profileModal?: ProfileModalComponent;

  menuItems: MenuItem[] = [];
  userProfile: UserProfile | null = null;
  showProfileModal = false;
  currentYear = new Date().getFullYear();
  private destroy$ = new Subject<void>();

  constructor(
    private menuService: MenuService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadMenuItems();
    this.loadUserProfile();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadMenuItems(): void {
    this.menuService
      .getMenuItems()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: items => {
          this.menuItems = items;
        },
        error: error => {
          console.error('Error loading menu items:', error);
        },
      });
  }

  private loadUserProfile(): void {
    this.menuService
      .getUserProfile()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: profile => {
          this.userProfile = profile;
        },
        error: error => {
          console.error('Error loading user profile:', error);
        },
      });
  }

  onMenuItemClick(item: MenuItem): void {
    if (item.route) {
      this.router.navigate([item.route]);
    }
  }

  onProfileIconClick(): void {
    this.showProfileModal = true;
  }

  onProfileModalClose(): void {
    this.showProfileModal = false;
  }

  onProfileUpdate(updatedProfile: Partial<UserProfile>): void {
    this.menuService
      .updateUserProfile(updatedProfile)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: profile => {
          this.userProfile = profile;
          this.showProfileModal = false;
        },
        error: error => {
          console.error('Error updating profile:', error);
          const message = error.error?.message || error.message || 'Error al actualizar el perfil';
          
          if (this.profileModal) {
            this.profileModal.setErrorMessage(message);
          }
        },
      });
  }

  toggleDarkMode(): void {
    document.documentElement.classList.toggle('dark');
  }
}
