package com.misinski.ai.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import java.net.URL;
import java.util.ResourceBundle;

public class UIController implements Initializable {

    @FXML
    private LineChart exchange_chart;

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void addSeries(XYChart.Series series) {
        exchange_chart.getData().add(series);
    }
}
