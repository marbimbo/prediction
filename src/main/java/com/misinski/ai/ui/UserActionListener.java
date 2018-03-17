package com.misinski.ai.ui;

import java.time.LocalDate;

public interface UserActionListener {

    void onPredictionValueChange(String value);

    void onDateFromChange(LocalDate value);

    void onDateToChange(LocalDate value);

    void onDownloadClicked();

    void onDirectorySelected(String value);
}
