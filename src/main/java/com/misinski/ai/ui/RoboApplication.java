package com.misinski.ai.ui;

import com.misinski.ai.db.NbpRow;
import com.misinski.ai.db.PostgreSQLJDBC;
import com.misinski.ai.explorer.NBPFileSniffer;
import com.misinski.ai.prediction.PredictionFitter;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class RoboApplication extends Application implements UserActionListener {

    private PostgreSQLJDBC mJdbc;
    private PredictionFitter mFitter;
    private UIController mController;
    private ArrayList<ObservableList<XYChart.Data<Date, Number>>> mActualList = new ArrayList<>();
    private ArrayList<ObservableList<XYChart.Data<Date, Number>>> mPredictedList = new ArrayList<>();

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

        // initialize with empty lists
        mActualList.add(FXCollections.observableArrayList());
        mActualList.add(FXCollections.observableArrayList());
        mActualList.add(FXCollections.observableArrayList());
        mActualList.add(FXCollections.observableArrayList());
        mActualList.add(FXCollections.observableArrayList());
        mPredictedList.add(FXCollections.observableArrayList());
        mPredictedList.add(FXCollections.observableArrayList());
        mPredictedList.add(FXCollections.observableArrayList());
        mPredictedList.add(FXCollections.observableArrayList());
        mPredictedList.add(FXCollections.observableArrayList());
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getClassLoader().getResource("main.fxml").openStream());
        mController = loader.getController();
        mController.addListener(this);

        primaryStage.setScene(new Scene(root));

        primaryStage.setTitle("Kursy walut NBP");

        mJdbc.getArrayOfCharts().forEach(this::drawSeries);

        mController.setActualData(mActualList);
        mController.setPredictedData(mPredictedList);

        recalculatePrediction("Brak");

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
        mActualList.get(0).add(new XYChart.Data<>(nbpRow.effectiveDate, nbpRow.eur));
        mActualList.get(1).add(new XYChart.Data<>(nbpRow.effectiveDate, nbpRow.usd));
        mActualList.get(2).add(new XYChart.Data<>(nbpRow.effectiveDate, nbpRow.gbp));
        mActualList.get(3).add(new XYChart.Data<>(nbpRow.effectiveDate, nbpRow.chf));
        mActualList.get(4).add(new XYChart.Data<>(nbpRow.effectiveDate, nbpRow.aud));
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

    @Override
    public void onUserAction(String value) {
        System.out.println("onUserAction");
        recalculatePrediction(value);
    }

    private void recalculatePrediction(String value) {
        String newPrediction = value;
        System.out.println(newPrediction);
        int numberOfPredictedDays;

        if (newPrediction.equals("Brak")) {
            numberOfPredictedDays = 0;
        } else if (newPrediction.equals("tydzień")) {
            numberOfPredictedDays = 7;
        } else if (newPrediction.equals("miesiąc")) {
            numberOfPredictedDays = 30;
        } else {
            numberOfPredictedDays = 365;
        }

        for (int arrayIndex = 0; arrayIndex < mActualList.size(); ++arrayIndex) {
            ArrayList<XYChart.Data<Date, Number>> tempList = new ArrayList<>();

            double[][] xArray = new double[mJdbc.getArrayOfCharts().size()][1];
            double[] yArray = new double[mJdbc.getArrayOfCharts().size()];

            int i = 0;
            for (XYChart.Data<Date, Number> data : mActualList.get(arrayIndex)) {
                xArray[i][0] = data.getXValue().getTime();
                yArray[i] = data.getYValue().doubleValue();
                ++i;
            }

            double[] params = mFitter.getFunction(xArray, yArray);
            for (int j = 0; j < params.length; ++j) {
                System.out.println(params[j]);
            }

            Date lastDate = mActualList.get(arrayIndex).get(mActualList.get(arrayIndex).size() - 1).getXValue();

            DateTime dtOrg = new DateTime(lastDate);
            DateTime dtPlusOne = dtOrg.plusDays(1);
            Date futureDate = dtPlusOne.toDate();

            for (int k = mActualList.get(arrayIndex).size(); k < mActualList.get(arrayIndex).size() + numberOfPredictedDays; ++k) {
                tempList.add(new XYChart.Data<>(futureDate, calculateYValue(k, params)));
                dtPlusOne = dtPlusOne.plusDays(1);
                futureDate = dtPlusOne.toDate();
            }

            mPredictedList.get(arrayIndex).setAll(tempList);
        }
    }
}
