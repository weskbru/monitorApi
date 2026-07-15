import { useMemo, type ReactNode } from "react";
import { BarChart } from "@mui/x-charts/BarChart";
import { LineChart } from "@mui/x-charts/LineChart";
import { PieChart } from "@mui/x-charts/PieChart";
import { Box, Card, CardContent, Skeleton, Typography, useTheme } from "@mui/material";
import type { ApiHistoryItem, SystemOverview } from "../../../entities/monitoring/types";

interface MonitoringChartsProps {
  systems: SystemOverview[];
  history: ApiHistoryItem[];
  endpointName?: string;
  historyLoading: boolean;
}

function ChartCard({ title, description, children }: { title: string; description: string; children: ReactNode }) {
  return <Card><CardContent><Typography variant="h2">{title}</Typography><Typography variant="body2" color="text.secondary" sx={{ mt: 0.5, mb: 1 }}>{description}</Typography>{children}</CardContent></Card>;
}

export function MonitoringCharts({ systems, history, endpointName, historyLoading }: MonitoringChartsProps) {
  const theme = useTheme();
  const colors = useMemo(() => ({ up: theme.palette.success.main, slow: theme.palette.warning.main, down: theme.palette.error.main, unknown: theme.palette.grey[500] }), [theme]);
  const pieData = useMemo(() => [
    { id: "up", label: "UP", value: systems.filter(({ status }) => status.status === "UP").length },
    { id: "slow", label: "SLOW", value: systems.filter(({ status }) => status.status === "SLOW").length },
    { id: "down", label: "DOWN", value: systems.filter(({ status }) => status.status === "DOWN").length },
    { id: "unknown", label: "Sem leitura", value: systems.filter(({ status }) => status.status === "UNKNOWN").length },
  ], [systems]);
  const systemData = useMemo(() => [...systems].sort((a, b) => b.status.totalEndpoints - a.status.totalEndpoints).slice(0, 8), [systems]);
  const historyData = useMemo(() => [...history].sort((a, b) => new Date(a.checkedAt).getTime() - new Date(b.checkedAt).getTime()).slice(-20), [history]);
  const historyLabels = useMemo(() => historyData.map((item) => new Intl.DateTimeFormat("pt-BR", { day: "2-digit", month: "2-digit", hour: "2-digit", minute: "2-digit" }).format(new Date(item.checkedAt))), [historyData]);
  const hasStatusData = pieData.some((item) => item.value > 0);

  return <Box component="section" aria-label="Gráficos operacionais" sx={{ display: "grid", gridTemplateColumns: { xs: "1fr", lg: "repeat(2, minmax(0, 1fr))" }, gap: 2 }}>
    <ChartCard title="Distribuição de status" description="Proporção atual dos sistemas monitorados.">
      {hasStatusData ? <PieChart height={280} colors={[colors.up, colors.slow, colors.down, colors.unknown]} series={[{ data: pieData, innerRadius: 55, paddingAngle: 3, cornerRadius: 5, arcLabel: (item) => item.value > 0 ? String(item.value) : "", arcLabelMinAngle: 18 }]} /> : <Box sx={{ display: "grid", placeItems: "center", height: 280 }}><Typography color="text.secondary">Ainda não há sistemas para representar.</Typography></Box>}
    </ChartCard>
    <ChartCard title="Endpoints por sistema" description="Composição dos sistemas com mais endpoints.">
      {systemData.length > 0 ? <BarChart height={280} colors={[colors.up, colors.slow, colors.down, colors.unknown]} xAxis={[{ scaleType: "band", data: systemData.map(({ system }) => system.name) }]} series={[
        { data: systemData.map(({ status }) => status.upEndpoints), label: "UP", stack: "status" },
        { data: systemData.map(({ status }) => status.slowEndpoints), label: "SLOW", stack: "status" },
        { data: systemData.map(({ status }) => status.downEndpoints), label: "DOWN", stack: "status" },
        { data: systemData.map(({ status }) => status.unknownEndpoints), label: "Sem leitura", stack: "status" },
      ]} /> : <Box sx={{ display: "grid", placeItems: "center", height: 280 }}><Typography color="text.secondary">Cadastre sistemas e endpoints para visualizar.</Typography></Box>}
    </ChartCard>
    <Box sx={{ gridColumn: { lg: "1 / -1" } }}><ChartCard title="Tempo de resposta" description={endpointName ? `Últimas medições de ${endpointName}.` : "Abra o histórico de um endpoint para visualizar sua tendência."}>
      {historyLoading ? <Skeleton variant="rounded" height={280} /> : historyData.length > 0 ? <LineChart height={280} colors={[theme.palette.primary.main]} xAxis={[{ scaleType: "point", data: historyLabels }]} yAxis={[{ label: "ms" }]} series={[{ data: historyData.map((item) => item.responseTimeMs), label: "Resposta (ms)", connectNulls: false, showMark: historyData.length <= 12 }]} /> : <Box sx={{ display: "grid", placeItems: "center", height: 280 }}><Typography color="text.secondary">Nenhuma medição selecionada.</Typography></Box>}
    </ChartCard></Box>
  </Box>;
}
