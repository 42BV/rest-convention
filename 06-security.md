# Rest API Security

A work in progress.

# Introduction

Rest API's are basically thin wrappers around the service layer of an application and as such expose a larger attack surface than a traditional web application, which limits the actions to what the user interface can do. As such great care must be taken in protecting the data and operations provided by the API. 

It should not come as a surprise that any data available from the API without authentication will soon be used for (automated) information gathering. 
So, unless the API is a public resource, authentication must be required.    
Even if the API is a public resource, some form request rate limiting is necessary because API's may be subjected to what is called 'high velocity farming', basically continuously polling the service for updated data. 

An API rarely stands on its own, so any data entered into the API may be propagated to other systems. That is why it is essential that all API input is validated and or filtered. Don't accept control characters as input if simple alphanumerics are all that is required. While the API and it subsystems may be safe against various forms of injection there is no guarantee that other (legacy) systems connected to the API are.

If authentication or API tokens are required (and they probably are) for use of the API, they must be kept safe against eavesdropping and theft. So use transport layer security (HTTPS). 
If a browser session is used, make sure it is http-only and secure. Also enable cross site request forgery (CSRF) tokens to prevent a malicious tab in the same browser to masquerade as the user.

# API Security Guidelines

## Use HTTPS

HTTP traffic can be easily monitored, intercepted and modified while in transit. Even if the data the API exposes does not contain sensitive data, your API keys and/or session tokens are.

So, with the availability of free and easy to install [https certificates](https://letsencrypt.org/) there is no excuse for not having transport level security using HTTPS. 
Also check your configuration with [SSLTest](https://www.ssllabs.com/ssltest/) or equivalent.

## Minimize publicly available information

### No brands and versions in HTTP Headers

These inform potential attackers on what software you're using an make vulnerability mapping easier.  

### No technical details in error messages

Stacktraces and vendor specific error codes tell a lot about the system and make informed guessing easier. If an error occurs these details should be logged but not presented to the user of the API. You can provide a timestamp reference for easy finding the stacktrace in the log. 

### Do not repeat (erroneous) input in error messages

In some conditions the browser may display the contents unescaped, potentially triggering cross site scripting. (Also this eliminates a lot of false positives when using vulnerability scanners).  

### Restrict access to API documentation

Unless the API is intended for public use, documentation should be available to developers only (Tools like [Swagger](http://swagger.io/) are useful when developing but should not be part of the production environment). 

## Require Authentication

There are various ways to implement API authentication. For now this section will focus on the traditional session based authentication as most of our applications use a browser based user interface.

### Require Authentication on all API calls.

The one notable exception being a GET request for the current authenticated user (if any). This can also be used to retrieve the XSRF token.

### Sessions must be reset after logging in and out.

The session id must change after a successful login and logout. Otherwise session fixation attacks may occur.  

### Use Secure Session Tokens

Session Cookies must have the flags `http-only` and `secure` set. 

### Protect the session against Cross Site Request Forgery (XSRF)

A malicious browser tab may send requests to the API while the user is logged in. These requests will be sent with the correct session id by the browser. 
To prevent abuse an additional token must be sent with each write operation, the `XSRF-TOKEN`. 

Typically the token is sent to the browser in the form of a cookie by the first GET request to the API, read by the JavaScript on the page and added as a request header (`X-XSRF-TOKEN`) to subsequent PUT, POST or DELETE request.  The API checks the presence of the correct header on the request. If not correct, the request fails.
 
The malicious tab cannot read the contents of the `XSRF-TOKEN` cookie as it is on a different domain. 
    
An XSRF token is assigned to the browser on the first GET request. XSRF cookies MUST have the flag `secure` set. The API must check PUT, POST, PATCH and DELETE requests for the presence of the correct XSRF token.

### Passwords must be salted and hashed using BCrypt

BCrypt is a computational heavy hashing algorithm with built in salt. If the user database is stolen if will take a lot more effort to brute force the passwords than using conventional algorithms (MD5, SHA-1, SHA-256). Note that there is a better hashing algorithm called Argon2 but this is not (yet) supported by Spring-Security.  

### Limit the number of Login attempts per unit of time.

If the login form is accessible from the Internet, the number of (failed) login attempts must be limited per unit of time to prevent brute forcing. The counting must be done in the user account, not in the session (as sessions may be reset).

### Require a minimum password length

While having a minimum password length will not prevent [pattern based brute forcing techniques](https://www.youtube.com/watch?v=zUM7i8fsf0g) it will prevent users from picking ridiculously short passwords.

### Configure Cross Origin Resource Sharing if needed

If a browser application that resides on a different domain access needs to access the API the single origin policy (SOP) will prevent the browser from reading the responses. There is a way around this by supporting the OPTIONS request method and returning appropriate CORS headers. Before performing a request on a different domain the browser will first issue an OPTIONS request to see if it is allowed to perform the request. Typically a CORS response should look something like:

````
Access-Control-Allow-Credentials: true
Access-Control-Allow-Origin: https://somedomain.org, https://otherdomain.org
Access-Control-Allow-Methods: GET, OPTIONS, POST, PUT, PATCH, DELETE
Access-Control-Allow-Headers: Origin, X-Requested-With, Content-Type, Accept, X-XSRF-TOKEN
Access-Control-Max-Age: 3600
````

Access-Control-Allow-Credentials allows the passing of a session cookie. 
The Access-Control-Allow-Origin lists the domains that are allowed to access the API.
The Access-Control-Allow-Methods list the permissible HTTP Methods.
Access-Control-Allow-Headers informs the browser of the allowed headers to send.
Finally Access-Control-Max-Age informs the browser on how long the information in the response may be cached.

*Do not* use the wildcard on Access-Control-Allow-Origin unless you truly want your API to be accessible to the world.  
````
Access-Control-Allow-Origin: *
```` 

### Use browser security response headers.

Various [headers](https://www.owasp.org/index.php/List_of_useful_HTTP_headers) exist to prevent abuse of resources in and from the browser. 

````
X-Frame-Options: deny
X-XSS-Protection: 1; mode=block
X-Content-Type-Options: nosniff
````

The `X-Frame-Options` header disallows the contents to be displayed in an IFrame. The `X-XSS-Protection` header makes the browser apply a filter to the URL to prevent reflected Cross Site Scripting. Finally, the `X-Content-Type-Options` header disallows the browser from guessing the content type. 

### Disable caching

In general responses from Rest API's should not be cached. This is partly because the data returned may be sensitive and also because the data must be fresh (so not an old copy of the data). For this various headers must be set: 

````
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
````
The HTTP/1.1 Cache-Control header can hold multiple values. The value `no-cache` tells the cache that the response must be revalidated. `no-store` tells the cache that the contents may not be stored on disk. The `max-age` tells the maximum age of the response before it must be retrieved again. Finally, `must-revalidate` demands that the liveness of the data is always checked at the server.

The `Pragma: no-cache` header forces revalidation of resources for old HTTP/1.0 proxies.

The `Expires` header field gives the date/time after which the response is considered stale. If its value is 0 its always stale and triggers a refresh. 


### Use strict transport security

The `Strict-Transport-Security` header makes sure that a resource is only accessible over HTTPS, preventing tools such as [SSLStrip](http://www.thoughtcrime.org/software/sslstrip/) from working.

````
Strict-Transport-Security: max-age=... ; includeSubDomains
````

The `max-age` parameter tells how long the domain must be accessed using HTTPS before it may be accessed over HTTP again. The `includeSubDomains` flag tells that this is true for all subdomains as well. 

## Output

### Use Data Transfer Objects to map API output

The entities of the service layer that are exposed by the API may hold internal details (such as password hashes) that should not be exposed to the outside world. Use separate Data Transfer Objects (DTO's) that represent the output of the service. There are various frameworks that make mapping entities to DTO's easy (try [Beanmapper](https://github.com/42BV/beanmapper)).

## Input

### Use Data Transfer Objects to map API input.

Incoming JSON structures frequently contain additional fields because of programming error, framework issues or malicious intent. That is why these must be mapped to an intermediate Data Transfer Object (sometimes called a Form) before applying it to the entity for which it is intended. Again, frameworks such as [Beanmapper](https://github.com/42BV/beanmapper) make this easy.

### Validate path and request parameters.

All path and request parameters should be validated.

### Validate Data Transfer Objects

All fields in the Data Transfer Objects should be validated. This is most easily done using a framework such as [Hibernate Validator](http://hibernate.org/validator/).

### Check Object Access Permissions 

In a typical CRUD system, access is granted on a collection (or type) basis. So if a user can edit one object of a type, its possible to edit them all. 
This coarse grained access mechanism is not suitable if a resource is owned by a specific user. If this is the case additional checks must be made at the service layer.

### Validate Strings 

An innocent looking unvalidated String field such as 'name' may contain any character, including newline's null char's and exotic symbols. These characters may have interesting effects in downstream (legacy) systems or libraries. (such as [JavaMail Header Injection via Subject](http://www.csnc.ch/misc/files/advisories/CSNC-2014-001_javamail_advisory_public.txt)). In general, only allow characters that have meaning for the field and reject any unwanted ones (especially those below ASCII value 32) and limit the length of the field.

### Filter HTML 

If your application allows rich HTML editing make sure that the server side sanitizes the HTML. Various libraries exist to do this, for example [JSoup](http://jsoup.org/cookbook/cleaning-html/whitelist-sanitizer).

# Implementing the Security Guidelines with Spring Security

TBD.

# Further reading

* [OWASP REST Security Cheat sheet](https://www.owasp.org/index.php/REST_Security_Cheat_Sheet)
* [CORS with Spring MVC](http://dontpanic.42.nl/2015/04/cors-with-spring-mvc.html)
* [Angular JS and Spring Security](https://spring.io/guides/tutorials/spring-security-and-angular-js/)
* [Angular JS and Spring Security Part II](https://spring.io/blog/2015/01/12/the-login-page-angular-js-and-spring-security-part-ii)
* [How to Hack an API and get away with it](http://blog.smartbear.com/readyapi/api-security-testing-how-to-hack-an-api-and-get-away-with-it-part-1-of-3/)
* [Understanding HTTP Strict Transport Security](http://www.troyhunt.com/2015/06/understanding-http-strict-transport.html)
