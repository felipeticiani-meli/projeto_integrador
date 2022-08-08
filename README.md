# Projeto_Integrador
API REST desenvolvida pelo grupo Beta Campers para o Projeto Integrador feito durante o IT Bootcamp Backend Java (wave 6). 

## Autores
<a href="https://github.com/vfreitasmeli">
  <img src="https://avatars.githubusercontent.com/u/107959338?s=50&v=4" style="width: 50px">
</a>
<a href="https://github.com/brunavottri">
  <img src="https://avatars.githubusercontent.com/u/108009877?s=120&v=4" style="width: 50px">
</a>
<a href="https://github.com/pealmeida-meli">
  <img src="https://avatars.githubusercontent.com/u/108008922?s=120&v=4" style="width: 50px">
</a>
<a href="https://github.com/thiagosordiMELI">
  <img src="https://avatars.githubusercontent.com/u/108008559?s=120&v=4" style="width: 50px">
</a>
<a href="https://github.com/bdonadel">
  <img src="https://avatars.githubusercontent.com/u/108012641?s=120&v=4" style="width: 50px">
</a>
<a href="https://github.com/felipeticiani-meli">
  <img src="https://avatars.githubusercontent.com/u/108010964?s=120&v=4" style="width: 50px">
</a>

# Sumário

- [Observações](#observações)
- [Funcionalidades](#funcionalidades)
- <a href="https://app.diagrams.net/#G1X_05jbEF7Yt2yFOZ2y3OfKW_KCPjm5MC">Diagrama UML </a>
- [Inbound Order](#inboundOrder)
  - [Post - Cria uma nova entrada do pedido](#createInboundOrder)
  - [Put - Atualiza entrada do pedido](#putInboundOrder)

# Funcionalidades

## Inbound Order <br name="inboundOrder">

`POST /api/v1/fresh-products/inboundorder` <br name="createInboundOrder">
Cria uma nova entrada do pedido.
<pre><code><b>Payload Example:</b>
{
  "sectionCode": 1,
  "batchStock": [
          {
            "productId": 1,
            "currentTemperature":20,
            "minimumTemperature": 15,
            "initialQuantity": 10,
            "currentQuantity": 7,
            "manufacturingDate": "2021-12-31",
            "manufacturingTime": "2021-12-31T00:00:00",
            "dueDate": "2022-12-31"
            "productPrice": 22.50
         },
          {
            "productId": 2,
            "currentTemperature":19,
            "minimumTemperature": 16,
            "initialQuantity": 20,
            "currentQuantity": 13,
            "manufacturingDate": "2022-06-16",
            "manufacturingTime": "2022-06-16T22:16:23",
            "dueDate": "2022-07-01",
            "productPrice": 7.90
         },
   ]
 }
 
 <b>Response:</b>
  "batchStock": [
          {
            "productId": 1,
            "currentTemperature":20,
            "minimumTemperature": 15,
            "initialQuantity": 10,
            "currentQuantity": 7,
            "manufacturingDate": "2021-12-31",
            "manufacturingTime": "2021-12-31 00:00:00",
            "dueDate": "2022-12-31"
            "productPrice": 22.50
         },
          {
            "productId": 2,
            "currentTemperature":19,
            "minimumTemperature": 16,
            "initialQuantity": 20,
            "currentQuantity": 13,
            "manufacturingDate": "2022-06-16",
            "manufacturingTime": "2022-06-16 22:16:23",
            "dueDate": "2022-07-01",
            "productPrice": 7.90
         },
   ]
 
 </code></pre>
 
 `PUT /api/v1/fresh-products/inboundorder` <br name="putInboundOrder">
Atualiza entrada do pedido.
<pre><code><b>Payload Example:</b>
{
  "sectionCode": 1,
  "batchStock": [
          {
            "productId": 1,
            "currentTemperature":20,
            "minimumTemperature": 15,
            "initialQuantity": 10,
            "currentQuantity": 7,
            "manufacturingDate": "2021-12-31",
            "manufacturingTime": "2021-12-31T00:00:00",
            "dueDate": "2022-12-31"
            "productPrice": 22.50
         },
          {
            "productId": 2,
            "currentTemperature":19,
            "minimumTemperature": 16,
            "initialQuantity": 20,
            "currentQuantity": 13,
            "manufacturingDate": "2022-06-16",
            "manufacturingTime": "2022-06-16T22:16:23",
            "dueDate": "2022-07-01",
            "productPrice": 7.90
         },
   ]
 }
 
 <b>Response:</b>
  "batchStock": [
          {
            "productId": 1,
            "currentTemperature":20,
            "minimumTemperature": 15,
            "initialQuantity": 10,
            "currentQuantity": 7,
            "manufacturingDate": "2021-12-31",
            "manufacturingTime": "2021-12-31 00:00:00",
            "dueDate": "2022-12-31"
            "productPrice": 22.50
         },
          {
            "productId": 2,
            "currentTemperature":19,
            "minimumTemperature": 16,
            "initialQuantity": 20,
            "currentQuantity": 13,
            "manufacturingDate": "2022-06-16",
            "manufacturingTime": "2022-06-16 22:16:23",
            "dueDate": "2022-07-01",
            "productPrice": 7.90
         },
   ]
 
 </code></pre>
 - Será validado se:<br>
  - Todos os campos não estão vazios
  - O código do setor, id do produto, e preço do produto são positivos
  - Se a lista "batchStock" não está vazia
  - Se a data de fabricação e a data de vencimento estão no formato dd-MM-yyyy
  - Se a hora de fabricação está no formato dd-MM-yyyy HH:mm:ss
  - Se a data e hora de fabricação e a data de vencimento são posteriores a data de criação

