package br.com.agibank.web.steps;

import br.com.agibank.web.config.WebDriverFactory;
import br.com.agibank.web.pages.BlogSearchPage;
import io.qameta.allure.Allure;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlogSearchSteps {

    private final BlogSearchPage blogSearchPage = new BlogSearchPage();

    @Dado("que o usuario acessa o Blog do Agi")
    public void queOUsuarioAcessaOBlogDoAgi() {
        blogSearchPage.accessBlog();
        anexarEvidenciasDaPagina();
    }

    @Quando("pesquisar pelo termo {string}")
    public void pesquisarPeloTermo(String term) {
        blogSearchPage.searchByTerm(term);
        anexarEvidenciasDaPagina();
    }

    @Quando("tentar realizar a pesquisa sem informar termo")
    public void tentarRealizarAPesquisaSemInformarTermo() {
        blogSearchPage.searchWithoutTerm();
        anexarEvidenciasDaPagina();
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

        anexarEvidenciasDaPagina();
    }

    @Entao("deve visualizar a pagina de resultado da pesquisa")
    public void deveVisualizarAPaginaDeResultadoDaPesquisa() {
        assertTrue(
                blogSearchPage.isResultPage(),
                "A pagina atual deveria ser uma pagina de resultado de pesquisa."
        );

        anexarEvidenciasDaPagina();
    }

    @Entao("nao deve ocorrer erro na aplicacao")
    public void naoDeveOcorrerErroNaAplicacao() {
        assertTrue(
                blogSearchPage.isValidPage(),
                "A aplicacao nao deveria apresentar erro."
        );

        anexarEvidenciasDaPagina();
    }

    private void anexarEvidenciasDaPagina() {
        WebDriver driver = WebDriverFactory.getCurrentDriver();

        if (driver == null) {
            return;
        }

        String dadosPagina = "URL: " + driver.getCurrentUrl()
                + System.lineSeparator()
                + "Titulo: " + driver.getTitle();
        Allure.addAttachment("Dados da pagina", "text/plain", dadosPagina);

        byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        Allure.addAttachment("Screenshot", "image/png", new ByteArrayInputStream(screenshot), ".png");
    }
}
