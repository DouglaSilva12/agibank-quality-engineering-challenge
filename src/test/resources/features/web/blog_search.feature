# language: pt

Funcionalidade: WEB
  Como usuario do Blog do Agi
  Quero pesquisar artigos no blog
  Para encontrar conteudos relacionados ao meu interesse

  Contexto:
    Dado que o usuario acessa o Blog do Agi

  @web @blog @pesquisa @sucesso
  Cenario: Pesquisar artigo com termo valido
    Quando pesquisar pelo termo "INSS"
    Entao deve visualizar resultados relacionados ao termo "INSS"

  @web @blog @pesquisa @sem_resultado
  Cenario: Pesquisar artigo com termo inexistente
    Quando pesquisar pelo termo "xptoautomacao123"
    Entao deve visualizar a pagina de resultado da pesquisa
    E nao deve ocorrer erro na aplicacao

  @web @blog @pesquisa @campo_vazio
  Cenario: Pesquisar sem informar termo
    Quando tentar realizar a pesquisa sem informar termo
    Entao nao deve ocorrer erro na aplicacao
