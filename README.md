# Monitor API

Backend em Spring Boot para cadastrar APIs e acompanhar a disponibilidade delas.

Este projeto tambem esta sendo usado como laboratorio de aprendizado em Java,
Spring Boot, API REST, persistencia, validacoes, Docker e boas praticas.

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
  "url": "https://viacep.com.br/ws/01001000/json/"
}
```

Regras atuais:

- `name` nao pode ser vazio.
- `url` nao pode ser vazia.
- `url` precisa comecar com `http://` ou `https://`.

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
- Validacao de `name` vazio.
- Validacao de `url` vazia.
- Validacao de URL sem `http://` ou `https://`.
- Acesso ao Swagger UI.
- Acesso ao JSON OpenAPI.

## Proximos passos

- Melhorar erro de busca por id inexistente para retornar `404 Not Found`.
- Criar testes automatizados.
- Implementar verificacao manual de disponibilidade.
- Salvar historico das verificacoes.
