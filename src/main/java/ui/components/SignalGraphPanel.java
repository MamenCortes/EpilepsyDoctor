package ui.components;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Date;

import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import ui.ECGFileReader;
import ui.windows.Application;
/**
 * A panel that displays a biosignal over time using a scrolling
 * time–series graph. The panel preprocesses the raw signal, initializes a
 * JFreeChart time-series plot, and provides navigation controls to scroll
 * through the signal window.
 *
 *  @author MamenCortes
 */
public class SignalGraphPanel extends JPanel {
    private TimeSeries ecgSeries;
    private int windowSize = 1000;  // show 10,000 samples (~10s at 1kHz)
    private int currentIndex = 0;
    private int sf;
    private double[] fullData;
    private final Font titleFont = new Font("sansserif", 3, 15);
    private final Color titleColor = Application.dark_purple;
    private final Font contentFont = new Font("sansserif", 1, 12);
    private final Color contentColor = Application.dark_turquoise;
    private ImageIcon icon  = new ImageIcon(getClass().getResource("/icons/ekg-monitor64_02.png"));
    /**
     * Creates a new graph panel for displaying a time–series physiological signal.
     * The signal is preprocessed (centered and normalized), displayed in a
     * time–windowed chart, and presented with navigation buttons to scroll the data.
     *
     * @param rawData the raw signal samples
     * @param samplingFrequency the sampling frequency of the signal in Hz
     * @param title the title to display on the chart
     */
    public SignalGraphPanel(double[] rawData, int samplingFrequency, String title) {
        sf = samplingFrequency;
        //Process the signal: center and normalize
        this.fullData = preprocessSignal(rawData);
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

        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setDateFormatOverride(new java.text.SimpleDateFormat("s.S")); // show seconds.milliseconds
        domainAxis.setLabel("Time (s)");

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
    /**
     * Updates the displayed segment of the signal by clearing the current time series
     * and repopulating it with samples from the indicated range.
     *
     * @param start the starting sample index of the window
     * @param end the ending sample index of the window (exclusive)
     */
    private void updateWindow(int start, int end) {
        ecgSeries.clear();
        double msPerSample = 1000.0 / sf;  // 10 ms per sample for 100 Hz

        for (int i = start; i < end && i < fullData.length; i++) {
            long timeMillis = Math.round(i * msPerSample);
            ecgSeries.addOrUpdate(new Millisecond(new Date(timeMillis)), fullData[i]);
        }
    }
    /**
     * Scrolls the signal window by a given offset. The method adjusts the current
     * index while ensuring that the view remains within the bounds of the signal
     * data, then refreshes the display.
     *
     * @param delta the number of samples to shift the window by (positive or negative)
     */
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
