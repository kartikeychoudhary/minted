export interface DashboardConfigResponse {
  id: number | null;
  excludedCategoryIds: number[];
}

export interface DashboardConfigRequest {
  excludedCategoryIds: number[];
}
