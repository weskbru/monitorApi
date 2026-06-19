# Nome do Projeto

Monitor API

# Visao Geral

O Monitor API e um sistema backend para cadastrar APIs e acompanhar se elas estao disponiveis.

O problema que ele resolve e simples: permitir que uma pessoa ou equipe saiba se uma API cadastrada esta respondendo corretamente e mantenha um historico basico dessas verificacoes.

O sistema poderia ser utilizado por desenvolvedores, estudantes, pequenas equipes tecnicas ou qualquer pessoa que precise acompanhar a disponibilidade de APIs durante o desenvolvimento ou operacao de sistemas.

O valor entregue pela V1 e dar visibilidade sobre o estado atual de APIs monitoradas e registrar o historico das verificacoes realizadas.

Estamos construindo este projeto por dois motivos:

1. Resolver um problema real de monitoramento simples de APIs.
2. Servir como plataforma de aprendizado para Java, Spring Boot, Arquitetura de Software, Modelagem, Testes e Boas Praticas.

# Escopo da V1

A V1 deve ser pequena, funcional e suficiente para aprender os principais conceitos do backend.

## Cadastrar uma API para Monitorar

Descricao:
Permitir o cadastro de uma API com as informacoes necessarias para que ela seja verificada.

Objetivo:
Criar a base do sistema, definindo quais APIs serao acompanhadas.

Valor para o sistema:
Sem APIs cadastradas, nao existe o que monitorar.

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
Validar se uma API esta respondendo no momento da solicitacao.

Valor para o sistema:
Permite testar o monitoramento sem depender de execucao automatica.

## Executar Verificacao Automatica

Descricao:
Executar verificacoes periodicas das APIs cadastradas.

Objetivo:
Automatizar o acompanhamento da disponibilidade das APIs.

Valor para o sistema:
Transforma o sistema em um monitor simples, sem depender de acao manual constante.

## Salvar Historico de Verificacoes

Descricao:
Registrar o resultado de cada verificacao realizada.

Objetivo:
Permitir consulta posterior sobre o comportamento das APIs monitoradas.

Valor para o sistema:
Cria memoria historica e permite analisar se uma API esteve disponivel ou indisponivel ao longo do tempo.

## Retornar Status Atual

Descricao:
Informar o estado mais recente de uma API monitorada.

Objetivo:
Mostrar rapidamente se a API esta disponivel ou indisponivel.

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
