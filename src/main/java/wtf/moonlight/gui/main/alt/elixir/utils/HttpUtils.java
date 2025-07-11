package wtf.moonlight.gui.main.alt.elixir.utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpUtils {
    public static final String DEFAULT_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36";
    public static HttpURLConnection make(String url, String method) throws IOException {
        return make(url, method, "", new HashMap<>(), DEFAULT_AGENT);
    }
    public static HttpURLConnection make(String url, String method, String data) throws IOException {
        return make(url, method, data, new HashMap<>(), DEFAULT_AGENT);
    }
    public static HttpURLConnection make(String url, String method, String data, Map<String, String> header) throws IOException {
        return make(url, method, data, header, DEFAULT_AGENT);
    }

    public static HttpURLConnection make(String url, String method, String data, Map<String, String> header, String agent) throws IOException {
        HttpURLConnection httpConnection = (HttpURLConnection) new URL(url).openConnection();

        httpConnection.setRequestMethod(method);
        httpConnection.setConnectTimeout(2000);
        httpConnection.setReadTimeout(10000);

        httpConnection.setRequestProperty("User-Agent", agent);
        for (Map.Entry<String, String> entry : header.entrySet()) {
            httpConnection.setRequestProperty(entry.getKey(), entry.getValue());
        }

        httpConnection.setInstanceFollowRedirects(true);
        httpConnection.setDoOutput(true);

        if (!data.isEmpty()) {
            try (DataOutputStream dataOutputStream = new DataOutputStream(httpConnection.getOutputStream())) {
                dataOutputStream.writeBytes(data);
                dataOutputStream.flush();
            }
        }

        httpConnection.connect();

        return httpConnection;
    }

    public static String request(String url, String method, String data, Map<String, String> header, String agent) throws IOException {
        HttpURLConnection connection = make(url, method, data, header, agent);

        try (InputStream inputStream = connection.getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
            StringBuilder response = new StringBuilder();
            char[] buffer = new char[1024];
            int bytesRead;
            while ((bytesRead = inputStreamReader.read(buffer)) != -1) {
                response.append(buffer, 0, bytesRead);
            }
            return response.toString();
        }
    }

    public static String get(String url, Map<String, String> header) throws IOException {
        return request(url, "GET", "", header, DEFAULT_AGENT);
    }

    public static String post(String url, String data, Map<String, String> header) throws IOException {
        return request(url, "POST", data, header, DEFAULT_AGENT);
    }

    public static String readText(final HttpURLConnection connection) throws IOException {
        return readText(toReader(connection));
    }

    public static InputStreamReader toReader(final HttpURLConnection urlConnection) throws IOException {
        return new InputStreamReader(urlConnection.getInputStream());
    }

    public static String readText(InputStreamReader reader) throws IOException {
        int buffer;
        StringBuilder stringBuilder = new StringBuilder();
        while ((buffer = reader.read()) != -1){
            stringBuilder.append((char) buffer);
        }
        return stringBuilder.toString();
    }
}