# Architecture Considerations

## Versioning

There are basically 4 common approaches to versioning an API: 
* don't, (`/api/answer/42`)
* use a version in the URL, (`/api/v1/answer/42`) 
* use a Custom request header (`X-API-VERSION: v1`) with the unversioned url
* or put the version in the `Accept` header (`Accept: application/vnd.myapi.v1+json`) with the unversioned url.




## The Roads not Taken

The goal of this REST convention was to create a practical set of guidelines that can be used in day to day work as a sensible default. 
Something that is easily and commonly understood.
This also means that a lot of possible other standards, technologies and concepts were not chosen to be part of this convention.
This chapter explains which of those we examined and why we choose not to make them part of this convention[.](https://www.youtube.com/watch?v=tg7XSGNKySg)

### HATEOAS

[HATEOAS](https://en.wikipedia.org/wiki/HATEOAS), an abbreviation for *Hypermedia as the Engine of Application State*, encapsulates application state as links inside the response, much like an HTML page with links leading to another page.

This means that a client, given the applications initial access point, theoretically, can navigate and operate the application without any knowledge of the API itself.

For example, a regular JSON giving the ultimate answer would look like:

```json
{ 
    "answer" : "42"
}
```

The HATEOAS version of the JSON could look like:

```json
{
    "answer": "42",
    "links": [ {
        "rel": "self",
        "href": "http://localhost:8080/answer/1"
    } ]
}
```

So, for each domain object in the response you tell the client what it can do with it using named links. 
This eliminates the need for URL/path knowledge in the client at the expense of an extra layer of redirection on the client (you've got to know the link names).
On the server you get extra complexity because for each domain object returned you have to enumerate all possible actions the client can take.
Even when using the Spring `ControllerLinkBuilder` and `ResourceSupport` classes this increases the controllers size significantly.
 
HATEOAS is perfect if you want to use a generic user interface, driven by the named links and metadata or if you don't write your the client or have many client implementations.

In most of our projects however, we make one very specific custom client (the user interface) of which we have total control.

In our view, the complexity of adding HATEOAS to the API doesn't weigh up to its benefits. 

### Other standards

Why not take another existing standard? That is a fair question and it's also what we started out with. 
However we could not find any standard that was both complete and written with a Spring/Angular implementation in mind.

Some of the existing standards and best practices we evaluated and were inspired by: 

* [JSON API](http://jsonapi.org/format/)
* [Best practices for a pragmatic restful api](http://www.vinaysahni.com/best-practices-for-a-pragmatic-restful-api) 
* [10 Best practices for a better restful api](http://blog.mwaysolutions.com/2014/06/05/10-best-practices-for-better-restful-api/)
* [RESTFul best practices v1.1](http://www.restapitutorial.com/media/RESTful_Best_Practices-v1_1.pdf)
* [Design Beautiful REST + JSON APIs](http://www.slideshare.net/stormpath/rest-jsonapis)

And then there was (OData)[http://www.odata.org/], an OASIS approved standard, which has a very deviant interpretation of what a REST API should look like. We didn't use that.  
