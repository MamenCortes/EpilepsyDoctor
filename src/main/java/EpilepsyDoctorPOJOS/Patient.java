package EpilepsyDoctorPOJOS;

public class Patient {

    private int id;
    private String name;
    private String surname;
    private String email;
    private int phoneNumber;
    private String sex;
    private int age;
    private int assignedDoctorId;
    public Patient() {}

    public Patient(int id, String name, String surname, String email, int phoneNumber, String sex, int age, int assignedDoctorId) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.sex = sex;
        this.age = age;
        this.assignedDoctorId = assignedDoctorId;
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

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
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
                ", sex='" + sex + '\'' +
                ", age=" + age +
                ", assignedDoctorId=" + assignedDoctorId +
                '}';
    }

}
