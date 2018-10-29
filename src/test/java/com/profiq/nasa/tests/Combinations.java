package com.profiq.nasa.tests;

import com.profiq.nasa.Utils;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Combinations {

    @Test
    public void CombineMediaAndQSearch() throws IOException {
        final String testedMediaString = "audio";
        final String testedQSearchString = "apollo";

        Map<String, String> query = new HashMap<String, String>(){
            {
                put("media_type", testedMediaString);
                put("q", testedQSearchString);
            }
        };

        JSONObject data = Utils.getSearchAPIDataResponse(query);

        MediaSearch.verifySearchResults(data, testedMediaString);
        QSearch.verifySearchResults(data, testedQSearchString);
    }

    @Test
    public void CombineMediaQTitleSearch() throws IOException {
        final String testedMediaString = "image";
        final String testedQSearchString = "crash";
        final String testedTitleString = "apollo";

        Map<String, String> query = new HashMap<String, String>(){
            {
                put("media_type", testedMediaString);
                put("q", testedQSearchString);
                put("title", testedTitleString);
            }
        };

        JSONObject data = Utils.getSearchAPIDataResponse(query);

        MediaSearch.verifySearchResults(data, testedMediaString);
        QSearch.verifySearchResults(data, testedQSearchString);
        TitleSearch.verifySearchResults(data, testedTitleString);
    }
}
