import type {
  ApiCheckResult, ApiHistoryItem, ApiInput, ApiStatus, MonitoredApi, MonitoredSystem, SystemInput, SystemStatus,
} from "../../../entities/monitoring/types";
import { HttpError, http } from "../../../shared/api/http";

export const monitoringApi = {
  listSystems: () => http.get<MonitoredSystem[]>("/api/monitored-systems"),
  getSystemStatus: async (system: MonitoredSystem): Promise<SystemStatus> => {
    try {
      return await http.get<SystemStatus>(`/api/monitored-systems/${system.id}/status`);
    } catch {
      return {
        systemId: system.id, name: system.name, status: "UNKNOWN",
        message: "Ainda nao ha leitura suficiente para calcular a saude do sistema.",
        totalEndpoints: 0, upEndpoints: 0, slowEndpoints: 0, downEndpoints: 0,
        unknownEndpoints: 0, lastCheckedAt: null,
      };
    }
  },
  listApis: (systemId: number) => http.get<MonitoredApi[]>(`/api/monitored-systems/${systemId}/apis`),
  getApiStatus: async (systemId: number, apiId: number): Promise<ApiStatus | null> => {
    try {
      return await http.get<ApiStatus>(`/api/monitored-systems/${systemId}/apis/${apiId}/status`);
    } catch (error) {
      if (error instanceof HttpError && error.status === 404) return null;
      throw error;
    }
  },
  getHistory: (systemId: number, apiId: number) =>
    http.get<ApiHistoryItem[]>(`/api/monitored-systems/${systemId}/apis/${apiId}/history?page=0&size=100`),
  checkApi: (systemId: number, apiId: number) =>
    http.post<ApiCheckResult>(`/api/monitored-systems/${systemId}/apis/${apiId}/check`),
  createSystem: (input: SystemInput) => http.post<MonitoredSystem>("/api/monitored-systems", input),
  updateSystem: (id: number, input: SystemInput) => http.put<MonitoredSystem>(`/api/monitored-systems/${id}`, input),
  setSystemActive: (id: number, active: boolean) => http.patch<MonitoredSystem>(`/api/monitored-systems/${id}/active`, { active }),
  deleteSystem: (id: number) => http.delete(`/api/monitored-systems/${id}`),
  createApi: (systemId: number, input: ApiInput) => http.post<MonitoredApi>(`/api/monitored-systems/${systemId}/apis`, input),
  updateApi: (systemId: number, apiId: number, input: ApiInput) => http.put<MonitoredApi>(`/api/monitored-systems/${systemId}/apis/${apiId}`, input),
  setApiActive: (systemId: number, apiId: number, active: boolean) =>
    http.patch<MonitoredApi>(`/api/monitored-systems/${systemId}/apis/${apiId}/active`, { active }),
  deleteApi: (systemId: number, apiId: number) => http.delete(`/api/monitored-systems/${systemId}/apis/${apiId}`),
};
