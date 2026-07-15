import { lazy, Suspense, useState } from "react";
import { Close, DarkModeOutlined, DashboardOutlined, LightModeOutlined, Menu, SettingsOutlined } from "@mui/icons-material";
import { AppBar, Box, Button, CircularProgress, Container, Divider, Drawer, IconButton, Stack, Toolbar, Typography } from "@mui/material";
import { NavLink, Navigate, Route, Routes } from "react-router-dom";
import { AppErrorBoundary } from "../shared/components/AppErrorBoundary";
import { useColorMode } from "./colorMode";

const DashboardPage = lazy(() => import("../pages/DashboardPage").then((module) => ({ default: module.DashboardPage })));
const RegisterPage = lazy(() => import("../pages/RegisterPage").then((module) => ({ default: module.RegisterPage })));
const drawerWidth = 260;

const navigation = [
  { to: "/", label: "Dashboard", icon: <DashboardOutlined fontSize="small" />, end: true },
  { to: "/cadastro", label: "Gestão", icon: <SettingsOutlined fontSize="small" />, end: false },
];

function SidebarContent({ onClose, onNavigate }: { onClose: () => void; onNavigate?: () => void }) {
  return <Stack sx={{ height: "100%", p: 2 }}>
    <Stack direction="row" spacing={1.25} sx={{ alignItems: "center", px: 1, py: 1 }}>
      <Box sx={{ display: "grid", placeItems: "center", width: 40, height: 40, borderRadius: 2, bgcolor: "secondary.main", color: "common.white", fontWeight: 900 }}>MA</Box>
      <Box><Typography sx={{ fontWeight: 800, lineHeight: 1.1 }}>Monitor API</Typography><Typography variant="caption" color="text.secondary">Command Center</Typography></Box>
      <Box sx={{ flexGrow: 1 }} />
      <IconButton onClick={onClose} aria-label="Fechar menu lateral"><Close /></IconButton>
    </Stack>
    <Divider sx={{ my: 2 }} />
    <Typography variant="overline" color="text.secondary" sx={{ px: 1.5, mb: 0.75, fontWeight: 800 }}>Menu principal</Typography>
    <Stack spacing={0.75} component="nav" aria-label="Navegação principal">
      {navigation.map((item) => <Button key={item.to} component={NavLink} to={item.to} end={item.end} onClick={onNavigate} startIcon={item.icon} color="inherit"
        sx={{ justifyContent: "flex-start", px: 1.5, py: 1.1, "&.active": { bgcolor: "primary.main", color: "primary.contrastText", "&:hover": { bgcolor: "primary.dark" } } }}>
        {item.label}
      </Button>)}
    </Stack>
    <Box sx={{ flexGrow: 1 }} />
    <Box sx={{ borderRadius: 2, bgcolor: "action.hover", p: 1.5 }}><Typography variant="caption" color="text.secondary">Monitoramento operacional</Typography><Typography variant="body2" sx={{ mt: 0.25, fontWeight: 700 }}>Ambiente em tempo real</Typography></Box>
  </Stack>;
}

export function App() {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [desktopOpen, setDesktopOpen] = useState(true);
  const { mode, toggleMode } = useColorMode();

  return <Box sx={{ display: "flex", minHeight: "100vh" }}>
    <Box component="aside" sx={{ width: { sm: desktopOpen ? drawerWidth : 0 }, flexShrink: { sm: 0 }, transition: (theme) => theme.transitions.create("width") }}>
      <Drawer variant="temporary" open={mobileOpen} onClose={() => setMobileOpen(false)} ModalProps={{ keepMounted: true }}
        slotProps={{ paper: { sx: { width: drawerWidth } } }} sx={{ display: { xs: "block", sm: "none" } }}>
        <SidebarContent onClose={() => setMobileOpen(false)} onNavigate={() => setMobileOpen(false)} />
      </Drawer>
      <Drawer variant="persistent" open={desktopOpen} slotProps={{ paper: { sx: { width: drawerWidth, borderRight: 1, borderColor: "divider" } } }} sx={{ display: { xs: "none", sm: "block" } }}>
        <SidebarContent onClose={() => setDesktopOpen(false)} />
      </Drawer>
    </Box>
    <Box sx={{ display: "flex", flexDirection: "column", flexGrow: 1, minWidth: 0 }}>
      <AppBar position="sticky" color="transparent" elevation={0} sx={{ borderBottom: 1, borderColor: "divider", backdropFilter: "blur(14px)", bgcolor: "rgba(var(--mui-palette-background-defaultChannel) / 0.85)" }}>
        <Toolbar sx={{ minHeight: 68, gap: 1 }}>
          <IconButton sx={{ display: { sm: "none" } }} onClick={() => setMobileOpen(true)} aria-label="Abrir menu lateral"><Menu /></IconButton>
          <IconButton sx={{ display: { xs: "none", sm: desktopOpen ? "none" : "inline-flex" } }} onClick={() => setDesktopOpen(true)} aria-label="Abrir menu lateral"><Menu /></IconButton>
          <Typography variant="body2" color="text.secondary" sx={{ flexGrow: 1 }}>Central de observabilidade</Typography>
          <IconButton onClick={toggleMode} aria-label={mode === "light" ? "Ativar tema escuro" : "Ativar tema claro"}>{mode === "light" ? <DarkModeOutlined /> : <LightModeOutlined />}</IconButton>
        </Toolbar>
      </AppBar>
      <Container component="main" maxWidth="xl" sx={{ py: { xs: 3, md: 5 } }}>
        <AppErrorBoundary><Suspense fallback={<Stack sx={{ alignItems: "center", py: 10 }}><CircularProgress aria-label="Carregando página" /></Stack>}><Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/cadastro" element={<RegisterPage />} />
          <Route path="*" element={<Navigate replace to="/" />} />
        </Routes></Suspense></AppErrorBoundary>
      </Container>
    </Box>
  </Box>;
}
