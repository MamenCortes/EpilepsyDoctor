package pojos;
import org.example.SymptomType;

import java.time.LocalDate;

public class Report {

    private int id;
    private LocalDate date;
    private SymptomType symptom;
    private int patientId;


    public Report() {
        this.date = LocalDate.now();
        this.symptom = SymptomType.None;
        this.patientId = patientId;
    }

    public Report(LocalDate date, SymptomType symptom, int patientId, int doctorId) {
        this.date = date;
        this.symptom = symptom;
        this.patientId = patientId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public SymptomType getSymptom() { return symptom; }
    public void setSymptom(SymptomType symptom) { this.symptom = symptom; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }


    @Override
    public String toString() {
        return "Report{" +
                "id=" + id +
                ", date=" + date +
                ", symptoms='" + symptom + '\'' +
                ", patientId=" + patientId +
                '}';
    }
}
