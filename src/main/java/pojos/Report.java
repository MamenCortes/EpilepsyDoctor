package pojos;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Report {

    private int id;
    private LocalDate date;
    private List<SymptomType> symptom;
    private int patientId;


    public Report() {
        this.date = LocalDate.now();
        this.symptom = new ArrayList<>();
        this.patientId = patientId;
    }

    public Report(LocalDate date, List<SymptomType> symptom, int patientId, int doctorId) {
        this.date = date;
        this.symptom = symptom;
        this.patientId = patientId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public List<SymptomType> getSymptoms() { return symptom; }
    public void setSymptomList(List<SymptomType> symptom) { this.symptom = symptom; }
    public void addSymptom(SymptomType symptom) {
        this.symptom.add(symptom);
    }

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

    /**
     * Creates a new {@code Report} instance from a {@link JsonObject}
     *
     * @param jsonObject  the JSON object containing this {@code Report} data
     * @return  a {@code Report} instance from the {@link JsonObject}
     *
     * @see JsonObject
     */
    public static Report fromJson(JsonObject jsonObject) {
        Report report = new Report();
        report.setId(jsonObject.get("id").getAsInt());
        report.setDate(LocalDate.parse(jsonObject.get("date").getAsString()));
        report.setPatientId(jsonObject.get("patientId").getAsInt());
        JsonArray symptomsJsonArray = jsonObject.get("symptoms").getAsJsonArray();
        List<SymptomType> symptoms = new ArrayList<>();
        for(JsonElement elem : symptomsJsonArray) {
            symptoms.add(SymptomType.valueOf(elem.getAsString()));
        }
        report.setSymptomList(symptoms);
        return report;
    }
}
