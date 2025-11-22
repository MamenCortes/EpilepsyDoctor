package ui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import pojos.Signal;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
    public static Signal readSignalFromZip(File zipFile) throws IOException {

        // 1) Crear carpeta temporal donde descomprimir
        Path tempDir = Files.createTempDirectory("signal_unzip_");
        File extractedCSV = null;

        // 2) Descomprimir ZIP
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(tempDir.toFile(), entry.getName());

                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    zis.transferTo(fos);
                }

                if (entry.getName().endsWith(".csv")) {
                    extractedCSV = outFile;
                }
            }
        }

        if (extractedCSV == null) {
            throw new IOException("CSV file not found inside ZIP");
        }

        // 3) Leer el CSV para obtener ECG + ACC
        return readSignalFromCsv(extractedCSV);
    }
    public static Signal readSignalFromCsv(File csvFile) throws IOException {
        List<Double> ecgList = new ArrayList<>();
        List<Double> accX = new ArrayList<>();
        List<Double> accY = new ArrayList<>();
        List<Double> accZ = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts.length < 4) continue;

                ecgList.add(Double.parseDouble(parts[0]));
                accX.add(Double.parseDouble(parts[1]));
                accY.add(Double.parseDouble(parts[2]));
                accZ.add(Double.parseDouble(parts[3]));
            }
        }

        // Construimos un Signal temporal
        Signal s = new Signal();
        s.setEcg(ecgList.stream().mapToDouble(Double::doubleValue).toArray());
        //como funciona lo de acc para q con solo un array pueda guardar los 3 ejes?
       //s.setAcc(accX, accY, accZ);
        s.setFrequency(1000); // o descubre del CSV o del metadata

        return s;
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