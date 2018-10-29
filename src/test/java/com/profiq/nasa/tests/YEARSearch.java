package com.profiq.nasa.tests;

import com.profiq.nasa.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class YEARSearch {


    @Test
    public void year_startSearchHasCorrectData() throws IOException {

        Map<String, String> query = new HashMap<String, String>(){
            {
                put("year_start", "2018");
            }
        };

        JSONArray responseData = Utils.extractItemsFromJSONResponse(Utils.getSearchAPIDataResponse(query));

        for (int i = 0; i < responseData.length(); i++) {
            JSONObject item = responseData.getJSONObject(i);
            JSONObject data = item.getJSONArray("data").getJSONObject(0);
            String description = data.getString("description");
            String title = data.getString("title");

//            Assert.assertTrue(description.toLowerCase().contains(testedString) || title.toLowerCase().contains(testedString));
        }

    }



}
