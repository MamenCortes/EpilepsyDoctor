package pojos;

import com.google.gson.JsonObject;
import ui.RandomData;

import java.time.LocalDate;
import java.util.ArrayList;

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
    private int assignedDoctorId; //No creo que haga falta


    public Patient() {
        this.name = "Jane";
        this.surname = "Doe";
        this.email = "jane.doe@gmail.com";
        this.phoneNumber = 12345678;
        this.gender = "NonBinay";
        this.dateOfBirth = LocalDate.now();
        symptoms = RandomData.generateRandomSymptomReports();
        recordings = RandomData.generateRandomSignalRecordings();
    }

    public Patient(int id, String name, String surname, String email, int phoneNumber, String gender, LocalDate dateOfBirth, int assignedDoctorId) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.assignedDoctorId = assignedDoctorId;
        symptoms = RandomData.generateRandomSymptomReports();
        recordings = RandomData.generateRandomSignalRecordings();
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

    public int getAssignedDoctorId() {
        return assignedDoctorId;
    }

    public void setAssignedDoctorId(int assignedDoctorId) {
        this.assignedDoctorId = assignedDoctorId;
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
                ", assignedDoctorId=" + assignedDoctorId +
                '}';
    }

    public ArrayList<Report> getSymptoms() {
        return symptoms;
    }

    public ArrayList<Signal> getRecordings() {
        return recordings;
    }

    public static Patient fromJson(JsonObject jason) {
        Patient patient = new Patient();
        patient.setId(jason.get("id").getAsInt());
        patient.setName(jason.get("name").getAsString());
        patient.setSurname(jason.get("surname").getAsString());
        patient.setEmail(jason.get("email").getAsString());
        patient.setPhoneNumber(jason.get("contact").getAsInt());
        patient.setDateOfBirth(LocalDate.parse(jason.get("dateOfBirth").getAsString()));
        patient.setGender(jason.get("gender").getAsString());
        patient.setAssignedDoctorId(jason.get("doctorId").getAsInt());
        return patient;
    }
}
