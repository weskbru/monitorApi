# Frontend

Frontend do Monitor API.

Este diretorio vai conter a aplicacao web separada do backend Spring Boot, mas
dentro do mesmo repositorio do produto.

## Stack atual

- React
- TypeScript
- Vite
- Material UI 9 e Material Icons
- MUI X Charts para visualizacao operacional
- Emotion para o tema e estilos tipados
- React Router
- TanStack Query
- React Hook Form
- Vitest e Testing Library
- ESLint com regras de React Hooks e TanStack Query

## Estado atual

O frontend e compilado pelo Vite e servido pelo nginx. O TypeScript usa modo
`strict`, e o backend continua sendo a fonte das regras de negocio.
O design system parte do template oficial de marketing do Material UI, adaptado
para uma aplicacao operacional com tema claro/escuro e navegacao responsiva.

A primeira tela ja funciona como dashboard operacional:

- lista sistemas monitorados vindos do backend;
- consulta o status agregado de cada sistema;
- mostra contadores de sistemas `UP`, `SLOW`, `DOWN` e `UNKNOWN`;
- permite selecionar um sistema;
- mostra os endpoints criticos do sistema selecionado;
- destaca prioridades operacionais por sistema.
- apresenta distribuicao de status, endpoints por sistema e tendencia de tempo de resposta.

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

## Validacao isolada com Docker

```bash
docker run --rm -v "$PWD/frontend:/app" -v /app/node_modules -w /app node:24-alpine \
  sh -c 'npm ci && npm run lint && npm run test && npm run build'
```

## Camadas

```text
src/
  app/        inicializacao, rotas e providers
  pages/      composicao das telas
  features/   casos de uso e componentes por funcionalidade
  entities/   contratos TypeScript do dominio
  shared/     HTTP, estilos, componentes e utilitarios reutilizaveis
```

### `app`

Base da aplicacao.

Responsabilidades:

- inicializacao;
- rotas;
- configuracao global.

### `pages`

Telas completas da aplicacao.

Implementado:

- `pages/dashboard`: dashboard operacional;
- `pages/register`: cadastro de sistemas e endpoints.

### `features`

Organiza os componentes e a comunicacao de cada funcionalidade:

- cadastrar sistema monitorado;
- cadastrar API monitorada;
- editar API monitorada;
- ativar ou desativar API;
- executar verificacao manual;
- filtrar APIs por status.

### `entities`

Representacoes tipadas dos contratos expostos pelo backend:

- `monitored-system`;
- `monitored-api`;
- `api-check`;
- `api-current-status`;
- `api-check-history`.

### `shared`

Codigo reutilizavel e sem dependencia direta de uma regra especifica.

- `shared/api/http.ts`: cliente HTTP generico e tipado;
- `shared/components`: componentes visuais reutilizaveis;
- `shared/utils`: formatacao e tratamento seguro de erros;
- estilos globais responsivos.

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
