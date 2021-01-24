import io.restassured.http.ContentType;
import io.swagger.client.model.Pet;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

abstract class PetTestAbstract {
    static final String PATH = "https://petstore.swagger.io/v2";
    static Map<Long, Pet> pets = new HashMap<>();


    static Long createPetAndGetId(Pet pet) {
        Long id = given()
                .contentType(ContentType.JSON)
                .body(pet)
                .when().post(PATH + "/pet").then().assertThat().statusCode(200).extract().path("id");
        pet.id(id);
        pets.put(id, pet);
        return id;
    }

    static void delete(Long id) {
        when().delete(PATH + "/pet/{petId}", id);

    }

    static void deleteAll() {
        for (Map.Entry<Long, Pet> entry : pets.entrySet()) {
            delete(entry.getKey());
        }
        pets.clear();
    }
}
