package br.com.agibank.api;

import br.com.agibank.config.BaseApiTest;
import br.com.agibank.utils.Endpoints;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
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
public class BreedsListTest extends BaseApiTest {

    @BeforeEach
    public void configurarSuiteAllure() {
        io.qameta.allure.Allure.label("parentSuite", "API");
        io.qameta.allure.Allure.label("subSuite", "Listagem de racas");
    }

    @Test
    @Story("GET /breeds/list/all")
    @DisplayName("Deve retornar a lista de todas as racas com sucesso")
    public void deveRetornarListaDeTodasAsRacasComSucesso() {

        given()
                .log().all()

                .when()
                .get(Endpoints.LIST_ALL_BREEDS)

                .then()
                .log().all()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("success"))
                .body("message", notNullValue())
                .body("message", instanceOf(java.util.Map.class))
                .body("message", aMapWithSize(greaterThan(0)))
                .body("message.keySet()", hasItems("akita", "bulldog", "hound"))
                .body("message.bulldog", hasItems("boston", "english", "french"));
    }
}
