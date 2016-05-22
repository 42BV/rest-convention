# Pagination, Querying and Sorting

## What is pagination?

Pagination of data is a way to limit the output of a list of resources to a well-defined (maximum) size.
This paginated list is provided to the consumer of the REST api as a result of a list operation or query.

Instead of returning a complete list of records, only a fraction of the data is returned with information necessary to retrieve the rest of the data.
The result is often wrapped in an envelope that contains the following data:
* The number of the current page
* The total number of pages
* The content of this page, which is a list of resources.

## Why paginate data?
Often the user of a web application is not interested in hundreds of resources.
During a search operation for example they typically specify some filters to the best of their knowledge so
the best results end up on top of the page.

Because the user is typically not interested in most of the resources present on the server pagination (in combination with a good search function)
can provide them the data they need without sending data over the line which the user doesn't ever look at.
This saves bandwidth and is especially important when the user is using a mobile application or
has a slow or unreliable internet connection. It helps to reduce the bandwidth needed to work with the application.

Another very important reason why pagination is used is to reduce the load on the server. The resource data is often stored
on the server in a data store of some kind. When the user requests a certain resource list this means that the server has to load that list from the
data store into memory. This is ok for lists of a controllable size, but when the number of resources on the server
grows it can become a large burden on the service and cause it to go out of memory.

This could happen by accident, for example if the user is not very restrictive in his search parameters (find all people in the database whose last name starts with a 'B') but
it is also a potential target for a DDOS attack.

If the resources are returned in a paginated list rather than an unrestricted one the number of resources the service has to load into memory can be restricted
to a sensible and controllable amount. This way the load on the service is reduced.

## When (not) to paginate data?
Because of the possible performance implications the default should be to use pagination _everywhere_ when returning a list of resources.

This is especially true when the a user has the opportunity to add elements to this resource that can also be requested in the list operation.
If the user is unlimited in the number of elements that can be added to the resource it means that it could be potentially thousands of records.
If pagination is not used all these records have to be loaded into memory when the list of elements is requested.

Even though this may conceptually not make any sense in your specific use case and a '_user would never, ever do that_' it _does_ give people with malicious
intents an opportunity to load  large amount of data into the application using an automated script and then requesting it again to cause out of memory errors.

Therefore, _the only_ time a list of elements can be returned without pagination is when the following conditions are met:
* The list of items is sufficiently small.
* The list of items is unable to grow large by user interaction.
* The list of items is unable to grow large by automatic operations.
* The items that are returned are sufficiently small themselves, meaning that they shouldn't contain large collections of subelements.

If you cannot _guarantee_ that these conditions will _always_ be true you should paginate the data that is returned in the REST API. When in doubt, paginate.

## How to do pagination?

Since any sizable request should use pagination by default, calling '/cars' will return the first page of cars found.

For example:

Request:

`
GET /cars
`

Response:

~~~~
{
	"content": [
	  {
		"make": "Audi",
		"model": "A1"
	  },
	  {
		"make": "Audi",
		"model": "A3"
	  },
	  {
		"make": "Audi",
		"model": "A4"
   	  },
   	  {
		"make": "Audi",
		"model": "A5"
	  },
	  {
		"make": "Audi",
		"model": "A6"
	  }
	],
	"number": 0,
	"size": 5,
	"first": true,
	"last": false,
	"totalElements": 100,
	"totalPages": 20,
}
~~~~

The result contains the following properties:

- Content: the actual elements in this page.
- Size: the number of elements in this page
- First: whether or not this is the first page
- Last: whether or not this is the last page
- Total elements: the total number of elements in all pages
- Total pages: the total number of pages spanning all elements

This information can be used to represent a paginated list to the user. Pages are zero indexed.

To request the next page, the following request can be executed:

Request

`
GET /cars?page=1
`

Response:

~~~~
{
	"content": [
	  {
		"make": "BWM",
		"model": "E46"
	  },
      {
        "make": "BWM",
        "model": "E60"
      },
	  {
		"make": "BWM",
		"model": "E65"
	  },
	  {
		"make": "BWM",
		"model": "E83"
   	  },
   	  {
		"make": "BWM",
		"model": "E85"
	  }
	],
	"number": 1,
	"size": 5,
	"first": false,
	"last": false,
	"totalElements": 100,
	"totalPages": 20,
}
~~~~

A query parameter can also be given to specify the number of
elements the API consumer wants back. This can be done using the 'size' parameter.
It is up for the implementor of the REST API to think of a sensible upper limit and enforce this.

## Sorting ##

When requesting a resource it may be useful for the user to apply sorting in order to find the required elements quicker.
This can be done by applying one or more 'sort' parameters to the request:

Request:

`GET /cars?sort=make`

Response:
~~~~
{
	"content": [
	  {
		"make": "Audi",
		"model": "A1"
	  },
	  {
		"make": "Audi",
		"model": "A3"
	  },
	  {
		"make": "Audi",
		"model": "A4"
   	  },
   	  {
		"make": "Audi",
		"model": "A5"
	  },
	  {
		"make": "Audi",
		"model": "A6"
	  }
	],
	"number": 0,
	"size": 5,
	"first": true,
	"last": false,
	"totalElements": 100,
    "totalPages": 20,
    "sort": [{
        "direction": "ASC",
        "property": "make"
    }]
}
~~~~

In the response, a 'sort' property is given. In this case, it applied sorting on the 'make' property in ascending order. This can
be overridden by specifying the sorting order in the following manner:

Request

`GET /cars?sort=make,desc`

Response

~~~~
{
	"content": [
	  {
		"make": "Volkswagen",
		"model": "Amarok"
	  },
	  {
		"make": "Volkswagen",
		"model": "Beetle"
	  },
	  {
		"make": "Volkswagen",
		"model": "Golf"
   	  },
   	  {
		"make": "Volkswagen",
		"model": "Polo"
	  },
	  {
		"make": "Volkswagen",
		"model": "Santana"
	  }
	],
	"number": 0,
	"size": 5,
	"first": true,
	"last": false,
	"totalElements": 100,
    "totalPages": 20,
    "sort": [{
        "direction": "DESC",
        "property": "make"
    }]
}
~~~~

The sorting on 'make' is now done in a descending order.

If you want you can also specify multiple 'sort' parameters. They are processed in order, for example:

Request
`GET /cars?sort=make&sort=model,desc`

Would first order the elements by make, and then by descending model.

Response:
~~~~
{
	"content": [
	  {
		"make": "Audi",
		"model": "Q7"
	  },
	  {
		"make": "Audi",
		"model": "Q6"
	  },
	  {
		"make": "Audi",
		"model": "Q5"
   	  },
   	  {
		"make": "Audi",
		"model": "Q4"
	  },
	  {
		"make": "Audi",
		"model": "Q3"
	  }
	],
	"number": 0,
	"size": 5,
	"first": true,
	"last": false,
	"totalElements": 100,
    "totalPages": 20,
    "sort": [
      {
        "property": "make",
        "direction": "ASC"
      },
      {
        "property": "model,
        "direction": "DESC"
      }
    ]
}
~~~~

## Querying ##

Most of the time the user is not interested to browse through a large paginated
list of all data present in the system. To allow the user to find information he/she is looking for,
it is possible to provide query parameters to narrow the results.

For example, if our Car resource has a make and a model and the user is only interested
in finding Hyundai cars, the following request is be make:

Request:

`GET /cars?make=hyundai`

Response:

~~~~
{
	"content": [
	  {
		"make": "Hyundai",
		"model": "Accent"
	  },
	  {
		"make": "Hyundai",
		"model": "Aero"
	  },
	  {
		"make": "Hyundai",
		"model": "Atos"
   	  },
   	  {
		"make": "Hyundai",
		"model": "Azera"
	  },
	  {
		"make": "Hyundai",
		"model": "Chorus"
	  }
	],
	"number": 0,
	"size": 5,
	"first": true,
	"last": false,
	"totalElements": 10,
	"totalPages": 2,
}
~~~~

The user can further narrow the search by applying more query parameters. For example:

`GET /cars?make=hyundai&model=a`

Could return all Hyundai cars for which the model name starts with an 'a'.

## Combining pagination, querying and sorting ##
The parameters to apply pagination, querying and sorting mentioned in the previous sections of this chapter
can be combined.

For example using the following request:

`GET /cars?page=1&make=hyundai&sort=model,desc`

Would return the second page of Hyundai cars and sort them by model in descending order.