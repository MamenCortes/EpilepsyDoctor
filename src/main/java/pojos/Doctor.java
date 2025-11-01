package pojos;

import com.google.gson.JsonObject;

public class Doctor {

    private String name;
    private String surname;
    private Integer phoneNumber;
    private String email;
    private String speciality;
    private String department;
    private Integer ID;

    public Doctor() {
        this.name = "";
        this.surname = "";
        this.phoneNumber = 123456789;
        this.email = "";
        this.speciality = "Neurology";
        this.department = "";
    }

    public Doctor(String name, String surname, Integer phoneNumber, String email, String speciality, String department) {
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.speciality = speciality;
        this.department = department;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public Integer getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(Integer phoneNumber) { this.phoneNumber = phoneNumber; }

    public int getID() { return ID; }
    public void setID(int ID) { this.ID = ID; }

    @Override
    public String toString() {
        return "Doctor{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", phoneNumber=" + phoneNumber +
                ", ID=" + ID +
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
        doctor.setID(json.get("id").getAsInt());
        doctor.setName(json.get("name").getAsString());
        doctor.setSurname(json.get("surname").getAsString());
        doctor.setPhoneNumber(json.get("contact").getAsInt());
        doctor.setEmail(json.get("email").getAsString());
        doctor.setDepartment(json.get("department").getAsString());
        doctor.setSpeciality(json.get("speciality").getAsString());
        return doctor;
    }
}