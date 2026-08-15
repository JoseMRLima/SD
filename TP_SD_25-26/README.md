# TP SD 2025/2026

Trabalho Prático de **Sistemas Distribuídos** 2025/2026.

## Descrição

Neste projeto pretende-se a implementação de um serviço de registo de eventos em séries temporais e de agregação de informação, relativos a venda de produtos, em que a informação é mantida num servidor e acedida remotamente. Clientes interagem com o servidor através de **sockets TCP**, de forma a inserir e consultar informação. O servidor atende clientes concorrentemente e armazena a informação. Assuma que todas as operações se referem apenas aos D dias anteriores, sendo D um parâmetro de inicialização do servidor. O serviço deverá suportar as seguintes funcionalidades.

- autenticação e registo do utilizador
- registo de eventos
- agregação de informação
- filtrar eventos de uma série temporal
- notificação de ocorrências
- suporte a clientes multi-threaded

## Grupo

Constituintes do grupo de trabalho:
- Diogo Luís Barros Costa 100751
- Eduardo Freitas Fernandes 106919
- Gonçalo Rodrigues Ribeiro 106842
- José Mário Raimundo Lima 106888

