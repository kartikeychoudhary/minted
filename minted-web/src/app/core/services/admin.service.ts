import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
    JobExecution,
    JobScheduleConfig,
    DefaultCategory,
    DefaultAccountType
} from '../models/admin.model';

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
}
