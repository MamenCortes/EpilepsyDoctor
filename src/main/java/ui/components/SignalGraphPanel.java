package ui.components;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Date;

import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import ui.ECGFileReader;
import ui.windows.Application;

public class SignalGraphPanel extends JPanel {
    private TimeSeries ecgSeries;
    private int windowSize = 10000;  // show 10,000 samples (~10s at 1kHz)
    private int currentIndex = 0;
    private double[] fullData;
    private final Font titleFont = new Font("sansserif", 3, 15);
    private final Color titleColor = Application.dark_purple;
    private final Font contentFont = new Font("sansserif", 1, 12);
    private final Color contentColor = Application.dark_turquoise;
    private ImageIcon icon  = new ImageIcon(getClass().getResource("/icons/ekg-monitor64_02.png"));

    public SignalGraphPanel(double[] rawData, int samplingFrequency, String title) {
        double[] trimmedECG = skipFirstMinute(rawData, samplingFrequency);
        System.out.println("Cut to " + trimmedECG.length + " samples.");

        //Process the signal: center and normalize
        this.fullData = preprocessSignal(trimmedECG);
        System.out.println("Signal Preprocessed");

        this.setLayout(new MigLayout("fill, inset 20, gap 0, wrap 3", "[]", "[95%][5%]"));
        this.setBackground(Color.white);

        ecgSeries = new TimeSeries("ECG");
        TimeSeriesCollection dataset = new TimeSeriesCollection(ecgSeries);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title, "Time", "Normalized Amplitude", dataset);
        ChartPanel chartPanel = new ChartPanel(chart);
        add(chartPanel, "cell 0 0, alignx center");

        //Change aesthetics and axis limits
        XYPlot plot = chart.getXYPlot();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis(); // eje Y
        rangeAxis.setAutoRange(false); // desactiva que el eje se ajuste automáticamente
        rangeAxis.setRange(-1, 1);


        // initial window
        updateWindow(0, windowSize);

        // add buttons to scroll left/right
        JButton left = new MyButton("←");
        JButton right = new MyButton("→");
        JButton resetZoom = new MyButton("Reset Zoom");
        JPanel nav = new JPanel();
        nav.setBackground(Color.white);
        nav.add(left);
        nav.add(right);
        nav.add(resetZoom);
        add(nav, "cell 0 1, alignx center");

        left.addActionListener(e -> scroll(-windowSize / 2));
        right.addActionListener(e -> scroll(windowSize / 2));
        //If autorange
        //resetZoom.addActionListener(e -> chartPanel.restoreAutoBounds());
        //IF fixed y bounds to -1,1
        resetZoom.addActionListener(e -> {
            // Get the plot
            XYPlot plot2 = chart.getXYPlot();
            // Set the y-axis range to [-1, 1]
            plot2.getRangeAxis().setRange(-1.0, 1.0);
            // Optionally reset the x-axis to auto or some fixed range
            plot.getDomainAxis().setAutoRange(true);
        });

        //pack();
        setVisible(true);
    }

    private void updateWindow(int start, int end) {
        ecgSeries.clear();
        for (int i = start; i < end && i < fullData.length; i++) {
            ecgSeries.addOrUpdate(new Millisecond(new Date(i)), fullData[i]);
        }
    }

    private void scroll(int delta) {
        currentIndex = Math.max(0, Math.min(currentIndex + delta, fullData.length - windowSize));
        updateWindow(currentIndex, currentIndex + windowSize);
    }

    /**
     * Centers and normalizes a signal.
     * @param signal Original signal values
     * @return Processed signal (centered around 0, normalized between -1 and 1)
     */
    public static double[] preprocessSignal(double[] signal) {
        int n = signal.length;
        double[] processed = new double[n];

        // Step 1: Compute the mean (centering)
        double sum = 0;
        for (double v : signal) sum += v;
        double mean = sum / n;

        // Step 2: Center the signal
        double maxAbs = 0; // to find max absolute value for normalization
        for (int i = 0; i < n; i++) {
            processed[i] = signal[i] - mean;
            if (Math.abs(processed[i]) > maxAbs) maxAbs = Math.abs(processed[i]);
        }

        // Step 3: Normalize to [-1, 1]
        if (maxAbs > 0) {
            for (int i = 0; i < n; i++) {
                processed[i] /= maxAbs;
            }
        }

        return processed;
    }

    /**
     * Returns a subarray starting after the first minute of recording.
     * @param signal Original signal
     * @param samplingFrequency Frecuencia de muestreo en Hz
     * @return Subarray de la señal a partir del minuto 1
     */
    public static double[] skipFirstMinute(double[] signal, int samplingFrequency) {
        int samplesToSkip = samplingFrequency * 60; // 60 segundos * frecuencia de muestreo
        if (samplesToSkip >= signal.length) {
            // Si la señal dura menos de un minuto, devolvemos un array vacío
            return new double[0];
        }

        double[] subSignal = new double[signal.length - samplesToSkip];
        System.arraycopy(signal, samplesToSkip, subSignal, 0, subSignal.length);
        return subSignal;
    }
}
