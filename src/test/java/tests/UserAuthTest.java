package tests;

import io.qameta.allure.*;
import io.restassured.response.Response;
import lib.BaseTestCase;
import lib.Assertions;
import lib.ApiCoreRequests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;

@Epic("Authorisation cases")
@Feature("Authorisation")
@Owner("Valeria A.")
@Severity(SeverityLevel.CRITICAL)
@Link(name = "Open user API", url = "https://playground.learnqa.ru/api/map")
@Tag("authorization")
public class UserAuthTest extends BaseTestCase {

    String cookie;
    String header;
    int userIdOnAuth;
    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();
    private static final String url = "https://playground.learnqa.ru/api/user/";

    @BeforeEach
    public void loginUser(){
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseGetAuth = apiCoreRequests
                .makePostRequest(url + "login", authData);

        this.cookie = this.getCookie(responseGetAuth, "auth_sid");
        this.header = this.getHeader(responseGetAuth, "x-csrf-token");
        this.userIdOnAuth = this.getIntFromJson(responseGetAuth,"user_id");
    }

    @Test
    @Description("This test successfully authorize user by email and password")
    @DisplayName("Test positive auth user")
    @Story("As a registered user, I can successfully log in with valid credentials")
    @Tag("positive")
    @Tag("smoke")
    @Tag("happy-path")
    public void testAuthUser() {

        Response responseCheckAuth = apiCoreRequests
                .makeGetRequest(
                        url + "auth",
                        this.header,
                        this.cookie);
        Assertions.assertJsonByName(responseCheckAuth, "user_id", this.userIdOnAuth);
    }

    @Description("This test checks authorization status w/o sending auth cookie or token")
    @DisplayName("Test negative auth user")
    @Story("As an unauthorized user, I should not have access to protected resources")
    @Tag("negative")
    @Tag("authorization")
    @Tag("security")
    @ParameterizedTest
    @ValueSource(strings = {"cookie", "headers"})
    public void testNegativeAuthUser(String condition) {
        if (condition.equals("cookie")) {
            Response responseForCheck = apiCoreRequests.makeGetRequestWithCookie(
                    url + "auth",
                    this.cookie
            );
            Assertions.assertJsonByName(responseForCheck, "user_id", 0);
        } else if (condition.equals("headers")) {
            Response responseForCheck = apiCoreRequests.makeGetRequestWithToken(
                    url + "auth",
                    this.header
            );
            Assertions.assertJsonByName(responseForCheck, "user_id", 0);
        } else {
            throw new IllegalArgumentException("Condition value is not known: " + condition);
        }
    }
}
