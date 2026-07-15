export type CheckStatus = "UP" | "SLOW" | "DOWN" | "UNKNOWN";

export interface MonitoredSystem {
  id: number;
  name: string;
  baseUrl: string;
  description: string | null;
  active: boolean;
  createdAt: string;
}

export interface MonitoredApi {
  id: number;
  name: string;
  url: string;
  description: string | null;
  active: boolean;
  createdAt: string;
  expectedStatusCode: number;
  slowThresholdMs: number;
  timeoutMs: number;
  monitoredSystem: MonitoredSystem;
}

export interface SystemStatus {
  systemId: number;
  name: string;
  status: CheckStatus;
  message: string;
  totalEndpoints: number;
  upEndpoints: number;
  slowEndpoints: number;
  downEndpoints: number;
  unknownEndpoints: number;
  lastCheckedAt: string | null;
}

export interface ApiStatus {
  id: number;
  monitoredApiId: number;
  monitoredApiName: string;
  monitoredApiUrl: string;
  status: CheckStatus;
  message: string;
  available: boolean;
  statusCode: number | null;
  responseTimeMs: number | null;
  checkedAt: string | null;
  errorMessage: string | null;
}

export interface ApiHistoryItem {
  id: number;
  status: CheckStatus;
  message: string;
  available: boolean;
  statusCode: number | null;
  responseTimeMs: number | null;
  checkedAt: string;
  errorMessage: string | null;
}

export interface ApiCheckResult {
  apiId: number;
  name: string;
  url: string;
  available: boolean;
  status: CheckStatus;
  message: string;
  statusCode: number | null;
  responseTimeMs: number;
  checkedAt: string;
  errorMessage: string | null;
}

export interface SystemInput {
  name: string;
  baseUrl: string;
  description: string;
}

export interface ApiInput {
  name: string;
  url: string;
  description: string;
  expectedStatusCode: number;
  slowThresholdMs: number;
  timeoutMs: number;
}

export interface SystemOverview {
  system: MonitoredSystem;
  status: SystemStatus;
}

export interface EndpointOverview {
  api: MonitoredApi;
  status: ApiStatus | null;
}
