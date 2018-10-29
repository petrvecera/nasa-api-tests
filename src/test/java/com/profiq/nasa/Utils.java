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

    /**
     * Default way to make an HTTP connection. Expects the correct behaviour. (response 200)
     * @param url Any full URL
     * @param requestMethod Can be "GET", "POST" etc
     * @return HttpURLConnection
     * @throws IOException
     */
    public static HttpURLConnection makeHTTPConnection(String url, String requestMethod) throws IOException {

        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod(requestMethod);
        con.disconnect();

        if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException(MessageFormat.format("Expected: {0} got {1}",HttpURLConnection.HTTP_OK ,con.getResponseCode()));
        }

        return con;
    }

    /**
     * Make http connection but you can select the response code
     * @param url full url
     * @param requestMethod Can be "GET", "POST" etc
     * @param responseCode You can enter response code which you are expecting for it to return.
     * @return HttpURLConnection
     * @throws IOException
     */
    public static HttpURLConnection makeHTTPConnection(String url, String requestMethod, int responseCode) throws IOException {

        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod(requestMethod);
        con.disconnect();

        if (con.getResponseCode() != responseCode) {
            throw new IOException(MessageFormat.format("Expected: {0} got {1}",responseCode ,con.getResponseCode()));
        }

        return con;
    }

    /**
     * Parse the output of the request
     * @param con HttpURLConnection
     * @return JSONObject with all the data
     * @throws IOException
     */
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

    /**
     * Build the url for the Search API endpoint
     * @param query Of the params
     * @return Full url for the request
     */
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

    /**
     * TEST - This functions tests that the version object  is present in the response JSON
     * @param data
     */
    public static void testVersion(JSONObject data){
        String version = data.getJSONObject("collection").getString("version");
        Assert.assertEquals(version, Utils.APIVersion);
    }

    /**
     * TEST - This function tests that JSON structure is correct.
     * @param data
     * @param links
     */
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

    /**
     * TEST - This function tests if href param is present in all responses and that it's correct.
     * @param data
     * @param url
     */
    public static void testHrefParam(JSONObject data,String url){
        String hrefUrl = data.getJSONObject("collection").getString("href");
        Assert.assertEquals(hrefUrl, url);
    }

    /**
     * Extract the items key from the JSON Object
     * @param data
     * @return
     */
    public static JSONArray extractItemsFromJSONResponse(JSONObject data){
        return data.getJSONObject("collection").getJSONArray("items");
    }

    /**
     * Makes HTTP request, parses the response into JSON, verifies href, stucture and version
     * @param query
     * @return
     * @throws IOException
     */
    public static JSONObject getSearchAPIDataResponse(Map<String, String> query) throws IOException {
        String url = Utils.buildSearchAPIUrl(query);
        JSONObject data = parseJSONResponse(makeHTTPConnection(url, "GET", 200));
        testHrefParam(data, url);
        testJSONStructure(data, false); // TODO: Fix the links
        testVersion(data);
        return data;
    }

    /**
     * Makes HTTP request, parses the response into JSON, verifies href, structure and version
     * You can enter
     * @param query
     * @param responseCode
     * @return
     * @throws IOException
     */
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

    /**
     * Generates random string based on the input lenght
     * @param length
     * @return
     */
    public static String generateRandomString(int length){
        String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

        char[] array = new char[length];
        for (int i = 0; i < length; i++) {
            array[i] = characters.charAt(Utils.sr.nextInt(characters.length()));
        }

        return new String(array);
    }

}
