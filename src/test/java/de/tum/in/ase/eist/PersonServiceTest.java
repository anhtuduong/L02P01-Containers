package de.tum.in.ase.eist;

import de.tum.in.ase.eist.model.Person;
import de.tum.in.ase.eist.repository.PersonRepository;
import de.tum.in.ase.eist.service.PersonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
class PersonServiceTest {
    @Autowired
    private PersonService personService;
    @Autowired
    private PersonRepository personRepository;

    @Test
    void testAddPerson() {
        var person = new Person();
        person.setFirstName("Max");
        person.setLastName("Mustermann");
        person.setBirthday(LocalDate.now());

        personService.save(person);

        assertEquals(1, personRepository.findAll().size());
    }

    @Test
    void testDeletePerson() {
        var person = new Person();
        person.setFirstName("Max");
        person.setLastName("Mustermann");
        person.setBirthday(LocalDate.now());

        person = personRepository.save(person);

        personService.delete(person);

        assertTrue(personRepository.findAll().isEmpty());
    }

    @Test
    void testAddParent() {
        // Create parent
        var parent = createAndSavePerson("Reino", "Wendell");
        // Create child
        var child = createAndSavePerson("Wilfreda", "Steube");

        // Check if all Person is saved to database
        assertEquals(2, personRepository.findAll().size());

        // Add parent to child
        child = personService.addParent(child, parent);

        // Check if parent-child relationship is established
        assertEquals(1, child.getParents().size());
        assertTrue(child.getParents().contains(parent));
    }

    @Test
    void testAddThreeParents() {
        // Create three parents
        var parent1 = createAndSavePerson("Ben", "Feuchtwanger");
        var parent2 = createAndSavePerson("Arne", "Hohenstein");
        var parent3 = createAndSavePerson("Silvio", "Hauer");

        // Create child
        var child = createAndSavePerson("Fynn", "Hirsch");

        // Check if all Person is saved to database
        assertEquals(4, personRepository.findAll().size());

        // Add two parents to the child
        child = personService.addParent(child, parent1);
        child = personService.addParent(child, parent2);

        // Check if the parent-child relationship is established for the first two parents
        assertEquals(2, child.getParents().size());
        assertTrue(child.getParents().contains(parent1));
        assertTrue(child.getParents().contains(parent2));

        // Try to add the third parent, which should throw an exception
        try {
            child = personService.addParent(child, parent3);
        } catch (ResponseStatusException e) {
            assertEquals(new ResponseStatusException(HttpStatus.BAD_REQUEST).getStatusCode(), e.getStatusCode());
        }
    }

    private Person createAndSavePerson(String firstName, String lastName) {
        var person = new Person();
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setBirthday(LocalDate.now());
        return personService.save(person);
    }
}
