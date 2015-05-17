package main;

import static main.ApiMethod.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class What3Words {

    private static final String W3W_URL = "http://api.what3words.com";
    private static final String API_KEY_FILE = "api.key";

    private static String threeWords;
    private static String position;

    private final String apiKey;

    public What3Words() {
        apiKey = getApiKey(API_KEY_FILE);
    }

    public static void main(String... args) {

        handleArgs(args);

        What3Words w3w = new What3Words();
        System.out.println(w3w.getPosition(threeWords));
        System.out.println(w3w.get3Words(position));
    }

    public String getPosition(String threeWords) {
        return getJson(getURL(W3W, threeWords));
    }

    public String get3Words(String position) {
        return getJson(getURL(POSITION, position));
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
                position = String.format("%s,%s", args[0], args[1]);
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
