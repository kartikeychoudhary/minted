export interface LlmModel {
  id: number;
  name: string;
  provider: string;
  modelKey: string;
  description: string;
  isActive: boolean;
  isDefault: boolean;
}

export interface LlmConfig {
  id: number | null;
  provider: string;
  hasApiKey: boolean;
  selectedModel: LlmModel | null;
  merchantMappings: MerchantMapping[];
}

export interface LlmConfigRequest {
  apiKey?: string;
  modelId?: number;
}

export interface MerchantMapping {
  id: number;
  snippets: string;
  snippetList: string[];
  categoryId: number;
  categoryName: string;
  categoryIcon: string;
  categoryColor: string;
}

export interface MerchantMappingRequest {
  snippets: string;
  categoryId: number;
}

export interface LlmModelRequest {
  name: string;
  provider?: string;
  modelKey: string;
  description?: string;
  isActive?: boolean;
  isDefault?: boolean;
}
