import { Card, CardContent, Skeleton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from "@mui/material";
import type { ApiHistoryItem, MonitoredApi } from "../../../entities/monitoring/types";
import { StatusBadge } from "../../../shared/components/StatusBadge";
import { formatDate } from "../../../shared/utils/formatters";

interface EndpointHistoryProps { endpoint?: MonitoredApi; history: ApiHistoryItem[]; loading: boolean; error: boolean; }

export function EndpointHistory({ endpoint, history, loading, error }: EndpointHistoryProps) {
  const emptyMessage = error ? "Não foi possível carregar o histórico." : endpoint ? "Nenhum registro encontrado." : "Escolha um endpoint para consultar as verificações.";
  return <Card><CardContent sx={{ p: 0 }}><Typography variant="h2" sx={{ px: 2.5, pt: 2.5 }}>Histórico do endpoint</Typography><Typography variant="body2" color="text.secondary" sx={{ px: 2.5, pb: 2 }}>{endpoint ? `${history.length} registro(s) de ${endpoint.name}.` : "Acompanhe as verificações anteriores."}</Typography>
    {loading ? <Skeleton variant="rounded" height={150} sx={{ mx: 2.5, mb: 2.5 }} /> : <TableContainer><Table size="small"><TableHead><TableRow><TableCell>Status</TableCell><TableCell>Código</TableCell><TableCell>Resposta</TableCell><TableCell>Data</TableCell><TableCell>Detalhe</TableCell></TableRow></TableHead><TableBody>
      {history.length === 0 ? <TableRow><TableCell colSpan={5} align="center" sx={{ py: 5, color: error ? "error.main" : "text.secondary" }}>{emptyMessage}</TableCell></TableRow> : history.map((item) => <TableRow hover key={item.id}><TableCell><StatusBadge status={item.status} /></TableCell><TableCell>{item.statusCode ?? "-"}</TableCell><TableCell>{item.responseTimeMs != null ? `${item.responseTimeMs}ms` : "-"}</TableCell><TableCell>{formatDate(item.checkedAt)}</TableCell><TableCell>{item.errorMessage || item.message}</TableCell></TableRow>)}
    </TableBody></Table></TableContainer>}
  </CardContent></Card>;
}
