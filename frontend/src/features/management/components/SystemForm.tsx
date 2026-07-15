import { useEffect } from "react";
import { SaveOutlined } from "@mui/icons-material";
import { Button, Card, CardContent, Stack, TextField, Typography } from "@mui/material";
import { useForm } from "react-hook-form";
import type { MonitoredSystem, SystemInput } from "../../../entities/monitoring/types";

interface SystemFormProps { editing?: MonitoredSystem; busy: boolean; resetVersion: number; onSubmit: (input: SystemInput) => void; onCancel: () => void; }

export function SystemForm({ editing, busy, resetVersion, onSubmit, onCancel }: SystemFormProps) {
  const { register, handleSubmit, reset, formState: { errors } } = useForm<SystemInput>({ defaultValues: { name: "", baseUrl: "", description: "" } });
  useEffect(() => reset(editing ? { name: editing.name, baseUrl: editing.baseUrl, description: editing.description ?? "" } : { name: "", baseUrl: "", description: "" }), [editing, reset, resetVersion]);

  return <Card><CardContent><Typography variant="h2">{editing ? "Editar sistema" : "Novo sistema"}</Typography><Typography variant="body2" color="text.secondary" sx={{ mb: 2.5 }}>Base que agrupa os endpoints monitorados.</Typography>
    <Stack component="form" spacing={2} onSubmit={(event) => void handleSubmit(onSubmit)(event)}>
      <TextField label="Nome do sistema" placeholder="Sistema Financeiro" {...register("name", { required: "Informe o nome." })} error={Boolean(errors.name)} helperText={errors.name?.message} />
      <TextField label="Base URL" type="url" placeholder="https://financeiro.empresa.com" {...register("baseUrl", { required: "Informe a URL." })} error={Boolean(errors.baseUrl)} helperText={errors.baseUrl?.message} />
      <TextField label="Descrição" multiline minRows={3} {...register("description")} />
      <Stack direction="row" sx={{ gap: 1 }}><Button type="submit" variant="contained" startIcon={<SaveOutlined />} disabled={busy}>{busy ? "Salvando..." : editing ? "Salvar alterações" : "Cadastrar sistema"}</Button>{editing && <Button color="inherit" onClick={onCancel}>Cancelar</Button>}</Stack>
    </Stack>
  </CardContent></Card>;
}
