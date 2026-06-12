package br.com.agibank.api;

import br.com.agibank.config.BaseApiTest;
import br.com.agibank.utils.AllureEvidence;
import br.com.agibank.utils.Endpoints;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("API")
@Feature("Dog API")
@Tag("api")
@DisplayName("Dog API")
public class RandomImageTest extends BaseApiTest {

    @BeforeEach
    public void configurarSuiteAllure() {
        Allure.label("parentSuite", "API");
        Allure.label("subSuite", "Imagem aleatoria");
    }

    @Test
    @Story("GET /breeds/image/random")
    @DisplayName("Deve retornar uma imagem aleatoria com sucesso")
    public void deveRetornarUmaImagemAleatoriaComSucesso() {

        Response response = given()
                .log().all()

                .when()
                .get(Endpoints.RANDOM_IMAGE);

        response.then()
                .log().all()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("success"))
                .body("message", notNullValue())
                .body("message", startsWith("https://images.dog.ceo/breeds/"))
                .body("message", matchesPattern("https://images\\.dog\\.ceo/breeds/.+\\.(?i)(jpg|jpeg|png|gif)"));

        String imagemAleatoria = response.jsonPath().getString("message");
        AllureEvidence.anexarImagemPorUrl("Evidencia - imagem aleatoria retornada pela API", imagemAleatoria);
    }
}
