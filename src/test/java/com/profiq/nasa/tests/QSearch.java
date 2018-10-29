package com.profiq.nasa.tests;

import com.profiq.nasa.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QSearch {


    @Test
    public void QSearchHasCorrectData() throws IOException {
        final String testedString = "fdf";

        Map<String, String> query = new HashMap<String, String>(){
            {
                put("q", testedString);
            }
        };

        JSONArray responseData = Utils.extractItemsFromJSONResponse(Utils.getSearchAPIDataResponse(query));

        for (int i = 0; i < responseData.length(); i++) {
            JSONObject item = responseData.getJSONObject(i);
            JSONObject data = item.getJSONArray("data").getJSONObject(0);
            String description = data.getString("description");
            String title = data.getString("title");

            Assert.assertTrue(description.toLowerCase().contains(testedString) || title.toLowerCase().contains(testedString));
        }

    }

    @Test
    public void QSearchAWithLongQuery() throws IOException {
        final String testedString = Utils.generateRandomString(200);

        Map<String, String> query = new HashMap<String, String>(){
            {
                put("q", testedString);
            }
        };

        JSONObject responseData = Utils.getSearchAPIDataResponse(query);


        Assert.assertEquals(responseData.getJSONObject("collection").getJSONArray("items").length(),0);
    }

    @Test
    public void QSearchResponsesEmpty() throws IOException {
        final String testedString = "";

        Map<String, String> query = new HashMap<String, String>(){
            {
                put("q", testedString);
            }
        };

        JSONObject responseData = Utils.getSearchAPIDataResponse(query, 400);
        Assert.assertEquals(responseData.getString("reason"), "Expected 'q' text search parameter or other keywords.");
    }

    @Test
    public void QSearchAWithExtremeLongQuery() throws IOException {
        final String testedString = Utils.generateRandomString(1600);

        Map<String, String> query = new HashMap<String, String>(){
            {
                put("q", testedString);
            }
        };

        JSONObject responseData = Utils.getSearchAPIDataResponse(query);

        Assert.assertEquals(responseData.getJSONObject("collection").getJSONArray("items").length(),0);
    }




}
