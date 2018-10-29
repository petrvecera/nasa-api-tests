package com.profiq.nasa.tests;

import com.profiq.nasa.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MediaSearch {

    /**
     * This function can test results for the media_type param string
     * AKA it verifies if all the results are correctly selected based on the Q param
     * @param inputData
     * @param testedString
     */
    public static void verifyMediaTypeSearchResults(JSONObject inputData, String testedString){

        JSONArray responseData = Utils.extractItemsFromJSONResponse(inputData);

        Assert.assertTrue(responseData.length() > 0);

        for (int i = 0; i < responseData.length(); i++) {
            JSONObject item = responseData.getJSONObject(i);
            JSONObject data = item.getJSONArray("data").getJSONObject(0);
            String mediaType = data.getString("media_type");

            if (testedString.equals("audio,%20image")){
                Assert.assertTrue(mediaType.equals("audio") || mediaType.equals("image"));
            }else{
                Assert.assertEquals(mediaType, testedString);
            }
        }
    }

    @Test
    public void MediaSearchForImageHasCorrectData() throws IOException {
        final String testedString = "image";

        Map<String, String> query = new HashMap<String, String>(){
            {
                put("media_type", testedString);
            }
        };

        verifyMediaTypeSearchResults(Utils.getSearchAPIDataResponse(query), testedString);
    }

    @Test
    public void MediaSearchForAudioHasCorrectData() throws IOException {
        final String testedString = "audio";

        Map<String, String> query = new HashMap<String, String>(){
            {
                put("media_type", testedString);
            }
        };

        verifyMediaTypeSearchResults(Utils.getSearchAPIDataResponse(query), testedString);
    }

    @Test
    public void MediaSearchForBothHasCorrectData() throws IOException {
        final String testedString = "audio,%20image";

        Map<String, String> query = new HashMap<String, String>(){
            {
                put("media_type", testedString);
            }
        };

        verifyMediaTypeSearchResults(Utils.getSearchAPIDataResponse(query), testedString);
    }

    @Test
    public void MediaSearchEmptyInput() throws IOException {
        final String testedString = "";

        Map<String, String> query = new HashMap<String, String>(){
            {
                put("media_type", testedString);
            }
        };

        // It's up to the discussion how the request should behave when there is no input
        JSONArray data = Utils.extractItemsFromJSONResponse(Utils.getSearchAPIDataResponse(query));
        Assert.assertEquals(0, data.length());
    }

    @Test
    public void MediaSearchWithSpecialCharacters() throws IOException {
        final String testedString = "%3c%3e+.,:%60!@";

        Map<String, String> query = new HashMap<String, String>(){
            {
                put("media_type", testedString);
            }
        };

        JSONArray data = Utils.extractItemsFromJSONResponse(Utils.getSearchAPIDataResponse(query));
        Assert.assertEquals(0, data.length());
    }

}
