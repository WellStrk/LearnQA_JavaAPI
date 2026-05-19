package tests;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;


@Epic("User editing")
@Feature("User data editing")
public class UserEditTest extends BaseTestCase {

    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();
    private static final String url = "https://playground.learnqa.ru/api/user/";

    @Test
    @Description("This test successfully edits just created user")
    @DisplayName("Test positive edit user")
    @Story("As an authorized user, I can edit my own data")
    public void testEditJustCreateTest() {
//GENERATE USER
        Map<String, String> userData = DataGenerate.getRegistrationData();
        JsonPath responseCreateAuth = RestAssured
                .given()
                .body(userData)
                .post("https://playground.learnqa.ru/api/user/")
                .jsonPath();

        String userId = responseCreateAuth.getString("id");
//LOGIN
        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));

        Response responseGetAuth = RestAssured
                .given()
                .body(authData)
                .post("https://playground.learnqa.ru/api/user/login")
                .andReturn();
//EDIT
        String newName = "Changed Name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        Response responseEditUser = RestAssured
                .given()
                .header("x-csrf-token", this.getHeader(responseGetAuth, "x-csrf-token"))
                .cookie("auth_sid", this.getCookie(responseGetAuth, "auth_sid"))
                .body(editData)
                .put("https://playground.learnqa.ru/api/user/" + userId)
                .andReturn();
//GET
        Response responseUserData = RestAssured
                .given()
                .header("x-csrf-token", this.getHeader(responseGetAuth, "x-csrf-token"))
                .cookie("auth_sid", this.getCookie(responseGetAuth, "auth_sid"))
                .get("https://playground.learnqa.ru/api/user/" + userId)
                .andReturn();
        Assertions.assertJsonByName(responseUserData, "firstName", newName);
    }

    @Test
    @Description("Attempt to edit user data without being authorized")
    @DisplayName("Test edit user without authorization")
    @Story("As an unauthorized user, I cannot edit any user data")
    public void testEditUserUnauthorized() {
        // CREATE NEW USER
        Response responseCreateUser = apiCoreRequests.createUser(url);
        Assertions.assertResponseCodeEquals(responseCreateUser, 200);
        Assertions.assertJsonHasField(responseCreateUser, "id");
        String userId = responseCreateUser.jsonPath().getString("id");

        // CHANGE DATA WITHOUT AUTH
        Map<String, String> editData = new HashMap<>();
        editData.put("username", "HackedName");

        Response responseEdit = apiCoreRequests.editUserUnauthorized(url, Integer.parseInt(userId), editData);

        // CHECKS
        Assertions.assertResponseCodeEquals(responseEdit, 400);
        Assertions.assertJsonByName(responseEdit, "error", "Auth token not supplied");
    }

    @Test
    @Description("Attempt to edit user data while being authorized as another user")
    @DisplayName("Test edit user data as another user")
    @Story("As an authorized user, I cannot edit data of another user")
    public void testEditUserAsAnotherUser() {

        //CREATE FIRST USER (FOR CHANGING)
        Response responseCreateUserToEdit = apiCoreRequests.createUser(url);
        Assertions.assertResponseCodeEquals(responseCreateUserToEdit, 200);
        Assertions.assertJsonHasField(responseCreateUserToEdit, "id");
        String userIdToEdit = responseCreateUserToEdit.jsonPath().getString("id");

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

        //CHANGING FIRST USER DATA
        Map<String, String> editData = new HashMap<>();
        editData.put("username", "NewName");

        Response responseEdit = apiCoreRequests.editUser(
                url,
                Integer.parseInt(userIdToEdit),
                editData,
                authCookie,
                authToken
        );

        //CHECKS
        Assertions.assertResponseCodeEquals(responseEdit, 400);
        Assertions.assertJsonByName(responseEdit, "error", "This user can only edit their own data.");
    }

    @Test
    @Description("Attempt to change email to invalid format (without '@') while being authorized as the same user")
    @DisplayName("Test edit user with invalid email")
    @Story("As an authorized user, I cannot change my email to an invalid format (without '@')")
    public void testEditUserWithInvalidEmail() {

        //CREATE NEW USER
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

        //CHANGE EMAIL (WITHOUT @)
        String invalidEmail = DataGenerate.getRandomEmail().replace("@", "");

        Response responseEdit = apiCoreRequests.editUserWithInvalidEmail(
                url,
                Integer.parseInt(userId),
                invalidEmail,
                authCookie,
                authToken
        );

        //CHECKS
        Assertions.assertResponseCodeEquals(responseEdit, 400);
        Assertions.assertJsonByName(responseEdit, "error", "Invalid email format");
    }

    @Test
    @Description("Attempt to change firstName to very short value (1 symbol) while being authorized as the same user")
    @DisplayName("Test edit user with too short first name")
    @Story("As an authorized user, I cannot change my first name to a value that is too short (1 symbol)")
    public void testEditUserWithShortFirstName() {
        //CREATE NEW USER
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

        //CHANGE FIRSTNAME BY ONE SYMBOL
        String shortFirstName = "A";

        Response responseEdit = apiCoreRequests.editUserWithShortFirstName(
                url,
                Integer.parseInt(userId),
                shortFirstName,
                authCookie,
                authToken
        );

        //CHECKS
        Assertions.assertResponseCodeEquals(responseEdit, 400);
        Assertions.assertJsonByName(responseEdit, "error", "The value for field `firstName` is too short");
    }
}
