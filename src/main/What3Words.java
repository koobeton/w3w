package main;

import static main.ApiMethod.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class What3Words {

    private static final String W3W_URL = "http://api.what3words.com";
    private static final String API_KEY_FILE = "api.key";

    private final String apiKey;

    public What3Words() {
        apiKey = getApiKey(API_KEY_FILE);
    }

    public static void main(String... args) {

        What3Words w3w = new What3Words();
        System.out.println(w3w.getPosition(args[0]));
    }

    public String getPosition(String threeWords) {
        return getJson(W3W, threeWords);
    }

    public String get3Words(String position) {
        return getJson(POSITION, position);
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

    private String getURL(ApiMethod method, String query) {
        return String.format("%s/%s?key=%s&%s=%s",
                W3W_URL,
                method.getPath(),
                apiKey,
                method.getParameter(),
                query);
    }

    private String getJson(ApiMethod method, String query) {

        StringBuilder result = new StringBuilder();
        URL url;
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            url = new URL(getURL(method, query));
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
}
