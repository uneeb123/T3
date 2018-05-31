package org.treasury.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.treasury.core.pojo.TransactionHistory;
import org.treasury.core.pojo.Treasury;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class TreasuryClient {
    private final String USER_AGENT = "Mozilla/5.0";
    private String baseUrl;
    private String treasuryId;

    public TreasuryClient(boolean testMode, String treasuryId) throws Exception {
        if (testMode) {
            baseUrl = "http://localhost:3000/";
        } else {
            baseUrl = "http://treasury.com/"; // replace this
        }
        this.treasuryId = treasuryId;
        if (this.treasuryId == null) {
            throw new Exception("Unable to create client");
        }
    }

    public Treasury getTreasury() throws Exception {
        String url = baseUrl + "treasury/" + treasuryId;
        String response = getRequest(url);
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new GsonUTCDateAdapter()).create();
        Treasury treasury = gson.fromJson(response, Treasury.class);
        return treasury;
    }

    public int postTransaction(TransactionHistory history) throws Exception {
        String url = baseUrl + "treasury/" + treasuryId;
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new GsonUTCDateAdapter()).create();
        String payload = gson.toJson(history);
        return postRequest(url, payload);
    }

    public int postAddress(String address) throws Exception {
        String url = baseUrl + "treasury/" + treasuryId + "/addr";
        String payload = "{\"address\":\"" + address + "\"}";
        return postRequest(url, payload);
    }

    private String getRequest(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Treasury not found");
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    private int postRequest(String requestUrl, String payload) throws Exception {
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            writer.write(payload);
            writer.close();
            int responseCode = connection.getResponseCode();
            return responseCode;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
