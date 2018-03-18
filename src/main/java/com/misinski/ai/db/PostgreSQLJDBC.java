package com.misinski.ai.db;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostgreSQLJDBC {

    public static final String DEFAULT_USER = "postgres";
    public static final String DEFAULT_PASSWORD = "postgres";
    public static final String DEFAULT_DB_NAME = "exchange";
    private static final String DB_URL = "jdbc:postgresql://localhost/";
    private static final String TABLE_NAME = "prediction";
    private static final String DB_DRIVER = "org.postgresql.Driver";
    private String mPgUser = DEFAULT_USER;
    private String mPgPassword = DEFAULT_PASSWORD;
    private String mDbName = DEFAULT_DB_NAME;
    private Connection mConn;
    private ArrayList<NbpRow> arrayOfCharts;

    public PostgreSQLJDBC() {
        try {
            Class.forName(DB_DRIVER);
            // TODO: 10.03.18 if DB does not exist - create one
            mConn = DriverManager.getConnection(DB_URL + mDbName, mPgUser, mPgPassword);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void setUser(String user) {
        this.mPgUser = user;
    }

    public void setmPassword(String pass) {
        this.mPgPassword = pass;
    }

    public void setDbName(String dbName) {
        this.mDbName = dbName;
    }

    public void dropTable() {
        try {
            Statement dropStatement = mConn.createStatement();

            String sqlStatement = "DROP TABLE IF EXISTS " + TABLE_NAME;

            dropStatement.executeUpdate(sqlStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTable() {
        try {
            Statement createStatement = mConn.createStatement();

            String sqlStatement = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " " +
                    "(id CHAR(14) not NULL, " +
                    " effective_date DATE, " +
                    " eur FLOAT, " +
                    " usd FLOAT, " +
                    " gbp FLOAT, " +
                    " chf FLOAT, " +
                    " aud FLOAT, " +
                    " PRIMARY KEY ( id ))";

            createStatement.executeUpdate(sqlStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void consumeCurrencies(final JSONObject table) throws SQLException {

        JSONArray currencyArray = table.getJSONArray("rates");

        String sqlStatement = "INSERT INTO " +
                TABLE_NAME +
                " VALUES (?, ?, ?, ?, ?, ?, ?)" +
                " ON CONFLICT DO NOTHING ";

        PreparedStatement insertStatement = mConn.prepareStatement(sqlStatement);

        insertStatement.setString(1, table.getString("no"));
        insertStatement.setDate(2, Date.valueOf(table.getString("effectiveDate")));

        double eurRate = 0, usdRate = 0, gbpRate = 0, chfRate = 0, audRate = 0;

        for (int i = 0; i < currencyArray.length(); ++i) {
            if (currencyArray.getJSONObject(i).getString("code").equals("EUR")) {
                eurRate = currencyArray.getJSONObject(i).getDouble("mid");
            } else if (currencyArray.getJSONObject(i).getString("code").equals("USD")) {
                usdRate = currencyArray.getJSONObject(i).getDouble("mid");
            } else if (currencyArray.getJSONObject(i).getString("code").equals("GBP")) {
                gbpRate = currencyArray.getJSONObject(i).getDouble("mid");
            } else if (currencyArray.getJSONObject(i).getString("code").equals("CHF")) {
                chfRate = currencyArray.getJSONObject(i).getDouble("mid");
            } else if (currencyArray.getJSONObject(i).getString("code").equals("AUD")) {
                audRate = currencyArray.getJSONObject(i).getDouble("mid");
            }
        }

        insertStatement.setDouble(3, eurRate);
        insertStatement.setDouble(4, usdRate);
        insertStatement.setDouble(5, gbpRate);
        insertStatement.setDouble(6, chfRate);
        insertStatement.setDouble(7, audRate);
        insertStatement.executeUpdate();
    }

    public void insertTables(JSONArray jsonArray) throws SQLException {
        for (int i = 0; i < jsonArray.length(); ++i) {
            consumeCurrencies(jsonArray.getJSONObject(i));
        }
    }

    public ArrayList<NbpRow> getArrayOfCharts() {
        return arrayOfCharts;
    }

    private ArrayList<NbpRow> produceArrayFromDB(final LocalDate dateFrom, final LocalDate dateTo) {
        ArrayList<NbpRow> dbList = new ArrayList<>();
        PreparedStatement selectStatement = null;
        String sqlStatement = "SELECT * FROM " + TABLE_NAME + " " +
                "WHERE (effective_date BETWEEN '" +
                dateFrom.toString() + "' AND '" + dateTo.toString() + "')" +
                "ORDER BY effective_date";
        try {
            mConn = DriverManager.getConnection(DB_URL + mDbName, mPgUser, mPgPassword);
            selectStatement = mConn.prepareStatement(sqlStatement);
            ResultSet selectedResult = selectStatement.executeQuery();
            NbpRow selectedRow;
            while (selectedResult.next()) {
                selectedRow = new NbpRow();
                selectedRow.id = selectedResult.getString(1);
                // or selectedRow.id=selectedResult.getInt("userid"); by name of column
                selectedRow.effectiveDate = selectedResult.getDate(2);
                // or selectedRow.name=selectedResult.getString("firstname"); by name of column
                selectedRow.eur = selectedResult.getDouble(3);
                selectedRow.usd = selectedResult.getDouble(4);
                selectedRow.gbp = selectedResult.getDouble(5);
                selectedRow.chf = selectedResult.getDouble(6);
                selectedRow.aud = selectedResult.getDouble(7);
                dbList.add(selectedRow);
            }
            return dbList;
        } catch (SQLException ex) {
            Logger.getLogger(PostgreSQLJDBC.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (selectStatement != null) {
                try {
                    selectStatement.close();
                } catch (SQLException ex) {
                    Logger.getLogger(PostgreSQLJDBC.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void produceArray(final LocalDate dateFrom, final LocalDate dateTo) {
        arrayOfCharts = produceArrayFromDB(dateFrom, dateTo);
    }

    public void close() {
        Logger.getLogger(PostgreSQLJDBC.class.getName()).log(Level.INFO, "closing...");
        if (mConn != null) {
            try {
                mConn.close();
            } catch (SQLException ex) {
                Logger.getLogger(PostgreSQLJDBC.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void reconnect() {
        if (mConn != null) {
            try {
                mConn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        try {
            mConn = DriverManager.getConnection(DB_URL + mDbName, mPgUser, mPgPassword);
        } catch (SQLException e) {
            e.printStackTrace();
            // TODO: 18.03.18 baza danych nie istnieje
        }
    }
}
