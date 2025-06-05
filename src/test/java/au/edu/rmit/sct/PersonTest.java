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

    @Test
    @DisplayName("updatePersonalDetails: rename only → succeeds")
    public void testUpdatePersonalDetails_renameOnly() {
        // First insert a valid Person into persons.txt
        Person original = new Person(
            "56@#_&$%AB",
            "Alice",
            "Walker",
            "32|Highland Street|Melbourne|Victoria|Australia",
            "15-11-1990"
        );
        assertTrue(original.addPerson(),
                   "We must have a valid person in persons.txt for update tests");

        // Now attempt to update only the firstName (no other field changes)
        boolean result = original.updatePersonalDetails(
            "56@#_&$%AB",         // originalID
            "56@#_&$%AB",         // newID (same as old ID)
            "Alicia",             // new firstName
            "Walker",             // new lastName (unchanged)
            "32|Highland Street|Melbourne|Victoria|Australia", // address unchanged
            "15-11-1990"          // birthdate unchanged
        );
        assertTrue(result,
                   "Updating only firstName (everything else identical) should succeed");
    }

    @Test
    @DisplayName("updatePersonalDetails: under-18 address change → fails")
    public void testUpdatePersonalDetails_ageUnder18_addressChange() {
        // Create and add a teen who is under 18 based on birthdate 01-01-2008
        Person teen = new Person(
            "23!!@@ZZAA",
            "Teen",
            "Tester",
            "10|Test St|Melbourne|Victoria|Australia",
            "01-01-2008"
        );
        assertTrue(teen.addPerson(),
                   "Teen must be in persons.txt so update can run");

        // Now try to change teen's address (under-18 cannot change address)
        boolean result = teen.updatePersonalDetails(
            "23!!@@ZZAA",
            "23!!@@ZZAA",     // ID unchanged
            "Teen",           // firstName unchanged
            "Tester",         // lastName unchanged
            "11|New St|Melbourne|Victoria|Australia", // NEW address
            "01-01-2008"      // birthdate unchanged
        );
        assertFalse(result,
                    "Under-18 must not be allowed to change address");
    }

    @Test
    @DisplayName("updatePersonalDetails: birthdate change only → succeeds")
    public void testUpdatePersonalDetails_birthdateChangeOnly() {
        // Create and add a teen under 18
        Person teen = new Person(
            "23!!@@ZZAA",
            "Teen",
            "Tester",
            "10|Test St|Melbourne|Victoria|Australia",
            "01-01-2008"
        );
        assertTrue(teen.addPerson(),
                   "Teen must exist before we can update birthdate");

        // Only change birthdate (no other field changes)
        boolean result = teen.updatePersonalDetails(
            "23!!@@ZZAA",
            "23!!@@ZZAA",       // ID unchanged
            "Teen",             // firstName unchanged
            "Tester",           // lastName unchanged
            "10|Test St|Melbourne|Victoria|Australia", // address unchanged
            "02-02-2008"        // NEW birthdate
        );
        assertTrue(result,
                   "Under-18 is allowed to change birthdate only if no other fields change");
    }

    @Test
    @DisplayName("updatePersonalDetails: even-first-digit ID change → fails")
    public void testUpdatePersonalDetails_evenIDchange() {
        // “24@@##ABCD” starts with '2' (even), so ID change must not be allowed
        Person p2 = new Person(
            "24@@##ABCD",   // ID first char '2' (even)
            "John",
            "Doe",
            "5|Park St|Melbourne|Victoria|Australia",
            "10-10-2000"
        );
        assertTrue(p2.addPerson(),
                   "Person with even-first-digit ID must be created first");

        // Attempt to change ID from "24@@##ABCD" to "26%%!!WXYZ" → should fail
        boolean result = p2.updatePersonalDetails(
            "24@@##ABCD",  // originalID
            "26%%!!WXYZ",  // newID (different)
            "John",        // firstName unchanged
            "Doe",         // lastName unchanged
            "5|Park St|Melbourne|Victoria|Australia", // address unchanged
            "10-10-2000"   // birthdate unchanged
        );
        assertFalse(result,
                    "Cannot change ID if the first digit is even");
    }

    @Test
    @DisplayName("updatePersonalDetails: invalid new address format → fails")
    public void testUpdatePersonalDetails_invalidNewAddressFormat() {
        // Add a valid person first
        Person p = new Person(
            "56@#_&$%AB",
            "Alice",
            "Walker",
            "32|Highland Street|Melbourne|Victoria|Australia",
            "15-11-1990"
        );
        assertTrue(p.addPerson(),
                   "Must have a valid person in persons.txt");

        // Now attempt to update with an address that has no '|' separators
        boolean result = p.updatePersonalDetails(
            "56@#_&$%AB",
            "56@#_&$%AB",  // ID unchanged
            "Alice",       // firstName unchanged
            "Walker",      // lastName unchanged
            // Missing any '|' so validateAddress() should fail
            "32 Highland Street Melbourne Victoria Australia",
            "15-11-1990"   // birthdate unchanged
        );
        assertFalse(result,
                    "New address missing '|' separators should fail validation");
    }
