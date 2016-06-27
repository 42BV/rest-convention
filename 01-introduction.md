# Introduction

## REST's origin story

In 1989 a man by the name of [Tim Berners-Lee](https://en.wikipedia.org/wiki/Tim_Berners-Lee) 
started working on the [HTTP](https://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol) 
protocol. HTTP is a request / response protocol, it defines a way for two computers
to communicate with each other. This led to the world wide web as we
know it.

In the early days of the web, the HTTP protocol was not really used
to its full extent. We mostly used [GET](https://tools.ietf.org/html/rfc7231#section-4.3.1) 
request to retrieve pages, and the [POST](https://tools.ietf.org/html/rfc7231#section-4.3.3) 
request to manipulate data. 

This way of using POST for data manipulation is called a Remote Procedure Call 
or [RPC](https://en.wikipedia.org/wiki/Remote_procedure_call). RPC is 
very simple, but not very standardized, two RPC applications have
nothing in common. In RPC designing request and responses is up mostly
up to the individual programmer.

Also some people did not think that HTTP was sufficient enough to create
complex applications. So they invented things like [CORBA](https://en.wikipedia.org/wiki/Common_Object_Request_Broker_Architecture) 
and [SOAP](https://en.wikipedia.org/wiki/SOAP).

Then a man by the name of [Roy T. Fielding](http://roy.gbiv.com/) showed
us the light. Turns out that there is more to HTTP than just POST and GET,
there is also: [PUT](https://tools.ietf.org/html/rfc7231#section-4.3.4), 
[PATCH](https://tools.ietf.org/html/rfc5789) and [DELETE](https://tools.ietf.org/html/rfc7231#section-4.3.5).

Mister Fielding defined something he called REpresentational State Transfer,
more commonly known as REST. The idea is to defined one URL for one particular
resource and use GET, PUT, POST, PATCH and DELETE to manipulate that
resource.

REST is better than RPC because two REST applications look very similar.
Once you know the URL of the resource you know how to manipulate the
resource. REST is better for web applications, than CORBA and SOAP, because 
it is directly supported in the browser, since REST is simply HTTP.

In conclusion REST is pretty awesome.

## A REST convention

This document represents the REST convention for the Dutch company 42.
With this document we want to give you guidelines on how to design a
REST API. We hope that by having a REST convention that the API's that
we will produce are of a higher quality. 

Also this convention may serve as a learning tool for beginning programmers.
That is why we have chosen to make this document [Spring MVC](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html) 
and [AngularJS](https://angularjs.org/) centric. This way our new recruits 
and interns can get up and running as quickly as possible. We try to 
explain the general principles behind REST first and then show the 
convention in practice.

## A reading guide

This document has multiple chapters: Chapter two is a gentile introduction
into basic URL design, here we show and define our first resource. It
also explains when to use PUT, POST, GET, DELETE or PATCH.

Chapter three takes us one step further: REST resources can have relationships
between each other. For example one bookshelf can contain many books.
Chapter three JSON Object Design shows you how to model these relationships.

Chapter four is all about HTTP Status Codes. When everything goes OK
we send 200, but what do we send when things go haywire? Chapter four
answers these questions.

Chapter five: Pagination and Filtering, shows how to limit the number
of objects using pagination, and how to limit the data using filtering. 

Chapter six is all about Security. Security is a cross cutting concern:
it does not really belong in a REST convention but is to important to 
ignore. This chapter shows you how to setup [Spring Security](http://projects.spring.io/spring-security/) 
and gives you some guidelines on other concerns such as HTTPS and CSRF
tokens.

Once you have a REST API it would be nice to document it, this way your
Java-less teammates know how to use your API. This is what Chapter seven: 
Documentation is about. It shows you how to setup Spring in such a way 
that it generates the documentation for you automatically.

Chapter eight: Architecture Considerations is mostly about what we
did not include in this document and why.

## Reference implementations

This convention also contains a sample application. It has two parts
a front-end written in AngularJS and a back-end written with Spring MVC.
They serve as useful places to steal code from, but also as places where
the REST convention comes to life.

The application that was implemented contains .... (NOTE EXPAND THIS
SECTION ONCE THE REF IMPLEMENTATION IS COMPLETE)