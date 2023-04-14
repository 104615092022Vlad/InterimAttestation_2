import io.qameta.allure.Flaky;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;


public class MainTest extends Basis {

    Response response;

    @Test
    @DisplayName("Все данные введены и корректны")
    public void shouldGetWeatherByCorrectData() {
        response = createResponsePositive(
                createReqSpec(
                        appid,
                        "Saint Petersburg",
                        "498817",
                        "59.8944",
                        "30.2642",
                        "190000,ru",
                        "metric",
                        "en",
                        "json"),
                200,
                null,
                "Saint Petersburg",
                498817,
                59.8944F,
                30.2642F);

    }

    @Test
    @DisplayName("Существующий город на латинице")
    public void shouldGetWeatherByCorrectCityName_1() {
        response = createResponsePositive(
                createReqSpec(
                        appid,
                        "Cucuyagua",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null),
                200,
                null,
                "Cucuyagua",
                3613015,
                14.65F,
                -88.8667F);
    }

    @Test
    @DisplayName("Существующий город на кириллице")
    public void shouldGetWeatherByCorrectCityName_2() {
        response = createResponsePositive(
                createReqSpec(
                        appid,
                        "Канберра",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null),
                200,
                null,
                "Canberra",
                2172517,
                -35.2835F,
                149.1281F);
    }

    @Test
    @DisplayName("Название города на английском соответствует названию на русском")
    public void shouldGetWeatherEnToRuCityName() {
        response = createResponsePositive(
                createReqSpec(
                        appid,
                        "Massalaves",
                        null,
                        null,
                        null,
                        null,
                        "metric",
                        "ru",
                        "json"),
                200,
                null,
                "Масалавес",
                6362027,
                39.152F,
                -0.5141F);
    }

    @Test
    @DisplayName("Существующий почтовый индекс")
    public void shouldGetWeatherByZipCode() {
        response = createResponsePositive(
                createReqSpec(
                        appid,
                        null,
                        null,
                        null,
                        null,
                        "74970,fr",
                        null,
                        null,
                        null),
                200,
                null,
                "Marignier",
                2995744,
                46.0901F,
                6.5000F);
    }

    @Test
    @Flaky
    @DisplayName("Корректные координаты")
    public void shouldGetWeatherByCoordinates_1() {
        BigDecimal latitude = new BigDecimal(random.nextDouble(-90, 90));
        BigDecimal longitude = new BigDecimal(random.nextFloat(-180, 180));
        Float lat = latitude.setScale(4, RoundingMode.HALF_EVEN).floatValue();
        Float lon = longitude.setScale(4, RoundingMode.HALF_EVEN).floatValue();

        response = createResponsePositive(
                createReqSpec(
                        appid,
                        null,
                        null,
                        lat.toString(),
                        lon.toString(),
                        null,
                        "metric",
                        "en",
                        "json"),
                200,
                null,
                "",
                0,
                lat,
                lon);
    }

    @Test
    @DisplayName("Северный полюс")
    public void shouldGetWeatherByCoordinates_2() {
        response = createReqSpec(
                appid,
                null,
                null,
                "90",
                "0",
                null,
                null,
                null,
                null
        ).when().get("/weather");

        response.then()
                .log().body()
                .statusCode(200)
                .body("coord.lat", equalTo(90),
                        "coord.lon", equalTo(0),
                        "main.temp", notNullValue(),
                        "main.pressure", notNullValue(),
                        "main.humidity", notNullValue());
    }

    @Test
    @DisplayName("180-й меридиан")
    public void shouldGetWeatherByCoordinates_3() {
        response = createReqSpec(
                appid,
                null,
                null,
                "0",
                "180",
                null,
                null,
                null,
                null
        ).when().get("/weather");

        response.then()
                .log().body()
                .statusCode(200)
                .body("coord.lat", equalTo(0),
                        "coord.lon", equalTo(180),
                        "main.temp", notNullValue(),
                        "main.pressure", notNullValue(),
                        "main.humidity", notNullValue());
    }

    @Test
    @DisplayName("Существующий id города")
    public void shouldGetWeatherByCityId() {
        response = createResponsePositive(
                createReqSpec(
                        appid,
                        null,
                        "4504915",
                        null,
                        null,
                        null,
                        "metric",
                        "en",
                        "json"),
                200,
                null,
                "Woodbury",
                4504915,
                39.8382F,
                -75.1527F);
    }

    @Test
    @DisplayName("Несоответствие исходных данных")
    public void shouldNotGetWeatherCauseConflictData() {
        response = createResponseNegative(
                createReqSpec(
                        appid,
                        "Saint Petersburg",
                        "6555068",
                        "27.773081",
                        "-82.407593",
                        "95050,us",
                        null,
                        null,
                        null),
                400,
                "Bad request");
    }

    @Test
    @DisplayName("Пустой ключ авторизации")
    public void shouldNotAuthorizeCauseEmptyKey() {
        response = createResponseNegative(
                createReqSpec(
                        "",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null),
                401,
                "Invalid API key. Please see https://openweathermap.org/faq#error401 for more info.");
    }

    @Test
    @DisplayName("Неверный ключ авторизации")
    public void shouldNotAuthorizeCauseInvalidKey() {
        String key = faker.crypto().md5();

        response = createResponseNegative(
                createReqSpec(
                        key,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null),
                401,
                "Invalid API key. Please see https://openweathermap.org/faq#error401 for more info.");
    }

    @Test
    @DisplayName("Пустой запрос")
    public void shouldNotGetWeatherCauseEmptyRequest() {
        response = createResponseNegative(
                createReqSpec(
                        appid,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null),
                400,
                "Nothing to geocode");
    }

    @Test
    @DisplayName("Несуществующий город")
    public void shouldNotGetWeatherCauseNonexistentCity() {
        response = createResponseNegative(
                createReqSpec(
                        appid,
                        "Night City",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null),
                404,
                "city not found");
    }

    @Test
    @DisplayName("Несуществующий почтовый индекс")
    public void shouldNotGetWeatherCauseNonexistentZipCode() {
        response = createResponseNegative(
                createReqSpec(
                        appid,
                        null,
                        null,
                        null,
                        null,
                        "77777",
                        null,
                        null,
                        null),
                404,
                "city not found");
    }

    @Test
    @DisplayName("Некорректные координаты")
    public void shouldNotGetWeatherCauseIncorrectCoordinates_1() {
        List<Float> coordList = new ArrayList<>();
        Float aboveRange = random.nextFloat(180.1f, Integer.MAX_VALUE);
        Float underRange = random.nextFloat(Integer.MIN_VALUE, -180.0f);
        coordList.add(aboveRange);
        coordList.add(underRange);

        response = createResponseNegative(
                createReqSpec(
                        appid,
                        null,
                        null,
                        coordList.get(random.nextInt(0, 2)).toString(),
                        coordList.get(random.nextInt(0, 2)).toString(),
                        null,
                        null,
                        null,
                        null),
                400,
                "wrong latitude");
    }

    @Test
    @DisplayName("Несоответствующий координатам тип данных")
    public void shouldNotGetWeatherCauseIncorrectCoordinates_2() {
        response = createResponseNegative(
                createReqSpec(
                        appid,
                        null,
                        null,
                        "a",
                        "b",
                        null,
                        null,
                        null,
                        null),
                400,
                "wrong latitude");
    }

    @Test
    @DisplayName("Координата только одна")
    public void shouldNotGetWeatherCauseInaccurateLocation() {
        Float coordinate = random.nextFloat(-90, 91);

        response = createResponseNegative(
                createReqSpec(
                        appid,
                        null,
                        null,
                        coordinate.toString(),
                        null,
                        null,
                        null,
                        null,
                        null),
                400,
                "Nothing to geocode");
    }

    @Test
    @DisplayName("Несуществующий id города")
    public void shouldNotGetWeatherCauseInvalidId() {
        response = createResponseNegative(
                createReqSpec(
                        appid,
                        null,
                        "123456789",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null),
                404,
                "city not found");
    }

    @Test
    @DisplayName("Значение id города лежит вне диапазона Long")
    public void shouldNotGetWeatherCauseOutOfRangeIdValue() {
        List<String> outOfRangeValues = new ArrayList<>();
        final String aboveRange = Long.MAX_VALUE + "1";
        final String underRange = Long.MIN_VALUE + "1";
        outOfRangeValues.add(aboveRange);
        outOfRangeValues.add(underRange);
        String outOfRangeValue = outOfRangeValues.get(random.nextInt(2));

        response = createResponseNegative(
                createReqSpec(
                        appid,
                        null,
                        outOfRangeValue,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null),
                400,
                outOfRangeValue + " is not a city ID");
    }


}
