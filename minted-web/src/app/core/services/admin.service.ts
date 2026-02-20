import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
    JobExecution,
    JobScheduleConfig,
    DefaultCategory,
    DefaultAccountType,
    SystemSettingResponse
} from '../models/admin.model';
import { AdminUserResponse, CreateUserRequest, ResetPasswordRequest, ApiResponse } from '../models/user.model';

@Injectable({
    providedIn: 'root'
})
export class AdminService {
    private apiUrl = `${environment.apiUrl}/admin`;

    constructor(private http: HttpClient) { }

    // --- Jobs ---
    getJobs(page: number = 0, size: number = 20): Observable<any> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<any>(`${this.apiUrl}/jobs`, { params });
    }

    getJobExecution(id: number): Observable<JobExecution> {
        return this.http.get<JobExecution>(`${this.apiUrl}/jobs/${id}`);
    }

    triggerJob(jobName: string): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/jobs/${jobName}/trigger`, {});
    }

    // --- Schedules ---
    getSchedules(): Observable<JobScheduleConfig[]> {
        return this.http.get<JobScheduleConfig[]>(`${this.apiUrl}/schedules`);
    }

    updateSchedule(id: number, cronExpression: string, enabled: boolean): Observable<JobScheduleConfig> {
        return this.http.put<JobScheduleConfig>(`${this.apiUrl}/schedules/${id}`, { cronExpression, enabled });
    }

    // --- Default Categories ---
    getDefaultCategories(): Observable<DefaultCategory[]> {
        return this.http.get<DefaultCategory[]>(`${this.apiUrl}/defaults/categories`);
    }

    createDefaultCategory(category: DefaultCategory): Observable<DefaultCategory> {
        return this.http.post<DefaultCategory>(`${this.apiUrl}/defaults/categories`, category);
    }

    deleteDefaultCategory(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/defaults/categories/${id}`);
    }

    // --- Default Account Types ---
    getDefaultAccountTypes(): Observable<DefaultAccountType[]> {
        return this.http.get<DefaultAccountType[]>(`${this.apiUrl}/defaults/account-types`);
    }

    createDefaultAccountType(accountType: DefaultAccountType): Observable<DefaultAccountType> {
        return this.http.post<DefaultAccountType>(`${this.apiUrl}/defaults/account-types`, accountType);
    }

    deleteDefaultAccountType(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/defaults/account-types/${id}`);
    }

    // --- User Management ---
    getUsers(): Observable<AdminUserResponse[]> {
        return this.http.get<ApiResponse<AdminUserResponse[]>>(`${this.apiUrl}/users`).pipe(map(r => r.data!));
    }

    getUserById(id: number): Observable<AdminUserResponse> {
        return this.http.get<ApiResponse<AdminUserResponse>>(`${this.apiUrl}/users/${id}`).pipe(map(r => r.data!));
    }

    createUser(request: CreateUserRequest): Observable<AdminUserResponse> {
        return this.http.post<ApiResponse<AdminUserResponse>>(`${this.apiUrl}/users`, request).pipe(map(r => r.data!));
    }

    toggleUserActive(id: number): Observable<AdminUserResponse> {
        return this.http.put<ApiResponse<AdminUserResponse>>(`${this.apiUrl}/users/${id}/toggle`, {}).pipe(map(r => r.data!));
    }

    deleteUser(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/users/${id}`);
    }

    resetPassword(id: number, request: ResetPasswordRequest): Observable<void> {
        return this.http.put<void>(`${this.apiUrl}/users/${id}/reset-password`, request);
    }

    // --- System Settings ---
    getSetting(key: string): Observable<SystemSettingResponse> {
        return this.http.get<ApiResponse<SystemSettingResponse>>(`${this.apiUrl}/settings/${key}`).pipe(map(r => r.data!));
    }

    updateSetting(key: string, value: string): Observable<SystemSettingResponse> {
        return this.http.put<ApiResponse<SystemSettingResponse>>(`${this.apiUrl}/settings/${key}`, { value }).pipe(map(r => r.data!));
    }
}
