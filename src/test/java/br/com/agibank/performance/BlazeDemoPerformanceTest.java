package br.com.agibank.performance;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("PERFORMANCE")
@Feature("BlazeDemo")
@Tag("performance")
@DisplayName("BlazeDemo")
public class BlazeDemoPerformanceTest {

    private static final Path JMETER_TEMPLATE = Path.of("src/test/jmeter/blazedemo-purchase-template.jmx");
    private static final Path LOAD_PROFILE = Path.of("src/test/jmeter/profiles/load.properties");
    private static final Path SPIKE_PROFILE = Path.of("src/test/jmeter/profiles/spike.properties");
    private static final Path JMETER_REPORTS = Path.of("target/jmeter/reports");

    @BeforeEach
    public void configurarSuiteAllure() {
        Allure.label("parentSuite", "PERFORMANCE");
        Allure.label("subSuite", "Compra de passagem");
    }

    @Test
    @Story("Compra de passagem aerea")
    @DisplayName("Deve possuir scripts JMeter de carga e pico com criterio de aceite")
    public void devePossuirScriptsJMeterDeCargaEPicoComCriterioDeAceite() throws IOException {
        Properties load = loadProfile(LOAD_PROFILE);
        Properties spike = loadProfile(SPIKE_PROFILE);

        Allure.addAttachment("Plano JMeter - template", "application/xml", Files.readString(JMETER_TEMPLATE));
        Allure.addAttachment("Perfil de carga", "text/plain", Files.readString(LOAD_PROFILE));
        Allure.addAttachment("Perfil de pico", "text/plain", Files.readString(SPIKE_PROFILE));
        attachGeneratedReports();

        assertAll(
                () -> assertTrue(Files.exists(JMETER_TEMPLATE), "Template JMeter deve existir."),
                () -> assertEquals("250", load.getProperty("target_rps")),
                () -> assertEquals("2000", load.getProperty("p90_limit_ms")),
                () -> assertEquals("250", spike.getProperty("target_rps")),
                () -> assertEquals("2000", spike.getProperty("p90_limit_ms")),
                () -> assertEquals("load", load.getProperty("scenario")),
                () -> assertEquals("spike", spike.getProperty("scenario"))
        );
    }

    private static Properties loadProfile(Path path) throws IOException {
        Properties properties = new Properties();
        try (var input = Files.newInputStream(path)) {
            properties.load(input);
        }
        return properties;
    }

    private static void attachGeneratedReports() throws IOException {
        if (!Files.exists(JMETER_REPORTS)) {
            Allure.addAttachment("Relatorios JMeter", "text/plain",
                    "Nenhum relatorio JMeter encontrado. Execute scripts/run-performance-jmeter.cmd para gerar os artefatos.");
            return;
        }

        List<Path> summaries;
        try (Stream<Path> files = Files.find(JMETER_REPORTS, 3,
                (path, attributes) -> attributes.isRegularFile() && path.getFileName().toString().equals("summary.md"))) {
            summaries = files.toList();
        }

        if (summaries.isEmpty()) {
            Allure.addAttachment("Relatorios JMeter", "text/plain",
                    "Diretorio de relatorios encontrado, mas nenhum summary.md foi gerado.");
            return;
        }

        for (Path summary : summaries) {
            String content = Files.readString(summary);
            Allure.addAttachment("Relatorio JMeter - " + summary.getParent().getFileName(), "text/markdown", content);
        }

        if (Boolean.getBoolean("performance.requireJmeterResults")) {
            assertFalse(summaries.isEmpty(), "Relatorios JMeter devem existir quando performance.requireJmeterResults=true.");
            for (Path summary : summaries) {
                assertFalse(Files.readString(summary).contains("Status: NAO SATISFEITO"),
                        "Criterio de aceite nao satisfeito em " + summary);
            }
        }
    }
}
