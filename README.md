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
- Verificacao manual de disponibilidade de uma API cadastrada.
- Tratamento global de erros de validacao.
- Documentacao OpenAPI em `/v3/api-docs`.
- Swagger UI estatico em `/swagger-ui/index.html`.

## Endpoints atuais

### Cadastrar API monitorada

```http
POST /api/monitored-apis
```

Exemplo de corpo valido:

```json
{
  "name": "ViaCEP",
  "url": "https://viacep.com.br/ws/01001000/json/",
  "description": "API publica para consulta de CEP"
}
```

Regras atuais:

- `name` nao pode ser vazio.
- `url` nao pode ser vazia.
- `url` precisa comecar com `http://` ou `https://`.
- `description` e opcional.

Campos retornados pela API:

```json
{
  "id": 1,
  "name": "ViaCEP",
  "url": "https://viacep.com.br/ws/01001000/json/",
  "description": "API publica para consulta de CEP",
  "active": true,
  "createdAt": "2026-06-23T20:30:00"
}
```

### Listar APIs cadastradas

```http
GET /api/monitored-apis
```

### Buscar API por id

```http
GET /api/monitored-apis/{id}
```

Observacao: o tratamento de erro para id inexistente ainda precisa ser melhorado
para retornar `404 Not Found`.

### Atualizar API monitorada

```http
PUT /api/monitored-apis/{id}
```

Exemplo de corpo valido:

```json
{
  "name": "ViaCEP",
  "url": "https://viacep.com.br/ws/01001000/json/",
  "description": "Consulta de endereco por CEP"
}
```

Regras atuais:

- `id` e recebido pela URL.
- `name` nao pode ser vazio.
- `url` nao pode ser vazia.
- `url` precisa comecar com `http://` ou `https://`.
- `description` e opcional.
- Se o `id` nao existir, hoje a API ainda retorna erro generico.

### Remover API monitorada

```http
DELETE /api/monitored-apis/{id}
```

Regras atuais:

- `id` e recebido pela URL.
- Se encontrar a API, ela e removida do banco.
- Se o `id` nao existir, hoje a API ainda retorna erro generico.

### Verificar disponibilidade manualmente

```http
POST /api/monitored-apis/{id}/check
```

Esse endpoint busca a API cadastrada pelo `id`, faz uma chamada HTTP para a URL
salva e retorna o resultado da verificacao.

Exemplo de resposta para uma API disponivel:

```json
{
  "apiId": 8,
  "name": "httpbin",
  "url": "https://httpbin.org",
  "available": true,
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
- `statusCode`: codigo HTTP retornado pela API testada.
- `responseTimeMs`: tempo de resposta em milissegundos.
- `checkedAt`: data e hora da verificacao.
- `errorMessage`: mensagem de erro, quando a verificacao falha.

## Metodos implementados no modulo `MonitoredApi`

Controller:

- `POST /api/monitored-apis`: chama `createMonitoredApi`.
- `GET /api/monitored-apis`: chama `listAll`.
- `GET /api/monitored-apis/{id}`: chama `getMonitoredApiById`.
- `PUT /api/monitored-apis/{id}`: chama `updateMonitoredApi`.
- `DELETE /api/monitored-apis/{id}`: chama `deleteMonitoredApi`.
- `POST /api/monitored-apis/{id}/check`: chama `checkMonitoredApi`.

Service:

- `create(name, url, description)`: cria uma nova API monitorada.
- `listAll()`: lista todas as APIs monitoradas.
- `getById(id)`: busca uma API monitorada pelo id.
- `update(id, name, url, description)`: atualiza nome, URL e descricao.
- `delete(id)`: remove uma API monitorada.
- `check(id)`: executa a verificacao manual da URL cadastrada.

Repository:

- `MonitoredApiRepository extends JpaRepository<MonitoredApi, Long>`.
- Ja herda metodos como `save`, `findAll`, `findById` e `delete`.

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
- Persistencia: salvar e buscar dados no PostgreSQL.
- Bean Validation: validar dados com anotacoes como `@NotBlank` e `@Pattern`.
- `@Valid`: manda o Spring validar o DTO antes de chamar o service.
- `@RestControllerAdvice`: centraliza tratamento de erros dos controllers.
- Docker Compose: sobe API e banco juntos para desenvolvimento.

## Como rodar

```bash
docker compose up --build
```

API:

```text
http://localhost:8090
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
- Verificacao manual com API real usando `POST /api/monitored-apis/{id}/check`.
- Validacao de `name` vazio.
- Validacao de `url` vazia.
- Validacao de URL sem `http://` ou `https://`.
- Acesso ao Swagger UI.
- Acesso ao JSON OpenAPI.

## Proximos passos

- Melhorar erro de busca por id inexistente para retornar `404 Not Found`.
- Criar testes automatizados.
- Salvar historico das verificacoes.
- Implementar verificacao automatica periodica.
