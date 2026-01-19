import { Component, Input, Output, EventEmitter, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserRoleService } from '../../../core/services/user-role.service';
import { Role } from '../../../core/models/user-role.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../ui/button/button.component';
import { BadgeComponent } from '../ui/badge/badge.component';

@Component({
  selector: 'app-role-picker',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe, ButtonComponent, BadgeComponent],
  templateUrl: './role-picker.component.html'
})
export class RolePickerComponent implements OnInit {
  private readonly userRoleService = inject(UserRoleService);

  @Input() assignedRoleIds: string[] = [];
  @Input() disabled: boolean = false;
  @Output() roleSelected = new EventEmitter<Role>();
  @Output() roleRemoved = new EventEmitter<string>();

  readonly availableRoles = signal<Role[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly selectedRoleId = signal<string>('');

  ngOnInit(): void {
    this.loadAvailableRoles();
  }

  loadAvailableRoles(): void {
    this.loading.set(true);
    this.error.set('');

    this.userRoleService.getAvailableRoles().subscribe({
      next: (roles) => {
        const roleList = Array.isArray(roles)
          ? roles
          : ((roles as { content?: Role[]; items?: Role[]; data?: Role[] }).content ||
              (roles as { items?: Role[] }).items ||
              (roles as { data?: Role[] }).data ||
              []);
        this.availableRoles.set(roleList);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading roles:', err);
        let errorMessage = 'Failed to load roles';
        
        if (err?.details && Array.isArray(err.details) && err.details.length > 0) {
          errorMessage = err.details[0];
        } else if (err?.message) {
          errorMessage = err.message;
        }
        
        this.error.set(errorMessage);
        this.loading.set(false);
      }
    });
  }

  getUnassignedRoles(): Role[] {
    return this.availableRoles().filter(role => !this.assignedRoleIds.includes(role.id));
  }

  onAssignRole(): void {
    const roleId = this.selectedRoleId();
    if (!roleId) return;

    const role = this.availableRoles().find(r => r.id === roleId);
    if (role) {
      this.roleSelected.emit(role);
      this.selectedRoleId.set('');
    }
  }

  onRemoveRole(roleId: string): void {
    this.roleRemoved.emit(roleId);
  }

  isAssigned(roleId: string): boolean {
    return this.assignedRoleIds.includes(roleId);
  }

  getRoleById(roleId: string): Role | undefined {
    return this.availableRoles().find(r => r.id === roleId);
  }
}
