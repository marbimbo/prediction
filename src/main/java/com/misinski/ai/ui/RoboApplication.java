package com.misinski.ai.ui;

import com.misinski.ai.db.NbpRow;
import com.misinski.ai.db.PostgreSQLJDBC;
import com.misinski.ai.explorer.NBPFileSniffer;
import com.misinski.ai.prediction.PredictionFitter;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RoboApplication extends Application {

    private PostgreSQLJDBC mJdbc;
    private PredictionFitter mFitter;
    private ArrayList<XYChart.Series> mSeriesList = new ArrayList<>();
    private UIController mController;

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

        mJdbc.produceArray();

        mFitter = new PredictionFitter();

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
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getClassLoader().getResource("main.fxml").openStream());
        mController = loader.getController();

        primaryStage.setScene(new Scene(root));

        primaryStage.setTitle("Kursy walut NBP");

        mJdbc.getArrayOfCharts().forEach(this::drawSeries);

        for (XYChart.Series<Date, Number> series : mSeriesList) {
            mController.addSeries(series);

            double[][] xArray = new double[mJdbc.getArrayOfCharts().size()][1];
            double[] yArray = new double[mJdbc.getArrayOfCharts().size()];

            int i = 0;
            for (XYChart.Data<Date, Number> data : series.getData()) {
                xArray[i][0] = data.getXValue().getTime();
                yArray[i] = data.getYValue().doubleValue();
                ++i;
            }

            double[] params = mFitter.getFunction(xArray, yArray);
            for (int j = 0; j < params.length; ++j) {
                System.out.println(params[j]);
            }

            XYChart.Series predictedSeries = new XYChart.Series();
            predictedSeries.setName("PREDICTION");

            java.util.Date lastDate = series.getData().get(series.getData().size() - 1).getXValue();
            DateTime dtOrg = new DateTime(lastDate);
            DateTime dtPlusOne = dtOrg.plusDays(1);
            Date futureDate = dtPlusOne.toDate();

            for (int k = series.getData().size(); k < series.getData().size() + 365; ++k) {
                predictedSeries.getData().add(new XYChart.Data(futureDate, calculateYValue(k, params)));
                dtPlusOne = dtPlusOne.plusDays(1);
                futureDate = dtPlusOne.toDate();
            }

            mController.addSeries(predictedSeries);
        }

        primaryStage.show();
    }

    private double calculateYValue(int k, double[] params) {
        return params[0] * k * k + params[1] * k + params[2];
    }

    private Map<Double, Double> map2Double(XYChart.Data<Date, Number> dateNumberData) {
        Double xValue = Double.valueOf(dateNumberData.getXValue().getTime());
        System.out.println(xValue);
        Double yValue = dateNumberData.getYValue().doubleValue();
        Map map = new HashMap<Double, Double>();
        map.put(xValue, yValue);
        return map;
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
