package lib;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;
import static io.restassured.RestAssured.given;


public class ApiCoreRequests {
    @Step("Make a GET-request with token and auth cookie")
    public Response makeGetRequest(String url, String token, String cookie) {
        return given()
                .filter(new AllureRestAssured())
                .header(new Header("x-csrf-token", token))
                .cookie("auth_sid", cookie)
                .get(url)
                .andReturn();
    }


    @Step("Make a GET-request with auth cookie only")
    public Response makeGetRequestWithCookie(String url, String cookie) {
        return given()
                .filter(new AllureRestAssured())
                .cookie("auth_sid", cookie)
                .get(url)
                .andReturn();
    }

    @Step("Make a GET-request with token only")
    public Response makeGetRequestWithToken(String url, String token) {
        return given()
                .filter(new AllureRestAssured())
                .header(new Header("x-csrf-token", token))
                .get(url)
                .andReturn();
    }

    @Step("Make a POST-request")
    public Response makePostRequest(String url, Map<String, String> authData) {
        return given()
                .filter(new AllureRestAssured())
                .body(authData)
                .post(url)
                .andReturn();
    }

    @Step("Make a POST-request to create user with invalid email (without @)")
    public Response createUserWithInvalidEmail(String url, String invalidEmail) {
        Map<String, String> userData = DataGenerate.getRegistrationData(
                Map.of("email", invalidEmail)
        );
        return given()
                .filter(new AllureRestAssured())
                .body(userData)
                .post(url)
                .andReturn();
    }

    @Step("Make a POST-request without required field")
    public Response createUserWithoutField(String url, String missingField) {
        Map<String, String> userData = DataGenerate.getRegistrationData();
        userData.remove(missingField);
        return given()
                .filter(new AllureRestAssured())
                .body(userData)
                .post(url)
                .andReturn();
    }

    @Step("Make a POST-request with very short username (1 symbol)")
        public Response createUserWithShortUsername (String url) {
            Map<String, String> userData = DataGenerate.getRegistrationData(
                    Map.of("username", "A")
            );
            return given()
                    .filter(new AllureRestAssured())
                    .body(userData)
                    .post(url)
                    .andReturn();
        }

    @Step("Make a POST-request with very long username (more than 250 symbols)")
    public Response createUserWithLongUsername(String url) {
        String longUsername = "a".repeat(251);
        Map<String, String> userData = DataGenerate.getRegistrationData(
                Map.of("username", longUsername)
        );
        return given()
                .filter(new AllureRestAssured())
                .body(userData)
                .post(url)
                .andReturn();
    }

    @Step("Login as user and get auth cookie and token")
    public Response loginUser(String email, String password) {
        Map<String, String> authData = new HashMap<>();
        authData.put("email", email);
        authData.put("password", password);
        return given()
                .filter(new AllureRestAssured())
                .body(authData)
                .post("https://playground.learnqa.ru/api/user/login")
                .andReturn();
    }

    @Step("Get user data by ID: userId' using auth cookie and token")
    public Response getUserDataById(String url, int userId, String cookie, String token) {
        return given()
                .filter(new AllureRestAssured())
                .header("x-csrf-token", token)
                .cookie("auth_sid", cookie)
                .get(url + userId)
                .andReturn();
    }

    @Step("Create new user with random data")
    public Response createUser(String url) {
        Map<String, String> userData = DataGenerate.getRegistrationData();
        return given()
                .filter(new AllureRestAssured())
                .body(userData)
                .post(url)
                .andReturn();
    }

    @Step("Make a PUT-request to edit user with ID: {userId} using auth cookie and token")
    public Response editUser(String url, int userId, Map<String, String> editData, String cookie, String token) {
        return given()
                .filter(new AllureRestAssured())
                .header("x-csrf-token", token)
                .cookie("auth_sid", cookie)
                .body(editData)
                .put(url + userId)
                .andReturn();
    }

    @Step("Make a PUT-request to edit user without authorization")
    public Response editUserUnauthorized(String url, int userId, Map<String, String> editData) {
        return given()
                .filter(new AllureRestAssured())
                .body(editData)
                .put(url + userId)
                .andReturn();
    }

    @Step("Make a PUT-request to edit user with invalid data (no '@' in email)")
    public Response editUserWithInvalidEmail(String url, int userId, String invalidEmail, String cookie, String token) {
        Map<String, String> editData = new HashMap<>();
        editData.put("email", invalidEmail);
        return given()
                .filter(new AllureRestAssured())
                .header("x-csrf-token", token)
                .cookie("auth_sid", cookie)
                .body(editData)
                .put(url + userId)
                .andReturn();
    }

    @Step("Make a PUT-request to edit user with too short first name (1 symbol)")
    public Response editUserWithShortFirstName(String url, int userId, String shortFirstName, String cookie, String token) {
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", shortFirstName);
        return given()
                .filter(new AllureRestAssured())
                .header("x-csrf-token", token)
                .cookie("auth_sid", cookie)
                .body(editData)
                .put(url + userId)
                .andReturn();
    }
    }

