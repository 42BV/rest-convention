# Frequent scenarios

## Introduction

Many use cases can be captured in a small set of scenarios. This chapter aims to illustrate this small set and get you up and running as soon as possible with the REST convention. 

## Scenarios

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

A redirection takes place if the server can determine whether a resource has been moved to another location, and also knows what that new location is. A typical example of valid usage here, is a resource which is addressed by a human-readable permalink (for example, the title of an article). When the title changes, the resource has effectively moved. This situation warrants a redirect operation.

Ideally, if a resource has been moved to another link, the client will be told so in the standard HTTP way. In this case, the server method responsible for looking up the resource will throw an exception that is handled by a @ControllerAdvice method. This method will set the response code (301) and add the header with the new location of the resource.

```java
@ExceptionHandler(CarRedirectException.class)
@ResponseStatus(HttpStatus.MOVED_PERMANENTLY)
public void handleCarRedirect(CarRedirectException ex) {
    resp.setHeader("Location", ex.getRedirectLink());
}
```

No client-side measures need to be taken to deal with the redirect. The browser takes care of this handling.

### 304 / Apply cache
A cache is by default implemented in every browser. It will cache resources that a user visited. When those resource are once more requested, the cache will serve the cached resources if they are still 'fresh'. The most important aspect for a server to implement is the mechanism to determine 'freshness' of a resource.

The following cache control mechanisms are supported by the browser cache:
* do not cache; also known as 'no-store'. This forbids your browser to cache at all.
* use cache, but verify first; also known as 'no-cache' (don't ask). The browser will first fetch the resource from the server, passing one of two, or both optional headers (If-Modified-Since, If-None-Match). If the servers returns a 304 status code, it will use its cached resource. Otherwise, it will use the response and cache it.
* use cache, but verify first if resource is stale; also known as 'must-revalidate'. Works similar to 'no-cache', but only calls the server if the maximum age has not been exceeded. Additionaly, on calling the server, the age will be updated if the resource is still fresh.

For regular browser caches, the 'no-cache' option is preferred. Passing a max age, means it will be hard to evict a resource from cache until it has grown stale. The 'no-cache' has no such problems.

A case can be made to use 'must-revalidate' in combination with a 'max-age', especially for sites that expect high traffic load on resources that are consumed manifold, but updated rarely.

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
public Car getCar(
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
public void handleCarNotModified() {}
```

Additionally, if max-age is used (in combination with must-revalidate), the max-age response header must be set in this handler method as well. This causes the already cached result to be 'refreshed' with a new max-age value.

### 400 / Validation error

A validation error typically occurs when the input is not up to par with the service standard. The desired feedback consists of a field by field breakdown containing all the violated validation rules for every field. Any UI can then choose to decorate the field with the violated rules to inform the user.

**Note:** that in an ongoing discussion, this error is sometimes generated under status code [422][422-definition]. The rationale behind using 422 instead of 400, is that 422 specifically speaks about syntactically correct (which it is), but semantically erroneous input. A validation error is just that.

Two levels of validations are usually recognized:
* validation which can be made in isolation; a typical example of this kind of validation is whether a zip code conforms to a nationally defined standard. 
* validation which is made in context; a typical example of this kind of validation is to see if a similar record is already in place.  

The first kind of validation lends itself very well to be implemented both in the UI (as standalone rules) and in the backend (in a similar vein). The second kind of validation is typically enforced by a service which has the capability to make calls to a repository layer.

Both kinds can be made to fit in a generic validation framework. Java Bean Validation (aka JSR-303), is a decent and standardized way to deal with this. 

If you want to be able to validate your input against standard validation rules, be sure to include the Hibernate validator (or a similar library):
```xml
<dependency>
    <groupId>org.hibernate<
    <artifactId>hibernate-validator</artifactId>
    <version>5.1.3.Final</version>
</dependency>
```

The Java SDK API offers a number of validation annotations in its javax.validation.constraints package. These annotations can be used to annotate input classes (such as forms):

```java
public class CarForm {

    @NotEmpty @Size(max = 80)
    public String name;
}
```

It now becomes possible to further annotate your input classes in the controller methods to enforce validation on method entry:
 
```java
@RequestMapping(method = RequestMethod.POST)
public Car createCar(@RequestBody @Valid Car car) {
    return carService.createCar(car);
}
```

Now we can add a handler to deal with MethodArgumentNotValidException exceptions, which are thrown if the validation fails. A typical @ControllerAdvice handler method would be:

```java
@ExceptionHandler({ MethodArgumentNotValidException.class })
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
@ResponseBody
public List<FieldErrorMessage> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
    return ex.getBindingResult().getFieldErrors().stream().map(fe -> new FieldErrorMessage(fe)).collect(Collectors.toList());
}
```

Note that the FieldErrorMessage is a custom construct that allows for all fields to be disclosed, something which is regrettably lacking from Spring's FieldError. Just copy this code:

```java
public class FieldErrorMessage {

    private final String field;
    private final String code;
    private final String message;

    public FieldErrorMessage(FieldError error) {
        this.field = error.getField();
        this.code = error.getCode();
        this.message = error.getDefaultMessage();
    }

    public String getField() { return field; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}
```

### 401 / Unauthenticated

When a user is not logged in to the system and at least an authenticated user is required, this status code will be returned. Note that the official title for this status code is "Unauthorized", which is really a misnomer, since the [explanation][401-definition] is quite clear on what is intended:

> The request requires user authentication

That out of the way, suppose you have a service-level method that has been secured with the @Secured annotation.

```java
@Secured({ Roles.CUSTOMER })
public Car findCar(Long id) {
    return ...
}
```

If Spring Security has been properly configured, it will automatically throw an AuthenticationException error when the user is not authenticated whilst being required to. A typical @ControllerAdvice handler method would be:

```java
@ExceptionHandler(AuthenticationException.class)
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public void handlesAuthenticationException() {}
```

Client side changes dealing with a 401 exception, generally entail redirecting the user to a login screen. Note that several resource requests may simultaneously trigger a 401 response, potentially triggering multiple login redirects. You can consider an [immediate / leading debounce][leading-debounce] or a check to see if the login page has already been loaded.

### 403 / Unauthorized

When a user has been authenticated, but does not have the roles required for a resource, a 403 Forbidden status code is returned. Assume a protected service method:
  
```java
@Secured({ Roles.CUSTOMER })
public Car findCar(Long id) {
    return ...
}
```

Spring Security will automatically throw an AccessDeniedException exception. Its @ControllerAdvice handler method would be:

```java
@ExceptionHandler(AccessDeniedException.class)
@ResponseStatus(HttpStatus.FORBIDDEN)
public void handlesAccessDeniedException() {}
```

In general, a no special measures have to be taken in the client to deal with 403 responses. Note, however, that it is adviced to implement a policy that prevents users from being able to request resources for which they are not authorized in the first place. This can be as simple as not being able to access the admin panel as a regular user. 

### 404 / Not found

### 500 / Critical error


[http-status-codes]: https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
[401-definition]: https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.2
[422-definition]: https://tools.ietf.org/html/rfc4918#section-11.2
[leading-debounce]: https://css-tricks.com/debouncing-throttling-explained-examples/