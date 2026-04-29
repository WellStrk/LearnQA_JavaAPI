import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;


public class Homework2 {
    @Test
    public void testGetText() {
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/get_json_homework")
                .andReturn();

        JsonPath jsonPath = response.jsonPath();
        String secondMessage = jsonPath.getString("messages[1].message");
        System.out.println("Текст второго сообщения: " + secondMessage);
    }
}