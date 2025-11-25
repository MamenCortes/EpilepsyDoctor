package pojos;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.example.SymptomType;
import ui.RandomData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Patient {

    private int id;
    private String name;
    private String surname;
    private String email;
    private int phoneNumber;
    private String gender;
    private LocalDate dateOfBirth;
    private ArrayList<Report> symptoms;
    private ArrayList<Signal> recordings;
    private int doctor_id;


    public Patient() {
        this.name = "Jane";
        this.surname = "Doe";
        this.email = "jane.doe@gmail.com";
        this.phoneNumber = 12345678;
        this.gender = "NonBinay";
        this.dateOfBirth = LocalDate.now();
        //symptoms = RandomData.generateRandomSymptomReports();
        //recordings = RandomData.generateRandomSignalRecordings();
        symptoms = new ArrayList<>();
        recordings = new ArrayList<>();
    }

    public Patient(int id, String name, String surname, String email, int phoneNumber, String gender, LocalDate dateOfBirth, int doctor_id) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.doctor_id = doctor_id;
        //symptoms = RandomData.generateRandomSymptomReports();
        //recordings = RandomData.generateRandomSignalRecordings();
        symptoms = new ArrayList<>();
        recordings = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public int getDoctor_id() {
        return doctor_id;
    }

    public void setDoctor_id(int doctor_id) {
        this.doctor_id = doctor_id;
    }

    public void setSymptoms(ArrayList<Report> symptoms) {
        this.symptoms = symptoms;
    }

    public void setRecordings(ArrayList<Signal> recordings) {
        this.recordings = recordings;
    }

    public ArrayList<Report> getSymptoms() {
        return symptoms;
    }

    public ArrayList<Signal> getRecordings() {
        return recordings;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber=" + phoneNumber +
                ", sex='" + gender + '\'' +
                ", age=" + dateOfBirth +
                ", assignedDoctorId=" + doctor_id +
                '}';
    }

    public static Patient fromJason(JsonObject jason) {
        Patient patient = new Patient();
        patient.setId(jason.get("id").getAsInt());
        patient.setName(jason.get("name").getAsString());
        patient.setSurname(jason.get("surname").getAsString());
        patient.setEmail(jason.get("email").getAsString());
        patient.setPhoneNumber(jason.get("contact").getAsInt());
        patient.setDateOfBirth(LocalDate.parse(jason.get("dateOfBirth").getAsString()));
        patient.setGender(jason.get("gender").getAsString());
        patient.setDoctor_id(jason.get("doctorId").getAsInt());

        // ----- SIGNALS -----
        if (jason.has("signals")) {
            JsonArray signalsJson = jason.getAsJsonArray("signals");
            ArrayList<Signal> signals = new ArrayList<>();

            for (JsonElement elem : signalsJson) {
                JsonObject sJson = elem.getAsJsonObject();
                signals.add(Signal.fromJson(sJson));
            }
            patient.setRecordings(signals);
        }

        // ----- SYMPTOMS / REPORTS -----
        if (jason.has("reports")) {
            JsonArray symptomsJson = jason.getAsJsonArray("reports");
            ArrayList<Report> reports = new ArrayList<>();

            for (JsonElement elem : symptomsJson) {
                JsonObject rJson = elem.getAsJsonObject();
                reports.add(Report.fromJson(rJson));
            }
            patient.setSymptoms(reports);
        }
        return patient;
    }

    /**
     * Converts this {@code Patient} into a {@link JsonObject}. The JSON object specifies all public fields
     * except the {@code active} field //TODO: por que no lo especifica?
     *
     * @return  a JSON representation of this patient
     *
     * @see JsonObject
     */
    public JsonObject toJason() {
        JsonObject jason = new JsonObject();
        jason.addProperty("id", id);
        jason.addProperty("name", name);
        jason.addProperty("surname", surname);
        jason.addProperty("email", email);
        jason.addProperty("contact", phoneNumber);
        jason.addProperty("dateOfBirth", dateOfBirth.toString());
        jason.addProperty("gender", gender);
        jason.addProperty("doctorId", doctor_id);
        return jason;
    }
}
