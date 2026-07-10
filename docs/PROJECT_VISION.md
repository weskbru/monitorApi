# Nome do Projeto

Monitor API

# Visao Geral

O Monitor API e um sistema backend para acompanhar, de forma externa, se APIs e
sistemas importantes estao funcionando, lentos ou indisponiveis.

O problema que ele resolve nasceu de uma dor real de trabalho: existem sistemas
rodando em producao, mas a equipe de desenvolvimento nem sempre tem acesso ao
ambiente de producao e nem sempre recebe informacoes claras da infraestrutura
quando algo para de funcionar.

Na pratica, o problema nao e apenas saber se uma URL abre. Muitas vezes o
sistema parece estar de pe, mas alguma funcionalidade importante falha, uma API
responde erro, o login nao funciona, ou o sistema fica lento demais. Em varios
casos, a equipe so descobre que existe problema quando usuarios reclamam.

O Monitor API sera um observador externo. Ele nao precisa acessar servidores,
logs ou banco de dados de producao. Ele apenas chama endpoints importantes dos
sistemas monitorados e registra o resultado dessas verificacoes.

O valor entregue pela V1 e dar visibilidade operacional basica:

- quais sistemas e endpoints estao sendo monitorados;
- se o ultimo resultado foi `UP`, `SLOW` ou `DOWN`;
- qual foi o codigo HTTP retornado;
- quanto tempo a API demorou para responder;
- quando cada verificacao aconteceu;
- qual historico de disponibilidade e lentidao existe para cada API.

Estamos construindo este projeto por dois motivos:

1. Resolver uma dor real de falta de visibilidade sobre sistemas em producao.
2. Servir como plataforma de aprendizado para Java, Spring Boot, Arquitetura de Software, Modelagem, Testes e Boas Praticas.

# Dor que o Projeto Soluciona

A dor principal e: a equipe precisa saber se sistemas importantes estao
funcionando antes de depender apenas de reclamacao de usuarios ou aviso tardio
da infraestrutura.

Os cenarios mais importantes para este projeto sao:

- a tela ou sistema abre, mas uma API importante falha;
- o login ou outro fluxo essencial nao funciona;
- a API responde, mas demora demais;
- o sistema fica lento a ponto de prejudicar o uso;
- a equipe nao tem acesso direto ao ambiente de producao;
- a equipe nao recebe aviso claro quando algo esta indisponivel;
- a descoberta do problema acontece tarde, quando usuarios ja foram afetados.

Por isso, o Monitor API deve acompanhar endpoints que representem partes
importantes do sistema, e nao apenas testar se a pagina inicial abre.

A evolucao natural do produto e organizar esses endpoints por sistema. Assim, o
usuario acompanha primeiro o estado de um sistema completo e depois detalha
quais endpoints criticos estao saudaveis, lentos ou indisponiveis.

Exemplo:

```text
Sistema Financeiro
Endpoint monitorado: https://financeiro.empresa.com/api/auth/status
Status esperado: 200
Lento acima de: 3000ms
Timeout: 10000ms
```

Resultados possiveis:

```text
UP    - respondeu com o status esperado dentro do tempo aceitavel
SLOW  - respondeu com o status esperado, mas demorou mais que o limite
DOWN  - nao respondeu, retornou erro, status inesperado ou timeout
```

# O Que Estamos Aprendendo

Este projeto tambem e um laboratorio de aprendizado. O objetivo nao e apenas
criar uma ferramenta pronta, mas entender como um backend real nasce e evolui.

Com ele, vamos aprender:

- Java aplicado a um problema real;
- Spring Boot para construir APIs REST;
- modelagem de dominio;
- separacao entre controller, service, repository, entity e DTO;
- persistencia com banco de dados;
- validacoes de entrada;
- tratamento de erros;
- consumo de APIs externas via HTTP;
- medicao de tempo de resposta;
- classificacao de resultado de negocio, como `UP`, `SLOW` e `DOWN`;
- historico de eventos;
- tarefas automaticas/agendadas;
- testes automatizados;
- Docker para rodar a aplicacao e dependencias;
- documentacao da API;
- evolucao incremental de arquitetura.

O aprendizado mais importante e entender por que cada parte existe. A
arquitetura deve crescer para resolver problemas reais do projeto, e nao para
parecer mais sofisticada do que precisa.

# Escopo da V1

A V1 deve ser pequena, funcional e suficiente para aprender os principais
conceitos do backend e resolver a primeira versao da dor real: enxergar se APIs
importantes estao respondendo corretamente, lentas ou fora do ar.

## Cadastrar uma API para Monitorar

Descricao:
Permitir o cadastro de uma API ou endpoint importante de um sistema.

Objetivo:
Criar a base do sistema, definindo quais APIs serao acompanhadas.

Valor para o sistema:
Sem APIs cadastradas, nao existe o que monitorar.

Informacoes minimas esperadas:

- nome;
- URL;
- descricao opcional;
- status HTTP esperado;
- limite de lentidao;
- timeout da verificacao.

## Listar APIs Cadastradas

Descricao:
Permitir a consulta das APIs que ja foram cadastradas.

Objetivo:
Dar visibilidade sobre os recursos monitorados pelo sistema.

Valor para o sistema:
Permite que o usuario saiba quais APIs fazem parte do monitoramento.

## Executar Verificacao Manual

Descricao:
Permitir que uma API cadastrada seja verificada manualmente.

Objetivo:
Validar se uma API esta respondendo no momento da solicitacao, qual codigo HTTP
ela retorna e quanto tempo demora para responder.

Valor para o sistema:
Permite testar o monitoramento sem depender de execucao automatica.

## Executar Verificacao Automatica

Descricao:
Executar verificacoes periodicas das APIs cadastradas.

Objetivo:
Automatizar o acompanhamento da disponibilidade das APIs.

Valor para o sistema:
Transforma o sistema em um monitor simples, sem depender de acao manual
constante.

Observacao:
Na V1, a verificacao automatica pode usar um intervalo simples e igual para
todas as APIs. Configuracoes avancadas de agendamento ficam para depois.

## Classificar Resultado da Verificacao

Descricao:
Classificar cada verificacao como `UP`, `SLOW` ou `DOWN`.

Objetivo:
Representar melhor a dor real, porque uma API pode estar respondendo, mas lenta
demais para ser considerada saudavel.

Valor para o sistema:
Evita uma leitura simplista de disponibilidade baseada apenas em verdadeiro ou
falso.

## Salvar Historico de Verificacoes

Descricao:
Registrar o resultado de cada verificacao realizada.

Objetivo:
Permitir consulta posterior sobre o comportamento das APIs monitoradas.

Valor para o sistema:
Cria memoria historica e permite analisar se uma API esteve disponivel, lenta
ou indisponivel ao longo do tempo.

## Retornar Status Atual

Descricao:
Informar o estado mais recente de uma API monitorada.

Objetivo:
Mostrar rapidamente se a API esta `UP`, `SLOW` ou `DOWN`.

Valor para o sistema:
Entrega a informacao mais importante para quem consulta o monitoramento.

## Expor Documentacao da API

Descricao:
Disponibilizar documentacao dos endpoints da V1.

Objetivo:
Facilitar o entendimento e o teste da API.

Valor para o sistema:
Ajuda o usuario e o desenvolvedor a conhecerem os recursos disponiveis.

# Fora do Escopo

Nao fazem parte da V1:

- frontend;
- autenticacao;
- autorizacao;
- microservicos;
- notificacoes;
- envio de e-mail;
- envio de mensagens para Slack, Teams ou similares;
- integracoes externas alem da verificacao HTTP das APIs cadastradas;
- dashboards avancados;
- metricas complexas;
- sistema de usuarios;
- multi-tenant;
- filas;
- mensageria;
- cache distribuido;
- deploy em nuvem;
- qualquer funcionalidade que aumente a complexidade sem gerar aprendizado relevante neste momento.

# Criterios de Conclusao

A V1 sera considerada concluida quando:

- for possivel cadastrar uma API para monitoramento;
- for possivel listar as APIs cadastradas;
- for possivel executar uma verificacao manual;
- existir verificacao automatica periodica;
- cada verificacao gerar um registro de historico;
- cada verificacao registrar codigo HTTP, tempo de resposta e data/hora;
- cada verificacao for classificada como `UP`, `SLOW` ou `DOWN`;
- for possivel consultar o status atual de uma API;
- a documentacao da API estiver disponivel;
- os principais fluxos tiverem testes;
- o usuario conseguir explicar o que foi construido;
- o usuario conseguir explicar por que cada parte principal existe;
- o usuario conseguir fazer pequenas alteracoes sem depender de codigo pronto.

# Filosofia do Projeto

Este projeto segue as seguintes regras:

- aprender e mais importante que terminar rapido;
- compreender e mais importante que copiar;
- simplicidade e preferivel a complexidade desnecessaria;
- arquitetura deve resolver problemas reais;
- o projeto deve evoluir em pequenos passos;
- cada etapa deve ser compreendida antes da proxima.

# Restricoes

Este documento define apenas a visao da V1.

Ele nao deve definir:

- arquitetura detalhada;
- modelo de banco de dados;
- estrutura de pacotes;
- tecnologias adicionais;
- codigo;
- implementacoes especificas.

O objetivo e manter uma direcao clara sobre o que sera construido na V1 e quais sao os limites do projeto.
