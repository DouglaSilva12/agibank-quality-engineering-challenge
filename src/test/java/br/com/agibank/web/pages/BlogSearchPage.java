package br.com.agibank.web.pages;

import br.com.agibank.web.config.WebDriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

public class BlogSearchPage {

    private static final String BLOG_URL = "https://blogdoagi.com.br/";

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final By body = By.tagName("body");

    public BlogSearchPage() {
        this.driver = WebDriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void accessBlog() {
        driver.get(BLOG_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(body));
    }

    public void searchByTerm(String term) {
        openSearchResults(term);
    }

    public void searchWithoutTerm() {
        openSearchResults("");
    }

    public boolean pageContainsText(String text) {
        return driver.getPageSource()
                .toLowerCase(Locale.ROOT)
                .contains(text.toLowerCase(Locale.ROOT));
    }

    public boolean isResultPage() {
        String currentUrl = driver.getCurrentUrl().toLowerCase(Locale.ROOT);
        String pageSource = driver.getPageSource().toLowerCase(Locale.ROOT);

        return currentUrl.contains("?s=")
                || pageSource.contains("resultado")
                || pageSource.contains("pesquisa")
                || pageSource.contains("busca");
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

    private void openSearchResults(String term) {
        String encodedTerm = URLEncoder.encode(term, StandardCharsets.UTF_8);

        driver.get(BLOG_URL + "?s=" + encodedTerm);
        wait.until(ExpectedConditions.visibilityOfElementLocated(body));
        waitPageLoad();
    }

    private void waitPageLoad() {
        wait.until(webDriver ->
                List.of("interactive", "complete").contains(
                        ((JavascriptExecutor) webDriver)
                                .executeScript("return document.readyState")
                )
        );
    }
}
