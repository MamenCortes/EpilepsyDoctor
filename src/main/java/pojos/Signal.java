package pojos;

import java.time.LocalDate;

public class Signal {
    private int id;
    private Integer frequency;
    private double[] ecg;
    private String accx;
    private String accy;
    private String accz;
    private String timestamp;
    private String comments;
    private LocalDate date;
    private int reportId;

    public Signal() {
        ecg = new double[0];
    }

    public Signal(LocalDate date, Integer frequency, double[] ecg, String accx, String accy, String accz, String timestamp, String comments, int reportId) {
        this.id = id;
        this.frequency = frequency;
        this.ecg = ecg;
        this.timestamp = timestamp;
        this.comments = comments;
        this.reportId = reportId;
        this.accx = accx;
        this.accy = accy;
        this.accz = accz;
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

    public String getAccx() {
        return accx;
    }

    public void setAccx(String accx) {
        this.accx = accx;
    }

    public String getAccy() {
        return accy;
    }

    public void setAccy(String accy) {
        this.accy = accy;
    }

    public String getAccz() {
        return accz;
    }

    public void setAccz(String accz) {
        this.accz = accz;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}

