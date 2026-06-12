package br.com.agibank.web.steps;

import br.com.agibank.web.pages.BlogSearchPage;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlogSearchSteps {

    private final BlogSearchPage blogSearchPage = new BlogSearchPage();

    @Dado("que o usuario acessa o Blog do Agi")
    public void queOUsuarioAcessaOBlogDoAgi() {
        blogSearchPage.accessBlog();
    }

    @Quando("pesquisar pelo termo {string}")
    public void pesquisarPeloTermo(String term) {
        blogSearchPage.searchByTerm(term);
    }

    @Quando("tentar realizar a pesquisa sem informar termo")
    public void tentarRealizarAPesquisaSemInformarTermo() {
        blogSearchPage.searchWithoutTerm();
    }

    @Entao("deve visualizar resultados relacionados ao termo {string}")
    public void deveVisualizarResultadosRelacionadosAoTermo(String term) {
        assertTrue(
                blogSearchPage.isValidPage(),
                "A aplicacao deveria permanecer em uma pagina valida."
        );

        assertTrue(
                blogSearchPage.pageContainsText(term),
                "A pagina deveria conter o termo pesquisado: " + term
        );
    }

    @Entao("deve visualizar a pagina de resultado da pesquisa")
    public void deveVisualizarAPaginaDeResultadoDaPesquisa() {
        assertTrue(
                blogSearchPage.isResultPage(),
                "A pagina atual deveria ser uma pagina de resultado de pesquisa."
        );
    }

    @Entao("nao deve ocorrer erro na aplicacao")
    public void naoDeveOcorrerErroNaAplicacao() {
        assertTrue(
                blogSearchPage.isValidPage(),
                "A aplicacao nao deveria apresentar erro."
        );
    }
}
