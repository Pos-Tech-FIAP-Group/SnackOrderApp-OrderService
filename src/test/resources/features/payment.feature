# language: pt

Funcionalidade: Solicitação de Pagamento de Pedido
  O sistema deve processar a intenção de pagamento de um pedido existente,
  alterando seu status e notificando o serviço de pagamentos via mensageria.

  Cenario: Solicitação de pagamento com sucesso
    Dado que existe um pedido com id 1 no status "INICIADO" contendo itens
    Quando eu solicito a criação do pagamento para o pedido 1
    Entao o status do pedido deve ser atualizado para "PAGAMENTO_PENDENTE"
    E uma mensagem de criação de pagamento deve ser enviada para a fila