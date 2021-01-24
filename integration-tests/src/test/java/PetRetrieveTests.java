import io.restassured.RestAssured;
import io.swagger.client.model.Category;
import io.swagger.client.model.Pet;
import io.swagger.client.model.Tag;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;

public class PetRetrieveTests extends PetTestAbstract {

    private static final String FIND_BY_STATUS = "/pet/findByStatus";
    private static final String GET_BY_ID = "/pet/{id}";


    @BeforeClass
    public static void setUp() {
        RestAssured.baseURI = PATH;
        Pet pet1 = new Pet().category(new Category().id(1L).name("cat")).name("1")
                .photoUrls(Collections.singletonList("1234"))
                .tags(Collections.singletonList(new Tag().id(1L).name("small"))).status(Pet.StatusEnum.AVAILABLE);
        Pet pet2 = new Pet().category(new Category().id(2L).name("cat")).name("2")
                .photoUrls(Collections.singletonList("abcd"))
                .tags(Collections.singletonList(new Tag().id(2L).name("small"))).status(Pet.StatusEnum.PENDING);
        Pet pet3 = new Pet().category(new Category().id(3L).name("cat")).name("3")
                .photoUrls(Collections.singletonList("1a2b"))
                .tags(Collections.singletonList(new Tag().id(3L).name("small"))).status(Pet.StatusEnum.SOLD);
        createPet(pet1);
        createPet(pet2);
        createPet(pet3);
    }

    @AfterClass
    public static void tearDown() {
        deleteAll();
    }


    @Test
    public void testFindPetById() {
        Pet pet = pets.get(0);
        Long id = pet.getId();
        Pet result = given().when().get(GET_BY_ID, id)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(Pet.class);
        assertThat(result).isEqualTo(pet);

    }

    @Test
    public void testFindPetByNonExistingId() {
        when().get(GET_BY_ID, -10)
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .assertThat().body("type", equalTo("error"),
                "message", equalTo("Pet not found"));
    }

    @Test
    public void testFindPetByIdInvalidId() {
        when().get(GET_BY_ID, "test")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .assertThat().body("type", equalTo("unknown"),
                "message", equalTo("java.lang.NumberFormatException: For input string: \"test\""));
    }

    @Test
    public void testFindPetByStatusAvailable() {
        given()
                .queryParam("status", Pet.StatusEnum.AVAILABLE.toString())
                .when()
                .get(FIND_BY_STATUS)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .assertThat().body("status", hasItems(Pet.StatusEnum.AVAILABLE.toString()),
                "status", Matchers.not(hasItems(Pet.StatusEnum.SOLD.toString(), Pet.StatusEnum.PENDING.toString())));

    }

    @Test
    public void testFindPetByStatusAvailablePending() {
        given().queryParam("status", String.format("%s,%s", Pet.StatusEnum.AVAILABLE.toString(), Pet.StatusEnum.PENDING.toString()))
                .when()
                .get(FIND_BY_STATUS)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .assertThat().body("status", hasItems(Pet.StatusEnum.AVAILABLE.toString(), Pet.StatusEnum.PENDING.toString()),
                "status", not(hasItems(Pet.StatusEnum.SOLD.toString())));

    }

    @Test
    public void testFindPetByStatusAvailablePendingSold() {
        given().queryParam("status", String.format("%s,%s,%s", Pet.StatusEnum.AVAILABLE.toString(), Pet.StatusEnum.PENDING.toString(), Pet.StatusEnum.SOLD.toString()))
                .when().get(FIND_BY_STATUS)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .assertThat().body("status", hasItems(Pet.StatusEnum.AVAILABLE.toString(),
                Pet.StatusEnum.PENDING.toString(), Pet.StatusEnum.SOLD.toString()));
    }

    @Test
    public void testFindPetByInvalidStatus() {
        given().queryParam("status", "New")
                .when().get(FIND_BY_STATUS)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .assertThat().body("message", is(empty()));
    }

}
