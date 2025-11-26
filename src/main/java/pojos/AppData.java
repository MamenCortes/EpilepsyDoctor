package pojos;

public class AppData {
    private Doctor doctor;
    private User user;
    public Doctor getDoctor() {
        return doctor;
    }
    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public AppData() {
    }
    public AppData(Doctor doctor, User user) {
        this.doctor = doctor;
        this.user = user;
    }
}
