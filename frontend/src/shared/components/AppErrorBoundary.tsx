import { Component, type ErrorInfo, type ReactNode } from "react";
import { Alert, AlertTitle } from "@mui/material";

interface Props { children: ReactNode }
interface State { failed: boolean }

export class AppErrorBoundary extends Component<Props, State> {
  state: State = { failed: false };

  static getDerivedStateFromError(): State {
    return { failed: true };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error("Falha inesperada na interface", error, info);
  }

  render() {
    if (this.state.failed) {
      return <Alert severity="error"><AlertTitle>Falha inesperada</AlertTitle>A interface encontrou um erro inesperado. Atualize a página para tentar novamente.</Alert>;
    }
    return this.props.children;
  }
}
