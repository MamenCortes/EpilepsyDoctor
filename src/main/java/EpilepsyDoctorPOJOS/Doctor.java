package EpilepsyDoctorPOJOS;

public class Doctor {

    private String name;
    private String surname;
    private int phoneNumber;
    private int ID;

    public Doctor() {

    }

    public Doctor(String name, String surname, int phoneNumber, int ID) {
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.ID = ID;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public int getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(int phoneNumber) { this.phoneNumber = phoneNumber; }

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
}