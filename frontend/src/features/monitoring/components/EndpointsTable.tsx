import { History, PlayArrow } from "@mui/icons-material";
import { Box, Button, Card, CardContent, FormControl, InputLabel, MenuItem, Select, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from "@mui/material";
import type { CheckStatus, EndpointOverview } from "../../../entities/monitoring/types";
import { StatusBadge } from "../../../shared/components/StatusBadge";
import { formatDate } from "../../../shared/utils/formatters";

interface EndpointsTableProps { systemName?: string; items: EndpointOverview[]; statusFilter: CheckStatus | "ALL"; checkingId: number | null; onStatusChange: (status: CheckStatus | "ALL") => void; onCheck: (apiId: number) => void; onHistory: (apiId: number) => void; }

export function EndpointsTable({ items, systemName, statusFilter, checkingId, onStatusChange, onCheck, onHistory }: EndpointsTableProps) {
  return <Card><CardContent sx={{ p: 0 }}>
    <Stack direction={{ xs: "column", sm: "row" }} sx={{ justifyContent: "space-between", gap: 2, p: 2.5 }}><Box><Typography variant="h2">Endpoints do sistema</Typography><Typography variant="body2" color="text.secondary">{systemName ? `Endpoints críticos vinculados a ${systemName}.` : "Selecione um sistema."}</Typography></Box>
      <FormControl sx={{ minWidth: 165 }}><InputLabel id="endpoint-status-label">Status</InputLabel><Select labelId="endpoint-status-label" label="Status" value={statusFilter} onChange={(event) => onStatusChange(event.target.value as CheckStatus | "ALL")}><MenuItem value="ALL">Todos</MenuItem><MenuItem value="DOWN">DOWN</MenuItem><MenuItem value="SLOW">SLOW</MenuItem><MenuItem value="UP">UP</MenuItem><MenuItem value="UNKNOWN">Sem leitura</MenuItem></Select></FormControl></Stack>
    <TableContainer><Table size="small"><TableHead><TableRow><TableCell>Endpoint</TableCell><TableCell>URL</TableCell><TableCell>Status</TableCell><TableCell>Código</TableCell><TableCell>Resposta</TableCell><TableCell>Última leitura</TableCell><TableCell>Ações</TableCell></TableRow></TableHead>
      <TableBody>{items.length === 0 ? <TableRow><TableCell colSpan={7} align="center" sx={{ py: 5, color: "text.secondary" }}>Nenhum endpoint encontrado.</TableCell></TableRow> : items.map(({ api, status }) => <TableRow hover key={api.id}>
        <TableCell><Typography sx={{ fontWeight: 700 }}>{api.name}</Typography><Typography variant="caption" color="text.secondary">#{api.id} · {api.active ? "Ativo" : "Inativo"}</Typography></TableCell>
        <TableCell sx={{ maxWidth: 260, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }} title={api.url}>{api.url}</TableCell><TableCell><StatusBadge status={status?.status} /></TableCell><TableCell>{status?.statusCode ?? "-"}</TableCell><TableCell>{status?.responseTimeMs != null ? `${status.responseTimeMs}ms` : "-"}</TableCell><TableCell>{formatDate(status?.checkedAt)}</TableCell>
        <TableCell><Stack direction="row" sx={{ gap: 0.5 }}><Button size="small" startIcon={<PlayArrow />} disabled={!api.active || checkingId === api.id} onClick={() => onCheck(api.id)}>{checkingId === api.id ? "Verificando" : "Verificar"}</Button><Button size="small" color="inherit" startIcon={<History />} onClick={() => onHistory(api.id)}>Histórico</Button></Stack></TableCell>
      </TableRow>)}</TableBody>
    </Table></TableContainer>
  </CardContent></Card>;
}
