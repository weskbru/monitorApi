import type { CheckStatus } from "../../entities/monitoring/types";

export function formatDate(value: string | Date | null | undefined): string {
  if (!value) return "-";
  return new Intl.DateTimeFormat("pt-BR", {
    day: "2-digit", month: "2-digit", hour: "2-digit", minute: "2-digit",
  }).format(new Date(value));
}

export function formatTime(value: Date): string {
  return new Intl.DateTimeFormat("pt-BR", { hour: "2-digit", minute: "2-digit" }).format(value);
}

export const statusWeight: Record<CheckStatus, number> = {
  DOWN: 1,
  SLOW: 2,
  UNKNOWN: 3,
  UP: 4,
};

export function errorMessage(error: unknown): string {
  return error instanceof Error ? error.message : "Erro inesperado.";
}
