import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { StatusBadge } from "./StatusBadge";

describe("StatusBadge", () => {
  it("mostra o status recebido", () => {
    render(<StatusBadge status="DOWN" />);
    expect(screen.getByText("DOWN")).toBeTruthy();
  });

  it("usa UNKNOWN quando nao ha leitura", () => {
    render(<StatusBadge />);
    expect(screen.getByText("SEM LEITURA")).toBeTruthy();
  });
});
