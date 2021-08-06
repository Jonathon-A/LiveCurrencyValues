package livecurrencyvalues;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class LiveCurrencyValues extends Application {

    //program doesnt work because api has stopped working
    private static final int MAX_DATA_POINTS = 50;
    private int xSeriesData = 0;
    private final XYChart.Series<Number, Number> series1 = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> series2 = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> series3 = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> series4 = new XYChart.Series<>();
    private  ExecutorService executor;
    private final ConcurrentLinkedQueue<Number> dataQ1 = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Number> dataQ2 = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Number> dataQ3 = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Number> dataQ4 = new ConcurrentLinkedQueue<>();

    private NumberAxis xAxis;

    public double EUR;
    public double GBP;
    public double AUD;
    public double NZD;
    URL url;

    private void init(Stage primaryStage) {

        xAxis = new NumberAxis();

        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        xAxis.setMinorTickVisible(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("USD");

        final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis) {

            @Override
            protected void dataItemAdded(XYChart.Series<Number, Number> series, int itemIndex, XYChart.Data<Number, Number> item) {
            }
        };

        lineChart.setAnimated(false);
        lineChart.setTitle("Live Currency Values");
        lineChart.setHorizontalGridLinesVisible(true);

        try {
            url = new URL("https://financialmodelingprep.com/api/v3/forex");
        } catch (MalformedURLException ex) {
            Logger.getLogger(LiveCurrencyValues.class.getName()).log(Level.SEVERE, null, ex);
        }
        series1.setName("EUR");
        series2.setName("GBP");
        series3.setName("AUD");
        series4.setName("NZD");
        lineChart.getData().addAll(series1, series2, series3, series4);
        primaryStage.setScene(new Scene(lineChart));
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Animated Line Chart Sample");
        init(stage);
        stage.show();

        executor = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });
        AddToQueue addToQueue = new AddToQueue();
        executor.execute(addToQueue);
        prepareTimeline();
    }

    private class AddToQueue implements Runnable {

        public void run() {
            try {
                web();
            } catch (IOException ex) {
                Logger.getLogger(LiveCurrencyValues.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                dataQ1.add(EUR);
                dataQ2.add(GBP);
                dataQ3.add(AUD);
                dataQ4.add(NZD);
                Thread.sleep(1000);
                executor.execute(this);
            } catch (Exception e) {

            }
        }
    }

    private void prepareTimeline() {

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                addDataToSeries();
            }
        }.start();
    }

    private void addDataToSeries() {
        for (int i = 0; i < 20; i++) {
            if (dataQ1.isEmpty()) {
                break;
            }
            series1.getData().add(new XYChart.Data<>(xSeriesData++, dataQ1.remove()));
            series2.getData().add(new XYChart.Data<>(xSeriesData++, dataQ2.remove()));
            series3.getData().add(new XYChart.Data<>(xSeriesData++, dataQ3.remove()));
            series4.getData().add(new XYChart.Data<>(xSeriesData++, dataQ4.remove()));
        }

        xAxis.setLowerBound(xSeriesData - MAX_DATA_POINTS);
        xAxis.setUpperBound(xSeriesData - 1);
    }

    public void web() throws IOException {
        int i = 0;
        int j = 0;
        String date = null;
        String str1 = null;
        String str2 = null;
        String str3 = null;
        String str4 = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
        for (String line; (line = reader.readLine()) != null;) {
            if ((j == 0) && (line.contains("date"))) {
                date = line;
                j = 1;
            }

            if (line.contains("bid")) {
                i++;
                if (i == 1) {
                    str1 = ("EUR ($" + (line.replaceAll("[^\\d.]", "")) + ")");
                    EUR = Double.parseDouble(line.replaceAll("[^\\d.]", ""));
                }
                if (i == 3) {
                    str2 = ("GBP ($" + (line.replaceAll("[^\\d.]", "")) + ")");
                    GBP = Double.parseDouble(line.replaceAll("[^\\d.]", ""));
                }
                if (i == 9) {
                    str3 = ("AUD ($" + (line.replaceAll("[^\\d.]", "")) + ")");
                    AUD = Double.parseDouble(line.replaceAll("[^\\d.]", ""));
                }
                if (i == 25) {
                    str4 = ("NZD ($" + (line.replaceAll("[^\\d.]", "")) + ")");
                    NZD = Double.parseDouble(line.replaceAll("[^\\d.]", ""));
                }
            }
        }

        String dateNew = date.substring(14, 33);
        System.out.println("EST: " + dateNew);
        System.out.println(str1);
        System.out.println(str2);
        System.out.println(str3);
        System.out.println(str4);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
