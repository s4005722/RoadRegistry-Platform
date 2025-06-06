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


    @Test
    @DisplayName("addPerson: valid data → returns true, file contains record")
    public void testAddPerson_valid() {
        Person p = new Person(
            "56@#_&$%AB",
            "Alice",
            "Walker",
            "32|Highland Street|Melbourne|Victoria|Australia",
            "15-11-1990"
        );
        assertTrue(p.addPerson(),
                   "Valid person should be added successfully");

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
            "12@_ABC", 
            "Bob",
            "Smith",
            "10|Main St|Melbourne|Victoria|Australia",
            "01-01-2000"
        );
        assertFalse(p.addPerson(),
                    "An ID shorter than 10 characters should cause addPerson() to return false");

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
            "23abcdefAB",   
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
        Person original = new Person(
            "56@#_&$%AB",
            "Alice",
            "Walker",
            "32|Highland Street|Melbourne|Victoria|Australia",
            "15-11-1990"
        );
        assertTrue(original.addPerson(),
                   "We must have a valid person in persons.txt for update tests");

        boolean result = original.updatePersonalDetails(
            "56@#_&$%AB",         
            "56@#_&$%AB",         
            "Alicia",             
            "Walker",             
            "32|Highland Street|Melbourne|Victoria|Australia", 
            "15-11-1990"          
        );
        assertTrue(result,
                   "Updating only firstName (everything else identical) should succeed");
    }

    @Test
    @DisplayName("updatePersonalDetails: under-18 address change → fails")
    public void testUpdatePersonalDetails_ageUnder18_addressChange() {
        Person teen = new Person(
            "23!!@@ZZAA",
            "Teen",
            "Tester",
            "10|Test St|Melbourne|Victoria|Australia",
            "01-01-2008"
        );
        assertTrue(teen.addPerson(),
                   "Teen must be in persons.txt so update can run");

        boolean result = teen.updatePersonalDetails(
            "23!!@@ZZAA",
            "23!!@@ZZAA",     
            "Teen",           
            "Tester",         
            "11|New St|Melbourne|Victoria|Australia", 
            "01-01-2008"      
        );
        assertFalse(result,
                    "Under-18 must not be allowed to change address");
    }

    @Test
    @DisplayName("updatePersonalDetails: birthdate change only → succeeds")
    public void testUpdatePersonalDetails_birthdateChangeOnly() {
        Person teen = new Person(
            "23!!@@ZZAA",
            "Teen",
            "Tester",
            "10|Test St|Melbourne|Victoria|Australia",
            "01-01-2008"
        );
        assertTrue(teen.addPerson(),
                   "Teen must exist before we can update birthdate");

        boolean result = teen.updatePersonalDetails(
            "23!!@@ZZAA",
            "23!!@@ZZAA",       
            "Teen",             
            "Tester",           
            "10|Test St|Melbourne|Victoria|Australia", 
            "02-02-2008"        
        );
        assertTrue(result,
                   "Under-18 is allowed to change birthdate only if no other fields change");
    }

    @Test
    @DisplayName("updatePersonalDetails: even-first-digit ID change → fails")
    public void testUpdatePersonalDetails_evenIDchange() {
        Person p2 = new Person(
            "24@@##ABCD",  
            "John",
            "Doe",
            "5|Park St|Melbourne|Victoria|Australia",
            "10-10-2000"
        );
        assertTrue(p2.addPerson(),
                   "Person with even-first-digit ID must be created first");

        boolean result = p2.updatePersonalDetails(
            "24@@##ABCD",  
            "26%%!!WXYZ",  
            "John",       
            "Doe",         
            "5|Park St|Melbourne|Victoria|Australia", 
            "10-10-2000"  
        );
        assertFalse(result,
                    "Cannot change ID if the first digit is even");
    }

    @Test
    @DisplayName("updatePersonalDetails: invalid new address format → fails")
    public void testUpdatePersonalDetails_invalidNewAddressFormat() {
        Person p = new Person(
            "56@#_&$%AB",
            "Alice",
            "Walker",
            "32|Highland Street|Melbourne|Victoria|Australia",
            "15-11-1990"
        );
        assertTrue(p.addPerson(),
                   "Must have a valid person in persons.txt");

        boolean result = p.updatePersonalDetails(
            "56@#_&$%AB",
            "56@#_&$%AB",  
            "Alice",       
            "Walker",      
            "32 Highland Street Melbourne Victoria Australia",
            "15-11-1990"  
        );
        assertFalse(result,
                    "New address missing '|' separators should fail validation");
    }



    @Test
    @DisplayName("addDemeritPoints: pts <1 → Failed")
    public void testAddDemeritPoints_ptsTooLow() {
        Person p = new Person(
            "45&*()!$GH",
            "Bob",
            "Smith",
            "100|Main Road|Melbourne|Victoria|Australia",
            "15-11-1990"
        );
        assertTrue(p.addPerson(),
                   "Person must be added before adding demerit points");

        assertEquals("Failed", p.addDemeritPoints("10-10-2024", 0),
                     "Points <1 should cause addDemeritPoints() to return 'Failed'");
    }

    @Test
    @DisplayName("addDemeritPoints: invalid date format → Failed")
    public void testAddDemeritPoints_invalidDate() {
        Person p = new Person(
            "45&*()!$GH",
            "Bob",
            "Smith",
            "100|Main Road|Melbourne|Victoria|Australia",
            "15-11-1990"
        );
        assertTrue(p.addPerson());

        assertEquals("Failed", p.addDemeritPoints("2024/10/10", 3),
                     "Invalid date format should cause addDemeritPoints() to return 'Failed'");
    }

    @Test
    @DisplayName("addDemeritPoints: total under threshold for over-21 → no suspension")
    public void testAddDemeritPoints_underThreshold() {
        Person p = new Person(
            "45&*()!$GH",
            "Bob",
            "Smith",
            "100|Main Road|Melbourne|Victoria|Australia",
            "15-11-1990"
        );
        assertTrue(p.addPerson());

        assertEquals("Success", p.addDemeritPoints("01-01-2023", 3));
        assertFalse(p.isSuspended(),
                    "3 points < 12 threshold for over-21 → not suspended");

        assertEquals("Success", p.addDemeritPoints("01-06-2023", 4));
        assertFalse(p.isSuspended(),
                    "Total 7 < 12 → still not suspended for over-21");
    }

    @Test
    @DisplayName("addDemeritPoints: total > threshold for under-21 → suspend")
    public void testAddDemeritPoints_under21_exceed() {
        Person teen = new Person(
            "78**&&RTYU",
            "Cara",
            "Lee",
            "12|Elm St|Melbourne|Victoria|Australia",
            "01-01-2005"
        );
        assertTrue(teen.addPerson());

        assertEquals("Success", teen.addDemeritPoints("01-03-2023", 4));
        assertFalse(teen.isSuspended(),
                    "4 points < 6 threshold for under-21 → not suspended");

        assertEquals("Success", teen.addDemeritPoints("01-06-2023", 3));
        assertTrue(teen.isSuspended(),
                   "7 points > 6 threshold for under-21 → should be suspended");
    }

    @Test
    @DisplayName("addDemeritPoints: total > threshold for over-21 → suspended")
    public void testAddDemeritPoints_over21_exceed() {
       Person p = new Person(
            "45&*()!$GH",
            "Bob",
            "Smith",
            "100|Main Road|Melbourne|Victoria|Australia",
            "15-11-1990"
        );
        assertTrue(p.addPerson());

        assertEquals("Success", p.addDemeritPoints("01-01-2023", 6));
        assertFalse(p.isSuspended(),
                    "6 points < 12 threshold for over-21 → not suspended");

        assertEquals("Success", p.addDemeritPoints("01-06-2023", 6));
        assertFalse(p.isSuspended(),
                    "12 points = 12 threshold for over-21 ⇒ still not suspended");

        assertEquals("Success", p.addDemeritPoints("01-12-2023", 1));
        assertTrue(p.isSuspended(),
                   "13 points > 12 threshold for over-21 ⇒ should be suspended");
    }
}