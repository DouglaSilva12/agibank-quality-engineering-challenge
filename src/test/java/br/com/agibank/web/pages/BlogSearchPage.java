package br.com.agibank.web.pages;

import br.com.agibank.web.config.WebDriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

public class BlogSearchPage {

    private static final String BLOG_URL = "https://blogdoagi.com.br/";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By body = By.tagName("body");
    private final By botaoLupa = By.xpath("//*[@id=\"ast-desktop-header\"]/div/div/div/div/div/div[3]/div[2]/div[1]/a/span");
    private final By campoPesquisa = By.xpath("//*[@id=\"ast-seach-full-screen-form\"]/div/div/form/fieldset/label/input");
    private final By botaoPesquisar = By.id("search_submit");

    public BlogSearchPage() {
        this.driver = WebDriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
    }

    public void accessBlog() {
        System.out.println("[WEB] Acessando o Blog do Agi: " + BLOG_URL);
        driver.get(BLOG_URL);
        aguardarBodyVisivel();
        System.out.println("[WEB] Blog carregado. Titulo: " + driver.getTitle());
    }

    public void searchByTerm(String term) {
        pesquisar(term);
    }

    public void searchWithoutTerm() {
        pesquisar("");
    }

    public void pesquisar(String term) {
        clicarNoBotaoLupa();
        preencherCampoPesquisa(term);
        clicarNoBotaoPesquisar();
    }

    public void clicarNoBotaoLupa() {
        System.out.println("[WEB] Clicando na lupa de pesquisa.");
        clicar(botaoLupa);
    }

    public void preencherCampoPesquisa(String term) {
        System.out.println("[WEB] Preenchendo campo de pesquisa com: " + term);
        preencher(campoPesquisa, term);
    }

    public void clicarNoBotaoPesquisar() {
        System.out.println("[WEB] Clicando no botao pesquisar.");
        clicar(botaoPesquisar);
        aguardarCarregamentoPagina();
        aguardarBodyVisivel();
        System.out.println("[WEB] Resultado carregado. URL: " + driver.getCurrentUrl());
        System.out.println("[WEB] Resultado carregado. Titulo: " + driver.getTitle());
    }

    public boolean pageContainsText(String text) {
        return driver.getPageSource()
                .toLowerCase(Locale.ROOT)
                .contains(text.toLowerCase(Locale.ROOT));
    }

    public boolean isResultPage() {
        String title = driver.getTitle().toLowerCase(Locale.ROOT);
        String pageSource = driver.getPageSource().toLowerCase(Locale.ROOT);

        return title.contains("blog do agi")
                && (title.contains("-")
                || pageSource.contains("resultado")
                || pageSource.contains("pesquisa")
                || pageSource.contains("busca"));
    }

    public boolean isValidPage() {
        String title = driver.getTitle();
        String currentUrl = driver.getCurrentUrl();

        return title != null
                && !title.isBlank()
                && currentUrl != null
                && !currentUrl.isBlank()
                && !pageContainsText("erro 500")
                && !pageContainsText("internal server error")
                && !pageContainsText("404 not found");
    }

    private void clicar(By elemento) {
        wait.until(ExpectedConditions.elementToBeClickable(elemento)).click();
    }

    private void preencher(By elemento, String texto) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(elemento));
        input.clear();
        input.sendKeys(texto);
    }

    private void aguardarBodyVisivel() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(body));
    }

    private void aguardarCarregamentoPagina() {
        wait.until(webDriver ->
                List.of("interactive", "complete").contains(
                        ((JavascriptExecutor) webDriver)
                                .executeScript("return document.readyState")
                )
        );
    }
}
