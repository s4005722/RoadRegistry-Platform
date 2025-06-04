package au.edu.rmit.sct;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;


public class Person {

    private String personID;    
    private String firstName;
    private String lastName;
    private String address;      
    private String birthdate;    
    private boolean isSuspended = false;

    private static final Path PERSONS_FILE  = Paths.get("persons.txt");
    private static final Path DEMERITS_FILE = Paths.get("demeritPoints.txt");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");


    public Person(String personID,
                  String firstName,
                  String lastName,
                  String address,
                  String birthdate) {
        this.personID  = personID;
        this.firstName = firstName;
        this.lastName  = lastName;
        this.address   = address;
        this.birthdate = birthdate;
    }


    public boolean addPerson() {
        // 1) Validate ID
        if (!validatePersonID(personID)) {
            return false;
        }
        // 2) Validate address
        if (!validateAddress(address)) {
            return false;
        }
        // 3) Validate birthdate format
        if (!validateDate(birthdate)) {
            return false;
        }

        String record = String.join("|",
            personID,
            firstName,
            lastName,
            address,
            birthdate
        );

        try {
            Files.write(
                PERSONS_FILE,
                Collections.singletonList(record),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
            return true;
        } catch (IOException e) {
            return false;
        }
    }
