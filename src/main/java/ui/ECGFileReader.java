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

    public static Signal readSignalFromZip(File zipFile,int freq) throws IOException {
        System.out.println("Soy el servidor y voy a leer el zip "+zipFile.getAbsolutePath());
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
        System.out.println("CSV extraido en "+extractedCSV.getAbsolutePath());
        // 3) Leer el CSV para obtener ECG + ACC
        return readSignalFromCsv(extractedCSV,freq);
    }
    public static Signal readSignalFromCsv(File csvFile, int freq) throws IOException {
        List<Double> ecgList = new ArrayList<>();
        List<Double> accX = new ArrayList<>();
        List<Double> accY = new ArrayList<>();
        List<Double> accZ = new ArrayList<>();
        List<Double> accMagnitude = new ArrayList<>();
        System.out.println("Leyendo CSV "+csvFile.getAbsolutePath());
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;

            while ((line = br.readLine()) != null) {
                System.out.println("Linea leida: "+line);
                String[] parts = line.split(";");

                if (parts.length > 4) continue;

                ecgList.add(Double.parseDouble(parts[0]));
                System.out.println("ecg "+parts[0]);
                accMagnitude.add(Double.parseDouble(parts[1]));
            }
        }

        // Construimos un Signal temporal
        Signal s = new Signal();
        s.setEcg(ecgList.stream().mapToDouble(Double::doubleValue).toArray());
        s.setAcc(accMagnitude.stream().mapToDouble(Double::doubleValue).toArray());
        s.setFrequency(freq);
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