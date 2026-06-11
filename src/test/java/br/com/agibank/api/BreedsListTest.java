package br.com.agibank.api;

import br.com.agibank.config.BaseApiTest;
import br.com.agibank.utils.Endpoints;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class BreedsListTest extends BaseApiTest {

    @Test
    @DisplayName("Deve retornar a lista de todas as raças com sucesso")
    public void deveRetornarListaDeTodasAsRacasComSucesso() {

        given()
                .log().all()

                .when()
                .get(Endpoints.LIST_ALL_BREEDS)

                .then()
                .log().all()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("message", notNullValue())
                .body("message", instanceOf(java.util.Map.class));
    }
}