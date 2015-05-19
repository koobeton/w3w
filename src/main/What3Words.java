package main;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import static main.W3WApiMethod.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;

public class What3Words {

    private static final String W3W_URL = "http://api.what3words.com";
    private static final String API_KEY_FILE = "api.key";

    private String threeWords = null;
    private BigDecimal latitude = null;
    private BigDecimal longitude = null;
    private String error = null;
    private String errorMessage = null;

    private final String apiKey;

    private What3Words() {
        apiKey = getApiKey(API_KEY_FILE);
    }

    public static void main(String... args) {

        What3Words w3w = new What3Words();

        handleArgs(w3w, args);

        if (w3w.threeWords != null) {
            w3w.setPosition(w3w.threeWords);
        } else if (w3w.latitude != null && w3w.longitude != null) {
            w3w.set3Words(w3w.latitude, w3w.longitude);
        }

        w3w.showResults();
    }

    private void setPosition(String threeWords) {
        if (is3Words(threeWords)) {
            parseJson(getJson(buildURLString(W3W, threeWords)));
        } else {
            System.out.printf("Not a 3 words: %s%n", threeWords);
            showUsage();
            System.exit(0);
        }
    }

    private void set3Words(BigDecimal latitude, BigDecimal longitude) {
        parseJson(getJson(buildURLString(POSITION, String.format("%s,%s", latitude, longitude))));
    }

    private boolean is3Words(String words) {
        return words.matches("^\\p{L}+\\.\\p{L}+\\.\\p{L}+$");
    }

    private String getApiKey(String file) {
        String apiKey = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            apiKey = reader.readLine();
        } catch (FileNotFoundException e) {
            System.err.printf("API key file not found: %s%n", file);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return apiKey;
    }

    private String buildURLString(W3WApiMethod method, String query) {
        return String.format("%s/%s?key=%s&%s=%s",
                W3W_URL,
                method.getPath(),
                apiKey,
                method.getParameter(),
                query);
    }

    private JsonObject getJson(String urlString) {

        URL url;
        HttpURLConnection connection = null;
        JsonReader reader = null;
        JsonObject result = null;

        try {
            url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            reader = Json.createReader(connection.getInputStream());
            result = reader.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) reader.close();
            if (connection != null) connection.disconnect();
        }

        return result;
    }

    private void parseJson(JsonObject object) {

        if (object == null) return;

        JsonArray words = object.getJsonArray("words");
        if (words != null) {
            threeWords = String.format("%s.%s.%s",
                    words.getString(0, null),
                    words.getString(1, null),
                    words.getString(2, null));
        }

        JsonArray position = object.getJsonArray("position");
        if (position != null) {
            latitude = position.getJsonNumber(0).bigDecimalValue();
            longitude = position.getJsonNumber(1).bigDecimalValue();
        }

        error = object.getString("error", null);
        errorMessage = object.getString("message", null);
    }

    private void showResults() {
        if (error != null) {
            System.out.printf("Error:\t\t%s%nMessage:\t%s%n",
                    error,
                    errorMessage);
        } else {
            System.out.printf("3 words:\t%s%nPosition:\t%s, %s%n",
                    threeWords,
                    latitude,
                    longitude);
        }
    }

    private static void handleArgs(What3Words w3w, String... args) {
        switch (args.length) {
            case 3:
                w3w.threeWords = String.format("%s.%s.%s", args[0], args[1], args[2]);
                break;
            case 2:
                try {
                    w3w.latitude = new BigDecimal(args[0].replace(',', '.'));
                    w3w.longitude = new BigDecimal(args[1].replace(',', '.'));
                } catch (NumberFormatException e) {
                    System.out.println("Wrong number format");
                    showUsage();
                    System.exit(0);
                }
                break;
            default:
                showUsage();
                System.exit(0);
        }
    }

    private static void showUsage() {
        System.out.println("Usage:\tjava -jar w3w.jar word1 word2 word3\n" +
                "\tjava -jar w3w.jar latitude longitude");
    }
}
