import com.github.javafaker.Faker;
import java.util.Random;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


public class Basis {
    static Faker faker;
    static Random random;

    @BeforeAll
    public static void setBaseURI() {
        faker = new Faker();
        random = new Random();

        RestAssured.baseURI = "https://api.openweathermap.org/data/2.5";
    }

    protected final String appid = "756d9446f0ffabc32ee9e239acbfd399";

    public RequestSpecification createReqSpec(String appid, String cityName, String cityID,
                                              String latitude, String longitude, String zipCode,
                                              String units, String language, String mode) {

        RequestSpecification requestSpec= given()
                .contentType(ContentType.JSON)
                .param("appid", appid)
                .param("q", cityName)
                .param("id", cityID)
                .param("lat", latitude)
                .param("lon", longitude)
                .param("zip", zipCode)
                .param("units", units)
                .param("lang", language)
                .param("mode", mode);

        return requestSpec;
    }

    public static Response createResponsePositive(RequestSpecification requestSpec, Integer code, String message,
                                                  String cityName, Integer id, Float lat, Float lon) {
        Response response = requestSpec
                .when()
                .get("/weather");

        response.then()
                .log().body()
                .statusCode(code)
                .body("message", equalTo(message),
                        "name", equalTo(cityName),
                        "id", equalTo(id),
                        "coord.lat", equalTo(lat),
                        "coord.lon", equalTo(lon),
                        "main.temp", notNullValue(),
                        "main.pressure", notNullValue(),
                        "main.humidity", notNullValue());

        return response;
    }

    public static Response createResponseNegative(RequestSpecification requestSpec, Integer code, String message) {
        Response response = requestSpec
                .when()
                .get("/weather");

        response.then()
                .log().body()
                .statusCode(code)
                .body("message", equalTo(message));

        return response;
    }
}
