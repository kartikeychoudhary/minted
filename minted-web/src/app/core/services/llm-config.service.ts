import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  LlmConfig,
  LlmConfigRequest,
  LlmModel,
  LlmModelRequest,
  MerchantMapping,
  MerchantMappingRequest
} from '../models/llm-config.model';

@Injectable({ providedIn: 'root' })
export class LlmConfigService {
  private apiUrl = `${environment.apiUrl}/llm-config`;
  private adminApiUrl = `${environment.apiUrl}/admin/llm-models`;

  constructor(private http: HttpClient) {}

  // --- User Config ---
  getConfig(): Observable<LlmConfig> {
    return this.http.get<{ success: boolean; data: LlmConfig }>(this.apiUrl)
      .pipe(map(r => r.data));
  }

  saveConfig(request: LlmConfigRequest): Observable<LlmConfig> {
    return this.http.put<{ success: boolean; data: LlmConfig }>(this.apiUrl, request)
      .pipe(map(r => r.data));
  }

  getAvailableModels(): Observable<LlmModel[]> {
    return this.http.get<{ success: boolean; data: LlmModel[] }>(`${this.apiUrl}/models`)
      .pipe(map(r => r.data));
  }

  // --- Merchant Mappings ---
  getMappings(): Observable<MerchantMapping[]> {
    return this.http.get<{ success: boolean; data: MerchantMapping[] }>(`${this.apiUrl}/mappings`)
      .pipe(map(r => r.data));
  }

  createMapping(request: MerchantMappingRequest): Observable<MerchantMapping> {
    return this.http.post<{ success: boolean; data: MerchantMapping }>(`${this.apiUrl}/mappings`, request)
      .pipe(map(r => r.data));
  }

  updateMapping(id: number, request: MerchantMappingRequest): Observable<MerchantMapping> {
    return this.http.put<{ success: boolean; data: MerchantMapping }>(`${this.apiUrl}/mappings/${id}`, request)
      .pipe(map(r => r.data));
  }

  deleteMapping(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/mappings/${id}`);
  }

  // --- Admin: LLM Models ---
  getAllModels(): Observable<LlmModel[]> {
    return this.http.get<{ success: boolean; data: LlmModel[] }>(this.adminApiUrl)
      .pipe(map(r => r.data));
  }

  createModel(model: LlmModelRequest): Observable<LlmModel> {
    return this.http.post<{ success: boolean; data: LlmModel }>(this.adminApiUrl, model)
      .pipe(map(r => r.data));
  }

  updateModel(id: number, model: LlmModelRequest): Observable<LlmModel> {
    return this.http.put<{ success: boolean; data: LlmModel }>(`${this.adminApiUrl}/${id}`, model)
      .pipe(map(r => r.data));
  }

  deleteModel(id: number): Observable<void> {
    return this.http.delete<void>(`${this.adminApiUrl}/${id}`);
  }
}
