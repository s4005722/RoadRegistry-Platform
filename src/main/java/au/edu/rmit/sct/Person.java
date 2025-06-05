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

    public boolean updatePersonalDetails(
        String originalID,
        String newID,
        String newFirstName,
        String newLastName,
        String newAddress,
        String newBirthdate
    ) {
        
        if (!validatePersonID(newID)) {
            return false;
        }
        if (!validateAddress(newAddress)) {
            return false;
        }
        if (!validateDate(newBirthdate)) {
            return false;
        }

       
        LocalDate dob = LocalDate.parse(this.birthdate, DATE_FMT);
        int ageNow = Period.between(dob, LocalDate.now()).getYears();

        if (ageNow < 18 && !newAddress.equals(this.address)) {
            return false;
        }

        boolean birthChanged = !newBirthdate.equals(this.birthdate);
        boolean nameOrIDOrAddressChanged =
               !newID.equals(originalID)
            || !newFirstName.equals(this.firstName)
            || !newLastName.equals(this.lastName)
            || !newAddress.equals(this.address);

        if (birthChanged && nameOrIDOrAddressChanged) {
            return false;
        }

        
        char firstChar = originalID.charAt(0);
        if (Character.isDigit(firstChar)) {
            int digitValue = firstChar - '0';
            if ((digitValue % 2) == 0 && !newID.equals(originalID)) {
                return false;
            }
        }
        try {
            // Read all existing lines
            List<String> allLines = Files.readAllLines(PERSONS_FILE);

            List<String> updatedLines = new ArrayList<>(allLines.size());
            boolean replacedOne = false;

            for (String line : allLines) {
                // Split into at most 5 parts
                String[] parts = line.split("\\|", 5);
                if (parts.length < 5) {
                    updatedLines.add(line);
                    continue;
                }

                if (parts[0].equals(originalID) && !replacedOne) {
                    String newRecord = String.join("|",
                        newID,
                        newFirstName,
                        newLastName,
                        newAddress,
                        newBirthdate
                    );
                    updatedLines.add(newRecord);
                    replacedOne = true;
                } else {
                    updatedLines.add(line);
                }
            }

            // Overwrite persons.txt with the updated content
            Files.write(
                PERSONS_FILE,
                updatedLines,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            );

            // Update this object’s fields
            this.personID  = newID;
            this.firstName = newFirstName;
            this.lastName  = newLastName;
            this.address   = newAddress;
            this.birthdate = newBirthdate;

            return true;

        } catch (IOException e) {
            return false;
        }
    }

    public String addDemeritPoints(String offenseDate, int pts) {
        //Validate offenseDate format
        if (!validateDate(offenseDate)) {
            return "Failed";
        }
        if (pts < 1 || pts > 6) {
            return "Failed";
        }

        //Parse the offenseDate into a LocalDate
        LocalDate od;
        try {
            od = LocalDate.parse(offenseDate, DATE_FMT);
        } catch (DateTimeParseException e) {
            return "Failed";
        }

        //Build the record to append
        String record = String.join("|",
            personID,
            offenseDate,
            String.valueOf(pts)
        );

        try {
            //Append to demeritPoints.txt
            Files.write(
                DEMERITS_FILE,
                Collections.singletonList(record),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            return "Failed";
        }

        //Sum up all points in the last two years
        LocalDate cutoff = od.minusYears(2);
        int runningTotal = 0;

        try {
            List<String> lines = Files.readAllLines(DEMERITS_FILE);
            for (String line : lines) {
                String[] parts = line.split("\\|", 3);
                if (parts.length < 3) {
                    continue;
                }
                if (!parts[0].equals(personID)) {
                    continue;
                }
                //Parse the offense date
                LocalDate recordDate = LocalDate.parse(parts[1], DATE_FMT);
                int recordPts = Integer.parseInt(parts[2]);
                if (!recordDate.isBefore(cutoff)) {
                    runningTotal += recordPts;
                }
            }
        } catch (IOException e) {
        }

        //Calculate age on the offense date
        LocalDate dob;
        try {
            dob = LocalDate.parse(this.birthdate, DATE_FMT);
        } catch (DateTimeParseException ex) {
            return "Success";
        }
        int ageOnOffense = Period.between(dob, od).getYears();
        int threshold = (ageOnOffense < 21) ? 6 : 12;

        if (runningTotal > threshold) {
            this.isSuspended = true;
        }

        return "Success";
    }

  
    private boolean validatePersonID(String id) {
        if (id == null || id.length() != 10) {
            return false;
        }
        for (int i = 0; i < 2; i++) {
            char c = id.charAt(i);
            if (!Character.isDigit(c) || c < '2' || c > '9') {
                return false;
            }
        }
        for (int i = 8; i < 10; i++) {
            char c = id.charAt(i);
            if (c < 'A' || c > 'Z') {
                return false;
            }
        }
        int specialCount = 0;
        for (int i = 2; i < 8; i++) {
            char c = id.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                specialCount++;
            }
        }
        return (specialCount >= 2);
    }


    private boolean validateAddress(String addr) {
        if (addr == null) {
            return false;
        }
        String[] parts = addr.split("\\|", -1);
        if (parts.length != 5) {
            return false;
        }
        if (!"Victoria".equals(parts[3])) {
            return false;
        }
        try {
            Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private boolean validateDate(String d) {
        if (d == null) {
            return false;
        }
        try {
            LocalDate.parse(d, DATE_FMT);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public boolean isSuspended() {
        return isSuspended;
    }
}
