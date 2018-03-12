package com.misinski.ai.app;

import com.misinski.ai.db.PostgreSQLJDBC;
import com.misinski.ai.explorer.NBPFileSniffer;

import java.sql.SQLException;

public class RatePredictionApp {

    public static void mains(String[] args) {
        PostgreSQLJDBC jdbc = new PostgreSQLJDBC();
        jdbc.dropTable();
        jdbc.createTable();
        NBPFileSniffer sniffer = new NBPFileSniffer(jdbc);
        sniffer.sniffForFiles();
//        sniffer.sniffForFiles(jdbc);
//        jdbc.createTableFromJson(sniffer.getJsonObject());
//        try {
//            jdbc.insertTables(sniffer.getJsonArray());
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }

}
