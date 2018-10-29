package com.profiq.nasa.tests;

import com.profiq.nasa.Utils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.HttpURLConnection;

public class General {

    @Test(groups = {"responseCodes"})
    public void verifyResponse400() throws IOException {
        HttpURLConnection con =  Utils.makeHTTPConnection(Utils.apiHost + Utils.searchEndPoint,"GET", 400);
        Assert.assertEquals(con.getResponseCode(), 400);
    }

    @Test
    public void verifyResponse400isJSON() throws IOException {
        HttpURLConnection con =  Utils.makeHTTPConnection(Utils.apiHost + Utils.searchEndPoint,"GET", 400);
        // FYI No assertion in here, we relay on the Exception throw. However this might not be acceptable by some frameworks
        // which might require each Test to have at least 1 assertion
        Utils.parseJSONResponse(con);
    }


    @Test(groups = {"responseCodes"})
    public void verifyResponse404() throws IOException {
        HttpURLConnection con =  Utils.makeHTTPConnection(Utils.apiHost + "undefinedEndPointOfAPI", "GET", 404);
        Assert.assertEquals(con.getResponseCode(), 404);
    }

    @Test
    public void verifyResponse404isJSON() throws IOException {
        // This test could be possibly included directly in the TC verifyResponse404 but as we know it fails, separating the TC in case of failure
        // gives us better inside into tested assertions
        HttpURLConnection con =  Utils.makeHTTPConnection(Utils.apiHost + "undefinedEndPointOfAPI", "GET", 404);
        // FYI No assertion in here, we relay on the Exception throw. However this might not be acceptable by some frameworks
        // which might require each Test to have at least 1 assertion
        Utils.parseJSONResponse(con);
    }

    @Test
    public void verifyOPTIONSResponse() throws IOException {
        HttpURLConnection con =  Utils.makeHTTPConnection(Utils.getFullUrlToSearch(), "OPTIONS");
        Assert.assertEquals(con.getHeaderField("access-control-allow-methods"), "GET");
    }

    @Test
    public void verifyPOSTResponse() throws IOException {
        HttpURLConnection con =  Utils.makeHTTPConnection(Utils.getFullUrlToSearch(), "POST", 404);
    }

    // TODO: We could verify how the API behaves with all the other types of HTTP requests
}
