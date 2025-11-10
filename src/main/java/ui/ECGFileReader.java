package ui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import pojos.Signal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ECGFileReader {
    public static double[] readECGFromFile(String path) throws IOException {
        List<Double> ecgValues = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            boolean dataSection = false;

            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) {
                    if (line.contains("EndOfHeader")) dataSection = true;
                    continue;
                }
                if (!dataSection || line.trim().isEmpty()) continue;

                String[] parts = line.split("\t");
                if (parts.length >= 5) {
                    ecgValues.add(Double.parseDouble(parts[5]));
                }
            }
        }

        // Convert to primitive array
        double[] result = new double[ecgValues.size()];
        for (int i = 0; i < ecgValues.size(); i++) result[i] = ecgValues.get(i);
        return result;
    }

    public static double[] readACCFromFile(String path) throws IOException {
        List<Double> ecgValues = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            boolean dataSection = false;

            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) {
                    if (line.contains("EndOfHeader")) dataSection = true;
                    continue;
                }
                if (!dataSection || line.trim().isEmpty()) continue;

                String[] parts = line.split("\t");
                if (parts.length >= 5) {
                    ecgValues.add(Double.parseDouble(parts[6]));
                }
            }
        }

        // Convert to primitive array
        double[] result = new double[ecgValues.size()];
        for (int i = 0; i < ecgValues.size(); i++) result[i] = ecgValues.get(i);
        return result;
    }

    public static Signal readSignalFromFile(String path) throws IOException {
        List<Double> ecgValues = new ArrayList<>();
        List<Double> accValues = new ArrayList<>();
        Signal signal = new Signal();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            boolean dataSection = false;

            while ((line = br.readLine()) != null) {
                if (line.startsWith("# {")) {
                    // Remove the '#' and any spaces
                    String jsonPart = line.substring(1).trim();
                    signal.setFrequency(getSamplingRate(jsonPart));
                }
                if (line.startsWith("#")) {
                    if (line.contains("EndOfHeader")) dataSection = true;
                    continue;
                }
                if (!dataSection || line.trim().isEmpty()) continue;

                String[] parts = line.split("\t");
                if (parts.length >= 5) {
                    ecgValues.add(Double.parseDouble(parts[5]));
                    accValues.add(Double.parseDouble(parts[6]));
                }
            }
        }

        // Convert to primitive array
        double[] result = new double[ecgValues.size()];
        for (int i = 0; i < ecgValues.size(); i++) result[i] = ecgValues.get(i);
        signal.setEcg(result);
        double[] acc = new double[accValues.size()];
        for (int i = 0; i < accValues.size(); i++) acc[i] = accValues.get(i);
        signal.setAcc(acc);
        return signal;
    }

    private static Integer getSamplingRate(String metadata){
            // Parse JSON
            JsonObject root = JsonParser.parseString(metadata).getAsJsonObject();

            // The top-level key is the device address (unknown, so get first entry)
            String deviceKey = root.keySet().iterator().next();

            JsonObject deviceData = root.getAsJsonObject(deviceKey);

            // Get the sampling rate
            int samplingRate = deviceData.get("sampling rate").getAsInt();
            return samplingRate;
    }
}