package pojos;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Doctor {

    private String name;
    private String surname;
    private Integer phoneNumber;
    private String email;
    private String speciality;
    private String department;
    private Integer id;
    private List<Patient> patients;

    public Doctor() {
        id = 0;
        this.name = "";
        this.surname = "";
        this.phoneNumber = 123456789;
        this.email = "";
        this.speciality = "Neurology";
        this.department = "";
        patients = new ArrayList<>();
    }

    public Doctor(String name, String surname, Integer phoneNumber, String email, String speciality, String department) {
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.speciality = speciality;
        this.department = department;
        patients = new ArrayList<>();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public Integer getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(Integer phoneNumber) { this.phoneNumber = phoneNumber; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return "Doctor{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", phoneNumber=" + phoneNumber +
                ", ID=" + id +
                '}';
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public static Doctor fromJason(JsonObject json) {
        Doctor doctor = new Doctor();
        doctor.setId(json.get("id").getAsInt());
        doctor.setName(json.get("name").getAsString());
        doctor.setSurname(json.get("surname").getAsString());
        doctor.setPhoneNumber(json.get("contact").getAsInt());
        doctor.setEmail(json.get("email").getAsString());
        doctor.setDepartment(json.get("department").getAsString());
        doctor.setSpeciality(json.get("speciality").getAsString());
        return doctor;
    }

    public List<Patient> getPatients() {
        return patients;
    }

    public void setPatients(List<Patient> patients) {
        this.patients = patients;
    }

    /**
     * Converts this {@code Doctor} into a {@link JsonObject}. The JSON object specifies all public fields
     * except the {@code active} field //TODO: por que no lo especifica?
     *
     * @return  a JSON representation of this doctor
     *
     * @see JsonObject
     */
    public JsonObject toJason() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", name);
        json.addProperty("surname", surname);
        json.addProperty("contact", phoneNumber);
        json.addProperty("email", email);
        json.addProperty("department", department);
        json.addProperty("speciality", speciality);
        return json;
    }
}