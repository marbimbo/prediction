package com.misinski.ai.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Separator;

import java.net.URL;
import java.util.ResourceBundle;

public class UIController implements Initializable {

    @FXML
    private ChoiceBox prediction_choice;

    @FXML
    private LineChart exchange_chart;

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        prediction_choice.setItems(FXCollections.observableArrayList(
                "Brak",
                new Separator(),
                "tydzień", "miesiąc", "rok")
        );
        prediction_choice.setValue("Brak");
    }

    public void addSeries(XYChart.Series series) {
        exchange_chart.getData().add(series);
    }
}
