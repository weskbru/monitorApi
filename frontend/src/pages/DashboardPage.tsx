import { lazy, Suspense, useMemo, useState } from "react";
import { useMutation, useQueries, useQuery, useQueryClient } from "@tanstack/react-query";
import { Refresh } from "@mui/icons-material";
import { Alert, Box, Button, Chip, CircularProgress, Skeleton, Stack, Typography } from "@mui/material";
import type { CheckStatus, EndpointOverview, SystemOverview, SystemStatus } from "../entities/monitoring/types";
import { monitoringApi } from "../features/monitoring/api/monitoringApi";
import { EndpointHistory } from "../features/monitoring/components/EndpointHistory";
import { EndpointsTable } from "../features/monitoring/components/EndpointsTable";
import { MetricCard } from "../features/monitoring/components/MetricCard";
import { SystemSidebar } from "../features/monitoring/components/SystemSidebar";
import { SystemsTable } from "../features/monitoring/components/SystemsTable";
import { formatTime, statusWeight } from "../shared/utils/formatters";

const MonitoringCharts = lazy(() => import("../features/monitoring/components/MonitoringCharts").then((module) => ({ default: module.MonitoringCharts })));

const unknownStatus = (id: number, name: string): SystemStatus => ({
  systemId: id, name, status: "UNKNOWN", message: "Ainda nao ha leitura suficiente.", totalEndpoints: 0,
  upEndpoints: 0, slowEndpoints: 0, downEndpoints: 0, unknownEndpoints: 0, lastCheckedAt: null,
});

export function DashboardPage() {
  const queryClient = useQueryClient();
  const [requestedSystemId, setRequestedSystemId] = useState<number | null>(null);
  const [historyApiId, setHistoryApiId] = useState<number | null>(null);
  const [systemQuery, setSystemQuery] = useState("");
  const [systemStatus, setSystemStatus] = useState<CheckStatus | "ALL">("ALL");
  const [endpointStatus, setEndpointStatus] = useState<CheckStatus | "ALL">("ALL");

  const systemsQuery = useQuery({ queryKey: ["systems"], queryFn: monitoringApi.listSystems });
  const systems = systemsQuery.data ?? [];
  const statusQueries = useQueries({ queries: systems.map((system) => ({
    queryKey: ["system-status", system.id], queryFn: () => monitoringApi.getSystemStatus(system),
  })) });
  const overviews: SystemOverview[] = systems.map((system, index) => ({
    system, status: statusQueries[index]?.data ?? unknownStatus(system.id, system.name),
  }));

  const defaultSystemId = [...overviews].sort((a, b) => statusWeight[a.status.status] - statusWeight[b.status.status])[0]?.system.id ?? null;
  const selectedSystemId = overviews.some(({ system }) => system.id === requestedSystemId) ? requestedSystemId : defaultSystemId;

  const selected = overviews.find(({ system }) => system.id === selectedSystemId);
  const apisQuery = useQuery({
    queryKey: ["apis", selectedSystemId], queryFn: () => monitoringApi.listApis(selectedSystemId as number), enabled: selectedSystemId != null,
  });
  const apis = apisQuery.data ?? [];
  const apiStatusQueries = useQueries({ queries: apis.map((api) => ({
    queryKey: ["api-status", selectedSystemId, api.id], queryFn: () => monitoringApi.getApiStatus(selectedSystemId as number, api.id), enabled: selectedSystemId != null,
  })) });
  const endpoints: EndpointOverview[] = apis.map((api, index) => ({ api, status: apiStatusQueries[index]?.data ?? null }));
  const historyQuery = useQuery({
    queryKey: ["history", selectedSystemId, historyApiId],
    queryFn: () => monitoringApi.getHistory(selectedSystemId as number, historyApiId as number),
    enabled: selectedSystemId != null && historyApiId != null,
  });

  const checkMutation = useMutation({
    mutationFn: (apiId: number) => monitoringApi.checkApi(selectedSystemId as number, apiId),
    onSuccess: async (_, apiId) => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["api-status", selectedSystemId, apiId] }),
        queryClient.invalidateQueries({ queryKey: ["system-status", selectedSystemId] }),
        queryClient.invalidateQueries({ queryKey: ["history", selectedSystemId, apiId] }),
      ]);
    },
  });

  const filteredSystems = useMemo(() => overviews.filter(({ system, status }) => {
    const text = `${system.name} ${system.baseUrl}`.toLocaleLowerCase("pt-BR");
    return text.includes(systemQuery.toLocaleLowerCase("pt-BR")) && (systemStatus === "ALL" || status.status === systemStatus);
  }).sort((a, b) => statusWeight[a.status.status] - statusWeight[b.status.status]), [overviews, systemQuery, systemStatus]);

  const filteredEndpoints = endpoints.filter(({ status }) => endpointStatus === "ALL" || (status?.status ?? "UNKNOWN") === endpointStatus)
    .sort((a, b) => statusWeight[a.status?.status ?? "UNKNOWN"] - statusWeight[b.status?.status ?? "UNKNOWN"]);
  const totals = overviews.reduce((result, item) => {
    result[item.status.status] += 1; result.endpoints += item.status.totalEndpoints; return result;
  }, { UP: 0, SLOW: 0, DOWN: 0, UNKNOWN: 0, endpoints: 0 });
  const health = overviews.length ? Math.round(((totals.UP + totals.SLOW) / overviews.length) * 100) : 0;
  const signal = totals.DOWN ? ["Incidente", `${totals.DOWN} sistema(s) com falha critica`, "Priorize os sistemas DOWN e valide os endpoints afetados.", "danger"]
    : totals.SLOW ? ["Atencao", `${totals.SLOW} sistema(s) com lentidao`, "Acompanhe os endpoints SLOW antes que virem indisponibilidade.", "warning"]
      : overviews.length === 0 || totals.UNKNOWN === overviews.length ? ["Sem leitura", "Nenhuma leitura operacional", "Cadastre endpoints ou execute verificacoes para formar o estado.", "neutral"]
        : [`${health}% saudavel`, "Ambiente sem incidentes criticos", "Os sistemas com leitura recente estao respondendo dentro do esperado.", "ok"];

  async function refresh() {
    await queryClient.invalidateQueries({ queryKey: ["systems"] });
    await queryClient.invalidateQueries({ queryKey: ["system-status"] });
    await queryClient.invalidateQueries({ queryKey: ["apis"] });
    await queryClient.invalidateQueries({ queryKey: ["api-status"] });
  }

  if (systemsQuery.isLoading) return <Stack spacing={2} sx={{ alignItems: "center", py: 10 }}><CircularProgress /><Typography color="text.secondary">Carregando dashboard...</Typography></Stack>;
  if (systemsQuery.isError) return <Alert severity="error">Não foi possível conectar ao backend.</Alert>;

  const signalSeverity = signal[3] === "danger" ? "error" : signal[3] === "warning" ? "warning" : signal[3] === "ok" ? "success" : "info";
  return <Stack spacing={3}>
    <Stack direction={{ xs: "column", md: "row" }} sx={{ justifyContent: "space-between", alignItems: { md: "center" }, gap: 2 }}>
      <Box><Typography variant="overline" color="primary.main" sx={{ fontWeight: 800 }}>Ambiente monitorado</Typography><Typography variant="h1">Dashboard operacional</Typography><Typography color="text.secondary" sx={{ mt: 1, maxWidth: 720 }}>Visão central dos sistemas, endpoints críticos e sinais de indisponibilidade.</Typography></Box>
      <Stack direction="row" sx={{ alignItems: "center", gap: 1 }}><Chip color={signalSeverity} label={signal[0]} sx={{ fontWeight: 800 }} /><Button variant="outlined" startIcon={<Refresh />} onClick={() => void refresh()}>Atualizar</Button></Stack>
    </Stack>
    <Box aria-label="Resumo operacional" sx={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(160px, 1fr))", gap: 1.5 }}>
      <MetricCard label="Sistemas" value={overviews.length} helper="monitorados" /><MetricCard label="Saúde geral" value={`${health}%`} helper="sistemas operacionais" tone="accent" />
      <MetricCard label="Disponíveis" value={totals.UP} helper="UP" tone="up" /><MetricCard label="Lentos" value={totals.SLOW} helper="SLOW" tone="slow" /><MetricCard label="Indisponíveis" value={totals.DOWN} helper="DOWN" tone="down" /><MetricCard label="Sem leitura" value={totals.UNKNOWN} helper="UNKNOWN" />
      <MetricCard label="Endpoints" value={totals.endpoints} helper="críticos ativos" /><MetricCard label="Atualização" value={formatTime(new Date())} helper="horário local" tone="accent" />
    </Box>
    <Alert severity={signalSeverity} variant="outlined"><Typography sx={{ fontWeight: 750 }}>{signal[1]}</Typography><Typography variant="body2">{signal[2]}</Typography></Alert>
    <Suspense fallback={<Skeleton variant="rounded" height={620} />}><MonitoringCharts systems={overviews} history={historyQuery.data ?? []} endpointName={apis.find((api) => api.id === historyApiId)?.name} historyLoading={historyQuery.isLoading && historyApiId != null} /></Suspense>
    <Box sx={{ display: "grid", gridTemplateColumns: { xs: "1fr", lg: "minmax(0, 2fr) minmax(280px, 0.8fr)" }, gap: 2 }}><SystemsTable items={filteredSystems} selectedId={selectedSystemId} query={systemQuery} status={systemStatus} onQueryChange={setSystemQuery} onStatusChange={setSystemStatus} onSelect={(id) => { setRequestedSystemId(id); setHistoryApiId(null); }} /><SystemSidebar selected={selected} priorities={overviews} /></Box>
    <EndpointsTable systemName={selected?.system.name} items={filteredEndpoints} statusFilter={endpointStatus} checkingId={checkMutation.isPending ? checkMutation.variables : null} onStatusChange={setEndpointStatus} onCheck={(id) => checkMutation.mutate(id)} onHistory={setHistoryApiId} />
    <EndpointHistory endpoint={apis.find((api) => api.id === historyApiId)} history={historyQuery.data ?? []} loading={historyQuery.isLoading && historyApiId != null} error={historyQuery.isError} />
  </Stack>;
}
