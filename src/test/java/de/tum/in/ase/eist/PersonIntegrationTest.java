package de.tum.in.ase.eist;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tum.in.ase.eist.model.Person;
import de.tum.in.ase.eist.repository.PersonRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
class PersonIntegrationTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PersonRepository personRepository;

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void testAddPerson() throws Exception {
        var person = new Person();
        person.setFirstName("Max");
        person.setLastName("Mustermann");
        person.setBirthday(LocalDate.now());

        var response = this.mvc.perform(
                post("/persons")
                        .content(objectMapper.writeValueAsString(person))
                        .contentType("application/json")
        ).andReturn().getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(1, personRepository.findAll().size());
    }

    @Test
    void testDeletePerson() throws Exception {
        var person = new Person();
        person.setFirstName("Max");
        person.setLastName("Mustermann");
        person.setBirthday(LocalDate.now());

        person = personRepository.save(person);

        var response = this.mvc.perform(
                delete("/persons/" + person.getId())
                        .contentType("application/json")
        ).andReturn().getResponse();

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
        assertTrue(personRepository.findAll().isEmpty());
    }

    @Test
    void testAddParent() throws Exception {
        // Create parent
        var parent = createAndSavePerson("Reino", "Wendell");
        // Create child
        var child = createAndSavePerson("Wilfreda", "Steube");

        // Check if all Person is saved to database
        assertEquals(2, personRepository.findAll().size());

        // Add parent to child
        var response = this.mvc.perform(
                put("/persons/{childId}/parents", child.getId())
                        .content(objectMapper.writeValueAsString(parent))
                        .contentType("application/json")
        ).andReturn().getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        // Check if parent-child relationship is established
//        assertEquals(1, child.getParents().size());
//        assertTrue(child.getParents().contains(parent));
    }

    @Test
    void testAddThreeParents() throws Exception {
        // Create parent1
        var parent1 = new Person();
        parent1.setFirstName("Ben");
        parent1.setLastName("Feuchtwanger");
        parent1.setBirthday(LocalDate.now());

        var response = this.mvc.perform(
                post("/persons")
                        .content(objectMapper.writeValueAsString(parent1))
                        .contentType("application/json")
        ).andReturn().getResponse();

        // Check if this person is saved to database
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(1, personRepository.findAll().size());

        // Create parent2
        var parent2 = new Person();
        parent2.setFirstName("Arne");
        parent2.setLastName("Hohenstein");
        parent2.setBirthday(LocalDate.now());

        response = this.mvc.perform(
                post("/persons")
                        .content(objectMapper.writeValueAsString(parent2))
                        .contentType("application/json")
        ).andReturn().getResponse();

        // Check if this person is saved to database
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(2, personRepository.findAll().size());

        // Create parent3
        var parent3 = new Person();
        parent3.setFirstName("Silvio");
        parent3.setLastName("Hauer");
        parent3.setBirthday(LocalDate.now());

        response = this.mvc.perform(
                post("/persons")
                        .content(objectMapper.writeValueAsString(parent3))
                        .contentType("application/json")
        ).andReturn().getResponse();

        // Check if this person is saved to database
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(3, personRepository.findAll().size());

        // Create child
        var child = new Person();
        child.setFirstName("Fynn");
        child.setLastName("Hirsch");
        child.setBirthday(LocalDate.now());

        response = this.mvc.perform(
                post("/persons")
                        .content(objectMapper.writeValueAsString(child))
                        .contentType("application/json")
        ).andReturn().getResponse();

        // Check if this person is saved to database
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(4, personRepository.findAll().size());

        // Add parent1 to the child
        var childId = child.getId();
        response = this.mvc.perform(
                put("/persons" + "/" + childId + "parents")
                        .content(objectMapper.writeValueAsString(parent1))
                        .contentType("application/json")
        ).andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        // Add parent2 to the child
        response = this.mvc.perform(
                put("/persons" + "/" + childId + "parents")
                        .content(objectMapper.writeValueAsString(parent2))
                        .contentType("application/json")
        ).andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        // Check if parent-child relationship is established
        assertEquals(2, child.getParents().size());
        assertTrue(child.getParents().contains(parent1));
        assertTrue(child.getParents().contains(parent2));

        // Try to add the third parent, which should throw an exception
        response = this.mvc.perform(
                put("/persons" + "/" + childId + "parents")
                        .content(objectMapper.writeValueAsString(parent3))
                        .contentType("application/json")
        ).andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    private Person createAndSavePerson(String firstName, String lastName) {
        var person = new Person();
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setBirthday(LocalDate.now());
        return personRepository.save(person);
    }
}
