package tests;

import io.restassured.path.json.JsonPath;
import lib.BaseTestCase;
import org.junit.jupiter.api.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelloWorldTest extends BaseTestCase {
    @Test
    public void testGetText(){
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/get_text")
                .andReturn();
        response.print();
    }

    @Test
    public void testRestAssured(){
        Map<String, String> params = new HashMap<>();
        params.put("name", "Valeria");

        JsonPath response = RestAssured
                .given()
                .queryParams(params)
                .get("https://playground.learnqa.ru/api/hello")
                .jsonPath();
        String name = response.get("answer");
        if (name == null) {
            System.out.println("The key 'answer' is absent");
        } else {
            System.out.println(name);
        }
    }


    @Test
    public void testRestAssuredGet() {
        Response response = RestAssured
                .given()
                .queryParams("param1", "value1")
                .queryParams("param2", "value2")
                .get("https://playground.learnqa.ru/api/check_type")
                .andReturn();
        response.print();
    }

    @Test
    public void testRestAssuredPost() {
        Map<String,Object> body = new HashMap<>();
        body.put("param1", "value1");
        body.put("param2", "value2");

        Response response = RestAssured
                .given()
                .body(body)
                .post("https://playground.learnqa.ru/api/check_type")
                .andReturn();
        response.print();
    }

    @Test
    public void testGetStatusCode(){
        Response response = RestAssured
                .given()
                .redirects()
                .follow(true)
                .when()
                .get("https://playground.learnqa.ru/api/get_303")
                .andReturn();
        int statusCode = response.getStatusCode();
        System.out.println(statusCode);
    }

    @Test
    public void testGetHeaders(){
        Map<String,String> headers = new HashMap<>();
        headers.put("MyHeader1", "MyValue1");
        headers.put("MyHeader2", "MyValue2");
        Response response = RestAssured
                .given()
                .redirects()
                .follow(false)
                .when()
                .get("https://playground.learnqa.ru/api/get_303")
                .andReturn();
        response.prettyPrint();

        String locationHeader = response.getHeader("Location");
        System.out.println(locationHeader);
    }


    @Test
    public void testGetCookie(){
        Map<String,String> data = new HashMap<>();
        data.put("login", "secret_login");
        data.put("password", "secret_pass");
        Response responseForGet = RestAssured
                .given()
                .body(data)
                .when()
                .post("https://playground.learnqa.ru/api/get_auth_cookie")
                .andReturn();

        String responseCookie = responseForGet.getCookie("auth_cookie");

        Map<String,String> cookies =  new HashMap<>();
        if (responseCookie != null){
            cookies.put("auth_cookie", responseCookie);
        }

        Response responseForCheck = RestAssured.given()
                .body(data)
                .cookies(cookies)
                .when()
                .post("https://playground.learnqa.ru/api/check_auth_cookie")
                .andReturn();


        responseForCheck.print();
    }

    @Test
    public void testRestPassed(){
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/map")
                .andReturn();
        assertEquals(200, response.statusCode(), "Unexpected status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "John", "Pete"})
    public void testHelloMethodWithoutName(String name){
        Map<String,String> queryParams = new HashMap<>();

        if(name.length() > 0){
            queryParams.put("name", name);
        }

        JsonPath response = RestAssured
                .given()
                .queryParams(queryParams)
                .get("https://playground.learnqa.ru/api/hello")
                .jsonPath();
        String answer = response.getString("answer");
        String expectedName = (name.length() > 0) ? name : "someone";
        assertEquals("Hello, " + expectedName, answer, "The answer is not expected");
    }


}
