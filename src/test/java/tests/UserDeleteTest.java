package tests;

import io.qameta.allure.*;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

@Epic("User deletion")
@Feature("User data deletion")
@Owner("Valeria A.")
@Severity(SeverityLevel.CRITICAL)
@Link(name = "Open user API", url = "https://playground.learnqa.ru/api/map")
@Tag("delete")
public class UserDeleteTest extends BaseTestCase {

    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();
    private static final String url = "https://playground.learnqa.ru/api/user/";

    @Test
    @Description("Attempt to delete user with ID 2 while being authorized as vinkotov@example.com")
    @DisplayName("Test delete user ID 2")
    @Story("As an authorized user, I cannot delete test user's account")
    @Tag("negative")
    @Tag("security")
    public void testDeleteUserWithId2() {

        //AUTH DEFAULT USER
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseLogin = apiCoreRequests.loginUser(authData.get("email"), authData.get("password"));

        String authCookie = this.getCookie(responseLogin, "auth_sid");
        String authToken = this.getHeader(responseLogin, "x-csrf-token");

        //DELETE USER WITH ID 2
        Response responseDelete = apiCoreRequests.deleteUser(url, 2, authCookie, authToken);

        //CHECKS
        Assertions.assertResponseCodeEquals(responseDelete, 400);
        Assertions.assertJsonByName(responseDelete, "error", "Please, do not delete test users with ID 1, 2, 3, 4 or 5.");
    }

    @Test
    @Description("Create a new user, authorize, delete it, and verify it's gone")
    @DisplayName("Test positive delete user")
    @Story("As an authorized user, I can delete my own account")
    @Tag("positive")
    @Tag("smoke")
    @Tag("happy-path")
    public void testDeleteUserPositive() {

        // CREATE NEW USER
        Map<String, String> userData = DataGenerate.getRegistrationData();
        Response responseCreateUser = apiCoreRequests.makePostRequest(url, userData);

        Assertions.assertResponseCodeEquals(responseCreateUser, 200);
        Assertions.assertJsonHasField(responseCreateUser, "id");

        String userId = responseCreateUser.jsonPath().getString("id");
        String userEmail = userData.get("email");
        String userPassword = userData.get("password");

        //AUTH NEW USER
        Response responseLogin = apiCoreRequests.loginUser(userEmail, userPassword);

        String authCookie = this.getCookie(responseLogin, "auth_sid");
        String authToken = this.getHeader(responseLogin, "x-csrf-token");

        //DELETE NEW USER
        Response responseDelete = apiCoreRequests.deleteUser(
                url,
                Integer.parseInt(userId),
                authCookie,
                authToken
        );

        Assertions.assertResponseCodeEquals(responseDelete, 200);
        Assertions.assertJsonByName(responseDelete, "success", "!");

        //GET DATA DELETED USER
        Response responseGetDeletedUser = apiCoreRequests.getUserDataById(
                url,
                Integer.parseInt(userId),
                authCookie,
                authToken
        );

        //CHECKS
        Assertions.assertResponseCodeEquals(responseGetDeletedUser, 404);
        Assertions.assertResponseTextEquals(responseGetDeletedUser, "User not found");
    }

    @Test
    @Description("Create two users, authorize as second user, try to delete first user")
    @DisplayName("Test delete user as another user")
    @Story("As an authorized user, I cannot delete another user's account")
    @Tag("negative")
    @Tag("security")
    @Tag("authorization")
    @Tag("privacy")
    public void testDeleteUserAsAnotherUser() {

        //CREATE NEW USER (TO DELETE)
        Map<String, String> firstUserData = DataGenerate.getRegistrationData();
        Response responseCreateFirstUser = apiCoreRequests.makePostRequest(url, firstUserData);

        Assertions.assertResponseCodeEquals(responseCreateFirstUser, 200);
        Assertions.assertJsonHasField(responseCreateFirstUser, "id");
        String firstUserId = responseCreateFirstUser.jsonPath().getString("id");

        //CREATE SECOND USER (FOR AUTH)
        Map<String, String> secondUserData = DataGenerate.getRegistrationData();
        Response responseCreateSecondUser = apiCoreRequests.makePostRequest(url, secondUserData);

        Assertions.assertResponseCodeEquals(responseCreateSecondUser, 200);
        String secondUserEmail = secondUserData.get("email");
        String secondUserPassword = secondUserData.get("password");

        //AUTH SECOND USER
        Response responseLogin = apiCoreRequests.loginUser(secondUserEmail, secondUserPassword);

        String authCookie = this.getCookie(responseLogin, "auth_sid");
        String authToken = this.getHeader(responseLogin, "x-csrf-token");

        //DELETE FIRST USER
        Response responseDelete = apiCoreRequests.deleteUserAsAnotherUser(
                url,
                Integer.parseInt(firstUserId),
                authCookie,
                authToken
        );

        //CHECKS
        Assertions.assertResponseCodeEquals(responseDelete, 400);
        Assertions.assertJsonByName(responseDelete, "error", "This user can only delete their own account.");
    }
}
