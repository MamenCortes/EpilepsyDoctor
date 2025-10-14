package EpilepsyDoctorPOJOS;
import java.time.LocalDate;

public class Report {

    private int id;
    private LocalDate date;
    private String symptoms;
    private int patientId;
    private int doctorId;


    public Report() {}

    public Report(int id, LocalDate date, String symptoms, int patientId, int doctorId) {
        this.id = id;
        this.date = date;
        this.symptoms = symptoms;
        this.patientId = patientId;
        this.doctorId = doctorId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    @Override
    public String toString() {
        return "Report{" +
                "id=" + id +
                ", date=" + date +
                ", symptoms='" + symptoms + '\'' +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                '}';
    }
}
