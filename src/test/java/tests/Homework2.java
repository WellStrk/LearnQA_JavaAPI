package tests;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lib.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;


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
    public void testStringLength() {
        String hello = "Hello, world!";
        assertTrue(hello.length() > 15);
    }
        }