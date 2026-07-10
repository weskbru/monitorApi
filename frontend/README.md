# Frontend

Frontend do Monitor API.

Este diretorio vai conter a aplicacao web separada do backend Spring Boot, mas
dentro do mesmo repositorio do produto.

## Stack planejada

- React
- TypeScript
- Vite
- Tailwind CSS
- shadcn/ui

## Estado atual

O frontend esta sendo servido como uma tela estatica por nginx. Isso permite
subir frontend e backend juntos com Docker enquanto a base React/Vite ainda nao
foi criada.

A primeira tela ja funciona como dashboard operacional:

- lista sistemas monitorados vindos do backend;
- consulta o status agregado de cada sistema;
- mostra contadores de sistemas `UP`, `SLOW`, `DOWN` e `UNKNOWN`;
- permite selecionar um sistema;
- mostra os endpoints criticos do sistema selecionado;
- destaca prioridades operacionais por sistema.

A tela de cadastro tambem ja esta disponivel:

- cria sistemas monitorados;
- lista sistemas cadastrados;
- permite escolher um sistema existente;
- cadastra endpoints criticos vinculados ao sistema escolhido.

Comando na raiz do projeto:

```bash
docker compose up --build
```

URLs:

- frontend: `http://localhost:3000`
- backend: `http://localhost:8090`

O nginx do frontend encaminha chamadas para `/api` ao servico `api` do Docker
Compose.

## Camadas

```text
src/
  app/
  pages/
  shared/
```

### `app`

Base da aplicacao.

Responsabilidades:

- inicializacao;
- rotas;
- configuracao global.

### `pages`

Telas completas da aplicacao.

Implementado agora:

- `pages/dashboard`: dashboard operacional;
- `pages/register`: cadastro de sistemas e endpoints.

### `features`

Camada planejada para quando a tela crescer e os fluxos precisarem sair das
pages.

Exemplos planejados:

- cadastrar sistema monitorado;
- cadastrar API monitorada;
- editar API monitorada;
- ativar ou desativar API;
- executar verificacao manual;
- filtrar APIs por status.

### `entities`

Representacoes do dominio no frontend.

Exemplos planejados:

- `monitored-system`;
- `monitored-api`;
- `api-check`;
- `api-current-status`;
- `api-check-history`.

### `shared`

Codigo reutilizavel e sem dependencia direta de uma regra especifica.

Implementado agora:

- `shared/api/http.js`: cliente HTTP simples;
- `shared/ui/formatters.js`: helpers de data, escape HTML e badges de status;
- constantes;
- estilos globais.

## Identidade visual

O layout sera proprio do Monitor API, usando referencias de ferramentas como
UptimeRobot, Better Stack, Grafana e Checkly apenas como inspiracao.

Direcao:

- produto tecnico e operacional;
- dashboard como primeira tela;
- leitura rapida de status;
- tabela densa e clara;
- badges fortes para `UP`, `SLOW` e `DOWN`;
- visual limpo, sem cara de landing page.
