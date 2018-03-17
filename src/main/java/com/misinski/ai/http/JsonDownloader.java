package com.misinski.ai.http;

import org.apache.http.HttpStatus;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;

public class JsonDownloader {

    // TODO: 15.03.18 move to WebApi class
    public static final String BASE_URL = "http://api.nbp.pl/api/exchangerates/tables/";
    public static final String EXCHANGE_TABLE = "a/";
    public static final int DAYS_MAX_RANGE = 93;

    public static final String DIR_PATH = "nbp/" + EXCHANGE_TABLE;

    public void downloadJson() throws Exception {
        // TODO: 15.03.18 for a wider range use iterator
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(DAYS_MAX_RANGE);

        URL completeUrl = new URL(BASE_URL + EXCHANGE_TABLE + startDate + "/" + endDate + "/");
        HttpURLConnection conn = (HttpURLConnection) completeUrl.openConnection();
        conn.setRequestProperty("Accept", "application/json");

        // TODO: 15.03.18 handle errors
        int responseCode = conn.getResponseCode();
        if (responseCode != HttpStatus.SC_OK) {
            return;
        }

        // TODO: 15.03.18 add IOException, FileNotFoundException
        BufferedReader jsonReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuffer responseBuffer = new StringBuffer();
        String inputLine;

        while ((inputLine = jsonReader.readLine()) != null) {
            responseBuffer.append(inputLine);
        }
        jsonReader.close();

        try {
            BufferedWriter jsonWriter = new BufferedWriter(new FileWriter(new File(DIR_PATH + startDate + "--" + endDate)));
            jsonWriter.write(responseBuffer.toString());
            jsonWriter.flush();
            jsonWriter.close();
        } catch (java.io.FileNotFoundException e) {
            // TODO: 15.03.18  
        } finally {

        }
    }

}
