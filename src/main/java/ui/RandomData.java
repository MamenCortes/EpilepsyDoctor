package ui;

import org.example.SymptomType;
import pojos.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Random;

public class RandomData {
    public static Patient generateRandomPatient() {
        String[] POSSIBLE_NAMES = {"Alice", "Bob", "Charlie", "David", "Eve"};
        String[] POSSIBLE_SURNAMES = {"Smith", "Johnson", "Williams", "Brown", "Jones"};
        String[] POSSIBLE_SEXES = {"Male", "Female"};
        String EMAIL_DOMAIN = "@example.com";  // Fixed domain for email generation
        Random random = new Random();
        // Generate random values
        String name = POSSIBLE_NAMES[random.nextInt(POSSIBLE_NAMES.length)];
        String surname = POSSIBLE_SURNAMES[random.nextInt(POSSIBLE_SURNAMES.length)];
        String email = name.toLowerCase() + "." + surname.toLowerCase() + EMAIL_DOMAIN;
        int phone = 600000000 + random.nextInt(100000000);  // Random 10-digit number (1,000,000,000 to 1,999,999,999)
        String sex = POSSIBLE_SEXES[random.nextInt(POSSIBLE_SEXES.length)];
        // Create and populate the Patient object
        Patient patient = new Patient();
        patient.setName(name);
        patient.setSurname(surname);
        patient.setEmail(email);
        patient.setPhoneNumber(phone);  // Assuming setPhone accepts an Integer
        patient.setGender(sex);
        patient.setDateOfBirth(generateRandomLocalDate(new Random()));
        return patient;
    }

    public static Doctor generateRandomDoctor() {
        String[] POSSIBLE_NAMES = {"Alice", "Bob", "Charlie", "David", "Eve"};
        String[] POSSIBLE_SURNAMES = {"Smith", "Johnson", "Williams", "Brown", "Jones"};
        String[] POSSIBLE_ADDRESSES = {
                "123 Main St, Anytown",
                "456 Oak Ave, Othercity",
                "789 Pine Rd, Sometown",
                "101 Elm Blvd, Bigtown",
                "202 Maple Ln, Smallville"
        };
        String EMAIL_DOMAIN = "@example.com";  // Fixed domain for email generation

        Random random = new Random();
        // Generate random values
        String name = POSSIBLE_NAMES[random.nextInt(POSSIBLE_NAMES.length)];
        String surname = POSSIBLE_SURNAMES[random.nextInt(POSSIBLE_SURNAMES.length)];
        String email = name.toLowerCase() + "." + surname.toLowerCase() + EMAIL_DOMAIN;
        int phone = 900000000 + random.nextInt(100000000);  // Random 10-digit number (1,000,000,000 to 1,999,999,999)
        String address = POSSIBLE_ADDRESSES[random.nextInt(POSSIBLE_ADDRESSES.length)];
        // Create and populate the Doctor object
        Doctor doctor = new Doctor();
        doctor.setName(name);
        doctor.setSurname(surname);
        doctor.setEmail(email);
        doctor.setPhoneNumber(phone);  // Assuming setPhone accepts an Integer
        doctor.setDepartment(address);
        return doctor;
    }

    public static ArrayList<Signal> generateRandomSignalRecordings() {
        Random random = new Random();
        ArrayList<Signal> signalReports = new ArrayList<>();
        //ArrayList<Report> patientReports = new ArrayList<>();
        // Step 1: Generate a common date and create at least one Signal with it
        LocalDate commonDate = generateRandomLocalDate(random);  // Generate a random date to share

        Signal sharedReport = new Signal();
        sharedReport.setDate(commonDate);  // Same date as sharedSymptoms
        sharedReport.setComments("Random comments for shared signal");
        sharedReport.setFrequency(50 + random.nextInt(151));  // 50 to 200
        signalReports.add(sharedReport);
        // Step 2: Generate additional reports with random dates
        int additionalReports = 3 + random.nextInt(6);  // 3 to 8 more reports

        for (int i = 0; i < additionalReports; i++) {
            Signal sig = new Signal();
            sig.setDate(generateRandomLocalDate(random));  // Random date
            sig.setComments("Random comments " + i);
            sig.setFrequency(50 + random.nextInt(151));  // 50 to 200
            signalReports.add(sig);
        }
        return signalReports;  // Return the list of Reports*/
    }

    public static ArrayList<Report> generateRandomSymptomReports() {
        Random random = new Random();
        ArrayList<Report> signalReports = new ArrayList<>();
        //int numSymptoms = 1 + random.nextInt(15);  // 1 to 15 symptoms
        int numSymptoms = 15;

        for (int i = 0; i < numSymptoms; i++) {// Randomly choose Symptoms
            Report sym = new Report();
            sym.setDate(generateRandomLocalDate(random));  // Random date
            sym.setSymptom(SymptomType.values()[random.nextInt(SymptomType.values().length)]);
            signalReports.add(sym);
        }
        return signalReports;  // Return the list of Reports*/
    }

    // Helper method to generate a random date in "YYYY-MM-DD" format
    private static String generateRandomDate(Random random) {
        //int year = 2000 + random.nextInt(24);  // 2000 to 2023
        int year = 2025;
        int month = 1 + random.nextInt(12);    // 1 to 12
        int day = 1 + random.nextInt(28);      // 1 to 28 to avoid invalid dates
        return String.format("%02d/%02d/%04d", day, month, year);
    }
    private static LocalDate generateRandomLocalDate(Random random) {
        //int year = 2000 + random.nextInt(24);  // 2000 to 2023
        int year = 2025;
        int month = 1 + random.nextInt(12);    // 1 to 12
        int day = 1 + random.nextInt(28);      // 1 to 28 to avoid invalid dates
        return LocalDate.of(year, month, day);
    }

    public static void main(String[] args) {
        Patient randomPatient = RandomData.generateRandomPatient();
        Doctor randomDoctor = RandomData.generateRandomDoctor();
        System.out.println(randomPatient);
        System.out.println(randomDoctor);// Prints the generated Patient object

        /*ArrayList<Report> randomReports = generateRandomReports();
        for (Report report : randomReports) {
            System.out.println(report);  // Prints each report using its toString method
        }*/
    }
}
