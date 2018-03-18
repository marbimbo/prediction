package com.misinski.ai.ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import static com.misinski.ai.db.PostgreSQLJDBC.*;

public class UIController implements Initializable {

    private ArrayList<XYChart.Series<Date, Number>> mActualSeriesList = new ArrayList<>();
    private ArrayList<XYChart.Series<Date, Number>> mPredictedSeriesList = new ArrayList<>();

    private DirectoryChooser mDirectoryChooser = new DirectoryChooser();

    private UserActionListener mListener;

    @FXML
    private DatePicker picker_from;

    @FXML
    private DatePicker picker_to;

    @FXML
    private ChoiceBox prediction_choice;

    @FXML
    private Button button_download;

    @FXML
    private Button button_directory;

    @FXML
    private Label label_directory;

    @FXML
    private TextField field_user;

    @FXML
    private TextField field_pass;

    @FXML
    private TextField field_db;

    @FXML
    private Button button_db_reconnect;

    @FXML
    private Button button_db_drop;

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

        button_download.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                mListener.onDownloadClicked();
            }
        });

        label_directory.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                label_directory.setMinWidth(Double.POSITIVE_INFINITY);
            }
        });

        label_directory.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                label_directory.setMinWidth(0);
            }
        });

        button_directory.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Window stage = ((Node) event.getSource()).getScene().getWindow();
                final File selectedDirectory = mDirectoryChooser.showDialog(stage);
                if (selectedDirectory != null) {
                    mListener.onDirectorySelected(selectedDirectory.getPath());
                    label_directory.setText(selectedDirectory.getPath());
                }
            }
        });
        label_directory.setText(System.getProperty("user.dir"));

        field_user.setText(DEFAULT_USER);
        field_pass.setText(DEFAULT_PASSWORD);
        field_db.setText(DEFAULT_DB_NAME);

        button_db_reconnect.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                mListener.onDbReconnectRequested(field_user.getText(), field_pass.getText(), field_db.getText());
            }
        });

        button_db_drop.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                mListener.onDbDropRequested();
            }
        });

        prediction_choice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldIndex, Number newIndex) {
                mListener.onPredictionValueChange((String) prediction_choice.getItems().get((Integer) newIndex));
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

    void setCallBack() {
        picker_from.valueProperty().addListener(new ChangeListener<LocalDate>() {
            @Override
            public void changed(ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) {
                mListener.onDateFromChange(picker_from.getValue());
            }
        });

        picker_to.valueProperty().addListener(new ChangeListener<LocalDate>() {
            @Override
            public void changed(ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) {
                mListener.onDateToChange(picker_to.getValue());
            }
        });
    }

    void setListener(UserActionListener listener) {
        mListener = listener;
    }

    void setDateFrom(LocalDate dateFrom) {
        picker_from.setValue(dateFrom);
    }

    void setDateTo(LocalDate dateTo) {
        picker_to.setValue(dateTo);
    }
}
