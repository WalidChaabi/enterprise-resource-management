export interface OrganizationSummary {
  id: string;
  name: string;
}

export interface OrganizationSwitchRequest {
  orgId: string;
}

export interface OrganizationSwitchResponse {
  activeOrgId: string;
  message: string;
}
