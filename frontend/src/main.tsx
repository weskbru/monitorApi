import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { QueryClientProvider } from "@tanstack/react-query";
import { App } from "./app/App";
import { AppTheme } from "./app/AppTheme";
import { queryClient } from "./app/queryClient";
import "./shared/styles.css";

const root = document.getElementById("root");

if (!root) {
  throw new Error("Elemento raiz do frontend nao encontrado.");
}

createRoot(root).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AppTheme><App /></AppTheme>
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>,
);
