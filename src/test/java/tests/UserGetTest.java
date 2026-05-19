package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;


@Epic("User data access")
@Feature("User data visibility")
public class UserGetTest extends BaseTestCase {

    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();
    private static final String url = "https://playground.learnqa.ru/api/user/";

    @Test
    @Description("This test verifies that unauthorized user can see only 'username' field")
    @DisplayName("Test get user data without authorization")
    @Story("As an unauthorized user, I should only see username")
    public void testGetUserDataNotAuth() {
        Response responseUserData= RestAssured
                .get("https://playground.learnqa.ru/api/user/2")
                .andReturn();

        Assertions.assertJsonHasField(responseUserData, "username");
        String[] unexpectedFields = {"firstName", "lastName", "email"};
        Assertions.assertJsonHasNotFields(responseUserData, unexpectedFields);
    }

    @Test
    @Description("This test verifies that authorized user can see all fields when requesting own data")
    @DisplayName("Test get user data when authorized as same user")
    @Story("As an authorized user, I should see all my data")
    public void testGetUserDetailsAuthAsSameUser(){
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseGetAuth = RestAssured
                .given()
                .body(authData)
                .post("https://playground.learnqa.ru/api/user/login")
                .andReturn();

        String cookie = this.getCookie(responseGetAuth, "auth_sid");
        String header = this.getHeader(responseGetAuth, "x-csrf-token");

        Response responseUserData = RestAssured
                .given()
                .header("x-csrf-token", header)
                .cookie("auth_sid", cookie)
                .get("https://playground.learnqa.ru/api/user/2")
                .andReturn();

        String[] expectedFields = {"username", "firstName", "lastName", "email"};
        Assertions.assertJsonHasFields(responseUserData, expectedFields);
    }

    @Test
    @Description("This test verifies that authorized user cannot see full data of another user")
    @DisplayName("Test get user data when authorized as different user")
    @Story("As an authorized user, I should only see username of another user")
    public void testGetUserDataAuthAsAnotherUser() {
    //CREATE ANOTHER NEW USER
        Response responseCreateAnotherUser = apiCoreRequests.createUser(url);
        Assertions.assertResponseCodeEquals(responseCreateAnotherUser, 200);
        Assertions.assertJsonHasField(responseCreateAnotherUser, "id");
        String anotherUserId = responseCreateAnotherUser.jsonPath().getString("id");

    //AUTH DEFAULT USER
        Response responseLogin = apiCoreRequests.loginUser("vinkotov@example.com", "1234");

        String authCookie = this.getCookie(responseLogin, "auth_sid");
        String authToken = this.getHeader(responseLogin, "x-csrf-token");

    //GET ANOTHER USER DATA
        Response responseGetAnotherUser = apiCoreRequests.getUserDataById(
                url,
                Integer.parseInt(anotherUserId),
                authCookie,
                authToken
        );

    //CHECKS
        Assertions.assertJsonHasField(responseGetAnotherUser, "username");
        String[] unexpectedFields = {"firstName", "lastName", "email"};
        Assertions.assertJsonHasNotFields(responseGetAnotherUser, unexpectedFields);
    }
}
