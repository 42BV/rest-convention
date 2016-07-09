# Basic URL Design

This chapter will show you how to design the 
REST URL's for a specific resource. 

It will not cover the JSON structure of the
response and request bodies. This topic is
covered in chapter: JSON Object Design.

Here is a quick cheat sheet for a 'car' resource:

| URL           | method | response         | Request body  | Idempotent    | Read-Only |
| ------------- |--------| -----------------|---------------|---------------|-----------|
| /cars         | [GET](https://tools.ietf.org/html/rfc7231#section-4.3.1)    | A list of cars   | NO            | YES           | YES       | 
| /cars/1       | [GET](https://tools.ietf.org/html/rfc7231#section-4.3.1)    | A single car     | NO            | YES           | YES       | 
| /cars         | [POST](https://tools.ietf.org/html/rfc7231#section-4.3.3)   | The created car  | YES           | NO            | NO        | 
| /cars/1       | [PUT](https://tools.ietf.org/html/rfc7231#section-4.3.4)    | The updated car  | YES           | YES           | NO        | 
| /cars/1       | [PATCH](https://tools.ietf.org/html/rfc5789#section-2)  | The updated car  | YES           | NO            | NO        | 
| /cars/1       | [DELETE](https://tools.ietf.org/html/rfc7231#section-4.3.5) | The deleted car  | NO            | YES           | NO        | 

Note: A request method is considered "idempotent" if the intended effect 
on the server of multiple identical requests with that method is the
same as the effect for a single such request. For example sending the
same PUT request twice does nothing to the resource so it is idempotent.

# Anatomy of a Resource

A REST API is a collection of resources. A Resource
represents something in the domain of your application.
For example: a domain for a webshop will contain
Stock, Product and Customer resources.

A resource is something that can be manipulated.
For example we can add, remove and update a resource.

A resource can also be queried: we can get a list of
a particular resource, but we can also retrieve the details 
of a specific resource.

Before REST we would create specific URL's for each
operation or query type. One URL to request a list,
one to delete a resource etc etc. With REST we define
one 'URL' for a resource and use verbs to manipulate
a resource.

So what are these 'verbs'. 

# Request Methods
HTTP supports multiple 'requests methods' for a single
URL. Request methods are the 'verbs' which specify what 
you want to happen when you request that url.

The Request Methods that REST commonly use are: GET, POST, PUT, 
PATCH and DELETE. These are all specified in the 
[HTTP standard](https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.3) 
and act as a 'type' for a HTTP request.

When you put a Request Method (verb) before a Resource something
magical happens: You an probably guess what the request `DELETE /cars/1` 
does by looking at the Request Method.

Lets look at all Request methods and what they do.

## GET
The GET request method is the method your browser uses to
retrieve the contents of a web page. 

It is no wonder that the GET method is used in REST to get both the
single or a list of resources.

The list of resources is fetched using the following URL:

`GET /{entity}`

A singular resource is fetched via:

`GET /{entity}/{identifier}`

The relationship between a singular and list looks the same
as a folder / sub folder in a file system. This is intentional
and is used to denote that the singular resource is part of 
a larger list of resources.

The name of the entity is __always__ in plural. So if we have
a Car resource the urls would become:

`GET /cars` for the list of cars and `GET cars/1` for a single car.

## POST
The POST request method is what your browser uses when you
submit an HTML form.

In REST we use the POST method to 'create' new resources. The URL
for the POST request is the same as the list request. This is to
denote that we are 'creating' a new resource which belongs to that
list.

The request is defined as:

`POST /{entity}`

For example, to create a new car we would send:

`POST /cars`

## PUT
The PUT request updates an existing resource.

The request is defined as:

`PUT /{entity}/{identifier}`

For example, to update the car with identifier 1 we would send:

`PUT /cars/1`

Sending a PUT request updates the 'entire' entity. Put
overwrites a singular resource in its entirety. This
means that you cannot leave fields 'blank' as they will
be overwritten. For partial updating see PATCH.

Not that the PUT request is 'idempotent'. This means that
no matter how many times you send the same request the
result should be the same.

## PATCH
The PATCH request method is much the same as PUT. The big
difference is that PATCH is for partial updates. It only
updates the fields that you sent to it.

In practice you can use PATCH to for example only update
the license plate for a car but leave the other properties
intact.

The request is defined as:

`PATCH /{entity}/{identifier}`

For example, to partially update the car with identifier 1 we would send:

`PATCH /cars/1`

## DELETE
DELETE is used to delete a resource.

The request is defined as:

`DELETE /{entity}/{identifier}`

For example, to delete the car with identifier 1 we would send:

`DELETE /cars/1`
