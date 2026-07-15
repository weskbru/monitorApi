import { useMemo, useState, type PropsWithChildren } from "react";
import { CssBaseline, ThemeProvider, createTheme } from "@mui/material";
import type { PaletteMode } from "@mui/material";
import { ColorModeContext } from "./colorMode";

export function AppTheme({ children }: PropsWithChildren) {
  const [mode, setMode] = useState<PaletteMode>(() =>
    window.localStorage.getItem("monitor-api-color-mode") === "dark" ? "dark" : "light",
  );
  const value = useMemo(() => ({
    mode,
    toggleMode: () => setMode((current) => {
      const next = current === "light" ? "dark" : "light";
      window.localStorage.setItem("monitor-api-color-mode", next);
      return next;
    }),
  }), [mode]);
  const theme = useMemo(() => createTheme({
    palette: {
      mode,
      primary: { main: mode === "light" ? "#2563eb" : "#60a5fa" },
      secondary: { main: "#14b8a6" },
      background: mode === "light" ? { default: "#f7f9fc", paper: "#ffffff" } : { default: "#090e1a", paper: "#111827" },
    },
    shape: { borderRadius: 12 },
    typography: {
      fontFamily: 'Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
      h1: { fontSize: "clamp(1.8rem, 4vw, 2.5rem)", fontWeight: 750, letterSpacing: "-0.03em" },
      h2: { fontSize: "1.15rem", fontWeight: 700, letterSpacing: "-0.01em" },
      button: { fontWeight: 700, textTransform: "none" },
    },
    components: {
      MuiButton: { defaultProps: { disableElevation: true }, styleOverrides: { root: { borderRadius: 10 } } },
      MuiCard: { styleOverrides: { root: { backgroundImage: "none", border: "1px solid", borderColor: mode === "light" ? "#e2e8f0" : "#263247", boxShadow: mode === "light" ? "0 1px 2px rgba(15, 23, 42, 0.04)" : "none" } } },
      MuiPaper: { styleOverrides: { root: { backgroundImage: "none" } } },
      MuiTableCell: { styleOverrides: { head: { fontWeight: 700, whiteSpace: "nowrap" }, root: { borderColor: mode === "light" ? "#e8edf4" : "#263247" } } },
      MuiTextField: { defaultProps: { size: "small" } },
      MuiFormControl: { defaultProps: { size: "small" } },
    },
  }), [mode]);

  return <ColorModeContext.Provider value={value}><ThemeProvider theme={theme}><CssBaseline />{children}</ThemeProvider></ColorModeContext.Provider>;
}
