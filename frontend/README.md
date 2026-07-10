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

Nesta fase inicial, o frontend esta sendo servido como uma tela estatica por
nginx. Isso permite subir frontend e backend juntos com Docker enquanto a base
React/Vite ainda nao foi criada.

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
  features/
  entities/
  shared/
```

### `app`

Base da aplicacao.

Responsabilidades:

- inicializacao;
- rotas;
- providers;
- layout principal;
- configuracao global.

### `pages`

Telas completas da aplicacao.

Exemplos planejados:

- dashboard;
- lista de APIs monitoradas;
- detalhe de uma API monitorada;
- historico;
- configuracoes.

### `features`

Fluxos de negocio do frontend.

Exemplos planejados:

- cadastrar API monitorada;
- editar API monitorada;
- ativar ou desativar API;
- executar verificacao manual;
- filtrar APIs por status.

### `entities`

Representacoes do dominio no frontend.

Exemplos planejados:

- `monitored-api`;
- `api-check`;
- `api-current-status`;
- `api-check-history`.

### `shared`

Codigo reutilizavel e sem dependencia direta de uma regra especifica.

Exemplos planejados:

- componentes base de UI;
- cliente HTTP;
- helpers;
- formatadores;
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
