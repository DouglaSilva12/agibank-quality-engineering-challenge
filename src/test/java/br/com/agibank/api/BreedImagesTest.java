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
public class BreedImagesTest extends BaseApiTest {

    private static final String EXISTING_BREED = System.getProperty("dog.breed", "akita");
    private static final String NON_EXISTING_BREED = System.getProperty("dog.invalidBreed", "raca-inexistente");

    @BeforeEach
    public void configurarSuiteAllure() {
        Allure.label("parentSuite", "API");
        Allure.label("subSuite", "Imagens por raca");
    }

    @Test
    @Story("GET /breed/{breed}/images")
    @DisplayName("Deve retornar imagens de uma raca existente")
    public void deveRetornarImagensDeUmaRacaExistente() {
        Allure.parameter("Raca", EXISTING_BREED);

        Response response = given()
                .log().all()
                .pathParam("breed", EXISTING_BREED)

                .when()
                .get(Endpoints.BREED_IMAGES);

        response.then()
                .log().all()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("success"))
                .body("message", notNullValue())
                .body("message", instanceOf(java.util.List.class))
                .body("message", not(empty()))
                .body("message", everyItem(startsWith("https://images.dog.ceo/breeds/" + EXISTING_BREED + "/")));

        String primeiraImagem = response.jsonPath().getString("message[0]");
        AllureEvidence.anexarImagemPorUrl("Evidencia - primeira imagem da raca " + EXISTING_BREED, primeiraImagem);
    }

    @Test
    @Story("GET /breed/{breed}/images")
    @DisplayName("Deve retornar erro para raca inexistente")
    public void deveRetornarErroParaRacaInexistente() {
        Allure.parameter("Raca inexistente", NON_EXISTING_BREED);

        given()
                .log().all()
                .pathParam("breed", NON_EXISTING_BREED)

                .when()
                .get(Endpoints.BREED_IMAGES)

                .then()
                .log().all()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("status", equalTo("error"))
                .body("code", equalTo(404))
                .body("message", containsString("Breed not found"));
    }
}
