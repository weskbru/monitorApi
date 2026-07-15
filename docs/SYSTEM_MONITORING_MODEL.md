# Modelo de Monitoramento por Sistema

Este documento descreve a proxima evolucao do Monitor API: sair de APIs
monitoradas de forma isolada e passar a monitorar sistemas completos com seus
endpoints criticos.

## Ideia Central

Um usuario pode ter um sistema importante em producao, chamado aqui de
`Sistema X`.

Esse sistema pode ter:

- uma pagina inicial;
- uma API de login;
- uma API de usuarios;
- uma API financeira;
- uma API de relatorios;
- qualquer outro endpoint que represente uma funcionalidade importante.

O Monitor API deve permitir cadastrar o sistema e, dentro dele, cadastrar os
endpoints que precisam ser acompanhados.

Exemplo:

```text
Sistema X
  Pagina inicial: https://sistema-x.com
  Login:          https://sistema-x.com/api/auth/status
  Usuarios:       https://sistema-x.com/api/users/health
  Financeiro:     https://sistema-x.com/api/payments/health
```

## Por Que Isso E Importante

Na pratica, saber que uma URL responde nao e suficiente.

Um sistema pode estar aparentemente no ar, mas uma parte essencial pode estar
com problema. Por exemplo:

- a pagina inicial abre, mas o login falha;
- o login funciona, mas a API financeira esta indisponivel;
- a API responde, mas esta lenta demais;
- apenas um endpoint critico esta quebrado e afeta uma area inteira do negocio.

Por isso, o produto precisa mostrar o estado do sistema e tambem o estado de
cada endpoint critico.

## Modelo de Dominio

### MonitoredSystem

Representa um sistema monitorado.

Campos iniciais:

```text
id
name
description
baseUrl
active
createdAt
```

Exemplo:

```json
{
  "id": 1,
  "name": "Sistema Financeiro",
  "description": "Sistema responsavel por pagamentos e conciliacao",
  "baseUrl": "https://financeiro.empresa.com",
  "active": true,
  "createdAt": "2026-07-09T22:10:00"
}
```

### MonitoredApi

Representa um endpoint critico dentro de um sistema.

A entidade atual `MonitoredApi` passara a pertencer a um `MonitoredSystem`.

Campos atuais e planejados:

```text
id
monitoredSystem
name
url
description
active
expectedStatusCode
slowThresholdMs
timeoutMs
createdAt
```

Exemplo:

```json
{
  "id": 10,
  "systemId": 1,
  "name": "Login",
  "url": "https://financeiro.empresa.com/api/auth/status",
  "description": "Verifica se o login esta operacional",
  "active": true,
  "expectedStatusCode": 200,
  "slowThresholdMs": 3000,
  "timeoutMs": 10000
}
```

## Relacionamento

```text
MonitoredSystem 1 -> N MonitoredApi
```

Ou seja:

- um sistema pode ter varias APIs monitoradas;
- uma API monitorada pertence a um sistema;
- o status geral do sistema e calculado a partir dos endpoints vinculados.

## Status do Endpoint

Cada endpoint continua seguindo a classificacao atual:

```text
UP    - respondeu com o status esperado dentro do tempo aceitavel
SLOW  - respondeu com o status esperado, mas demorou mais que o limite
DOWN  - nao respondeu, retornou erro, status inesperado ou timeout
```

## Status Geral do Sistema

O status geral do sistema deve ser calculado com base no status atual dos seus
endpoints ativos.

Regra inicial:

```text
Se qualquer endpoint ativo estiver DOWN -> sistema DOWN
Se nenhum endpoint estiver DOWN, mas algum estiver SLOW -> sistema SLOW
Se nao houver falha conhecida, mas faltar leitura -> sistema UNKNOWN
Se todos os endpoints ativos estiverem UP -> sistema UP
Se nao houver endpoint ativo -> sistema UNKNOWN
```

Exemplo:

```text
Sistema Financeiro
  Login      UP
  Usuarios   UP
  Pagamentos DOWN

Status geral: DOWN
```

Outro exemplo:

```text
Sistema Financeiro
  Login      UP
  Usuarios   SLOW
  Pagamentos UP

Status geral: SLOW
```

## Endpoints REST Planejados

### Sistemas

```http
POST /api/monitored-systems
GET /api/monitored-systems
GET /api/monitored-systems/{id}
PUT /api/monitored-systems/{id}
DELETE /api/monitored-systems/{id}
PATCH /api/monitored-systems/{id}/active
```

### APIs Dentro de um Sistema

```http
POST /api/monitored-systems/{systemId}/apis
GET /api/monitored-systems/{systemId}/apis
GET /api/monitored-systems/{systemId}/apis/{apiId}
PUT /api/monitored-systems/{systemId}/apis/{apiId}
DELETE /api/monitored-systems/{systemId}/apis/{apiId}
PATCH /api/monitored-systems/{systemId}/apis/{apiId}/active
```

### Verificacoes

```http
POST /api/monitored-systems/{systemId}/apis/{apiId}/check
GET /api/monitored-systems/{systemId}/apis/{apiId}/status
GET /api/monitored-systems/{systemId}/apis/{apiId}/history
```

### Status Agregado do Sistema

```http
GET /api/monitored-systems/{id}/status
```

Exemplo de resposta:

```json
{
  "systemId": 1,
  "name": "Sistema Financeiro",
  "status": "DOWN",
  "message": "Existe endpoint critico indisponivel.",
  "totalEndpoints": 4,
  "upEndpoints": 2,
  "slowEndpoints": 1,
  "downEndpoints": 1,
  "unknownEndpoints": 0,
  "lastCheckedAt": "2026-07-09T22:20:00"
}
```

## Dashboard Planejado

A primeira tela do frontend deve mostrar sistemas, nao apenas endpoints
isolados.

Visao planejada:

```text
Dashboard Operacional

Resumo:
  Sistemas monitorados
  Sistemas UP
  Sistemas SLOW
  Sistemas DOWN
  Endpoints criticos indisponiveis

Lista:
  Sistema Financeiro    DOWN  4 endpoints  1 DOWN
  Sistema Comercial     UP    3 endpoints  0 DOWN
  Sistema Interno       SLOW  5 endpoints  1 SLOW
```

Ao clicar em um sistema:

```text
Detalhe do Sistema

Sistema Financeiro
Status geral: DOWN

Endpoints:
  Login       UP
  Usuarios    UP
  Pagamentos  DOWN
  Relatorios  SLOW

Historico:
  ultimas verificacoes dos endpoints
```

## Ordem de Implementacao

### Fase 1 - Backend de Sistemas

Status: implementada.

Itens incluidos:

- entidade `MonitoredSystem`;
- repository;
- service;
- DTOs de entrada;
- controller REST;
- tratamento global de `MonitoredSystemNotFoundException`;
- testes de service e controller.

### Fase 2 - Relacionar APIs a Sistemas

Status: implementada.

Itens incluidos:

- relacionamento entre `MonitoredApi` e `MonitoredSystem`;
- cadastro legado de API exigindo `systemId`;
- listagem de APIs por sistema;
- busca de API por sistema e id;
- atualizacao, ativacao/desativacao e remocao de API dentro de um sistema;
- endpoints aninhados em `/api/monitored-systems/{systemId}/apis`;
- compatibilidade temporaria com listagem e consulta antigas em `/api/monitored-apis`.

### Fase 3 - Status Agregado

Status: implementada.

Itens incluidos:

- service para calcular status geral do sistema;
- DTO de resposta do status agregado;
- endpoint `GET /api/monitored-systems/{id}/status`;
- contadores de endpoints `UP`, `SLOW`, `DOWN` e `UNKNOWN`;
- data da ultima leitura considerada;
- testes das regras `UP`, `SLOW`, `DOWN` e `UNKNOWN`.

### Fase 4 - Frontend

Status: implementada.

Itens incluidos:

- dashboard mostrando sistemas como entidade principal;
- consulta do status agregado de cada sistema;
- selecao de sistema na tabela principal;
- painel de detalhe do sistema selecionado;
- tabela de endpoints criticos do sistema selecionado;
- fila operacional priorizando `DOWN`, `SLOW` e `UNKNOWN`.
- filtros de sistemas e endpoints;
- verificacao manual pelo dashboard;
- historico por endpoint;
- edicao, ativacao/desativacao e exclusao no cadastro;
- configuracao de status esperado, lentidao e timeout.

### Fase 5 - Confiabilidade Operacional

Status: implementada.

Itens incluidos:

- leituras antigas passam a `UNKNOWN`;
- sistema inativo deixa de participar do agendamento;
- bloqueio por banco evita verificacao duplicada entre instancias;
- protecao contra destinos privados e reservados, configuravel por ambiente;
- migracoes de banco com Flyway e indices de consulta;
- historico paginado com limite de 100 registros por pagina;
- healthcheck e metricas pelo Actuator/Prometheus.

## Decisoes Iniciais

- O sistema sera o agrupador principal do produto.
- Endpoints continuarao sendo verificados individualmente.
- O status do sistema sera calculado, nao salvo manualmente.
- O historico continua pertencendo ao endpoint.
- O dashboard deve priorizar sistemas e depois permitir detalhar endpoints.
- A arquitetura continua monolito, com backend em `backend/` e frontend em
  `frontend/`.

## Fora do Escopo Desta Fase

- autenticacao;
- usuarios;
- permissoes;
- notificacoes externas;
- status page publica;
- multi-tenant;
- metricas avancadas;
- graficos complexos;
- checks com payload, headers ou autenticacao customizada.
