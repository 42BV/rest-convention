# Rest API Security

A work in progress.

# Introduction

REST API's are basically wrappers around the service layer of an application and as such expose a larger attack surface than a traditional web application, which limits the actions to what the user interface can do. As such great care must be taken in protecting the data and operations provided by the API. 

This chapter in the REST Convention consists of two parts. The first is a list of guidelines and the second are implementation notes on how to implement this convention using the popular Spring-Security framework. It should not come as a surprise that some of the recommendations match the defaults of Spring-Security :-)

While many of the guidelines are applicable to any REST API, there is a dedicated part for API's that are consumed via the browser (which is our typical use case). 

# API Security Guidelines

## Use HTTPS

HTTP traffic can be easily monitored, intercepted and modified while in transit. Even if the data the API exposes does not contain sensitive data, the API keys and/or session tokens are.

So, with the availability of free and easy to install [https certificates](https://letsencrypt.org/) there is no excuse for not having transport level security using HTTPS. 
Also check the configuration with [SSLTest](https://www.ssllabs.com/ssltest/) or equivalent.

## HTTP Status codes

### Use HTTP 401 Unauthorized

To signal that a user *is not* authenticated and needs to authenticate to access the resource.

### Use HTTP 403 Forbidden 

To signal that a user *is* authenticated and is *not allowed* to access the resource.

## Minimize publicly available information

### No brands and versions in HTTP Headers

These inform potential attackers on what software is being used and make vulnerability mapping easier. 

For example if the server header in the HTTP response is `Server: Apache/2.2.16 (Debian)` then the server is very likely running (the no longer supported) Debian Squeeze (6) because [that version](https://packages.debian.org/squeeze-lts/apache2) of Apache2 is packaged with it. Later versions have different Apache versions (e.g. Wheezy has 2.2.22 and Jessie 2.4.10).      

### No technical details in error messages

Stacktraces and vendor specific error codes tell a lot about the system and make informed guessing easier. If an error occurs these details should be logged but not presented to the user of the API. You can provide a timestamp as a reference for finding the stacktrace in the log. 

### Do not repeat (erroneous) input in error messages

In some conditions the browser may display the contents unescaped, potentially triggering cross site scripting. (Also this eliminates a lot of false positives when using vulnerability scanners).  

### Restrict access to API documentation

Unless the API is intended for public use, documentation should be available to developers only (Tools like [Swagger](http://swagger.io/) are useful when developing but should not be part of the production environment). 

## Security on an API for Browser based consumption 

This section will focus on the traditional session based authentication as most of our applications use a browser based user interface. 

### Require Authentication on all API calls.

The one notable exception being a GET request for the current authenticated user (if any). This can also be used to retrieve the XSRF token.

### Don't use HTTP Basic Authentication

When using Basic Authentication the credentials will be collected once by the browser and then sent along automatically on a header for each request.

While simple and supported by almost everything this authentication method has some serious disadvantages:

* the password is sent on each request.
* the password is held in memory in the browser.
* (header) logging may reveal the password.
* there is no standard way of logging out once authenticated. [Clever Hacks](http://stackoverflow.com/questions/233507/how-to-log-out-user-from-web-site-using-basic-authentication) do exist but don't work on all browsers.

### Don't use JSON Web Tokens (JWT) in a browser

While JWT is perfect for machine to machine authentication, its very susceptible to theft using cross site scripting (XSS) in a browser. 
Also JSON Web Tokens remain valid until they expire, so something equivalent to logging out is impossible without removing the statelessness advantage. 

### Don't store tokens or personal information in session or local storage.

These are easily read and tampered with by the user or a malicious script if XSS exists in the application.

### GET requests must not change server state

Of course this would be against the REST design principles elsewhere in this convention, but there is a good security reason too: they are easily executed using standard HTML tags.

### Sessions must be reset after logging in and out.

The session id must change after a successful login and logout. Otherwise session fixation attacks may occur.  

### Use Secure Session Tokens

Session Cookies must have the flags `http-only` and `secure` set. Http-only means that the cookie is not accessible from Javascript (otherwise the session could be stolen using cross-site-scripting). Secure means that it can only be transmitted over a HTTPS connection (otherwise the session could be read in transit by a man-in-the-middle).

### Protect the Session against Cross Site Request Forgery (XSRF)

A malicious browser tab may send requests to the API while the user is logged in. These requests will be sent with the correct session id by the browser. 
To prevent abuse an additional token must be sent with each write operation, the `XSRF-TOKEN`. 

Typically the token is sent to the browser in the form of a cookie by the first GET request to the API, read by the JavaScript on the page and added as a request header (`X-XSRF-TOKEN`) to subsequent PUT, POST or DELETE request.  The API checks the presence of the correct header on the request. If not correct, the request fails.
 
The malicious tab cannot read the contents of the `XSRF-TOKEN` cookie as it is on a different domain. 
    
An XSRF token is assigned to the browser on the first GET request. XSRF cookies MUST have the flag `secure` set. The API must check PUT, POST, PATCH and DELETE requests for the presence of the correct XSRF token.

### Authentication

Usernames and passwords are still the most commonly used way to authenticate a user especially if there is no supporting infrastructure already in place. However implementing proper authentication using usernames and passwords not a trivial task. Owasp has a [whole page](https://www.owasp.org/index.php/Authentication_Cheat_Sheet) on it. This convention describes just the minimal requirements for authenticating using a username and password. Other (important) subjects such as 'Forgotten Password' functionality or account activation is out of scope. 

#### Passwords must be salted and hashed using BCrypt

BCrypt is a computational heavy hashing algorithm with built in salt. If the user database is stolen if will take a lot more effort to brute force the passwords than using conventional algorithms (MD5, SHA-1, SHA-256). Note that there is a better hashing algorithm called Argon2 but this is not (yet) supported by Spring-Security.  

#### A failed login attempt must always return the same response.

When authentication fails the username may not exist, the password may be incorrect or the account may have been locked. If an error message is returned it should always be a generic error message ('Authentication Failed') that does not state the reason for failing. This prevents attackers from learning if actually an account exists with that username.

#### Limit the number of Login attempts per unit of time.

If the login form is accessible from the Internet, the number of (failed) login attempts must be limited per unit of time to prevent brute forcing. The counting must be done in the user account, not in the session (as a new session may be used for each attempt). 

#### Consider using password complexity requirements and blacklists

While having a these will not prevent [pattern based brute forcing techniques](https://www.youtube.com/watch?v=zUM7i8fsf0g) it will prevent users from picking too simple or common passwords.
Good blacklists can be found [here](https://github.com/danielmiessler/SecLists/tree/master/Passwords). 

### Configure Cross Origin Resource Sharing if needed

If a browser application that resides on a different domain access needs to access the API the single origin policy (SOP) will prevent the browser from reading the responses. There is a way around this by supporting the OPTIONS request method and returning appropriate CORS headers. Before performing a request on a different domain the browser will first issue an OPTIONS request to see if it is allowed to perform the request. Typically a CORS response should look something like:

```
Access-Control-Allow-Credentials: true
Access-Control-Allow-Origin: https://somedomain.org, https://otherdomain.org
Access-Control-Allow-Methods: GET, OPTIONS, POST, PUT, PATCH, DELETE
Access-Control-Allow-Headers: Origin, X-Requested-With, Content-Type, Accept, X-XSRF-TOKEN
Access-Control-Max-Age: 3600
```

Access-Control-Allow-Credentials allows the passing of a session cookie. 
The Access-Control-Allow-Origin lists the domains that are allowed to access the API.
The Access-Control-Allow-Methods list the permissible HTTP Methods.
Access-Control-Allow-Headers informs the browser of the allowed headers to send.
Finally Access-Control-Max-Age informs the browser on how long the information in the response may be cached.

*Do not* use the wildcard on Access-Control-Allow-Origin unless the API must be accessible to the world.  
```
Access-Control-Allow-Origin: *
``` 

### Use browser security response headers.

Various [headers](https://www.owasp.org/index.php/List_of_useful_HTTP_headers) exist to prevent abuse of resources in and from the browser. 

```
X-Frame-Options: deny
X-XSS-Protection: 1; mode=block
X-Content-Type-Options: nosniff
```

The `X-Frame-Options` header disallows the contents to be displayed in an IFrame. The `X-XSS-Protection` header makes the browser apply a filter to the URL to prevent reflected Cross Site Scripting. Finally, the `X-Content-Type-Options` header disallows the browser from guessing the content type. 

### Disable caching

In general responses from Rest API's should not be cached. This is partly because the data returned may be sensitive and also because the data must be fresh (so not an old copy of the data). For this various headers must be set: 

```
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
```
The HTTP/1.1 Cache-Control header can hold multiple values. The value `no-cache` tells the cache that the response must be revalidated. `no-store` tells the cache that the contents may not be stored on disk. The `max-age` tells the maximum age of the response before it must be retrieved again. Finally, `must-revalidate` demands that the liveness of the data is always checked at the server.

The `Pragma: no-cache` header forces revalidation of resources for old HTTP/1.0 proxies.

The `Expires` header field gives the date/time after which the response is considered stale. If its value is 0 its always stale and triggers a refresh. 


### Use strict transport security

The `Strict-Transport-Security` header makes sure that a resource is only accessible over HTTPS, preventing tools such as [SSLStrip](http://www.thoughtcrime.org/software/sslstrip/) from working.

```
Strict-Transport-Security: max-age=... ; includeSubDomains
```

The `max-age` parameter tells how long the domain must be accessed using HTTPS before it may be accessed over HTTP again. The `includeSubDomains` flag tells that this is true for all subdomains as well. 

## Output

### Use Data Transfer Objects to map API output

The entities of the service layer that are exposed by the API may hold internal details (such as password hashes) that should not be exposed to the outside world. Use separate Data Transfer Objects (DTO's) that represent the output of the service. There are various frameworks that make mapping entities to DTO's easy (try [Beanmapper](https://github.com/42BV/beanmapper)).

## Input

### Use Data Transfer Objects to map API input.

Incoming JSON structures frequently contain additional fields because of programming error, framework issues or malicious intent. That is why these must be mapped to an intermediate Data Transfer Object (sometimes called a Form) before applying it to the entity for which it is intended. Again, frameworks such as [Beanmapper](https://github.com/42BV/beanmapper) make this easy.

### Validate path and request parameters.

All path and request parameters should be validated and checked for sanity. For example a `pageSize` parameter used when paginating must stay at a modest level to prevent returning a huge response. 

### Validate Data Transfer Objects

All fields in the Data Transfer Objects should be validated. This is most easily done using a framework such as [Hibernate Validator](http://hibernate.org/validator/).

### Check Object Access Permissions 

In a typical CRUD system, access is granted on a collection (or type) basis. So if a user can edit one object of a type, its possible to edit them all. 
This coarse grained access mechanism is not suitable if a resource is owned by a specific user. If this is the case additional checks must be made at the service layer.

### Validate Strings 

An innocent looking unvalidated String field such as 'name' may contain any character, including newline's null char's and exotic symbols. These characters may have interesting effects in downstream (legacy) systems or libraries. (such as [JavaMail Header Injection via Subject](http://www.csnc.ch/misc/files/advisories/CSNC-2014-001_javamail_advisory_public.txt)). In general, only allow characters that have meaning for the field and reject any unwanted ones (especially those below ASCII value 32) and limit the length of the field.

### Filter HTML 

If the application allows rich HTML editing make sure that the server side sanitizes the HTML. Various libraries exist to do this, for example [JSoup](http://jsoup.org/cookbook/cleaning-html/whitelist-sanitizer). If you just want to block unsafe HTML you can use the `SafeHtml` annotation from hibernate-validator.

# Implementing the Guidelines with Spring Security

Spring Security 4 has a lot of sensible defaults which make implementing these guidelines relatively easy. Accompanied with this chapter is a working example web application which implements the recommendations.    

## Use HTTPS

In order to use HTTPS you need to configure your web container with a certificate. 
When developing a self-signed certificate is sufficient, for production purposes a certificate signed by a certificate authority is required.

The Tomcat7 website has a good [how-to](https://tomcat.apache.org/tomcat-7.0-doc/ssl-howto.html) for a self-signed certificate and also detailed instructions for a production certificate.

Creating a self-signed certificate in a keystore for development can be done using the Java `keystore` command; enter the following and follow instructions.
```bash
keytool -genkey -alias tomcat -keyalg RSA -keystore keystore.jks
```
It will provide you with a `keystore.jks` containing the certificate. 

You will need it if you want to configure your development environment:
* The [tomcat7-maven-plugin](https://tomcat.apache.org/maven-plugin-2.0/tomcat7-maven-plugin/run-mojo.html) is configured using the httpsPort, keystoreFile and keystorePass properties. 
* Spring-Boot allows [configuration via properties](https://docs.spring.io/spring-boot/docs/current/reference/html/howto-embedded-servlet-containers.html#howto-configure-ssl).  

You can set Spring security to accept only HTTPS traffic with the following configuration snippet: 
```java
@Configuration
public static class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.requiresChannel().anyRequest().requiresSecure();
    }
}

```
This will also activate automatic redirects from HTTP to HTTPS, secure cookies and Strict-Transport-Security header. However, Strict-Transport-Security is applied for the whole domain so if the domain the Rest API runs on is not fully HTTPS you may want to disable this header:  

```java
http.headers().httpStrictTransportSecurity().disable();
```

## API for Browser based consumption 

### Headers

The Spring Security defaults are very sensible, [fully documented in the reference manual](http://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#headers) 
and match those in this convention. For an Rest API consumed by a browser, this is all that is needed.

For an API that is accessed by a browser on a different domain, Cross Origin Resource Sharing (CORS) headers are required. The blog post and tutorial [CORS with Spring MVC](http://dontpanic.42.nl/2015/04/cors-with-spring-mvc.html) gives you all the details. 

Finally, if your WAR is also serving the single page web-application accessing the API, you should consider also sending appropriate [Content-Security-Policy](http://www.html5rocks.com/en/tutorials/security/content-security-policy/) (CSP) headers to prevent Cross Site Scripting. Additional headers are easily added in the [configuration](http://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#headers-static).

### Authentication

Authenticating to a REST API is not something Spring Security offers out of the box. Fortunately there are sufficient hooks to add this easily. 
You can use the extension points of the form based authentication, which is described in [great detail here](https://dzone.com/articles/secure-rest-services-using), or you can create your own RestAuthenticationFilter. Which is what will be explained here. All classes presented are in the example web application.

Lets start with a quick outline of the Spring Security Framework.  

Below there is high level diagram of a web application using Spring-Security. The browser sends HTTP requests which are passed through the Spring Security Filter Chain. If the request is an authentication request, Spring Security uses the Authentication Manager to validate the credentials. The manager uses the PasswordEncoder to verify the password and the UserDetailsService to obtain the user matching the username (wrapped in a UserDetails instance). If authentication is successful, the Authentication is stored in this sessions Security Context and can be accessed using the Security Context Holder which is a static singleton allowing the current user to be accessed from anywhere in the application.

Also part of the chain is functionality that implements the access restrictions on URLs and methods that are configured in Web MVC Security. After passing the chain, the request actually enters the controller. The controller may invoke (preferably one) service methods. These service methods can be protected by method-security, interceptors that check if the current Authentication is permitted to execute the method call.    

```
+-----------+
| Browser   |
+-----------+
  | Http  |                                  
+------------------------------------+------------------+
| Spring Security Filter Chain       | SecCtx Holder    |
+------------------------------------+------------------+-----------------------+-----------------+
|                                    | Security Context | Authentication Manager| PasswordEncoder |
+-----------+   Web MVC Security     |                  |                       +-----------------+
| Controler |                        |                  |-----------------------|
+-----------+------------------------+                  | UserDetailsService    |   +------------------------+
  |Method |                          |                  |                       |---| Principal Service      |
+------------------------------------+ +--------------+ |-----------------------+   +------------------------+
|                                    | |Authentication|-| UserDetails           |---| User                   |
+-----------+  Method Security       | +--------------+ |                       |   +------------------------+
| Service   |                        |                  |-----------------------+
+-----------+------------------------+------------------+
| Repository|
+-----------+ 
  |  SQL  |  
+-----------+
| Database  |
+-----------+
```

The minimum for an web application to work with Spring Security is to implement the UserDetailsService and UserDetails class. In this implementation the RestAuthenticationFilter (to login using JSON) and the AuthenticationController (to retrieve the current user) will be added. But first, the Principal Service:

#### PrincipalService

The `PrincipalService` implements the functionality that is needed to authenticate the user and to record failed and successful login attempts. Since it only accesses the UserRepository, one could ask why is this not called the UserService. The simple answer is that there is already a UserService which deals with all other User related activities, such as creating a new user. This UserService is secured Spring Security annotations which introduces a circular dependency if that service would also be required by Spring Security itself. That is why there is a separate PrincipalService that deals only with authentication (and is not secured by annotations).

The minimal requirement for the PrincipalService is that it can find a user given its user name (in this case, an email address). Also, there are two callback methods that update statistics in the User object itself, one for registering failed logins and one for registering successful logins. This allows the implementation of a temporary lockout mechanism to prevent password brute forcing. More about that later.   

#### SpringUserDetailsService

The `SpringUserDetailsService` gives Spring Security access to a mapped version of our domain specific User object. It uses the PrincipalService to obtain it and then maps it to UserDetails using the UserDetailsAdapter. Notice the four boolean flags that allow account expiration and locking, credential expiration and enabling the account. The flag for locking the account is used here to temporarily block authentication attempts. The enable flag is used to allow the administrator of the system to disable a user.      

#### User Lockout Logic

The `User` object has code that locks out a user for one minute after 5 failed attempts. After one minute the user gets another 5 attempts. This effectively reduces the maximum number of login attempts to 5 per minute making brute-forcing impractical.  

#### AuthenticationController

In order to authenticate, its useful to have a REST endpoint that can tell as whom the current user is authenticated and what its authorities (Roles) are. This enables the GUI frontend to alter its appearance accordingly and since the question also can be asked when the user isn't authenticated its useful to obtain a XSRF token as well. This endpoint is called the AuthenticationController.

The `AuthenticationController` leverages the PrincipalService to obtain the details of the current User and map it to a Data Transfer Object (DTO). Both the GET and POST methods return the current authenticated user. The GET is used for retrieval, the POST is the result part of the actual authentication attempt (implemented in a filter). 

#### RestAuthenticationFilter

The `RestAuthenticationFilter` is responsible for parsing the Login JSON and offering it to the authentication manager. It will be part of the Spring Security Filter Chain. If the login is successful the authentication is set on the SecurityContextHolder, if not a LoginError JSON message is sent back with appropriate status code and headers. 

As all requests pass this filter it also needs to check if the current request matches the authentication URL. This is what the matcher is used for. If the matcher does not match, the request is sent up further up the filter chain. If the request matches and authentication is successful the same happens. Only when authentication fails, the filter chain is aborted.

Also notice the markLoginSuccess and markLoginFailed calls to the PrincipalService which are used to temporarily lock an account when too many incorrect login attempts have taken place. 

### Logout

Logging out invalidates the session. The default behavior of Spring Security when a logout request is received is to redirect to the login page. This is not suitable for REST APIs. A simple 200 OK response suffices. This is easily configured using the `HttpStatusReturningLogoutSuccessHandler` which returns this by default. Also we need to match a DELETE on the authentication URL as a logout, which is done using a RequestMatcher. 

```java
protected void configure(HttpSecurity http) throws Exception {
  http.
    ...
    .logout()
      .logoutRequestMatcher(new AntPathRequestMatcher("/authentication", HttpMethod.DELETE.name()))
      .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
```   

### XSRF Protection

Spring Security supports XSRF protection out of the box but not in a way suitable for REST API's.
By default the token is exposed via the a JSP page and the token is verified back the using a form parameter or request header.   
For REST API's its common to read the token from a cookie and to send it back in a request header. 

Fortunately its easy to customize Spring Security. I will follow the method described [here](https://spring.io/blog/2015/01/12/the-login-page-angular-js-and-spring-security-part-ii) with a few modifications. The sample code will follow the [AngularJS defaults](https://docs.angularjs.org/api/ng/service/$http#cross-site-request-forgery-xsrf-protection).

#### XSRF Filter

The XSRF Token (which Spring Security calls CsrfToken) is made available by Spring Security as a request attribute. If the token is present, the request is checked if it contains a cookie with the same token. If it is not, a new cookie with the token will be set on the response. Notice that the secure flag is set on the cookie if the request was made via HTTPS and that the Path of the Cookie is set to the root of the application. Both prevent exposure of the token outside the context of the application.

```java
public class XsrfHeaderFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    if (csrf != null) {
      Cookie cookie = WebUtils.getCookie(request, "XSRF-TOKEN");
      String token = csrf.getToken();
      if (cookie==null || token!=null && !token.equals(cookie.getValue())) {
        cookie = new Cookie("XSRF-TOKEN", token);
        cookie.setSecure(request.isSecure());
        cookie.setPath(StringUtils.isEmpty(request.getContextPath()) ? "/" : request.getContextPath());
        response.addCookie(cookie);
      }
    }
    filterChain.doFilter(request, response);
  }
}
```

Also the filter must be added to the Spring Security Filter Chain:

```java
protected void configure(HttpSecurity http) throws Exception {
    http.
      ...
      .addFilterAfter(new XsrfHeaderFilter(), CsrfFilter.class);
}
```

#### Verifying the XSRF token	

For each modifying request (such as POST) Spring Security expects the XSRF token. The defaults don't match those of AngularJS which sends the token as a `X-XSRF-TOKEN` header (Spring Security expects an `X-CSRF-TOKEN` header) so some slight configuration is required.  

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
  http.
    ...
    .csrf().csrfTokenRepository(csrfTokenRepository());
}

private CsrfTokenRepository csrfTokenRepository() {
  HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
  repository.setHeaderName("X-XSRF-TOKEN");
  return repository;
}
```

### Validating Strings

An example `BasicStringValidator` and `@BasicString` annotation have been provided which will reject any control characters.  
For more information on writing custom hibernate-validator validations read the [reference documentation] (https://docs.jboss.org/hibernate/validator/5.0/reference/en-US/html/validator-customconstraints.html). 

# Further reading

* [OWASP REST Security Cheat sheet](https://www.owasp.org/index.php/REST_Security_Cheat_Sheet)
* [CORS with Spring MVC](http://dontpanic.42.nl/2015/04/cors-with-spring-mvc.html)
* [Angular JS and Spring Security](https://spring.io/guides/tutorials/spring-security-and-angular-js/)
* [Angular JS and Spring Security Part II](https://spring.io/blog/2015/01/12/the-login-page-angular-js-and-spring-security-part-ii)
* [Spring Security CSRF Reference](https://docs.spring.io/spring-security/site/docs/current/reference/html/csrf.html)
* [How to Hack an API and get away with it](http://blog.smartbear.com/readyapi/api-security-testing-how-to-hack-an-api-and-get-away-with-it-part-1-of-3/)
* [Understanding HTTP Strict Transport Security](http://www.troyhunt.com/2015/06/understanding-http-strict-transport.html)
* [Secure REST Services using Spring Security](https://dzone.com/articles/secure-rest-services-using)
* [OAuth 2.0 v.s. Json Web Tokens](http://www.seedbox.com/en/blog/2015/06/05/oauth-2-vs-json-web-tokens-comment-securiser-un-api/)
