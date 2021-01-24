import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.swagger.client.model.Category;
import io.swagger.client.model.Pet;
import io.swagger.client.model.Tag;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class PetModifyTests extends PetTestAbstract {

    private static final String PET_PICTURE_FILE = "src/test/resources/dog.jpg";

    @After
    public void tearDown() {
        deleteAll();
    }

    @BeforeClass
    public static void setUp() {
        RestAssured.baseURI = PATH;
    }

    @Test
    public void testAddPetByPost() {
        Pet pet = new Pet().id(1000L).category(new Category().id(1L).name("cat")).name("1")
                .photoUrls(Collections.singletonList("1234"))
                .tags(Collections.singletonList(new Tag().id(1L).name("small"))).status(Pet.StatusEnum.AVAILABLE);
        Pet result = given()
                .contentType(ContentType.JSON)
                .body(pet)
                .when().post("/pet")
                .then()
                .statusCode(200)
                .extract()
                .as(Pet.class);
        assertThat(result).isEqualTo(pet);
    }

    @Test
    public void testUpdateExistingPet() {
        Pet pet = new Pet().category(new Category().id(1L).name("cat")).name("1")
                .photoUrls(Collections.singletonList("1234"))
                .tags(Collections.singletonList(new Tag().id(1L).name("small"))).status(Pet.StatusEnum.AVAILABLE);
        Long id = createPet(pet);

        Pet pet1 = new Pet().id(id).category(new Category().id(1L).name("cat")).name("1")
                .photoUrls(Collections.singletonList("2344"))
                .tags(Collections.singletonList(new Tag().id(1L).name("kotek"))).status(null);

        Pet result = given()
                .contentType(ContentType.JSON)
                .body(pet1)
                .when()
                .put( "/pet")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(Pet.class);
        assertThat(result).isEqualTo(pet1);
    }

    @Test
    public void testUpdateExistingPetWhenPetNotFound() {
        Pet pet = new Pet().category(new Category().id(1L).name("cat")).name("1")
                .photoUrls(Collections.singletonList("1234"))
                .tags(Collections.singletonList(new Tag().id(1L).name("small"))).status(Pet.StatusEnum.AVAILABLE);

        Long id = createPet(pet);
        delete(id);
        Pet pet1 = new Pet().id(id).category(new Category().id(1L).name("dog")).name("1")
                .photoUrls(Collections.singletonList("1234"))
                .tags(Collections.singletonList(new Tag().id(1L).name("small"))).status(Pet.StatusEnum.AVAILABLE);

        Pet result = given()
                .contentType(ContentType.JSON)
                .body(pet1)
                .when()
                .put("/pet")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(Pet.class);
        assertThat(result).isEqualTo(pet1);
    }

    @Test
    public void testUpdateExistingPetByPostAndId() {
        Pet pet = new Pet().category(new Category().id(1L).name("cat")).name("1")
                .photoUrls(Collections.singletonList("1234"))
                .tags(Collections.singletonList(new Tag().id(1L).name("small"))).status(Pet.StatusEnum.AVAILABLE);

        Long id = createPet(pet);

        given().contentType("application/x-www-form-urlencoded")
                .queryParam("name", "Puszek")
                .queryParam("status", "pending").when().post("/pet/{petId}", id)
                .then()
                .statusCode(200);

        when().get("/pet/{petId}", id)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .assertThat().body("name", equalTo("Puszek"),
                "status", equalTo("pending"));

    }

    @Test
    public void testDeleteById() {
        Pet pet = new Pet().category(new Category().id(1L).name("cat")).name("1")
                .photoUrls(Collections.singletonList("1234"))
                .tags(Collections.singletonList(new Tag().id(1L).name("small"))).status(Pet.StatusEnum.AVAILABLE);

        Long id = createPet(pet);

        when().delete("/pet/{petId}", id)
                .then()
                .statusCode(HttpStatus.SC_OK);

        when().get("/pet/{petId}", id)
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .assertThat()
                .body("type", equalTo("error"),
                        "message", equalTo("Pet not found"));

    }


    @Test
    public void testPostUploadImage() {
        File file = new File(PET_PICTURE_FILE);
        Pet pet = new Pet().category(new Category().id(1L).name("cat")).name("1")
                .photoUrls(Collections.singletonList("1234"))
                .tags(Collections.singletonList(new Tag().id(1L).name("small"))).status(Pet.StatusEnum.AVAILABLE);

        Long id = given()
                .contentType("application/json")
                .body(pet)
                .when().post("/pet")
                .then()
                .statusCode(200)
                .extract()
                .path("id");

        given().contentType("multipart/form-data")
                .multiPart("file", file, "multipart/form-data")
                .accept(ContentType.JSON)
                .when()
                .post("/pet/{id}/uploadImage", id).then().statusCode(200);

    }
}
