import { Chip } from "@mui/material";
import type { ChipProps } from "@mui/material";
import type { CheckStatus } from "../../entities/monitoring/types";

const colors: Record<CheckStatus, ChipProps["color"]> = { UP: "success", SLOW: "warning", DOWN: "error", UNKNOWN: "default" };

export function StatusBadge({ status = "UNKNOWN" }: { status?: CheckStatus }) {
  return <Chip label={status === "UNKNOWN" ? "SEM LEITURA" : status} color={colors[status]} size="small" variant={status === "UNKNOWN" ? "outlined" : "filled"} sx={{ fontWeight: 800, fontSize: "0.68rem" }} />;
}
