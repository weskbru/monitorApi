import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Refresh } from "@mui/icons-material";
import { Alert, Box, Button, CircularProgress, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Stack, Typography } from "@mui/material";
import type { ApiInput, MonitoredApi, MonitoredSystem, SystemInput } from "../entities/monitoring/types";
import { monitoringApi } from "../features/monitoring/api/monitoringApi";
import { ApiForm } from "../features/management/components/ApiForm";
import { ManagementTables } from "../features/management/components/ManagementTables";
import { SystemForm } from "../features/management/components/SystemForm";
import { errorMessage } from "../shared/utils/formatters";

type ManagementAction =
  | { type: "toggle-system"; item: MonitoredSystem }
  | { type: "delete-system"; item: MonitoredSystem }
  | { type: "toggle-api"; item: MonitoredApi }
  | { type: "delete-api"; item: MonitoredApi };

export function RegisterPage() {
  const queryClient = useQueryClient();
  const [requestedSystemId, setRequestedSystemId] = useState<number | null>(null);
  const [editingSystem, setEditingSystem] = useState<MonitoredSystem>();
  const [editingApi, setEditingApi] = useState<MonitoredApi>();
  const [message, setMessage] = useState<{ text: string; error?: boolean }>();
  const [pendingAction, setPendingAction] = useState<ManagementAction>();
  const [systemFormVersion, setSystemFormVersion] = useState(0);
  const [apiFormVersion, setApiFormVersion] = useState(0);
  const systemsQuery = useQuery({ queryKey: ["systems"], queryFn: monitoringApi.listSystems });
  const systems = systemsQuery.data ?? [];

  const selectedSystemId = systems.some((system) => system.id === requestedSystemId)
    ? requestedSystemId
    : systems[0]?.id ?? null;

  const apisQuery = useQuery({
    queryKey: ["apis", selectedSystemId], queryFn: () => monitoringApi.listApis(selectedSystemId as number), enabled: selectedSystemId != null,
  });
  const apis = apisQuery.data ?? [];

  async function refresh(systemId = selectedSystemId) {
    await queryClient.invalidateQueries({ queryKey: ["systems"] });
    if (systemId != null) await queryClient.invalidateQueries({ queryKey: ["apis", systemId] });
  }

  const systemMutation = useMutation({
    mutationFn: (input: SystemInput) => editingSystem ? monitoringApi.updateSystem(editingSystem.id, input) : monitoringApi.createSystem(input),
    onSuccess: async (system) => {
      setMessage({ text: editingSystem ? "Sistema atualizado com sucesso." : "Sistema cadastrado com sucesso." });
      setEditingSystem(undefined); setSystemFormVersion((version) => version + 1); setRequestedSystemId(system.id); await refresh(system.id);
    },
    onError: (error) => setMessage({ text: errorMessage(error), error: true }),
  });
  const apiMutation = useMutation({
    mutationFn: (input: ApiInput) => editingApi
      ? monitoringApi.updateApi(selectedSystemId as number, editingApi.id, input)
      : monitoringApi.createApi(selectedSystemId as number, input),
    onSuccess: async () => {
      setMessage({ text: editingApi ? "Endpoint atualizado com sucesso." : "Endpoint cadastrado com sucesso." });
      setEditingApi(undefined); setApiFormVersion((version) => version + 1); await refresh();
    },
    onError: (error) => setMessage({ text: errorMessage(error), error: true }),
  });
  const actionMutation = useMutation({
    mutationFn: async (action: ManagementAction) => {
      if (action.type === "toggle-system") return monitoringApi.setSystemActive(action.item.id, !action.item.active);
      if (action.type === "delete-system") return monitoringApi.deleteSystem(action.item.id);
      if (action.type === "toggle-api") return monitoringApi.setApiActive(selectedSystemId as number, action.item.id, !action.item.active);
      return monitoringApi.deleteApi(selectedSystemId as number, action.item.id);
    },
    onSuccess: async () => { setMessage({ text: "Acao concluida com sucesso." }); setEditingApi(undefined); setEditingSystem(undefined); await refresh(); },
    onError: (error) => setMessage({ text: errorMessage(error), error: true }),
  });

  function confirmAction(action: ManagementAction) {
    if (action.type.startsWith("delete")) { setPendingAction(action); return; }
    actionMutation.mutate(action);
  }

  if (systemsQuery.isLoading) return <Stack spacing={2} sx={{ alignItems: "center", py: 10 }}><CircularProgress /><Typography color="text.secondary">Carregando cadastros...</Typography></Stack>;
  if (systemsQuery.isError) return <Alert severity="error">Não foi possível carregar os sistemas.</Alert>;

  return <Stack spacing={3}>
    <Stack direction={{ xs: "column", md: "row" }} sx={{ justifyContent: "space-between", alignItems: { md: "center" }, gap: 2 }}><Box><Typography variant="overline" color="primary.main" sx={{ fontWeight: 800 }}>Gestão operacional</Typography><Typography variant="h1">Sistemas e endpoints</Typography><Typography color="text.secondary" sx={{ mt: 1 }}>Cadastre, configure e mantenha os recursos que participam do monitoramento.</Typography></Box><Button variant="outlined" startIcon={<Refresh />} onClick={() => void refresh()}>Recarregar listas</Button></Stack>
    {message && <Alert severity={message.error ? "error" : "success"} onClose={() => setMessage(undefined)}>{message.text}</Alert>}
    <Box sx={{ display: "grid", gridTemplateColumns: { xs: "1fr", lg: "repeat(2, minmax(0, 1fr))" }, gap: 2 }}>
      <SystemForm editing={editingSystem} busy={systemMutation.isPending} resetVersion={systemFormVersion} onSubmit={(input) => systemMutation.mutate(input)} onCancel={() => setEditingSystem(undefined)} />
      <ApiForm systems={systems} systemId={selectedSystemId} editing={editingApi} busy={apiMutation.isPending} resetVersion={apiFormVersion} onSystemChange={(id) => { setRequestedSystemId(id); setEditingApi(undefined); }} onSubmit={(input) => apiMutation.mutate(input)} onCancel={() => setEditingApi(undefined)} />
    </Box>
    {apisQuery.isLoading ? <Stack sx={{ alignItems: "center", py: 5 }}><CircularProgress size={28} /></Stack> : <ManagementTables systems={systems} apis={apis}
      onEditSystem={setEditingSystem} onToggleSystem={(item) => confirmAction({ type: "toggle-system", item })} onDeleteSystem={(item) => confirmAction({ type: "delete-system", item })}
      onEditApi={setEditingApi} onToggleApi={(item) => confirmAction({ type: "toggle-api", item })} onDeleteApi={(item) => confirmAction({ type: "delete-api", item })} />}
    <Dialog open={Boolean(pendingAction)} onClose={() => setPendingAction(undefined)}><DialogTitle>Confirmar exclusão</DialogTitle><DialogContent><DialogContentText>Excluir “{pendingAction?.item.name}”? Esta ação não pode ser desfeita.</DialogContentText></DialogContent><DialogActions><Button color="inherit" onClick={() => setPendingAction(undefined)}>Cancelar</Button><Button color="error" variant="contained" onClick={() => { if (pendingAction) actionMutation.mutate(pendingAction); setPendingAction(undefined); }}>Excluir</Button></DialogActions></Dialog>
  </Stack>;
}
