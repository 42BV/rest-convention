# Rest API Security

A work in progress.

# Risks and Mitigations

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

Unless the API is intended for public use, documentation should be available to developers only (and not online using automatic documentation tools like [Swagger](http://swagger.io/)). 

## Require Authentication

There are various ways to implement API authentication. For now this section will focus on the traditional session based authentication as most of our applications use a browser based user interface.

### Limit the number of Login attempts per unit of time.

If the login form is accessible from the Internet, the number of (failed) login attempts must be limited per unit of time to prevent brute forcing. The counting must be done in the user account, not in the session (as sessions may be reset).

### Sessions must be reset after logging in and out.

The session id must change after a login and logout. Otherwise session fixation attacks may occur.  

### Secure Session Management

Session Cookies MUST have the flags *http-only* and *secure* set. 

### Protect the session against Cross Site Request Forgery (XSRF)

A malicious browser tab may send requests to the API while the user is logged in. These requests will be sent with the correct session id by the browser. 
To prevent abuse an additional token must be sent with each write operation, the XSRF-TOKEN. 

Typically the token is sent to the browser in the form of a cookie by the first GET request to the API, read by the JavaScript on the page and added as a request header (X-XSRF-TOKEN) to subsequent PUT, POST or DELETE request.  The API checks the presence of the correct header on the request. If not correct, the request fails.
 
The malicious tab cannot read the contents of the XSRF-TOKEN cookie as it is on a different domain. 
    
An XSRF token is assigned to the browser on the first GET request. XSRF cookies MUST have the flag *secure* set. The API must check PUT, POST, PATCH and DELETE requests for the presence of the correct XSRF token.

### Cross Origin Resource Sharing

If you need to have a different domain access your API use CORS headers and support the OPTIONS request method.

### Use browser security response headers.

Various [headers](https://www.owasp.org/index.php/List_of_useful_HTTP_headers) exist to prevent abuse of resources in and from the browser. 

* X-Frame-Options: deny
* X-XSS-Protection: 1; mode=block
* X-Content-Type-Options: nosniff

### Disable caching

* Cache-Control: no-cache, no-store, max-age=0, must-revalidate
* Pragma: no-cache
* Expires: 0

### Use strict transport security

* Strict-Transport-Security: max-age=... ; includeSubDomains

## Validate Input on the Server

### Validate Strings

An innocent looking unvalidated String field such as 'name' may contain any character, including newline's null char's and exotic symbols. These characters may have interesting effects in downstream (legacy) systems or libraries. (such as [JavaMail Header Injection via Subject](http://www.csnc.ch/misc/files/advisories/CSNC-2014-001_javamail_advisory_public.txt)). In general, only allow characters that have meaning for the field and reject any unwanted ones (especially those below ASCII value 32).

### Filter HTML 

If your application allows rich HTML editing make sure that the server side sanitizes the HTML. Various libraries exist to do this, for example [JSoup](http://jsoup.org/cookbook/cleaning-html/whitelist-sanitizer).

### Check Object Access Permissions 

In a typical CRUD system, access is granted on a collection (or type) basis. So if you can edit one object of a type, you can edit them all. 
This coarse grained access mechanism is not suitable if a resource is owned by a specific user. If this is the case additional checks must be made.       

# Implementing the Security Guidelines

TBD.

# Further reading

* [OWASP REST Security Cheat sheet](https://www.owasp.org/index.php/REST_Security_Cheat_Sheet)
* [CORS with Spring MVC](http://dontpanic.42.nl/2015/04/cors-with-spring-mvc.html)
* [Angular JS and Spring Security](https://spring.io/guides/tutorials/spring-security-and-angular-js/)
* [Angular JS and Spring Security Part II](https://spring.io/blog/2015/01/12/the-login-page-angular-js-and-spring-security-part-ii)
* [How to Hack an API and get away with it](http://blog.smartbear.com/readyapi/api-security-testing-how-to-hack-an-api-and-get-away-with-it-part-1-of-3/)

