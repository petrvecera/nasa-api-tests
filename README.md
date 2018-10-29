# NBC Universal - SQE Task

## Task:

Propose testing solution to `search/` API ednpoint at  https://images-api.nasa.gov/  
Documentation https://images.nasa.gov/docs/images.nasa.gov_api_docs.pdf

## Exploratory testing:

### API key and service limits
In general "terms" of using NASA API is stayed to use authorization API key
and that there should be API limits displayed in the return [headers](https://api.nasa.gov/api.html#authentication) of the requests. 
  
It was verified that other requests, such as `https://api.nasa.gov/planetary/apod?api_key=klln2GBGJD9EIPnE45MUBZLOw5RQOP2SecHzsAN6` returns these headers:
```
x-ratelimit-limit →1000
x-ratelimit-remaining →997
``` 

However the requests to `https://images-api.nasa.gov` **does not return such headers.**  
Also the requests doesn't accept the API key param either: `"reason": "Invalid search parameter: api_key"`

Thus the testing doesn't included these properties as it seems not relevant to this particular API.

This is most likely due to the fact the API itself is consumed by JS Angular web app hosted at https://images.nasa.gov  

## Automated testing:

#### General testing `profiq/nasa/tests/General.java`
- Testing general responses of the API

For example:
 - Verify status code 400
 - Verify response for error 400 is JSON
 - Verify status code 404
 - Verify response for error 404 is JSON
 - Verify response for OPTIONS, POST  
 TODO: We could add testing of the other responses such as PUT, DELETE, PATCH etc
 
 #### Univerals testing `profiq/nasa/Utils.java`
 - Class `Utils` have several helper method but it also consist of several tests:  
 For each correct request it's expected the following to be true:  
   - Response code 200
   - Response is JSON
   - Response have correct "href" object
   - Response have correct JSON structure  
**Every expected correct request ( request which is expected to end with response code 200 ) is automatically tested for these assertions.**
 
 #### Specific param testing:  
 Each param of the `/search` end point has it's own test class. For example `profiq/nasa/tests/QSearch.java`
 Also class for each param has it's own method `verifySearchResults` which verifies the search results for the particular param.
 
 **Param `q` tests:**  
   - Verify that search is returning only the data which really have values from the Q search  
   - Verify long query 200 chars  
   - Verify very long query 1600 chars
   - Verify top boundary ( 50k chars)
   - Verify bottom boundary ( empty string)
   - Verify special characters which are not allowed
   - Verify special characters which are allowed

 **Param `media_type` tests:**  
   - Verify that search is returning only the data which has correct media_type for type `audio`
   - Verify that search is returning only the data which has correct media_type for type `image`
   - Verify that search is returning only the data which has correct media_type for type `image, audio`
   - Verify bottom boundary ( empty string)   
   - Verify results from special characters
   
 **Param `title` tests:**  
   - Verify that search is returining items with correct title
   - Verify empty value  
    
TODO: Add tests for other params

#### Param combination testing
In file `\profiq\nasa\tests\Combinations.java` we have param combination tests.
Each search param can be used on it's onw but mostly like in combination with other param. That's why Test Class for each param implements function `verifySearchResults` so we can combine multiple params together and than simply run verification for each param used.
  
The class consits of tests:
- CombineMediaAndQSearch
- CombineMediaQTitleSearch

However there is 13 params in total. Which can be combined at will. Define each combination by hand is not possible. Thus we should use "parametrized" testing in here. I would create function which would generate combinations of the params and could also verify all the responses automatically. Didn't have more time to do. 


#### Other TODO tests due to lack of time:
- When the respones has more than 100 results, there is `link` in the JSON for the next page, the link itself should be tested and this shoudl be also tested with combination with some search params. 
- Only top structure of the JSON file is tested. It consits of arrays of other JSONObjets, the structure of these sub objets is not tested. 
  

## Performance testing:
If we would need to fully test this API. Performance testing should be included. I would personally use tools
JMeter or Gatling as I have personal experience with them. Also it would depend on what types of test we would like to perform -
load testing, stress testing, endurance testing etc...  

Also the testing would be different if we would test the software with the API or just the API.
If we should test the software, we would need to deploy it on a server with specific HW setup (VMs). So we can determine how well 
the software behaves with particular HW and what are its limits. It's important that the HW which performs the tests is more powerful
than the HW holding the tested software so we can reach it's limits (we can use JMeter Slave mode for distribuated testing). During performance testing it's important to watch closely 
the server resources. We could leverage plugins such us [PerfMonitor](https://jmeter-plugins.org/wiki/PerfMon/) to get server
information (CPU, RAM etc) directly to the JMeter and the tests. If the backend server is complex we might also need to 
determine the bottlenecks (DB, some micro-services etc).


**But what we can test right now with this particular API is the response time of the requests.**  
Tested url `https://images-api.nasa.gov/search?q=apollo`
```
10 concurent requets - average reponse time ~250ms, max 262ms
50 concurent requets - average reponse time ~359ms, max 439ms
100 concurent requets - average reponse time ~1569ms, max 3758ms
```
Keep in mind that no timers(delay before requets) were included. All requests were started in parallel. To simulate real life scenario I would recommend adding random uniform timers (or some other) before each requets.
Also these tests were stressing only search with the `q` param. Other params might shown different results. 

## Security testing:
This is public API without any authorization tokens. It looks like every infromation which is in the DB is shared here. Thus it doesn't seem that security would be priority task of this API. 

**SSL:**
- The certificate deployed on the server is fully valid without any known vulnerabilites - tested by https://www.digicert.com/help/
- HTTPS Redirect:  
   - Any http:// requets, such us http://images-api.nasa.gov correctly redirects to https://

Some of the basic security test is already in place in normal tests:
 - Input validation
 - Restricted HTTP methods (POST, PUT, DELETE ... )
 
 What could be tested more:
 - Request content types
 - Accept headers
 - No stacktrace in error messages  ( was not able to trigger error 5xx) 
 - SQL Injections , eval validations
 - IP Location source - depending on the nature of the API we might want to block anonymous sources such as TOR 

## Found issues:

#### 1. Incorrect response on requests after request with error 400
If you do an incorrect request which leads to error 400, every other request (regardless if they are correct or not)
is going to get the same error and response as the first bad request for the next 10 seconds.

**Steps to reproduce:**
1. Make a GET request on `https://images-api.nasa.gov/search?year_start=%22test%22`
2. Check the response for error `{"reason":"Invalid value year_start=\"test\"."}`
3. Immediately make a GET request on `https://images-api.nasa.gov/search?year_start=2005`
4. Check the response  

**Expected result:**
```
{"collection": ...}
```
**Actual result:**
```
{"reason":"Invalid value year_start=\"test\"."}
```

#### 2. Incorrect response format  for error 404 and 414
In the documentation there is stated that: `JSON is returned by all API responses, including errors.` However this ins't true for `ERROR 404` or `ERROR 414`.  
**Steps to reproduce:**
1. Make a GET request on `https://images-api.nasa.gov/searchasdfasdfasdf` to trigger 404 or make request with 50k length to trigger 414.
2. Check the response format  

**Expected result:**  
The response format is JSON.  
**Actual result:**  
The response is HTML 
```html
<html>
 <head>
  <title>404 Not Found</title>
 </head>
 <body>
  <h1>404 Not Found</h1>
  The resource could not be found.<br/><br/>
/searchasdfasdfasdf


 </body>
</html>
```

*Possible cause:*
These errors are handled by NGINX proxy server which is deployed before the API. 

#### 3. Confusing API version stated in the docs
The [documentation](https://images.nasa.gov/docs/images.nasa.gov_api_docs.pdf) states that it should be 
release `v1.1.1`
however the API response has version `1.0`. *It's possible that the version is in regards to the document but the documentation
version should be in sync with with the API version. This is confusing.* Nevertheless if the API has version in response the docs should address this.

**Steps to reproduce:**
1. Make a GET request on `https://images-api.nasa.gov/search?q=xxxx`
2. Check the response 

**Expected result:**
```
{
    "collection": {
        "href": "https://images-api.nasa.gov/search?q=xxxx",
        "items": [],
        "metadata": {
            "total_hits": 0
        },
        "version": "1.1.1"
    }
}
```

**Actual result:**
```
{
    "collection": {
        "href": "https://images-api.nasa.gov/search?q=xxxx",
        "items": [],
        "metadata": {
            "total_hits": 0
        },
        "version": "1.0"
    }
}
```


### Suggestions for improvements:
I encountered things which could be considered bugs but due to a lack of documentation it's hard
to determine if the issue is bug or just suggestion for improvement.

#### 1. Inconsistency around response JSON structure - key links
When you create a search request which has more than 100 results. In the main JSON response there is 
key `links` which has array of values. The array holds one or 2 objects and that is link to another 100 or if
you go to page2 it has links to previous side. 

```
"links": [
    {
        "prompt": "Previous",
        "rel": "prev",
        "href": "https://images-api.nasa.gov/search?q=apollo&page=1"
    },
    {
        "prompt": "Next",
        "rel": "next",
        "href": "https://images-api.nasa.gov/search?q=apollo&page=3"
    }
],
```

**However when you create a search request which has less than 100 results. The link key is omitted.
The `links` key should be present at all times but the array should be empty when needed** 

  
**Steps to reproduce:**
1. Make a GET HTTP request on `https://images-api.nasa.gov/search?q=nothing`
2. Check the response.
  
**Expected results:**
```
{
    "collection": {
        "version": "1.0",
        "href": "https://images-api.nasa.gov/search?q=nothing",
        "metadata": {
            "total_hits": 76
        },
        "items": [...],
        "links": []
    }
}
```
**Actual results:**
```
{
    "collection": {
        "version": "1.0",
        "href": "https://images-api.nasa.gov/search?q=nothing",
        "metadata": {
            "total_hits": 76
        },
        "items": [...]
    }
}
```
#### 2. Improved documentation
The documentation lacks the information about some parts of the API. It's not hard to understand how the API behaves without the documetnation. But without it we can't be sure how the developers can treat the undocumented parts. If they can use them? Is it guranteed that will not chnage? And other questions. 

### Used tools / frameworks
- Automated tests written in Java and TestNG
- For manual testing [Postman](https://www.getpostman.com/) and curl used 
- For response time JMeter used
