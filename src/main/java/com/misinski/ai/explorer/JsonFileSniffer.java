package com.misinski.ai.explorer;

import com.misinski.ai.db.PostgreSQLJDBC;
import javafx.application.Platform;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

public class JsonFileSniffer {

    public static final String EXCHANGE_TABLE = "a/";

    private WatchKey mDirWatchKey;
    private WatchService mWatcher;
    private PostgreSQLJDBC mJdbc;
    private String mDirPath = System.getProperty("user.dir");
    private FileCreatedListener mListener;

    public JsonFileSniffer(PostgreSQLJDBC jdbc, FileCreatedListener listener) {
        mJdbc = jdbc;
        mListener = listener;

        try {
            mWatcher = FileSystems.getDefault().newWatchService();
            mDirWatchKey = Paths.get(mDirPath).register(mWatcher, ENTRY_CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            try {
                startWathing();
            } catch (InterruptedException e) {
                e.printStackTrace();
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
                Platform.runLater(() -> mListener.onFileCreated());
            }
            key.reset();
        }
    }

    private synchronized void readFiles() {
        System.out.println("reading");
        try (Stream<Path> stream = Files.list(Paths.get(mDirPath))) {
            stream
                    .filter(new Predicate<Path>() {
                        @Override
                        public boolean test(Path path) {
                            System.out.println(path);
                            return (path.toString().endsWith(".json"));
                        }
                    })
                    .forEach(this::map2Json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void map2Json(Path filePath) {
        System.out.println("matching");
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

    public String getDirPath() {
        return mDirPath;
    }

    public void setDirectory(String path) {
        if (mDirWatchKey != null) {
            mDirWatchKey.cancel();
        }
        mDirPath = path;
        try {
            mDirWatchKey = Paths.get(mDirPath).register(mWatcher, ENTRY_CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        readFiles();
        mListener.onFileCreated();
    }
}
