package EpilepsyDoctorPOJOS;
import java.time.LocalDateTime;

    public class Signal {
        private int id;
        private double frequency;
        private String recording;
        private LocalDateTime timestamp;
        private String comments;
        private int reportId;

        public Signal() {}

        public Signal(int id, double frequency, String recording, LocalDateTime timestamp, String comments, int reportId) {
            this.id = id;
            this.frequency = frequency;
            this.recording = recording;
            this.timestamp = timestamp;
            this.comments = comments;
            this.reportId = reportId;
        }


        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public double getFrequency() { return frequency; }
        public void setFrequency(double frequency) { this.frequency = frequency; }

        public String getRecording() { return recording; }
        public void setRecording(String recording) { this.recording = recording; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }

        public int getReportId() { return reportId; }
        public void setReportId(int reportId) { this.reportId = reportId; }

        @Override
        public String toString() {
            return "Signal{" +
                    "id=" + id +
                    ", frequency=" + frequency +
                    ", recording='" + recording + '\'' +
                    ", timestamp=" + timestamp +
                    ", comments='" + comments + '\'' +
                    ", reportId=" + reportId +
                    '}';
        }
    }

