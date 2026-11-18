import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserAdminService } from '../../../../core/services/user-admin.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { SystemUser, CreateUserPayload, UpdateUserPayload } from '../../../../core/models/user.model';

@Component({
  selector: 'app-users-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './users-page.component.html',
  styleUrls: ['./users-page.component.css']
})
export class UsersPageComponent implements OnInit {
  private userService = inject(UserAdminService);
  private notificationService = inject(NotificationService);

  users = signal<SystemUser[]>([]);
  isLoading = signal<boolean>(false);
  searchTerm = signal<string>('');
  selectedRoleFilter = signal<string>('ALL');
  selectedStatusFilter = signal<string>('ALL');

  // Modals state
  isCreateModalOpen = signal<boolean>(false);
  isEditModalOpen = signal<boolean>(false);
  isDeleteModalOpen = signal<boolean>(false);
  selectedUser = signal<SystemUser | null>(null);

  // Create form model
  createForm: CreateUserPayload = {
    email: '',
    password: '',
    fullName: '',
    role: 'ADMIN',
    status: 'ACTIVE',
    avatarUrl: ''
  };

  // Edit form model
  editForm: UpdateUserPayload = {
    email: '',
    password: '',
    fullName: '',
    role: 'ADMIN',
    status: 'ACTIVE',
    avatarUrl: ''
  };

  showPassword = signal<boolean>(false);

  // Filtered users
  filteredUsers = computed(() => {
    let list = this.users();
    const query = this.searchTerm().toLowerCase().trim();
    const roleFilter = this.selectedRoleFilter();
    const statusFilter = this.selectedStatusFilter();

    if (query) {
      list = list.filter(u => 
        (u.fullName && u.fullName.toLowerCase().includes(query)) ||
        (u.email && u.email.toLowerCase().includes(query))
      );
    }

    if (roleFilter !== 'ALL') {
      list = list.filter(u => u.role === roleFilter);
    }

    if (statusFilter !== 'ALL') {
      list = list.filter(u => u.status === statusFilter);
    }

    return list;
  });

  // Metrics (Only 3 roles: Super Admin, Admin, User)
  totalUsers = computed(() => this.users().length);
  totalSuperAdmins = computed(() => this.users().filter(u => u.role === 'SUPER_ADMIN').length);
  totalAdmins = computed(() => this.users().filter(u => u.role === 'ADMIN').length);
  totalStandardUsers = computed(() => this.users().filter(u => u.role === 'USER' || u.role === 'CUSTOMER').length);

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading.set(true);
    this.userService.getUsers().subscribe({
      next: (data) => {
        this.users.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        // Fallback demo data if offline
        if (this.users().length === 0) {
          this.users.set([
            {
              id: 'usr_superadmin_01',
              email: 'admin@gmail.com',
              fullName: 'Super Administrator',
              role: 'SUPER_ADMIN',
              status: 'ACTIVE',
              avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&auto=format&fit=crop&q=80',
              createdAt: '2026-11-17T12:00:00'
            },
            {
              id: 'usr_manager_01',
              email: 'manager@e-comm.local',
              fullName: 'Sarah Administrator',
              role: 'ADMIN',
              status: 'ACTIVE',
              avatarUrl: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&auto=format&fit=crop&q=80',
              createdAt: '2026-11-17T12:30:00'
            },
            {
              id: 'usr_customer_01',
              email: 'customer@e-comm.local',
              fullName: 'Alex Customer',
              role: 'USER',
              status: 'ACTIVE',
              avatarUrl: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop&q=80',
              createdAt: '2026-11-17T13:00:00'
            }
          ]);
        }
      }
    });
  }

  openCreateModal(): void {
    this.createForm = {
      email: '',
      password: '',
      fullName: '',
      role: 'ADMIN',
      status: 'ACTIVE',
      avatarUrl: ''
    };
    this.showPassword.set(false);
    this.isCreateModalOpen.set(true);
  }

  closeCreateModal(): void {
    this.isCreateModalOpen.set(false);
  }

  submitCreate(): void {
    if (!this.createForm.email || !this.createForm.fullName || !this.createForm.password) {
      this.notificationService.warning('Please fill in all required fields (Name, Email, Password).');
      return;
    }

    this.isLoading.set(true);
    this.userService.createUser(this.createForm).subscribe({
      next: (created) => {
        this.isLoading.set(false);
        this.notificationService.success(`User '${created.fullName}' created successfully!`);
        this.closeCreateModal();
        this.loadUsers();
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  openEditModal(user: SystemUser): void {
    this.selectedUser.set(user);
    this.editForm = {
      email: user.email,
      fullName: user.fullName,
      role: user.role,
      status: user.status,
      avatarUrl: user.avatarUrl || '',
      password: ''
    };
    this.isEditModalOpen.set(true);
  }

  closeEditModal(): void {
    this.isEditModalOpen.set(false);
    this.selectedUser.set(null);
  }

  submitEdit(): void {
    const user = this.selectedUser();
    if (!user) return;

    this.isLoading.set(true);
    this.userService.updateUser(user.id, this.editForm).subscribe({
      next: (updated) => {
        this.isLoading.set(false);
        this.notificationService.success(`User '${updated.fullName}' updated successfully!`);
        this.closeEditModal();
        this.loadUsers();
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  confirmDelete(user: SystemUser): void {
    this.selectedUser.set(user);
    this.isDeleteModalOpen.set(true);
  }

  closeDeleteModal(): void {
    this.isDeleteModalOpen.set(false);
    this.selectedUser.set(null);
  }

  executeDelete(): void {
    const user = this.selectedUser();
    if (!user) return;

    if (user.email === 'admin@gmail.com') {
      this.notificationService.warning('Root Super Administrator account cannot be deleted.');
      this.closeDeleteModal();
      return;
    }

    this.isLoading.set(true);
    this.userService.deleteUser(user.id).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.notificationService.success(`User '${user.fullName}' deleted.`);
        this.closeDeleteModal();
        this.loadUsers();
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  toggleShowPassword(): void {
    this.showPassword.update(v => !v);
  }

  getRoleBadgeClass(role: string): string {
    switch (role) {
      case 'SUPER_ADMIN': return 'badge-role-superadmin';
      case 'ADMIN': return 'badge-role-admin';
      case 'USER':
      case 'CUSTOMER': return 'badge-role-user';
      default: return 'badge-role-default';
    }
  }

  getRoleLabel(role: string): string {
    switch (role) {
      case 'SUPER_ADMIN': return 'Super Admin';
      case 'ADMIN': return 'Admin';
      case 'USER':
      case 'CUSTOMER': return 'User';
      default: return role;
    }
  }
}
