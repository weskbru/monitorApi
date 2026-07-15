import { DeleteOutlined, EditOutlined, PowerSettingsNew } from "@mui/icons-material";
import { Button, Card, CardContent, Chip, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from "@mui/material";
import type { MonitoredApi, MonitoredSystem } from "../../../entities/monitoring/types";
import { formatDate } from "../../../shared/utils/formatters";

interface ManagementTablesProps { systems: MonitoredSystem[]; apis: MonitoredApi[]; onEditSystem: (system: MonitoredSystem) => void; onToggleSystem: (system: MonitoredSystem) => void; onDeleteSystem: (system: MonitoredSystem) => void; onEditApi: (api: MonitoredApi) => void; onToggleApi: (api: MonitoredApi) => void; onDeleteApi: (api: MonitoredApi) => void; }

function StateChip({ active }: { active: boolean }) { return <Chip size="small" label={active ? "Ativo" : "Inativo"} color={active ? "success" : "default"} variant={active ? "filled" : "outlined"} />; }

export function ManagementTables(props: ManagementTablesProps) {
  return <Stack spacing={2}>
    <Card><CardContent sx={{ p: 0 }}><Typography variant="h2" sx={{ px: 2.5, pt: 2.5 }}>Sistemas cadastrados</Typography><Typography variant="body2" color="text.secondary" sx={{ px: 2.5, pb: 2 }}>{props.systems.length} sistema(s) disponível(is).</Typography>
      <TableContainer><Table size="small"><TableHead><TableRow><TableCell>Sistema</TableCell><TableCell>Base URL</TableCell><TableCell>Estado</TableCell><TableCell>Criado em</TableCell><TableCell>Ações</TableCell></TableRow></TableHead><TableBody>
        {props.systems.length === 0 ? <TableRow><TableCell colSpan={5} align="center" sx={{ py: 5, color: "text.secondary" }}>Nenhum sistema cadastrado.</TableCell></TableRow> : props.systems.map((system) => <TableRow hover key={system.id}><TableCell><Typography sx={{ fontWeight: 700 }}>{system.name}</Typography><Typography variant="caption" color="text.secondary">#{system.id}</Typography></TableCell><TableCell>{system.baseUrl}</TableCell><TableCell><StateChip active={system.active} /></TableCell><TableCell>{formatDate(system.createdAt)}</TableCell><TableCell><Stack direction="row"><Button size="small" startIcon={<EditOutlined />} onClick={() => props.onEditSystem(system)}>Editar</Button><Button size="small" color="inherit" startIcon={<PowerSettingsNew />} onClick={() => props.onToggleSystem(system)}>{system.active ? "Desativar" : "Ativar"}</Button><Button size="small" color="error" startIcon={<DeleteOutlined />} onClick={() => props.onDeleteSystem(system)}>Excluir</Button></Stack></TableCell></TableRow>)}
      </TableBody></Table></TableContainer>
    </CardContent></Card>
    <Card><CardContent sx={{ p: 0 }}><Typography variant="h2" sx={{ px: 2.5, pt: 2.5 }}>Endpoints cadastrados</Typography><Typography variant="body2" color="text.secondary" sx={{ px: 2.5, pb: 2 }}>{props.apis.length} endpoint(s) no sistema selecionado.</Typography>
      <TableContainer><Table size="small"><TableHead><TableRow><TableCell>Endpoint</TableCell><TableCell>HTTP esperado</TableCell><TableCell>Lentidão</TableCell><TableCell>Timeout</TableCell><TableCell>Estado</TableCell><TableCell>Ações</TableCell></TableRow></TableHead><TableBody>
        {props.apis.length === 0 ? <TableRow><TableCell colSpan={6} align="center" sx={{ py: 5, color: "text.secondary" }}>Nenhum endpoint neste sistema.</TableCell></TableRow> : props.apis.map((api) => <TableRow hover key={api.id}><TableCell><Typography sx={{ fontWeight: 700 }}>{api.name}</Typography><Typography variant="caption" color="text.secondary">{api.url}</Typography></TableCell><TableCell>{api.expectedStatusCode}</TableCell><TableCell>{api.slowThresholdMs}ms</TableCell><TableCell>{api.timeoutMs}ms</TableCell><TableCell><StateChip active={api.active} /></TableCell><TableCell><Stack direction="row"><Button size="small" startIcon={<EditOutlined />} onClick={() => props.onEditApi(api)}>Editar</Button><Button size="small" color="inherit" startIcon={<PowerSettingsNew />} onClick={() => props.onToggleApi(api)}>{api.active ? "Desativar" : "Ativar"}</Button><Button size="small" color="error" startIcon={<DeleteOutlined />} onClick={() => props.onDeleteApi(api)}>Excluir</Button></Stack></TableCell></TableRow>)}
      </TableBody></Table></TableContainer>
    </CardContent></Card>
  </Stack>;
}
