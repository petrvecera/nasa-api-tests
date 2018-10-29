package com.profiq.nasa.tests;

import com.profiq.nasa.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TitleSearch
{

    /**
     * This function can test results for the media_type param string
     * AKA it verifies if all the results are correctly selected based on the Q param
     * @param inputData
     * @param testedString
     */
    public static void verifySearchResults(JSONObject inputData, String testedString){

        JSONArray responseData = Utils.extractItemsFromJSONResponse(inputData);

        Assert.assertTrue(responseData.length() > 0);

        for (int i = 0; i < responseData.length(); i++) {
            JSONObject item = responseData.getJSONObject(i);
            JSONObject data = item.getJSONArray("data").getJSONObject(0);
            String title = data.getString("title");

            Assert.assertTrue(title.toLowerCase().contains(testedString));
        }
    }

    @Test
    public void TitleSearchHasCorrectData() throws IOException {
        final String testedString = "apollo";

        Map<String, String> query = new HashMap<String, String>(){
            {
                put("title", testedString);
            }
        };

        verifySearchResults(Utils.getSearchAPIDataResponse(query), testedString);
    }

    @Test
    public void TitleSearchWithEmptyValue() throws IOException {
        final String testedString = "";

        Map<String, String> query = new HashMap<String, String>(){
            {
                put("title", testedString);
            }
        };

        JSONArray data = Utils.extractItemsFromJSONResponse(Utils.getSearchAPIDataResponse(query));
        Assert.assertEquals(0, data.length());
    }
}
