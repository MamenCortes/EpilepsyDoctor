package pojos;

import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;

public class Signal {
    private int id;
    private double[]  ecg;
    private double[] acc;
    private Integer frequency;
    private String timestamp;
    private String comments;
    private LocalDate date;
    private File zipFile;
    private int reportId;


    public Signal(int id, LocalDate date, String comments, double samplingRate) {
        this.id = id;
        this.date = date;
        this.comments = comments;
        this.frequency = (int) samplingRate;
        this.timestamp = "";
        this.reportId = -1;

    }

    public Signal(int id, LocalDate date, String comments, double samplingRate, File tempZip) {
        this.id = id;
        this.date = date;
        this.comments = comments;
        this.frequency = (int) samplingRate;
        this.timestamp = "";
        this.reportId = -1;
        this.zipFile = tempZip;

    }

    public Signal() {
        this.id = 0;
        this.date = LocalDate.now();
        this.comments = "";
        this.frequency = 0;
        this.timestamp = "";
        this.reportId = -1;
    }

    public static Signal fromJason(JsonObject json) {
        int id = json.has("signal_id") ? json.get("signal_id").getAsInt() : -1;
        int patientId = json.has("patient_id") ? json.get("patient_id").getAsInt() : -1;
        String comments = json.has("comments") ? json.get("comments").getAsString() : "";
        double samplingRate = json.has("sampling_rate") ? json.get("sampling_rate").getAsDouble() : 0;

        LocalDate date = null;
        if (json.has("date")) {
            date = LocalDate.parse(json.get("date").getAsString());
        }
        return new Signal(id, date, comments, samplingRate);
    }

    public static Signal fromJasonWithZip(JsonObject json) throws IOException {
        JsonObject meta = json.getAsJsonObject("metadata");
        int id = meta.get("signal_id").getAsInt();
        int patientId = meta.get("patient_id").getAsInt();
        String comments = meta.get("comments").getAsString();
        double samplingRate = meta.get("sampling_rate").getAsDouble();
        LocalDate date = LocalDate.parse(meta.get("date").getAsString());
        String base64Zip = json.get("data").getAsString();
        byte[] zipBytes = Base64.getDecoder().decode(base64Zip);
        File tempZip = File.createTempFile("signal_" + id + "_", ".zip");
        try (FileOutputStream fos = new FileOutputStream(tempZip)) {
            fos.write(zipBytes);
        }
        return new Signal(id, date, comments, samplingRate,tempZip);

    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getFrequency() { return frequency; }
    public void setFrequency(Integer frequency) { this.frequency = frequency; }


    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    @Override
    public String toString() {
        return "Signal{" +
                "id=" + id +
                ", frequency=" + frequency +
                ", timestamp=" + timestamp +
                ", comments='" + comments + '\'' +
                ", reportId=" + reportId +
                '}';
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double[] getEcg() {
        return this.ecg;
    }

    public double[] getAcc() {
        return this.acc;
    }

    public void setEcg(double[] result) {
        this.ecg= result;
    }

    public void setAcc(double[] acc) {
        this.acc = acc;
    }
    public File getZipFile() {
        return zipFile;
    }
}

