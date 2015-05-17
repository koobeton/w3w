package main;

import static main.W3WApiMethod.*;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;

public class What3Words {

    private static final String W3W_URL = "http://api.what3words.com";
    private static final String API_KEY_FILE = "api.key";

    private static String threeWords;
    private static BigDecimal latitude;
    private static BigDecimal longitude;

    private final String apiKey;

    private What3Words() {
        apiKey = getApiKey(API_KEY_FILE);
    }

    public static void main(String... args) {

        handleArgs(args);

        What3Words w3w = new What3Words();
        if (threeWords != null) {
            System.out.println(w3w.getPosition(threeWords));
        } else if (latitude != null && longitude != null) {
            System.out.println(w3w.get3Words(latitude, longitude));
        } else {
            showUsage();
        }
    }

    private String getPosition(String threeWords) {
        return getJson(buildURLString(W3W, threeWords));
    }

    private String get3Words(BigDecimal latitude, BigDecimal longitude) {
        return getJson(buildURLString(POSITION, String.format("%s,%s", latitude, longitude)));
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

    private String getJson(String urlString) {

        StringBuilder result = new StringBuilder();
        URL url;
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            String input;
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((input = reader.readLine()) != null) {
                result.append(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (connection != null) connection.disconnect();
        }

        return result.toString();
    }

    private static void handleArgs(String... args) {
        switch (args.length) {
            case 3:
                threeWords = String.format("%s.%s.%s", args[0], args[1], args[2]);
                break;
            case 2:
                latitude = new BigDecimal(args[0].replace(',', '.'));
                longitude = new BigDecimal(args[1].replace(',', '.'));
                break;
            default:
                showUsage();
                System.exit(0);
        }
    }

    private static void showUsage() {
        System.out.println("Usage:\tjava -jar w3w.jar word1 word2 word3\n" +
                "\tjava -jar w3w.jar position1 position2");
    }
}
