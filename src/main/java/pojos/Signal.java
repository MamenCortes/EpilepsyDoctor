package pojos;

import java.time.LocalDate;

public class Signal {
    private int id;
    private Integer frequency;
    private double[] ecg;
    private double[] acc;
    private String timestamp;
    private String comments;
    private LocalDate date;
    private int reportId;

    public Signal() {
        ecg = new double[0];
    }

    public Signal(LocalDate date, Integer frequency, double[] ecg, double[] acc, String timestamp, String comments, int reportId) {
        this.id = id;
        this.frequency = frequency;
        this.ecg = ecg;
        this.timestamp = timestamp;
        this.comments = comments;
        this.reportId = reportId;
        this.acc = acc;
        this.date = date;
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getFrequency() { return frequency; }
    public void setFrequency(Integer frequency) { this.frequency = frequency; }

    public double[] getEcg() { return ecg; }
    public void setEcg(double[] ecg) { this.ecg = ecg; }

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
                ", recording='" + ecg + '\'' +
                ", timestamp=" + timestamp +
                ", comments='" + comments + '\'' +
                ", reportId=" + reportId +
                '}';
    }

    public double[] getAcc() {
        return acc;
    }

    public void setAcc(double[] accx) {
        this.acc = accx;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}

