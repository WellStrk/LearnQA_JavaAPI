package tests;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lib.BaseTestCase;
import org.junit.jupiter.api.Test;
import io.restassured.http.Headers;
import io.restassured.http.Header;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class Homework2 extends BaseTestCase {
    @Test
    public void testEx5() {
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/get_json_homework")
                .andReturn();

        JsonPath jsonPath = response.jsonPath();
        String secondMessage = jsonPath.getString("messages[1].message");
        System.out.println("Текст второго сообщения: " + secondMessage);
    }

    @Test
    public void testEx6() {
        Response response = RestAssured
                .given()
                .redirects()
                .follow(false)
                .when()
                .get("https://playground.learnqa.ru/api/long_redirect")
                .andReturn();
        String FirstUrl = "https://playground.learnqa.ru/api/long_redirect";
        System.out.println("Первый редирект на URL: " + FirstUrl);
        int statusCode = response.getStatusCode();
        System.out.println(statusCode);
    }

    @Test
    public void testEx7() {
        String StartUrl = "https://playground.learnqa.ru/api/long_redirect";
        int redirectCount = 0;

        while (true) {
            Response response = RestAssured
                    .given()
                    .redirects()
                    .follow(false)
                    .when()
                    .get(StartUrl)
                    .andReturn();

            int statusCode = response.getStatusCode();

            if (statusCode == 200) {
                System.out.println("Конечный URL: " + StartUrl);
                System.out.println("Количество редиректов: " + redirectCount);
                break;
            } else {
                String locationHeader = response.getHeader("Location");
                System.out.println("Редирект №" + (redirectCount + 1) + ": " + locationHeader);
                StartUrl = locationHeader;
                redirectCount++;
            }
        }
    }

    @Test
    public void testEx8() throws InterruptedException {
        Response responseCreate = RestAssured
                .get("https://playground.learnqa.ru/ajax/api/longtime_job");

        JsonPath jsonPath = responseCreate.jsonPath();
        String token = jsonPath.getString("token");
        int seconds = jsonPath.getInt("seconds");
        System.out.println("Токен: " + token);
        System.out.println("Кол-во секунд: " + seconds);

        Response responseBefore = RestAssured
                .get("https://playground.learnqa.ru/ajax/api/longtime_job?token=" + token);

        JsonPath jsonPathBefore = responseBefore.jsonPath();
        String statusBefore = jsonPathBefore.getString("status");
        System.out.println("Статус до выполнения задачи: " + statusBefore);

        Thread.sleep(seconds * 1000L);

        Response responseAfter = RestAssured
                .get("https://playground.learnqa.ru/ajax/api/longtime_job?token=" + token);

        JsonPath jsonPathAfter = responseAfter.jsonPath();
        String statusAfter = jsonPathAfter.getString("status");
        String result = jsonPathAfter.getString("result");
        System.out.println("Статус после выполнения задачи: " + statusAfter);
        System.out.println("Результат: " + result);
    }

    @Test
    public void testEx9() {
        String login = "super_admin";
        List<String> passwords = Arrays.asList(
                "123456", "123456789", "qwerty", "password", "1234567",
                "12345678", "12345", "iloveyou", "111111", "123123",
                "abc123", "qwerty123", "1q2w3e4r", "admin", "qwertyuiop",
                "654321", "555555", "lovely", "7777777", "welcome",
                "888888", "princess", "dragon", "password1", "123qwe"
        );

        String correctPassword = null;
        String message = null;

        for (String password : passwords) {
            Map<String, String> data = new HashMap<>();
            data.put("login", login);
            data.put("password", password);

            Response responseForGet = RestAssured
                    .given()
                    .body(data)
                    .when()
                    .post("https://playground.learnqa.ru/ajax/api/get_secret_password_homework")
                    .andReturn();

            String authCookie = responseForGet.getCookie("auth_cookie");

            Map<String, String> cookies = new HashMap<>();
            if (authCookie != null) {
                cookies.put("auth_cookie", authCookie);
            }

            Response responseForCheck = RestAssured
                    .given()
                    .cookies(cookies)
                    .when()
                    .post("https://playground.learnqa.ru/ajax/api/check_auth_cookie")
                    .andReturn();

            String responseText = responseForCheck.asString();

            if (!responseText.equals("You are NOT authorized")) {
                correctPassword = password;
                message = responseText;
                break;
            }
        }

        if (correctPassword != null) {
            System.out.println("Верный пароль: " + correctPassword);
            System.out.println("Сообщение: " + message);
        } else {
            System.out.println("Правильного пароля нет в списке");
        }
    }

    @Test
    public void Ex10() {
        String hello = "Hello, world!";
        assertTrue(hello.length() > 15);
    }


    @Test
    public void Ex11() {
        Response response = RestAssured
                .given()
                .when()
                .get("https://playground.learnqa.ru/api/homework_cookie")
                .andReturn();

        Map<String, String> cookies = response.getCookies();

        String cookieName = cookies.keySet().iterator().next();
        String cookieValue = this.getCookie(response, cookieName);

        System.out.println(cookieName);
        System.out.println(cookieValue);

        assertEquals("HomeWork", cookieName);
        assertEquals("hw_value", cookieValue);
    }


    @Test
    public void Ex12() {
        Response response = RestAssured
                .given()
                .when()
                .get("https://playground.learnqa.ru/api/homework_header")
                .andReturn();

        Headers headers = response.getHeaders();

        for (Header header : headers) {
            String headerName = header.getName();
            String headerValue = header.getValue();

            System.out.println("Заголовок: " + headerName);
            System.out.println("Значение заголовка: " + headerValue);

            assertNotNull(headerName, "headerName should not be null");
            assertNotNull(headerValue, "headerValue should not be null");
        }
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        "Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; Galaxy Nexus Build/ICL53F) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30" | Mobile | No | Android
        "Mozilla/5.0 (iPad; CPU OS 13_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/91.0.4472.77 Mobile/15E148 Safari/604.1" | Mobile | Chrome | iOS
        "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)" | Googlebot | Unknown | Unknown
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36 Edg/91.0.100.0" | Web | Chrome | No
        "Mozilla/5.0 (iPad; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1" | Mobile | No | iPhone
    """)
    public void Ex13(String userAgent, String expectedPlatform, String expectedBrowser, String expectedDevice) {
        Response response = RestAssured
                .given()
                .header("User-Agent", userAgent)
                .get("https://playground.learnqa.ru/ajax/api/user_agent_check");

        String actualPlatform = response.jsonPath().getString("platform");
        String actualBrowser = response.jsonPath().getString("browser");
        String actualDevice = response.jsonPath().getString("device");

        if (!expectedPlatform.equals(actualPlatform) ||
                !expectedBrowser.equals(actualBrowser) ||
                !expectedDevice.equals(actualDevice)) {

            System.out.println("Ошибка для User-Agent: " + userAgent);
            System.out.println("Ожидаемые параметры: " + expectedPlatform + ", " + expectedBrowser + ", " + expectedDevice);
            System.out.println("Полученые параметры: " + actualPlatform + ", " + actualBrowser + ", " + actualDevice);
        }
    }
}

