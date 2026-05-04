import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;


public class Homework2 {
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
}