package ui;

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
                if (parts.length >= 3) {
                    ecgValues.add(Double.parseDouble(parts[2]));
                }
            }
        }

        // Convert to primitive array
        double[] result = new double[ecgValues.size()];
        for (int i = 0; i < ecgValues.size(); i++) result[i] = ecgValues.get(i);
        return result;
    }
}