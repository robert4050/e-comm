import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SystemUser, CreateUserPayload, UpdateUserPayload } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class UserAdminService {
  private http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/users';

  getUsers(): Observable<SystemUser[]> {
    return this.http.get<SystemUser[]>(this.baseUrl);
  }

  getUser(id: string): Observable<SystemUser> {
    return this.http.get<SystemUser>(`${this.baseUrl}/${id}`);
  }

  createUser(payload: CreateUserPayload): Observable<SystemUser> {
    return this.http.post<SystemUser>(this.baseUrl, payload);
  }

  updateUser(id: string, payload: UpdateUserPayload): Observable<SystemUser> {
    return this.http.put<SystemUser>(`${this.baseUrl}/${id}`, payload);
  }

  deleteUser(id: string): Observable<{ deleted: boolean; id: string; message: string }> {
    return this.http.delete<{ deleted: boolean; id: string; message: string }>(`${this.baseUrl}/${id}`);
  }
}
