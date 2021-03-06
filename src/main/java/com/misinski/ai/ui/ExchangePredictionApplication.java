package com.misinski.ai.ui;

import com.misinski.ai.db.NbpRow;
import com.misinski.ai.db.PostgreSQLJDBC;
import com.misinski.ai.explorer.FileCreatedListener;
import com.misinski.ai.explorer.JsonFileSniffer;
import com.misinski.ai.http.JsonDownloader;
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
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

public class ExchangePredictionApplication extends Application implements UserActionListener, FileCreatedListener {

    private static final int ONE_YEAR = 1;

    private PostgreSQLJDBC mJdbc;
    private PredictionFitter mFitter;
    private UIController mController;
    private ArrayList<ObservableList<XYChart.Data<Date, Number>>> mActualList = new ArrayList<>();
    private ArrayList<ObservableList<XYChart.Data<Date, Number>>> mPredictedList = new ArrayList<>();
    private LocalDate mDateFrom = LocalDate.now().minusYears(2 * ONE_YEAR);
    private LocalDate mDateTo = LocalDate.now();
    private JsonDownloader mDownloader;
    private JsonFileSniffer mSniffer;

    public static void main(final String[] arguments) {
        launch(arguments);
    }

    @Override
    public void init() {
        mDownloader = new JsonDownloader();

        mJdbc = new PostgreSQLJDBC();
        mJdbc.createTable();
        mSniffer = new JsonFileSniffer(mJdbc, this);
        mSniffer.sniffForFiles();

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
    public void start(final Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getClassLoader().getResource("main.fxml").openStream());
        mController = loader.getController();
        mController.setListener(this);

        primaryStage.setScene(new Scene(root));

        primaryStage.setTitle("Kursy walut NBP");

        mJdbc.produceArray(mDateFrom, mDateTo);
        mJdbc.getArrayOfCharts().forEach(this::drawSeries);

        mController.setActualData(mActualList);
        mController.setPredictedData(mPredictedList);

        mController.setDateFrom(mDateFrom);
        mController.setDateTo(mDateTo);

        mController.setCallBack();

        primaryStage.show();
        primaryStage.setMaximized(true);
    }

    private double calculateYValue(final int k, final double[] params) {
        return params[0] * k * k + params[1] * k + params[2];
    }

    private void drawSeries(final NbpRow nbpRow) {
        mActualList.get(0).add(new XYChart.Data<>(nbpRow.effectiveDate, nbpRow.eur));
        mActualList.get(1).add(new XYChart.Data<>(nbpRow.effectiveDate, nbpRow.usd));
        mActualList.get(2).add(new XYChart.Data<>(nbpRow.effectiveDate, nbpRow.gbp));
        mActualList.get(3).add(new XYChart.Data<>(nbpRow.effectiveDate, nbpRow.chf));
        mActualList.get(4).add(new XYChart.Data<>(nbpRow.effectiveDate, nbpRow.aud));
    }

    @Override
    public void stop() {
        mJdbc.close();
    }

    @Override
    public void onPredictionValueChange(String value) {
        recalculatePrediction(value);
    }

    @Override
    public void onDateFromChange(LocalDate value) {
        if (value.isBefore(LocalDate.now())) {
            mDateFrom = value;
            redrawActualValue();
        }
    }

    @Override
    public void onDateToChange(LocalDate value) {
        mDateTo = value;
        redrawActualValue();
    }

    @Override
    public void onDownloadClicked() {
        try {
            mDownloader.downloadJson(mSniffer.getDirPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDirectorySelected(String value) {
        mSniffer.setDirectory(value);
    }

    @Override
    public void onDbReconnectRequested(String user, String pass, String dbName) {
        mJdbc.setUser(user);
        mJdbc.setmPassword(pass);
        mJdbc.setDbName(dbName);
        mJdbc.reconnect();
    }

    @Override
    public void onDbDropRequested() {
        mJdbc.dropTable();
    }

    private void redrawActualValue() {
        mJdbc.produceArray(mDateFrom, mDateTo);
        mActualList.get(0).clear();
        mActualList.get(1).clear();
        mActualList.get(2).clear();
        mActualList.get(3).clear();
        mActualList.get(4).clear();
        mJdbc.getArrayOfCharts().forEach(this::drawSeries);
    }

    private void recalculatePrediction(final String newPrediction) {
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
                xArray[i][0] = i;
                yArray[i] = data.getYValue().doubleValue();
                ++i;
            }

            double[] params = mFitter.getParameters(xArray, yArray);

            Date lastDate = java.sql.Date.valueOf(mActualList.get(arrayIndex).get(mActualList.get(arrayIndex).size() - 1).getXValue().toString());

            DateTime dtOrg = new DateTime(lastDate);
            DateTime dtPlusOne = dtOrg.plusDays(1);
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
            Date futureDate = java.sql.Date.valueOf(dtPlusOne.toString(fmt));

            for (int k = mActualList.get(arrayIndex).size(); k < mActualList.get(arrayIndex).size() + numberOfPredictedDays; ++k) {
                tempList.add(new XYChart.Data<>(futureDate, calculateYValue(k, params)));
                dtPlusOne = dtPlusOne.plusDays(1);
                futureDate = java.sql.Date.valueOf(dtPlusOne.toString(fmt));
            }

            mPredictedList.get(arrayIndex).clear();
            mPredictedList.get(arrayIndex).addAll(tempList);
        }
    }

    @Override
    public void onFileCreated() {
        redrawActualValue();
    }
}
