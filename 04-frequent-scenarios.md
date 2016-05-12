# Frequent scenarios

## Introduction

Many use cases can be captured in a small set of scenarios. This chapter aims to illustrate this small set and get you up and running as soon as possible with the REST convention. 

## HTTP Response Status codes

The scenarios described center around the [HTTP Response Status Codes][http-status-codes]. 

We will use the following codes:
* [200](#200--happy-flow); the expected response, ie the happy flow scenario
* [301](#301--redirected); request for the client to retrieve the resource from another location
* [304](#304--apply-cache); request for the client to utilize its cache
* [400](#400--validation-error); an error was encountered that can be fixed by the client
* [401](#401--unauthenticated); the client has not been authenticated
* [403](#403--unauthorized); the client tried to access a resource for which it was not authorized
* [404](#404--not-found); the requested resource could not be found
* [500](#500--critical-error); the server encountered an error from which it cannot recover

## Scenarios

### 200 / Happy flow

Typical response status code: 200

Request
```
GET /cars/1
```

Response
```
Status code: 200
Content: <JSON body of cars instance>
```

```java
@RestController
@RequestMapping("/cars")
public class CarController {

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Car getCar(@PathVariable Long id) {
        return carService.findOne(id);
    }
}
```

If the car resource is found, Spring will automatically make sure a response is created with a 200 code. You will have to do nothing special for this to work.

### 301 / Redirected


### 304 / Apply cache
A cache is by default implemented in every browser. It will cache resources that a user visited. When those resource are once more requested, the cache will serve the cached resources if they are still 'fresh'. The most important aspect for a server to implement is the mechanism to determine 'freshness' of a resource.

The following cache control mechanisms are supported by the browser cache:
* do not cache; also known as 'no-store'. This forbids your browser to cache at all.
* use cache, but verify first; also known as 'no-cache' (don't ask). The browser will first fetch the resource from the server, passing one of two, or both optional headers (If-Modified-Since, If-None-Match). If the servers returns a 304 status code, it will use its cached resource. Otherwise, it will use the response and cache it.
* use cache, but verify first if resource is stale; also known as 'must-revalidate'. Works similar to 'no-cache', but only calls the server if the maximum age has not been exceeded. Additionaly, on calling the server, the age will be updated if the resource is still fresh.

For regular browser caches, the 'no-cache' option is preferred. Passing a max age, means it will be hard to evict a resource from cache until it has grown stale. The 'no-cache' has no such problems.

A case can be made to use 'must-revalidate' in combination with a max-age, especially for sites that expect high traffic load on resources that are consumed manifold, but updated rarely.

Request
```
GET /cars/1
If-Modified-Since: [DATE]
If-None-Match: [ETAG]
```

Response
```
Status code: 304
Cache-Control: [CACHE INSTRUCTIONS]
Last-Modified: [DATE] 
Etag: [ETAG]
```

No special measures have to be made in the browser application. The browser cache will automatically take care of the response and all required actions to update the cache.

The values for the Last-Modified and/or Etag are determined by the server. The client will use those values literally.

If you want a controller methode that can deal with a request header containing, for example, If-Modified-Since, this is what it could look like:

```java
public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
public static final DateTimeFormatter HTTP_DATE_FORMATTER = DateTimeFormatter.ofPattern(HTTP_DATE_FORMAT);

@RequestMapping(value = "/{id}", method = RequestMethod.GET)
public Car fetchCar(
        HttpServletResponse response,
        @PathVariable String id,
        @RequestHeader(value = "If-Modified-Since", required = false)
            @DateTimeFormat(pattern = HTTP_DATE_FORMAT) LocalDateTime ifModifiedSince) {
    Car car = carService.findCarIfStale(id, ifModifiedSince);
    response.setHeader("Last-Modified", car.getModified().format(HTTP_DATE_FORMATTER));
    return car;
}
```

Ideally, findCarIfStale in the example above, would throw a not-modified exception if the resource is still fresh. A separate @ControllerAdvice method can then deal with this exception:
 
```java
@ExceptionHandler(CarNotModifiedException.class)
@ResponseStatus(HttpStatus.NOT_MODIFIED)
public void handleArticleNotModified() {}
```

Additionally, if max-age is used (in combination with must-revalidate), the response header must be set in this handler as well.

### 400 / Validation error

### 401 / Unauthenticated

### 403 / Unauthorized

### 404 / Not found

### 500 / Critical error


[http-status-codes]: https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html