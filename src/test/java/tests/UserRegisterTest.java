package tests;

import io.qameta.allure.Story;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerate;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import lib.ApiCoreRequests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


@Epic("User registration")
@Feature("Positive and negative registration scenarios")
public class UserRegisterTest extends BaseTestCase {

    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();
    private static final String url = "https://playground.learnqa.ru/api/user/";

    @Test
    @Description("This test verifies that user cannot register with an email that already exists")
    @DisplayName("Test registration with existing email")
    @Story("As a new user, I cannot register with an email that is already taken")
    public void testCreateUserWithExistingEmail() {
        String email = "vinkotov@example.com";

        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData = DataGenerate.getRegistrationData(userData);

        Response responseCreateAuth = RestAssured
                .given()
                .body(userData)
                .post("https://playground.learnqa.ru/api/user/")
                .andReturn();

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "Users with email '" + email + "' already exists");
    }


    @Test
    @Description("This test verifies successful user registration with valid data")
    @DisplayName("Test registration with valid data")
    @Story("As a new user, I can successfully register with valid email and password")
    public void testCreateNewUser() {
        String email = DataGenerate.getRandomEmail();

        Map<String, String> userData = DataGenerate.getRegistrationData();

        Response responseCreateAuth = RestAssured
                .given()
                .body(userData)
                .post("https://playground.learnqa.ru/api/user/")
                .andReturn();

        Assertions.assertResponseCodeEquals(responseCreateAuth, 200);
        Assertions.assertJsonHasField(responseCreateAuth, "id");
    }

    @Test
    @Description("Test registration with email missing '@' symbol")
    @DisplayName("Test registration with invalid email")
    @Story("As a new user, I cannot register with an email that does not contain '@'")
    public void testCreateUserWithInvalidEmail() {
        String invalidEmail = DataGenerate.getRandomEmail().replace("@", "");

        Response response = apiCoreRequests.createUserWithInvalidEmail(url, invalidEmail);

        Assertions.assertResponseCodeEquals(response, 400);
        Assertions.assertResponseTextEquals(response, "Invalid email format");
    }

    @ParameterizedTest
    @ValueSource(strings = {"password", "username", "firstName", "lastName"})
    @Description("Test registration without one required field")
    @DisplayName("Test registration with missing required field")
    @Story("As a new user, I cannot register without providing all required fields")
    public void testCreateUserWithoutAnyField(String missingField) {

        Response response = apiCoreRequests.createUserWithoutField(url, missingField);

        Assertions.assertResponseCodeEquals(response, 400);
        Assertions.assertResponseTextEquals(response,
                "The following required params are missed: " + missingField);
    }

    @Test
    @Description("Test registration with very short username (1 symbol)")
    @DisplayName("Test registration with short username")
    @Story("As a new user, I cannot register with a username that is too short (1 symbol)")
    public void testCreateUserWithVeryShortUsername() {

        Response response = apiCoreRequests.createUserWithShortUsername(url);

        Assertions.assertResponseCodeEquals(response, 400);
        Assertions.assertResponseTextEquals(response,
                "The value of 'username' field is too short");
    }


    @Test
    @Description("Test registration with very long username (more than 250 symbols)")
    @DisplayName("Test registration with long username")
    @Story("As a new user, I cannot register with a username that exceeds maximum length (250 symbols)")
    public void testCreateUserWithVeryLongUsername() {

        Response response = apiCoreRequests.createUserWithLongUsername(url);

        Assertions.assertResponseCodeEquals(response, 400);
        Assertions.assertResponseTextEquals(response,
                "The value of 'username' field is too long");
    }
}
