package main;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class What3Words {

    private static final String URL = "http://api.what3words.com";
    private static final String API_KEY_FILE = "api.key";

    private final String apiKey;

    private What3Words() {
        apiKey = getApiKey(API_KEY_FILE);
    }

    public static void main(String... args) {

        What3Words w3w = new What3Words();
        System.out.println(w3w.get3Words(args[0]));
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

    private String getURL () {
        return String.format("%s/w3w?key=%s&string=",
                URL,
                apiKey);
    }

    private String get3Words(String string) {

        StringBuilder result = new StringBuilder();
        URL url;
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            url = new URL(getURL() + string);
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
