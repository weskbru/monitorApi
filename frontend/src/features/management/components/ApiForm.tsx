import { useEffect } from "react";
import { ExpandMore, SaveOutlined } from "@mui/icons-material";
import { Accordion, AccordionDetails, AccordionSummary, Button, Card, CardContent, MenuItem, Stack, TextField, Typography } from "@mui/material";
import { useForm } from "react-hook-form";
import type { ApiInput, MonitoredApi, MonitoredSystem } from "../../../entities/monitoring/types";

interface ApiFormProps { systems: MonitoredSystem[]; systemId: number | null; editing?: MonitoredApi; busy: boolean; resetVersion: number; onSystemChange: (id: number) => void; onSubmit: (input: ApiInput) => void; onCancel: () => void; }
const emptyApi: ApiInput = { name: "", url: "", description: "", expectedStatusCode: 200, slowThresholdMs: 3000, timeoutMs: 10000 };

export function ApiForm({ systems, systemId, editing, busy, resetVersion, onSystemChange, onSubmit, onCancel }: ApiFormProps) {
  const { register, handleSubmit, reset, formState: { errors } } = useForm<ApiInput>({ defaultValues: emptyApi });
  useEffect(() => reset(editing ? { name: editing.name, url: editing.url, description: editing.description ?? "", expectedStatusCode: editing.expectedStatusCode, slowThresholdMs: editing.slowThresholdMs, timeoutMs: editing.timeoutMs } : emptyApi), [editing, reset, resetVersion]);
  return <Card><CardContent><Typography variant="h2">{editing ? "Editar endpoint" : "Novo endpoint crítico"}</Typography><Typography variant="body2" color="text.secondary" sx={{ mb: 2.5 }}>Funcionalidade importante que será verificada.</Typography>
    <Stack component="form" spacing={2} onSubmit={(event) => void handleSubmit(onSubmit)(event)}>
      <TextField select label="Sistema" value={systemId ?? ""} disabled={systems.length === 0 || Boolean(editing)} onChange={(event) => onSystemChange(Number(event.target.value))}>{systems.length === 0 ? <MenuItem value="">Cadastre um sistema primeiro</MenuItem> : systems.map((system) => <MenuItem key={system.id} value={system.id}>{system.name}</MenuItem>)}</TextField>
      <TextField label="Nome" placeholder="Login" {...register("name", { required: "Informe o nome." })} error={Boolean(errors.name)} helperText={errors.name?.message} />
      <TextField label="URL" type="url" placeholder="https://sistema.com/api/health" {...register("url", { required: "Informe a URL." })} error={Boolean(errors.url)} helperText={errors.url?.message} />
      <TextField label="Descrição" multiline minRows={2} {...register("description")} />
      <Accordion disableGutters elevation={0} sx={{ border: 1, borderColor: "divider", borderRadius: 1, "&::before": { display: "none" } }}>
        <AccordionSummary expandIcon={<ExpandMore />} aria-controls="advanced-endpoint-settings">
          <Stack><Typography sx={{ fontWeight: 700 }}>Configurações avançadas</Typography><Typography variant="caption" color="text.secondary">Opcional — os valores recomendados já estão preenchidos.</Typography></Stack>
        </AccordionSummary>
        <AccordionDetails id="advanced-endpoint-settings">
          <Stack direction={{ xs: "column", sm: "row" }} sx={{ gap: 1.5 }}>
            <TextField fullWidth label="Resposta considerada válida" type="number" helperText="Normalmente 200." {...register("expectedStatusCode", { valueAsNumber: true, min: 100, max: 599 })} slotProps={{ htmlInput: { min: 100, max: 599 } }} />
            <TextField fullWidth label="Considerar lento após (ms)" type="number" helperText="Padrão: 3000 ms." {...register("slowThresholdMs", { valueAsNumber: true, min: 1 })} slotProps={{ htmlInput: { min: 1 } }} />
            <TextField fullWidth label="Parar de esperar após (ms)" type="number" helperText="Padrão: 10000 ms." {...register("timeoutMs", { valueAsNumber: true, min: 100, max: 120000 })} slotProps={{ htmlInput: { min: 100, max: 120000 } }} />
          </Stack>
        </AccordionDetails>
      </Accordion>
      <Stack direction="row" sx={{ gap: 1 }}><Button type="submit" variant="contained" startIcon={<SaveOutlined />} disabled={busy || systemId == null}>{busy ? "Salvando..." : editing ? "Salvar alterações" : "Cadastrar endpoint"}</Button>{editing && <Button color="inherit" onClick={onCancel}>Cancelar</Button>}</Stack>
    </Stack>
  </CardContent></Card>;
}
