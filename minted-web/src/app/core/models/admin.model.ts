export interface JobExecution {
  id: number;
  jobName: string;
  status: string;
  triggerType: string;
  startTime: string;
  endTime?: string;
  errorMessage?: string;
  totalSteps: number;
  completedSteps: number;
  steps?: JobStepExecution[];
}

export interface JobStepExecution {
  id: number;
  stepName: string;
  stepOrder: number;
  status: string;
  contextJson?: string;
  errorMessage?: string;
  startTime: string;
  endTime?: string;
}

export interface JobScheduleConfig {
  id: number;
  jobName: string;
  cronExpression: string;
  enabled: boolean;
  lastRunAt?: string;
  description?: string;
}

export interface DefaultCategory {
  id?: number;
  name: string;
  icon?: string;
  type: 'INCOME' | 'EXPENSE';
}

export interface DefaultAccountType {
  id?: number;
  name: string;
}

export interface SystemSettingResponse {
  id: number;
  settingKey: string;
  settingValue: string;
  description: string;
}
