package au.edu.rmit.sct;

import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class PersonTest {

    @BeforeEach
    public void cleanFiles() {
        try {
            Files.deleteIfExists(Paths.get("persons.txt"));
            Files.deleteIfExists(Paths.get("demeritPoints.txt"));
        } catch (IOException ignored) {}
    }

    // ======== Tests for addPerson() ========

    @Test
    @DisplayName("addPerson: valid data → returns true, file contains record")
    public void testAddPerson_valid() {
        // We start with no persons.txt (cleanFiles() has deleted it)
        Person p = new Person(
            "56@#_&$%AB",
            "Alice",
            "Walker",
            "32|Highland Street|Melbourne|Victoria|Australia",
            "15-11-1990"
        );
        assertTrue(p.addPerson(),
                   "Valid person should be added successfully");

        // Now read back the single line in persons.txt and compare with what we expect
        try {
            String content = Files.readString(Paths.get("persons.txt")).trim();
            String expected =
                "56@#_&$%AB|Alice|Walker|"
              + "32|Highland Street|Melbourne|Victoria|Australia|"
              + "15-11-1990";
            assertEquals(expected, content,
                         "persons.txt should contain exactly one pipe-delimited record");
        } catch (IOException e) {
            fail("Unable to read persons.txt after addPerson()");
        }
    }

    @Test
    @DisplayName("addPerson: ID too short → returns false")
    public void testAddPerson_invalidID_length() {
        Person p = new Person(
            "12@_ABC",  // only 7 characters, not 10
            "Bob",
            "Smith",
            "10|Main St|Melbourne|Victoria|Australia",
            "01-01-2000"
        );
        assertFalse(p.addPerson(),
                    "An ID shorter than 10 characters should cause addPerson() to return false");

        // Because addPerson() returned false, persons.txt should not exist at all
        assertFalse(Files.exists(Paths.get("persons.txt")),
                    "persons.txt should not be created when addPerson fails");
    }

    @Test
    @DisplayName("addPerson: state not Victoria → returns false")
    public void testAddPerson_invalidAddress_state() {
        Person p = new Person(
            "23!!@@ZZAA",
            "Carol",
            "Taylor",
            // State is "NewSouthWales" instead of "Victoria"
            "20|King St|Melbourne|NewSouthWales|Australia",
            "05-05-1995"
        );
        assertFalse(p.addPerson(),
                    "Address whose State is not 'Victoria' should cause addPerson() to return false");
        assertFalse(Files.exists(Paths.get("persons.txt")),
                    "persons.txt should remain absent when address validation fails");
    }

    @Test
    @DisplayName("addPerson: birthdate wrong format → returns false")
    public void testAddPerson_invalidBirthdate_format() {
        Person p = new Person(
            "34%$^&*GHJ",
            "David",
            "Brown",
            "15|Queen St|Melbourne|Victoria|Australia",
            // Wrong format: "YYYY-MM-DD" instead of "DD-MM-YYYY"
            "1990-11-15"
        );
        assertFalse(p.addPerson(),
                    "Birthdate not in DD-MM-YYYY format should cause addPerson() to return false");
        assertFalse(Files.exists(Paths.get("persons.txt")),
                    "persons.txt should not be created when birthdate is invalid");
    }

    @Test
    @DisplayName("addPerson: ID lacks ≥2 specials in positions 3–8 → returns false")
    public void testAddPerson_invalidID_noSpecials() {
        Person p = new Person(
            "23abcdefAB",   // positions 3..8 are only letters/digits (no special characters)
            "Eve",
            "Johnson",
            "50|Oak St|Melbourne|Victoria|Australia",
            "20-12-1992"
        );
        assertFalse(p.addPerson(),
                    "An ID with fewer than two special chars in positions 3–8 should fail addPerson()");
        assertFalse(Files.exists(Paths.get("persons.txt")),
                    "persons.txt should not exist after a failed addPerson()");
    }
