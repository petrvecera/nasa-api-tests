package com.profiq.nasa;


import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.Map;


public class Utils {

    private static final SecureRandom sr = new SecureRandom();
    public static final String apiHost = "https://images-api.nasa.gov/";
    public static final String searchEndPoint = "search";

    public static final String APIVersion = "1.0";

    public static String getFullUrlToSearch(){
        return MessageFormat.format("{0}{1}", apiHost, searchEndPoint);
    }

    public static HttpURLConnection makeHTTPConnection(String url, String requestMethod) throws IOException {

        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod(requestMethod);
        con.disconnect();

        if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException(MessageFormat.format("Expected: {0} got {1}",HttpURLConnection.HTTP_OK ,con.getResponseCode()));
        }

        return con;
    }

    public static HttpURLConnection makeHTTPConnection(String url, String requestMethod, int responseCode) throws IOException {

        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod(requestMethod);
        con.disconnect();

        if (con.getResponseCode() != responseCode) {
            throw new IOException(MessageFormat.format("Expected: {0} got {1}",responseCode ,con.getResponseCode()));
        }

        return con;
    }

    public static JSONObject parseJSONResponse(HttpURLConnection con) throws IOException {

        BufferedReader in;
        if (con.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {
            in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }

        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return new JSONObject(response.toString());
    }

    private static String buildSearchAPIUrl(Map<String, String> query) {
        StringBuilder url = new StringBuilder(Utils.getFullUrlToSearch() + "?");

        boolean multiQuery = true;
        for (Map.Entry<String, String> entry : query.entrySet()) {
            if (!multiQuery) url.append("&");
            else multiQuery = false;

            url.append(MessageFormat.format("{0}={1}", entry.getKey(), entry.getValue()));
        }

        return url.toString();
    }

    public static void testVersion(JSONObject data){
        String version = data.getJSONObject("collection").getString("version");
        Assert.assertEquals(version, Utils.APIVersion);
    }

    public static void testJSONStructure(JSONObject data, boolean links){
        Assert.assertTrue(data.has("collection"));
        data = data.getJSONObject("collection");
        Assert.assertTrue(data.has("metadata"));
        Assert.assertTrue(data.has("href"));
        Assert.assertTrue(data.has("version"));
        Assert.assertTrue(data.has("items"));
        if(links){
            Assert.assertTrue(data.has("links"));
            // Assert do not throw ERROR
            JSONArray linkItems = data.getJSONArray("links");
        }

        // Assert do not throw ERROR
        JSONArray items = data.getJSONArray("items");
    }

    public static void testHrefParam(JSONObject data,String url){
        String hrefUrl = data.getJSONObject("collection").getString("href");
        Assert.assertEquals(hrefUrl, url);
    }

    public static JSONArray extractItemsFromJSONResponse(JSONObject data){
        return data.getJSONObject("collection").getJSONArray("items");
    }

    public static JSONObject getSearchAPIDataResponse(Map<String, String> query) throws IOException {
        String url = Utils.buildSearchAPIUrl(query);
        JSONObject data = parseJSONResponse(makeHTTPConnection(url, "GET", 200));
        testHrefParam(data, url);
        testJSONStructure(data, false); // TODO: Fix the links
        testVersion(data);
        return data;
    }


    public static JSONObject getSearchAPIDataResponse(Map<String, String> query, int responseCode) throws IOException {
        String url = Utils.buildSearchAPIUrl(query);
        JSONObject data = parseJSONResponse(makeHTTPConnection(url, "GET", responseCode));
        if(responseCode == 200){
            // Just in case some one used this function instead of the with default response code
            testHrefParam(data, url);
            testJSONStructure(data, false); // TODO: Fix the links
            testVersion(data);
        }
        return data;
    }

    public static String generateRandomString(int length){
        String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

        char[] array = new char[length];
        for (int i = 0; i < length; i++) {
            array[i] = characters.charAt(Utils.sr.nextInt(characters.length()));
        }

        return new String(array);
    }

}
