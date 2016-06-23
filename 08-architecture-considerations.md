# Architecture Considerations

## Versioning

There are basically 4 common approaches to versioning an API: 
* don't, (`/api/answer/42`)
* use a version in the URL, (`/api/v1/answer/42`) 
* use a Custom request header (`X-API-VERSION: v1`) with the unversioned url
* or put the version in the `Accept` header (`Accept: application/vnd.myapi.v1+json`) with the unversioned url.

The first and second approach are simple and convenient. 
But the first (no version) is not always possible and the second is often declared semantically incorrect because it doesn't actually address a resource.
The third and fourth option rely on the setting of custom headers which of course is easy in code but less convenient when exploring urls in a browser (although you can get a lot done with plugins such as [Modify Headers](https://addons.mozilla.org/en-US/firefox/addon/modify-headers/) ).

In this convention we have chosen for simplicity above semantical correctness. Which leads to the following recommendations for versioning:

### Don't version if possible

If the API has only one web-client which is part of the project there is no need for versioning the API as both artifacts can be upgraded at the same time.

### Use a version in the root of the API if needed

Sometimes versioning is needed, for example if there are multiple clients that cannot be upgraded at the same time. 
This typically happens with IOS and Android native Apps because the user decides when the upgrade is applied. 
Also it takes time for an IOS App to be approved by Apple. 
This requires a roll over scenario where both APIs are supported at the same time, thus a version is needed to differentiate the two.  

The version of the API must immediately follow the API root and start with a `v` followed by the version number, like in the following examples:
```
/api/v1/answer/42
/api/v1.1/answer/42
/api/v3.2/answer/42
/api/v5/answer/42
```
The version number itself should be restricted to the MAYOR.MINOR format (as a patch should not introduce breaking changes).
If the minor version is 0 is may be omitted. 

### Only change version when necessary

Not all API changes require a version upgrade. 
Typically additions (new URLs) to the API can be handled transparently without an version upgrade.
The same applies for optional fields.

The version number should only be incremented when a breaking API change is introduced.

### Use URL Rewriting to avoid controller duplication

You can use [URL rewriting](https://github.com/paultuckey/urlrewritefilter) to map old urls to new controllers or vice versa. 
This can be useful if only a subset of the API was changed but you want the full API available on the newly versioned endpoint.

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

Why not take an other existing standard? That is a fair question and it's also what we started out with. 
However we could not find any standard that was both complete and written with a Spring/Angular implementation in mind.

Some of the existing standards and best practices we evaluated and were inspired by: 

* [JSON API](http://jsonapi.org/format/)
* [Best practices for a pragmatic restful api](http://www.vinaysahni.com/best-practices-for-a-pragmatic-restful-api) 
* [10 Best practices for a better restful api](http://blog.mwaysolutions.com/2014/06/05/10-best-practices-for-better-restful-api/)
* [RESTFul best practices v1.1](http://www.restapitutorial.com/media/RESTful_Best_Practices-v1_1.pdf)
* [Design Beautiful REST + JSON APIs](http://www.slideshare.net/stormpath/rest-jsonapis)

And then there was [OData](http://www.odata.org/), an OASIS approved standard, which has a very deviant interpretation of what a REST API should look like. We didn't use that.  
