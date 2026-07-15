import { DnsOutlined, ScheduleOutlined } from "@mui/icons-material";
import { Box, Card, CardContent, Divider, List, ListItem, ListItemIcon, ListItemText, Stack, Typography } from "@mui/material";
import type { SystemOverview } from "../../../entities/monitoring/types";
import { StatusBadge } from "../../../shared/components/StatusBadge";
import { formatDate, statusWeight } from "../../../shared/utils/formatters";

export function SystemSidebar({ selected, priorities }: { selected?: SystemOverview; priorities: SystemOverview[] }) {
  const actionable = priorities.filter(({ status }) => status.status !== "UP").sort((a, b) => statusWeight[a.status.status] - statusWeight[b.status.status]).slice(0, 4);
  return <Stack spacing={2}>
    <Card><CardContent><Stack direction="row" sx={{ alignItems: "flex-start", justifyContent: "space-between", gap: 1 }}><Box><Typography variant="h2">{selected?.system.name ?? "Sistema selecionado"}</Typography><Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>{selected?.status.message ?? "Aguardando dados."}</Typography></Box><StatusBadge status={selected?.status.status} /></Stack>
      <Divider sx={{ my: 2 }} /><List dense disablePadding><ListItem disableGutters><ListItemIcon sx={{ minWidth: 36 }}><DnsOutlined fontSize="small" /></ListItemIcon><ListItemText primary="Endpoints ativos" secondary={selected?.status.totalEndpoints ?? 0} /></ListItem><ListItem disableGutters><ListItemIcon sx={{ minWidth: 36 }}><ScheduleOutlined fontSize="small" /></ListItemIcon><ListItemText primary="Última leitura" secondary={formatDate(selected?.status.lastCheckedAt)} /></ListItem></List>
      <Typography variant="caption" color="text.secondary" sx={{ wordBreak: "break-all" }}>{selected?.system.baseUrl ?? "-"}</Typography>
    </CardContent></Card>
    <Card><CardContent><Typography variant="h2">Fila operacional</Typography><Typography variant="body2" color="text.secondary">Prioridades que precisam de atenção.</Typography><Divider sx={{ my: 1.5 }} />
      {actionable.length === 0 ? <Typography variant="body2" color="text.secondary" sx={{ py: 2 }}>Nenhuma prioridade no momento.</Typography> : <List disablePadding>{actionable.map(({ system, status }) => <ListItem key={system.id} disableGutters alignItems="flex-start"><ListItemText primary={<Stack direction="row" sx={{ justifyContent: "space-between", gap: 1 }}><Typography variant="body2" sx={{ fontWeight: 700 }}>{system.name}</Typography><StatusBadge status={status.status} /></Stack>} secondary={status.message} /></ListItem>)}</List>}
    </CardContent></Card>
  </Stack>;
}
