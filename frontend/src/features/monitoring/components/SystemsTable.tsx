import { Search } from "@mui/icons-material";
import { Box, Card, CardContent, FormControl, InputAdornment, InputLabel, MenuItem, Select, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography } from "@mui/material";
import type { CheckStatus, SystemOverview } from "../../../entities/monitoring/types";
import { StatusBadge } from "../../../shared/components/StatusBadge";
import { formatDate } from "../../../shared/utils/formatters";

interface SystemsTableProps { items: SystemOverview[]; selectedId: number | null; query: string; status: CheckStatus | "ALL"; onQueryChange: (value: string) => void; onStatusChange: (value: CheckStatus | "ALL") => void; onSelect: (id: number) => void; }

export function SystemsTable({ items, selectedId, query, status, onQueryChange, onStatusChange, onSelect }: SystemsTableProps) {
  return <Card><CardContent sx={{ p: 0 }}>
    <Stack direction={{ xs: "column", md: "row" }} sx={{ justifyContent: "space-between", gap: 2, p: 2.5 }}>
      <Box><Typography variant="h2">Sistemas monitorados</Typography><Typography variant="body2" color="text.secondary">{items.length} sistema(s) exibido(s). Selecione um para detalhar.</Typography></Box>
      <Stack direction={{ xs: "column", sm: "row" }} sx={{ gap: 1.25 }}>
        <TextField value={query} onChange={(event) => onQueryChange(event.target.value)} placeholder="Buscar sistema" slotProps={{ input: { startAdornment: <InputAdornment position="start"><Search fontSize="small" /></InputAdornment> } }} />
        <FormControl sx={{ minWidth: 160 }}><InputLabel id="system-status-label">Status</InputLabel><Select labelId="system-status-label" label="Status" value={status} onChange={(event) => onStatusChange(event.target.value as CheckStatus | "ALL")}>
          <MenuItem value="ALL">Todos</MenuItem><MenuItem value="DOWN">DOWN</MenuItem><MenuItem value="SLOW">SLOW</MenuItem><MenuItem value="UP">UP</MenuItem><MenuItem value="UNKNOWN">Sem leitura</MenuItem>
        </Select></FormControl>
      </Stack>
    </Stack>
    <TableContainer><Table size="small"><TableHead><TableRow><TableCell>Sistema</TableCell><TableCell>Base URL</TableCell><TableCell>Status</TableCell><TableCell>Endpoints</TableCell><TableCell>Última leitura</TableCell><TableCell>Ativo</TableCell></TableRow></TableHead>
      <TableBody>{items.length === 0 ? <TableRow><TableCell colSpan={6} align="center" sx={{ py: 5, color: "text.secondary" }}>Nenhum sistema corresponde aos filtros.</TableCell></TableRow> : items.map(({ system, status: current }) =>
        <TableRow hover selected={selectedId === system.id} key={system.id} onClick={() => onSelect(system.id)} sx={{ cursor: "pointer" }}>
          <TableCell><Typography sx={{ fontWeight: 700 }}>{system.name}</Typography><Typography variant="caption" color="text.secondary">#{system.id}</Typography></TableCell>
          <TableCell sx={{ maxWidth: 260, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }} title={system.baseUrl}>{system.baseUrl}</TableCell>
          <TableCell><StatusBadge status={current.status} /></TableCell><TableCell>{current.totalEndpoints}</TableCell><TableCell>{formatDate(current.lastCheckedAt)}</TableCell><TableCell>{system.active ? "Sim" : "Não"}</TableCell>
        </TableRow>)}</TableBody>
    </Table></TableContainer>
  </CardContent></Card>;
}
