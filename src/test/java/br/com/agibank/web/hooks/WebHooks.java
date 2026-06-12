package br.com.agibank.web.hooks;

import br.com.agibank.web.config.WebDriverFactory;
import io.cucumber.java.After;
import io.cucumber.java.Before;

public class WebHooks {

    @Before
    public void beforeScenario() {
        WebDriverFactory.getDriver();
    }

    @After
    public void afterScenario() {
        WebDriverFactory.quitDriver();
    }
}
