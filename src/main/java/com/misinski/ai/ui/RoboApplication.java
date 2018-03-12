package com.misinski.ai.ui;

import com.misinski.ai.db.NbpRow;
import com.misinski.ai.db.PostgreSQLJDBC;
import com.misinski.ai.explorer.NBPFileSniffer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Date;

public class RoboApplication extends Application {

    private PostgreSQLJDBC mJdbc;
    private ArrayList<XYChart.Series> mSeriesList = new ArrayList<>();

    /**
     * Main function that opens the "Hello World!" window
     *
     * @param args the command line arguments
     */
    public static void main(final String[] arguments) {
        launch(arguments);
    }

    @Override
    public void init() {
        mJdbc = new PostgreSQLJDBC();
        mJdbc.dropTable();
        mJdbc.createTable();
        NBPFileSniffer sniffer = new NBPFileSniffer(mJdbc);
        sniffer.sniffForFiles();
        //By default this does nothing, but it
        //can carry out code to set up your app.
        //It runs once before the start method,
        //and after the constructor.


        XYChart.Series series = new XYChart.Series();
        series.setName("EUR");
        mSeriesList.add(series);
        series = new XYChart.Series();
        series.setName("USD");
        mSeriesList.add(series);
        series = new XYChart.Series();
        series.setName("GBP");
        mSeriesList.add(series);
        series = new XYChart.Series();
        series.setName("CHF");
        mSeriesList.add(series);
        series = new XYChart.Series();
        series.setName("AUD");
        mSeriesList.add(series);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Kursy walut NBP");
        //defining the axes
        final DateAxis xAxis = new DateAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Data");
        yAxis.setLabel("x/PLN");
        //creating the chart
        final LineChart<Date, Number> lineChart = new LineChart<>(xAxis, yAxis);

        lineChart.setTitle("Kursy walut NBP");
        //defining a series


        mJdbc.getArrayOfCharts().forEach(this::drawSeries);

        Scene scene = new Scene(lineChart, 800, 600);

        for (XYChart.Series series : mSeriesList) {
            lineChart.getData().add(series);
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void drawSeries(NbpRow nbpRow) {
        mSeriesList.get(0).getData().add(new XYChart.Data(nbpRow.effectiveDate, nbpRow.eur));
        mSeriesList.get(1).getData().add(new XYChart.Data(nbpRow.effectiveDate, nbpRow.usd));
        mSeriesList.get(2).getData().add(new XYChart.Data(nbpRow.effectiveDate, nbpRow.gbp));
        mSeriesList.get(3).getData().add(new XYChart.Data(nbpRow.effectiveDate, nbpRow.chf));
        mSeriesList.get(4).getData().add(new XYChart.Data(nbpRow.effectiveDate, nbpRow.aud));
    }

    @Override
    public void stop() {
        //By default this does nothing
        //It runs if the user clicks the go-away button
        //closing the window or if Platorm.exit() is called.
        //Use Platorm.exit() instead of System.exit(0).
        //is called. This is where you should offer to
        //save unsaved stuff the user has generated.
    }
}
