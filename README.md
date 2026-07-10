# Monitor API

Backend em Spring Boot para cadastrar APIs e acompanhar se sistemas importantes
estao funcionando, lentos ou indisponiveis.

O projeto nasceu de uma dor real: alguns sistemas rodam em producao, mas a
equipe de desenvolvimento nao tem visibilidade suficiente sobre o ambiente e so
descobre problemas quando usuarios reclamam ou quando a infraestrutura avisa
tarde demais.

O Monitor API atua como um observador externo. Ele chama endpoints importantes,
mede tempo de resposta, registra o resultado e ajuda a responder perguntas como:

- a API esta respondendo?
- ela retornou o status esperado?
- ela esta lenta demais?
- quando ela falhou ou voltou a funcionar?

Este projeto tambem esta sendo usado como laboratorio de aprendizado em Java,
Spring Boot, API REST, persistencia, validacoes, Docker e boas praticas.

A visao completa do produto esta em `docs/PROJECT_VISION.md`.

## O que foi criado ate agora

- Aplicacao Spring Boot com Java 17.
- Configuracao com Docker e Docker Compose.
- Banco PostgreSQL para persistencia.
- Entidade `MonitoredApi`.
- Repository com Spring Data JPA.
- Service para concentrar o fluxo de negocio.
- Controller REST para expor endpoints HTTP.
- DTO `CreateMonitoredApiRequest` para entrada de dados.
- Validacoes com Bean Validation.
- Cadastro, listagem, busca por id, atualizacao e remocao de APIs monitoradas.
- Ativacao e desativacao de APIs monitoradas.
- Vinculo de APIs monitoradas a sistemas monitorados.
- Cadastro e listagem de APIs dentro de um sistema monitorado.
- Consulta do status agregado de um sistema monitorado.
- Dashboard frontend baseado em sistemas monitorados e endpoints por sistema.
- Verificacao manual de disponibilidade de uma API cadastrada.
- Classificacao da verificacao como `UP`, `SLOW` ou `DOWN`.
- Mensagem amigavel para explicar o resultado da verificacao.
- Historico de verificacoes por API monitorada.
- Consulta do status atual, baseada na ultima verificacao salva.
- Cadastro, listagem, busca, atualizacao, ativacao/desativacao e remocao de sistemas monitorados.
- Tratamento global de erros de validacao.
- Documentacao OpenAPI em `/v3/api-docs`.
- Swagger UI estatico em `/swagger-ui/index.html`.

## Endpoints atuais

### Cadastrar sistema monitorado

```http
POST /api/monitored-systems
```

Exemplo de corpo valido:

```json
{
  "name": "Sistema Financeiro",
  "baseUrl": "https://financeiro.empresa.com",
  "description": "Sistema responsavel por pagamentos e conciliacao"
}
```

Regras atuais:

- `name` nao pode ser vazio.
- `baseUrl` nao pode ser vazia.
- `baseUrl` precisa comecar com `http://` ou `https://`.
- `description` e opcional.

Campos retornados pela API:

```json
{
  "id": 1,
  "name": "Sistema Financeiro",
  "baseUrl": "https://financeiro.empresa.com",
  "description": "Sistema responsavel por pagamentos e conciliacao",
  "active": true,
  "createdAt": "2026-07-09T22:10:00"
}
```

### Listar sistemas monitorados

```http
GET /api/monitored-systems
```

### Buscar sistema monitorado por id

```http
GET /api/monitored-systems/{id}
```

Se o `id` nao existir, a API retorna `404 Not Found`.

### Atualizar sistema monitorado

```http
PUT /api/monitored-systems/{id}
```

Exemplo de corpo valido:

```json
{
  "name": "Sistema Financeiro",
  "baseUrl": "https://financeiro.empresa.com",
  "description": "Sistema financeiro principal"
}
```

### Ativar ou desativar sistema monitorado

```http
PATCH /api/monitored-systems/{id}/active
```

Exemplo de corpo valido:

```json
{
  "active": false
}
```

### Remover sistema monitorado

```http
DELETE /api/monitored-systems/{id}
```

### Consultar status agregado de um sistema

```http
GET /api/monitored-systems/{id}/status
```

Esse endpoint calcula a saude do sistema usando apenas as APIs ativas
vinculadas a ele.

Regras atuais:

- se algum endpoint ativo estiver `DOWN`, o sistema fica `DOWN`;
- se nao houver `DOWN`, mas existir endpoint `SLOW`, o sistema fica `SLOW`;
- se faltar leitura em algum endpoint ativo, sem falha conhecida, o sistema fica `UNKNOWN`;
- se todos os endpoints ativos estiverem `UP`, o sistema fica `UP`;
- se nao houver endpoint ativo, o sistema fica `UNKNOWN`.

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

### Cadastrar API monitorada

```http
POST /api/monitored-apis
```

Exemplo de corpo valido:

```json
{
  "name": "ViaCEP",
  "url": "https://viacep.com.br/ws/01001000/json/",
  "systemId": 1,
  "description": "API publica para consulta de CEP"
}
```

Regras atuais:

- `name` nao pode ser vazio.
- `url` nao pode ser vazia.
- `url` precisa comecar com `http://` ou `https://`.
- `systemId` e obrigatorio e precisa apontar para um sistema monitorado existente.
- `description` e opcional.

Campos retornados pela API:

```json
{
  "id": 1,
  "name": "ViaCEP",
  "url": "https://viacep.com.br/ws/01001000/json/",
  "description": "API publica para consulta de CEP",
  "active": true,
  "monitoredSystem": {
    "id": 1,
    "name": "Sistema Financeiro",
    "baseUrl": "https://financeiro.empresa.com",
    "description": "Sistema responsavel por pagamentos e conciliacao",
    "active": true,
    "createdAt": "2026-07-09T22:10:00"
  },
  "createdAt": "2026-06-23T20:30:00"
}
```

### Cadastrar API monitorada dentro de um sistema

```http
POST /api/monitored-systems/{systemId}/apis
```

Exemplo de corpo valido:

```json
{
  "name": "Login",
  "url": "https://financeiro.empresa.com/api/auth/status",
  "description": "Endpoint critico de login"
}
```

### Listar APIs de um sistema

```http
GET /api/monitored-systems/{systemId}/apis
```

### Buscar API de um sistema por id

```http
GET /api/monitored-systems/{systemId}/apis/{apiId}
```

### Atualizar API de um sistema

```http
PUT /api/monitored-systems/{systemId}/apis/{apiId}
```

### Ativar ou desativar API de um sistema

```http
PATCH /api/monitored-systems/{systemId}/apis/{apiId}/active
```

### Remover API de um sistema

```http
DELETE /api/monitored-systems/{systemId}/apis/{apiId}
```

### Listar APIs cadastradas

```http
GET /api/monitored-apis
```

### Buscar API por id

```http
GET /api/monitored-apis/{id}
```

Se o `id` nao existir, a API retorna `404 Not Found`.

### Atualizar API monitorada

```http
PUT /api/monitored-apis/{id}
```

Exemplo de corpo valido:

```json
{
  "name": "ViaCEP",
  "url": "https://viacep.com.br/ws/01001000/json/",
  "systemId": 1,
  "description": "Consulta de endereco por CEP"
}
```

Regras atuais:

- `id` e recebido pela URL.
- `name` nao pode ser vazio.
- `url` nao pode ser vazia.
- `url` precisa comecar com `http://` ou `https://`.
- `systemId` e obrigatorio.
- `description` e opcional.
- Se o `id` nao existir, a API retorna `404 Not Found`.

### Remover API monitorada

```http
DELETE /api/monitored-apis/{id}
```

Regras atuais:

- `id` e recebido pela URL.
- Se encontrar a API, ela e removida do banco.
- Se o `id` nao existir, a API retorna `404 Not Found`.

### Ativar ou desativar API monitorada

```http
PATCH /api/monitored-apis/{id}/active
```

Exemplo de corpo valido:

```json
{
  "active": false
}
```

Regras atuais:

- `id` e recebido pela URL.
- `active` e obrigatorio.
- Quando `active` for `false`, a API monitorada nao participa da verificacao automatica.
- Uma API inativa nao pode ser verificada manualmente.
- Se o `id` nao existir, a API retorna `404 Not Found`.

### Verificar disponibilidade manualmente

```http
POST /api/monitored-apis/{id}/check
```

Esse endpoint busca a API cadastrada pelo `id`, faz uma chamada HTTP para a URL
salva, mede o tempo de resposta, classifica o resultado e salva um registro no
historico.

Exemplo de resposta para uma API disponivel:

```json
{
  "apiId": 8,
  "name": "httpbin",
  "url": "https://httpbin.org",
  "available": true,
  "status": "UP",
  "message": "O endpoint respondeu normalmente.",
  "statusCode": 200,
  "responseTimeMs": 796,
  "checkedAt": "2026-06-24T01:34:53.935211809",
  "errorMessage": null
}
```

Campos da resposta:

- `apiId`: id da API cadastrada.
- `name`: nome da API cadastrada.
- `url`: URL testada.
- `available`: indica se a API respondeu com sucesso.
- `status`: status de negocio da verificacao: `UP`, `SLOW` ou `DOWN`.
- `message`: mensagem amigavel sobre o resultado.
- `statusCode`: codigo HTTP retornado pela API testada.
- `responseTimeMs`: tempo de resposta em milissegundos.
- `checkedAt`: data e hora da verificacao.
- `errorMessage`: mensagem de erro, quando a verificacao falha.

Regras atuais de classificacao:

- `UP`: endpoint respondeu com sucesso dentro do tempo esperado.
- `SLOW`: endpoint respondeu com sucesso, mas demorou mais que 3000ms.
- `DOWN`: endpoint nao respondeu corretamente, retornou erro ou falhou na chamada.

### Consultar historico de verificacoes

```http
GET /api/monitored-apis/{id}/history
```

Esse endpoint consulta os registros de verificacao ja salvos para uma API
monitorada. Ele nao executa uma nova verificacao.

Exemplo de resposta:

```json
[
  {
    "id": 6,
    "status": "SLOW",
    "message": "O endpoint respondeu, mas esta lento.",
    "available": true,
    "statusCode": 200,
    "responseTimeMs": 5769,
    "checkedAt": "2026-06-26T02:10:48.309818",
    "errorMessage": null
  }
]
```

Os registros sao retornados do mais recente para o mais antigo.

### Consultar status atual

```http
GET /api/monitored-apis/{id}/status
```

Esse endpoint consulta a ultima verificacao salva para uma API monitorada. Ele
nao chama a API externa novamente e nao cria um novo registro de historico.

Exemplo de resposta:

```json
{
  "id": 6,
  "status": "SLOW",
  "message": "O endpoint respondeu, mas esta lento.",
  "available": true,
  "statusCode": 200,
  "responseTimeMs": 5769,
  "checkedAt": "2026-06-26T02:10:48.309818",
  "errorMessage": null
}
```

Resumo da diferenca entre endpoints:

- `POST /api/monitored-apis/{id}/check`: executa uma nova verificacao.
- `GET /api/monitored-apis/{id}/status`: consulta a ultima verificacao salva.
- `GET /api/monitored-apis/{id}/history`: consulta o historico de verificacoes.

## Decisao tecnica: estado atual e historico

Para evitar crescimento excessivo da tabela de historico, o projeto deve separar
o estado atual da API monitorada do historico de eventos.

Regra mental:

- `ApiCurrentStatus` mostra o agora.
- `ApiCheckHistory` explica quando algo mudou ou registra uma amostra periodica.

Toda verificacao deve atualizar `ApiCurrentStatus`.

`ApiCheckHistory` so deve ser salvo quando houver mudanca relevante ou amostra
periodica.

Uma mudanca e considerada relevante quando:

- o status de negocio mudou;
- a categoria do `statusCode` mudou;
- a `errorMessage` mudou;
- passou 1 hora desde a ultima amostra salva.

Se mais de uma mudanca acontecer na mesma verificacao, deve ser salvo apenas um
registro de historico com todos os motivos.

Categorias de `statusCode`:

- `null`: sem resposta.
- `1xx`: informativo.
- `2xx`: sucesso.
- `3xx`: redirecionamento.
- `4xx`: erro do cliente.
- `5xx`: erro do servidor.
- `outros`: desconhecido.

Exemplos de motivos para salvar historico:

- `STATUS_CHANGED`
- `STATUS_CODE_CATEGORY_CHANGED`
- `ERROR_CHANGED`
- `PERIODIC_SAMPLE`

## Metodos implementados no modulo `MonitoredApi`

Controller:

- `POST /api/monitored-apis`: chama `createMonitoredApi`.
- `GET /api/monitored-apis`: chama `listAll`.
- `GET /api/monitored-apis/{id}`: chama `getMonitoredApiById`.
- `PUT /api/monitored-apis/{id}`: chama `updateMonitoredApi`.
- `DELETE /api/monitored-apis/{id}`: chama `deleteMonitoredApi`.
- `PATCH /api/monitored-apis/{id}/active`: chama `updateActive`.
- `POST /api/monitored-apis/{id}/check`: chama `checkMonitoredApi`.
- `GET /api/monitored-apis/{id}/status`: chama `getCurrentStatus`.
- `GET /api/monitored-apis/{id}/history`: chama `getHistory`.

Service:

- `create(name, url, description)`: cria uma nova API monitorada.
- `listAll()`: lista todas as APIs monitoradas.
- `getById(id)`: busca uma API monitorada pelo id.
- `update(id, name, url, description)`: atualiza nome, URL e descricao.
- `delete(id)`: remove uma API monitorada.
- `updateActive(id, active)`: ativa ou desativa uma API monitorada.
- `check(id)`: executa a verificacao manual da URL cadastrada.
- `getCurrentStatus(id)`: consulta a ultima verificacao salva.
- `getHistory(id)`: consulta o historico de verificacoes.

Repository:

- `MonitoredApiRepository extends JpaRepository<MonitoredApi, Long>`.
- Ja herda metodos como `save`, `findAll`, `findById` e `delete`.
- `ApiCurrentStatusRepository` busca o status atual por API monitorada.
- `ApiCheckHistoryRepository` busca historico por API monitorada.

## Mapa dos arquivos do projeto

Arquivo:

`MonitorApplication.java`

Funcao:

iniciar a aplicacao Spring Boot e ativar o agendamento automatico.

Arquivo:

`shared/GlobalExceptionHandler.java`

Funcao:

centralizar o tratamento de erros da API.

Arquivo:

`shared/config/RestTemplateConfig.java`

Funcao:

configurar o `RestTemplate` usado para chamar APIs externas.

Arquivo:

`modules/monitoredapi/CheckStatus.java`

Funcao:

representar os status possiveis de uma verificacao: `UP`, `SLOW` e `DOWN`.

Arquivo:

`modules/monitoredapi/entity/MonitoredApi.java`

Funcao:

representar a API monitorada salva no banco de dados.

Arquivo:

`modules/monitoredapi/entity/ApiCheckHistory.java`

Funcao:

representar um registro de historico de verificacao salvo no banco.

Arquivo:

`modules/monitoredapi/controllers/MonitoredApiController.java`

Funcao:

expor os endpoints HTTP para cadastrar, listar, atualizar, remover e verificar APIs.

Arquivo:

`modules/monitoredapi/service/MonitoredApiService.java`

Funcao:

concentrar as regras de negocio do cadastro de APIs monitoradas.

Arquivo:

`modules/monitoredapi/service/ApiCheckService.java`

Funcao:

executar a verificacao de uma API, classificar o resultado e salvar o historico.

Arquivo:

`modules/monitoredapi/service/AutomatedApiCheckService.java`

Funcao:

buscar APIs ativas e executar a verificacao automatica de cada uma.

Arquivo:

`modules/monitoredapi/scheduler/ApiCheckScheduler.java`

Funcao:

agendar a execucao periodica da verificacao automatica.

Arquivo:

`modules/monitoredapi/repository/MonitoredApiRepository.java`

Funcao:

buscar, salvar, atualizar, remover APIs monitoradas e buscar APIs ativas.

Arquivo:

`modules/monitoredapi/repository/ApiCheckHistoryRepository.java`

Funcao:

buscar historico e status atual das verificacoes.

Arquivo:

`modules/monitoredapi/dto/CreateMonitoredApiRequest.java`

Funcao:

representar os dados enviados para criar ou atualizar uma API monitorada.

Arquivo:

`modules/monitoredapi/dto/UpdateMonitoredApiActiveRequest.java`

Funcao:

representar o dado enviado para ativar ou desativar uma API monitorada.

Arquivo:

`modules/monitoredapi/dto/ApiCheckResponse.java`

Funcao:

representar a resposta de uma verificacao executada.

Arquivo:

`modules/monitoredapi/dto/ApiCheckHistoryResponse.java`

Funcao:

representar a resposta do historico ou status atual de uma API monitorada.

Arquivo:

`modules/monitoredapi/exception/MonitoredApiNotFoundException.java`

Funcao:

indicar que uma API monitorada nao foi encontrada.

Arquivo:

`modules/monitoredapi/exception/ApiCheckHistoryNotFoundException.java`

Funcao:

indicar que uma API monitorada ainda nao possui historico de verificacao.

Arquivo:

`modules/monitoredapi/exception/MonitoredApiInactiveException.java`

Funcao:

indicar que uma API inativa nao pode ser verificada manualmente.

Arquivo:

`backend/src/main/resources/application.properties`

Funcao:

guardar configuracoes da aplicacao, banco, porta e scheduler.

Arquivo:

`backend/src/main/resources/static/swagger-ui/index.html`

Funcao:

abrir a interface Swagger UI estatica.

Arquivo:

`MonitorApplicationTests.java`

Funcao:

testar se o contexto da aplicacao Spring carrega corretamente.

Arquivo:

`modules/monitoredapi/controllers/MonitoredApiControllerTest.java`

Funcao:

testar os endpoints do controller sem subir a aplicacao inteira.

Arquivo:

`modules/monitoredapi/service/MonitoredApiServiceTest.java`

Funcao:

testar as regras de negocio do cadastro de APIs monitoradas.

Arquivo:

`modules/monitoredapi/service/ApiCheckServiceTest.java`

Funcao:

testar as regras de verificacao, status e historico das APIs.

Arquivo:

`modules/monitoredapi/repository/ApiCheckHistoryRepositoryTest.java`

Funcao:

testar as consultas de historico usando banco de teste.

Arquivo:

`backend/src/test/resources/application-test.properties`

Funcao:

guardar configuracoes usadas somente nos testes automatizados.

## Erros de validacao

Quando o cadastro recebe dados invalidos, a API retorna `400 Bad Request` com uma
mensagem clara.

Exemplo de entrada invalida:

```json
{
  "name": "",
  "url": "viacep.com.br"
}
```

Exemplo de resposta:

```json
{
  "message": "Erro de validacao",
  "errors": {
    "name": "must not be blank",
    "url": "URL deve começar com http:// ou https://"
  }
}
```

## Conceitos aprendidos

- API REST: expor recursos do sistema por HTTP.
- Controller: recebe requisicoes e devolve respostas.
- Service: organiza o fluxo de negocio.
- Repository: acessa o banco usando Spring Data JPA.
- Entity: representa uma tabela no banco.
- DTO: representa os dados de entrada da API.
- DTO de saida: controla quais dados sao devolvidos para quem consome a API.
- Persistencia: salvar e buscar dados no PostgreSQL.
- Bean Validation: validar dados com anotacoes como `@NotBlank` e `@Pattern`.
- `@Valid`: manda o Spring validar o DTO antes de chamar o service.
- `@RestControllerAdvice`: centraliza tratamento de erros dos controllers.
- Enum: representa um conjunto fechado de estados, como `UP`, `SLOW` e `DOWN`.
- Query methods: metodos do Spring Data JPA gerados a partir do nome.
- Docker Compose: sobe frontend, API e banco juntos para desenvolvimento.

## Como rodar

```bash
docker compose up --build
```

API:

```text
http://localhost:8090
```

Frontend:

```text
http://localhost:3000
```

Swagger UI:

```text
http://localhost:8090/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8090/v3/api-docs
```

## Testes feitos manualmente

- Cadastro valido pelo Postman.
- Listagem das APIs cadastradas.
- Busca por id.
- Atualizacao de API cadastrada.
- Remocao de API cadastrada.
- Ativacao e desativacao de API monitorada.
- Verificacao manual com API real usando `POST /api/monitored-apis/{id}/check`.
- Classificacao `UP` com endpoint rapido.
- Classificacao `SLOW` com endpoint de delay.
- Consulta de historico com `GET /api/monitored-apis/{id}/history`.
- Consulta de status atual com `GET /api/monitored-apis/{id}/status`.
- Validacao de `name` vazio.
- Validacao de `url` vazia.
- Validacao de URL sem `http://` ou `https://`.
- Acesso ao Swagger UI.
- Acesso ao JSON OpenAPI.

## Proximos passos

- Permitir configurar status HTTP esperado por API monitorada.
- Permitir configurar timeout da verificacao.
- Permitir configurar limite de lentidao pelo endpoint de cadastro/atualizacao.
- Consolidar a documentacao conforme novas regras forem implementadas.
