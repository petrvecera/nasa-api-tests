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

**TODO** 


## Performance testing:
If we would need to fully test this API. Performance testing should be included. I would personally use tools
JMeter or Gatling as I have personal experience with them. Also it would depend on what types of test we would like to perform -
load testing, stress testing, endurance testing etc...  

Also the testing would be different if we would test the software with the API or just the API.
If we should test the software, we would need to deploy it on a server with specific HW setup. So we can determine how well 
the software behaves with particular HW and what are its limits. It's important that the HW which performs the tests is more powerful
than the HW holding the tested software so we can reach it's limits. During performance testing it's important to watch closely 
the server resources. We could leverage plugins such us [PerfMonitor](https://jmeter-plugins.org/wiki/PerfMon/) to get server
information (CPU, RAM etc) directly to the JMeter and the tests. If the backend server is complex we might be also able to 
determine the bottlenecks (DB, some micro-services etc).


But what we test right now with this particular API is the response time of the requests.   
- During the testing the API response were bellow `250ms` which could be considered as acceptable time. 


## Security testing:
**TODO**


 

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

#### 2. Incorrect response format  
In the documentation there is stated that: `JSON is returned by all API responses, including errors.` However this ins't true for `ERROR 404`.  
**Steps to reproduce:**
1. Make a GET request on `https://images-api.nasa.gov/searchasdfasdfasdf`
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





### Used tools / frameworks
- Automated tests written in Java and TestNG
- For manual testing [Postman](https://www.getpostman.com/) and curl used 




