import { describe, expect, it } from "vitest";
import { errorMessage, statusWeight } from "./formatters";

describe("formatters", () => {
  it("prioriza incidentes antes de estados saudaveis", () => {
    expect(statusWeight.DOWN).toBeLessThan(statusWeight.SLOW);
    expect(statusWeight.SLOW).toBeLessThan(statusWeight.UP);
  });

  it("normaliza erros desconhecidos", () => {
    expect(errorMessage(new Error("Falha de rede"))).toBe("Falha de rede");
    expect(errorMessage(null)).toBe("Erro inesperado.");
  });
});
