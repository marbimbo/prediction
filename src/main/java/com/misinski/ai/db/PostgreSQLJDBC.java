package com.misinski.ai.db;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostgreSQLJDBC {

    private static final String mDbUrl = "jdbc:postgresql://localhost:5432/";
    private static final String mDbName = "exchange";
    private static final String mPgUser = "postgres";
    private static final String mPgPassword = "postgres";
    private static final String mTableName = "prediction";

    private Connection mConn;
    private ArrayList<NbpRow> arrayOfCharts;

    public PostgreSQLJDBC() {
        try {
            Class.forName("org.postgresql.Driver");
            // TODO: 10.03.18 if DB does not exist - create one
            mConn = DriverManager.getConnection(mDbUrl + mDbName, mPgUser, mPgPassword);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }

    public void dropTable() {
        System.out.println("Drop table");
        try {
            Statement stmt = mConn.createStatement();

            String sql = "DROP TABLE " + mTableName;

            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTable() {
        System.out.println("Create table");
        try {
            Statement stmt = mConn.createStatement();

            String sql = "CREATE TABLE " + mTableName + " " +
                    "(id CHAR(14) not NULL, " +
                    " effective_date DATE, " +
                    " eur FLOAT, " +
                    " usd FLOAT, " +
                    " gbp FLOAT, " +
                    " chf FLOAT, " +
                    " aud FLOAT, " +
                    " PRIMARY KEY ( id ))";

            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void consumeCurrencies(JSONObject table) throws SQLException {

        JSONArray currencyArray = table.getJSONArray("rates");

        String sql = "INSERT INTO " + mTableName + " VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement statement = mConn.prepareStatement(sql);

        statement.setString(1, table.getString("no"));
        statement.setDate(2, Date.valueOf(table.getString("effectiveDate")));

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

        statement.setDouble(3, eurRate);
        statement.setDouble(4, usdRate);
        statement.setDouble(5, gbpRate);
        statement.setDouble(6, chfRate);
        statement.setDouble(7, audRate);
        statement.executeUpdate();
    }

    public void insertTables(JSONArray jsonArray) throws SQLException {
        for (int i = 0; i < jsonArray.length(); ++i) {
            consumeCurrencies(jsonArray.getJSONObject(i));
        }
    }

    public ArrayList<NbpRow> getArrayOfCharts() {
        return arrayOfCharts;
    }

    private ArrayList<NbpRow> produceArrayFromDB(LocalDate mDateFrom, LocalDate mDateTo) {
        ArrayList<NbpRow> dbList = new ArrayList<>();
        PreparedStatement ps = null;
        String SQL = "SELECT * FROM " + mTableName + " " +
                "WHERE (effective_date BETWEEN '" + mDateFrom.toString() + "' AND '" + mDateTo.toString() + "')" +
                "ORDER BY effective_date";
        try {
            mConn = DriverManager.getConnection(mDbUrl + mDbName, mPgUser, mPgPassword);
            ps = mConn.prepareStatement(SQL);
            ResultSet rs = ps.executeQuery();
            NbpRow p = null;
            while (rs.next()) {
                p = new NbpRow();
                p.id = rs.getString(1);
                // or p.id=rs.getInt("userid"); by name of column
                p.effectiveDate = rs.getDate(2);
                // or p.name=rs.getString("firstname"); by name of column
                p.eur = rs.getDouble(3);
                p.usd = rs.getDouble(4);
                p.gbp = rs.getDouble(5);
                p.chf = rs.getDouble(6);
                p.aud = rs.getDouble(7);
                dbList.add(p);
            }
            return dbList;
        } catch (SQLException ex) {
            Logger.getLogger(PostgreSQLJDBC.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ex) {
                    Logger.getLogger(PostgreSQLJDBC.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void produceArray(LocalDate mDateFrom, LocalDate mDateTo) {
        arrayOfCharts = produceArrayFromDB(mDateFrom, mDateTo);
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
}
