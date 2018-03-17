package com.misinski.ai.explorer;

import com.misinski.ai.db.PostgreSQLJDBC;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.stream.Stream;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

public class JsonFileSniffer {

    public static final String EXCHANGE_TABLE = "a/";
    public static final String DIR_PATH = "nbp/" + EXCHANGE_TABLE;

    private WatchService mWatcher;
    private PostgreSQLJDBC mJdbc;

    public JsonFileSniffer(PostgreSQLJDBC jdbc) {
        this.mJdbc = jdbc;
        try {
            mWatcher = FileSystems.getDefault().newWatchService();
            Paths.get(DIR_PATH).register(mWatcher, ENTRY_CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    startWathing();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startWathing() throws InterruptedException {
        WatchKey key;
        while ((key = mWatcher.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                System.out.println(
                        "Event kind:" + event.kind()
                                + ". File affected: " + event.context() + ".");
                readFiles();
            }
            key.reset();
        }
    }

    private synchronized void readFiles() {
        try (Stream<Path> paths = Files.walk(Paths.get(DIR_PATH))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(this::map2Json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void map2Json(Path filePath) {
        try {
            String jsonValue = readLineByLineJava8(filePath);
            JSONArray jsonArray = new JSONArray(jsonValue);
            mJdbc.insertTables(jsonArray);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e1) {
            // malformed: go to next
            e1.printStackTrace();
        }
    }

    // TODO: 10.03.18 no need of lines()
    private String readLineByLineJava8(Path filePath) {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines(filePath, StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }

    public void sniffForFiles() {
        readFiles();
    }
}
