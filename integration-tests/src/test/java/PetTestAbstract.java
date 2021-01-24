import io.restassured.response.ValidatableResponse;
import io.swagger.client.model.Pet;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

abstract class PetTestAbstract {
    static final String PATH = "https://petstore.swagger.io/v2";
    static List<Pet> pets = new ArrayList<>();


    static Long createPet(Pet pet) {
        ValidatableResponse r = given()
                .contentType("application/json")
                .body(pet)
                .when().post(PATH + "/pet").then().assertThat().statusCode(200);
        Long id = r.extract().path("id");
        pet.id(id);
        pets.add(pet);
        return id;
    }

    static void delete(Long id) {
        when().delete(PATH + "/pet/{petId}", id);
    }

    static void deleteAll() {
        for (Pet pet : pets) {
            delete(pet.getId());
        }
        pets.clear();
    }
}
