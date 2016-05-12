# Frequent scenarios

## Introduction

Many use cases can be captured in a small set of scenarios. This chapter aims to illustrate this small set and get you up and running as soon as possible with the REST convention. 

## HTTP Response Status codes

The scenarios described center around the [HTTP Response Status Codes][http-status-codes]. 

We will use the following codes:
* [200][]; the expected response, ie the happy flow scenario
* [301][]; request for the client to retrieve the resource from another location
* [304][]; request for the client to utilize its cache
* [400][]; an error was encountered that can be fixed by the client
* [401][]; the client has not been authenticated
* [403][]; the client tried to access a resource for which it was not authorized
* [404][]; the requested resource could not be found
* [500][]; the server encountered an error from which it cannot recover

## Scenarios

[200](### 200 / Happy flow)

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

...


### 301 / Redirected


[304](### 304 / Apply cache)

[400](### 400 / Validation error)

[401](### 401 / Unauthenticated)

[403](### 403 / Unauthorized)

[404](### 404 / Not found)

[500](### 500 / Critical error)


[http-status-codes]: https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html