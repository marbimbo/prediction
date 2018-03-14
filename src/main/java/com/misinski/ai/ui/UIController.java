package com.misinski.ai.ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Separator;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

public class UIController implements Initializable {

    private ArrayList<XYChart.Series<Date, Number>> mActualSeriesList = new ArrayList<>();
    private ArrayList<XYChart.Series<Date, Number>> mPredictedSeriesList = new ArrayList<>();

    private UserActionListener mListener;

    @FXML
    private ChoiceBox prediction_choice;

    @FXML
    private LineChart exchange_chart;

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeSeries();
        updateChart();
        prediction_choice.setItems(FXCollections.observableArrayList(
                "Brak",
                new Separator(),
                "tydzień", "miesiąc", "rok")
        );
        prediction_choice.setValue("Brak");
        prediction_choice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldIndex, Number newIndex) {
                mListener.onUserAction((String) prediction_choice.getItems().get((Integer) newIndex));
            }
        });
    }

    private void initializeSeries() {
        XYChart.Series<Date, Number> series = new XYChart.Series();
        series.setName("EUR");
        mActualSeriesList.add(series);
        series = new XYChart.Series();
        series.setName("USD");
        mActualSeriesList.add(series);
        series = new XYChart.Series();
        series.setName("GBP");
        mActualSeriesList.add(series);
        series = new XYChart.Series();
        series.setName("CHF");
        mActualSeriesList.add(series);
        series = new XYChart.Series();
        series.setName("AUD");
        mActualSeriesList.add(series);

        series = new XYChart.Series();
        series.setName("EUR PREDICTION");
        mPredictedSeriesList.add(series);
        series = new XYChart.Series();
        series.setName("USD PREDICTION");
        mPredictedSeriesList.add(series);
        series = new XYChart.Series();
        series.setName("GBP PREDICTION");
        mPredictedSeriesList.add(series);
        series = new XYChart.Series();
        series.setName("CHF PREDICTION");
        mPredictedSeriesList.add(series);
        series = new XYChart.Series();
        series.setName("AUD PREDICTION");
        mPredictedSeriesList.add(series);
    }

    private void updateChart() {
        for (XYChart.Series series : mActualSeriesList) {
            exchange_chart.getData().add(series);
        }
        for (XYChart.Series series : mPredictedSeriesList) {
            exchange_chart.getData().add(series);
        }
    }

    void setActualData(ArrayList<ObservableList<XYChart.Data<Date, Number>>> list) {
        for (int i = 0; i < mActualSeriesList.size(); ++i) {
            mActualSeriesList.get(i).setData(list.get(i));
        }
    }

    void setPredictedData(ArrayList<ObservableList<XYChart.Data<Date, Number>>> list) {
        for (int i = 0; i < mPredictedSeriesList.size(); ++i) {
            mPredictedSeriesList.get(i).setData(list.get(i));
        }
    }

    void addListener(UserActionListener listener) {
        mListener = listener;
    }
}
