import type { ReactNode } from "react";
import { CheckCircleOutlined, ErrorOutlined, QueryStats, SpeedOutlined, WarningAmberOutlined } from "@mui/icons-material";
import { Box, Card, CardContent, Stack, Typography } from "@mui/material";

interface MetricCardProps { label: string; value: ReactNode; helper: string; tone?: "up" | "slow" | "down" | "accent"; }

const toneMap: Record<NonNullable<MetricCardProps["tone"]>, { color: string; icon: ReactNode }> = {
  up: { color: "success.main", icon: <CheckCircleOutlined /> }, slow: { color: "warning.main", icon: <SpeedOutlined /> },
  down: { color: "error.main", icon: <ErrorOutlined /> }, accent: { color: "primary.main", icon: <QueryStats /> },
};

export function MetricCard({ label, value, helper, tone }: MetricCardProps) {
  const visual = tone ? toneMap[tone] : { color: "text.secondary", icon: <WarningAmberOutlined /> };
  return <Card><CardContent sx={{ p: 2.25, "&:last-child": { pb: 2.25 } }}>
    <Stack direction="row" sx={{ alignItems: "center", justifyContent: "space-between" }}><Typography variant="body2" color="text.secondary" sx={{ fontWeight: 650 }}>{label}</Typography>
      <Box sx={{ display: "flex", color: visual.color, "& svg": { fontSize: 20 } }}>{visual.icon}</Box></Stack>
    <Typography variant="h4" sx={{ my: 1, fontWeight: 800 }}>{value}</Typography><Typography variant="caption" color="text.secondary">{helper}</Typography>
  </CardContent></Card>;
}
