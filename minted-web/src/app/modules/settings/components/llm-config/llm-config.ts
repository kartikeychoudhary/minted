import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { LlmConfigService } from '../../../../core/services/llm-config.service';
import { LlmConfig, LlmModel } from '../../../../core/models/llm-config.model';

@Component({
  selector: 'app-llm-config',
  standalone: false,
  templateUrl: './llm-config.html',
  styleUrl: './llm-config.scss'
})
export class LlmConfigComponent implements OnInit {
  config: LlmConfig | null = null;
  models: LlmModel[] = [];
  modelOptions: { label: string; value: number }[] = [];
  configForm: FormGroup;
  loading = true;
  saving = false;

  constructor(
    private fb: FormBuilder,
    private llmConfigService: LlmConfigService,
    private messageService: MessageService,
    private cdr: ChangeDetectorRef
  ) {
    this.configForm = this.fb.group({
      apiKey: [''],
      modelId: [null]
    });
  }

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.llmConfigService.getAvailableModels().subscribe({
      next: (models) => {
        this.models = models;
        this.modelOptions = models.map(m => ({ label: m.name, value: m.id }));
        this.loadConfig();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  loadConfig(): void {
    this.llmConfigService.getConfig().subscribe({
      next: (config) => {
        this.config = config;
        this.configForm.patchValue({
          modelId: config.selectedModel?.id || null
        });
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  saveConfig(): void {
    this.saving = true;
    const formVal = this.configForm.value;
    const request: any = {};
    if (formVal.apiKey && formVal.apiKey.trim()) {
      request.apiKey = formVal.apiKey.trim();
    }
    if (formVal.modelId) {
      request.modelId = formVal.modelId;
    }

    this.llmConfigService.saveConfig(request).subscribe({
      next: (config) => {
        this.config = config;
        this.saving = false;
        this.configForm.patchValue({ apiKey: '' });
        this.messageService.add({ severity: 'success', summary: 'Saved', detail: 'LLM configuration updated.' });
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.saving = false;
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || 'Failed to save configuration.' });
        this.cdr.detectChanges();
      }
    });
  }
}
