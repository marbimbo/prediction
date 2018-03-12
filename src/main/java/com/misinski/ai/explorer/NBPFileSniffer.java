package com.misinski.ai.explorer;

import com.misinski.ai.db.PostgreSQLJDBC;
import org.json.JSONArray;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.stream.Stream;

public class NBPFileSniffer {

    private final String dirPath = "nbp/a/";
    private PostgreSQLJDBC mJdbc;

    public NBPFileSniffer(PostgreSQLJDBC jdbc) {
        this.mJdbc = jdbc;
    }

    private void readFiles() {
        try (Stream<Path> paths = Files.walk(Paths.get(dirPath))) {
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
