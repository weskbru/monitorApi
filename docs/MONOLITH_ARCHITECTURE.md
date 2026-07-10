# Arquitetura Monolito

O Monitor API sera mantido como um monolito de produto: um unico repositorio,
uma unica base de evolucao e uma fronteira clara entre backend e frontend.

O backend Spring Boot fica em `backend/`. O frontend fica em `frontend/`.
Mesmo separados em pastas, eles continuam fazendo parte do mesmo produto e do
mesmo repositorio.

## Estrutura inicial

```text
monitorApi/
  backend/
    pom.xml
    src/
      main/
      test/
  frontend/
    src/
      app/
      pages/
      features/
      entities/
      shared/
  docs/
```

## Backend

Responsabilidades:

- expor API REST;
- manter regras de negocio;
- persistir dados;
- executar verificacoes HTTP;
- classificar resultados como `UP`, `SLOW` ou `DOWN`;
- salvar historico e status atual;
- agrupar endpoints criticos por sistema monitorado.

Camadas atuais:

- `controller`: entrada HTTP;
- `dto`: contratos de entrada e saida;
- `service`: regras e orquestracao;
- `repository`: acesso ao banco;
- `entity`: modelo persistido;
- `client`: chamadas HTTP externas;
- `scheduler`: execucao automatica;
- `shared`: configuracoes e tratamento global.

## Frontend

Responsabilidades:

- mostrar o estado operacional das APIs monitoradas;
- permitir cadastro, edicao, ativacao e desativacao;
- executar verificacao manual;
- mostrar historico, status atual e indicadores;
- consumir o backend por HTTP.

O frontend sera separado por camadas para crescer sem virar uma tela unica com
toda a logica misturada.

## Fronteira entre front e back

O backend nao deve conhecer detalhes de layout.

O frontend nao deve duplicar regra de negocio central, como classificacao final
de status. Ele pode apenas adaptar dados para visualizacao, como cor do badge ou
texto curto na tela.

## Decisoes

- Nao usar microservicos nesta fase.
- Nao misturar codigo React dentro de `backend/src/main/resources`.
- Manter o backend em `backend/`.
- Manter o frontend em `frontend/`.
- Consumir o backend em desenvolvimento via `http://localhost:8090`.
- Subir frontend, backend e banco juntos pelo `docker-compose.yml` da raiz.

## Docker Compose

Servicos atuais:

- `frontend`: interface web servida por nginx em `http://localhost:3000`.
- `api`: backend Spring Boot em `http://localhost:8090`.
- `postgres`: banco PostgreSQL exposto localmente na porta `5434`.

O frontend faz proxy de `/api` para o container `api`, evitando problema de CORS
durante o desenvolvimento local.

## Evolucao de Produto

A proxima evolucao do dominio esta documentada em
`docs/SYSTEM_MONITORING_MODEL.md`.

O objetivo e tratar o sistema monitorado como agrupador principal, com varios
endpoints criticos vinculados a ele.
