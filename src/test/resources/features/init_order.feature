# language: pt

Funcionalidade: Inicialização de Pedido com Identificação
  O sistema deve permitir a abertura de um novo pedido
  vinculando-o a um cliente através do CPF informado.

  Cenario: Iniciar pedido com CPF existente
    Dado que possuo um CPF válido "12295626080"
    E que o cliente com este CPF já está cadastrado na base
    Quando eu solicito o início de um pedido
    Entao um novo pedido deve ser salvo no repositório
    E o sistema deve retornar a resposta com os dados do pedido