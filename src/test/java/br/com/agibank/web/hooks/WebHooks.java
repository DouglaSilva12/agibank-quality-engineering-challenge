package br.com.agibank.web.hooks;

import br.com.agibank.web.config.WebDriverFactory;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class WebHooks {

    @Before
    public void beforeScenario() {
        WebDriverFactory.getDriver();
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        WebDriver driver = WebDriverFactory.getCurrentDriver();

        if (driver == null) {
            return;
        }

        String stepEvidence = "URL atual: " + driver.getCurrentUrl()
                + System.lineSeparator()
                + "Titulo da pagina: " + driver.getTitle();
        scenario.attach(stepEvidence, "text/plain", "Evidencia - dados da pagina");

        byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        scenario.attach(screenshot, "image/png", "Evidencia - screenshot");
    }

    @After
    public void afterScenario() {
        WebDriverFactory.quitDriver();
    }
}
