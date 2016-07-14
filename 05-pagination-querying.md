# Pagination, Filtering and Sorting

## What is pagination?

Pagination of data is a way to limit the output of a list of resources to a well-defined (maximum) size.
This paginated list is provided to the consumer of the REST API as a result of a search operation on a certain resource.

Instead of returning a complete list of records, only a fraction of the data is returned with information necessary to retrieve the rest of the data.
The result is usually wrapped in an envelope that contains the following data:
* The number of the current page
* The total number of pages
* The content of this page, which is a list of elements.

## Why paginate data?
You should favor paginated data over returning a regular collection. The user might be able to specify a page size but it is important that an upper
 bound is set on the server.

There are several reasons why this is the case:
* **The user is typically not interested in seeing millions of records.**

    Most likely he/she is looking for one or a few specific records and browsing through the
search results to find them. Pagination (in combination with a good search function)
can provide them with the data they need without sending all of the other data over the line.
This saves bandwidth and is especially important when the user is using a mobile application or
has a slow or unreliable internet connection.

* **It reduces the load on the server.**

    The data the user is looking for is often stored
on the server in a data store of some kind. When the user requests a certain resource list this means that the server has to load that list from the
data store into memory. This is fine for lists of a controllable size, but when the number of elements on the server
grows it can become a large burden on the service and cause it to run out of memory.

    This could happen by accident, for example if the user is not very restrictive in his search parameters (find all people in the database whose last name starts with a 'B') but
it is also a potential target for a DDOS attack.

    If the data is returned in a paginated result rather than an unrestricted collection the number of resources the service has to load into memory can be restricted
to a sensible and controllable amount. This way the load on the service is reduced.

## When (not) to paginate data?
Because of the possible performance implications the default should be to use pagination _everywhere_ when returning a list of elements.

This is especially true when the a user has the opportunity to add elements to this resource that can also be requested in the list operation.
If the user is unlimited in the number of elements that can be added to the resource it means that it could be potentially millions of records.
If pagination is not used all these records have to be loaded into memory when the list of elements is requested.

Even though this may conceptually not make any sense in your specific use case and a '_user would never, ever do that_' it _does_ give people with malicious
intents an opportunity to save large amount of data in the application using an automated script and then requesting them again so the server
loads all of them into memory.

Therefore, _the only_ time a list of elements can be returned without pagination is when the following conditions are met:
* The list of items is sufficiently small.
* The list of items is unable to grow large by user interaction.
* The list of items is unable to grow large by automatic operations.
* The items that are returned are sufficiently small themselves, meaning that they shouldn't contain large collections of subelements.

If you cannot _guarantee_ that these conditions will _always_ be true you should paginate the data that is returned in the REST API. When in doubt, use pagination.

## How to do pagination?

Since any sizable request should use pagination by default, calling '/cars' will return the first page of cars found.

For example:

Request:

`
GET /cars
`

Response:

```json
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
```

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

```json
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
```

The user can also specify how many records he wants to retrieve. This can be done using the 'size' parameter.
It is up for the implementor of the REST API to think of a sensible upper limit and enforce this.

In a Spring MVC application you could achieve this by creating the following controller method:

```java
@RestController
@RequestMapping("/cars")
public class CarController {

  private final CarService carService;

  @Autowired
  public CarController(CarService carService) {
    this.carService = carService;
  }

  @RequestMapping
  public Page<Car> findAll(Pageable pageable) {
      return carService.findAll(pageable);
  }
}
```

When you call the endpoint without any additional parameters the page will default to 0, otherwise it takes the specified value.

You can use this response in your frontend to render a pagination component, for example using ui-bootstrap in Angular:

```JavaScript
angular.module('ui.bootstrap.demo').controller('CarController', function(carPage) {
  var carController = this;
  carController.carPage = carPage;
});
```

```HTML
<div ng-controller="CarController as carController">
    <uib-pagination
        total-items="carController.carPage.totalElements"
        items-per-page="carController.carPage.size"
        ng-model="carController.carPage.number"
        max-size="5"
        force-ellipses="true">
    </uib-pagination>
</div>
```

In the HTML above the max-size parameter decides how many page buttons will be shown at the same time and force-ellipses creates ellipses on the left or right
side to instantly jump to the previous or next page number not shown as a page button itself. See [this plunkr](http://plnkr.co/edit/yGjQUh?p=preview) to try it out.

In some applications the default parameter 'page' and 'size' parameters names used for pagination  might clash with other parameter names.
In that case it is possible in Spring MVC to specify a generic prefix that you want to provide in your requests by using
the PageableHandlerMethodArgumentResolver in the WebMvc configuration:

```java
@EnableWebMvc
@EnableSpringDataWebSupport
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
        resolver.setPrefix("pageable.");

        argumentResolvers.add(resolver);
    }

}
```

Now we've set the parameter prefix to 'pageable.'. We can now call the search method like this:

Request

`
GET /cars?pageable.page=1&pageable.size=20
`

## Sorting ##

When requesting a resource it may be useful for the user to apply sorting in order to find the required elements quicker.
This can be done by applying one or more 'sort' parameters to the request:

Request:

`GET /cars?sort=make`

Response:
```json
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
```

In the response, a 'sort' property is given. In this case, it applied sorting on the 'make' property in ascending order. This can
be overridden by specifying the sorting order in the following manner:

Request

`GET /cars?sort=make,desc`

Response

```json
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
```

The sorting on 'make' is now done in a descending order.

If you want you can also specify multiple 'sort' parameters. They are processed in order, for example:

Request
`GET /cars?sort=make&sort=model,desc`

Would first order the elements by make, and then by descending model.

Response:
```json
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
        "property": "model",
        "direction": "DESC"
      }
    ]
}
```

In a Spring MVC application you can achieve this goal by using the same controller method specified in the 'How to do pagination' section.
You can also specify a default search order if you want:

```java
@RestController
@RequestMapping("/cars")
public class CarController {

  private final CarService carService;

  @Autowired
  public CarController(CarService carService) {
    this.carService = carService;
  }

  @RequestMapping
  public Page<Car> findAll(@SortDefault("make") Pageable pageable) {
      return carService.findAll(pageable);
  }
}
```

Analogously to page parameter prefix you can also set a prefix for sort request parameters if the name clashes with parameters in your application.
We can do this by using SortHandlerMethodArgumentResolver and supplying it to the constructor of the PageableHandlerMethodArgumentResolver
in the WebMvc configuration:

```java
@EnableWebMvc
@EnableSpringDataWebSupport
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        SortHandlerMethodArgumentResolver sortResolver = new SortHandlerMethodArgumentResolver();
        sortResolver.setSortParameter("pageable.sort");

        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver(sortResolver);
        pageableResolver.setPrefix("pageable.");

        argumentResolvers.add(pageableResolver);
    }

}
```

Now we've set the sort parameter prefix to 'pageable.'. We can now call the search method like this:

Request

`
GET /cars?pageable.sort=model,desc
`

## Filtering ##

Most of the time the user is not interested to browse through a large paginated
list of all data present on the server. To allow the user to find information he/she is looking for,
it is possible to provide search parameters to narrow the results.

For example, if our Car resource has a make and a model and the user is only interested
in finding Hyundai cars, the following request is be make:

Request:

`GET /cars?make=hyundai`

Response:

```json
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
```

The user can further narrow the search by applying more search parameters. For example:

`GET /cars?make=hyundai&model=a`

Could return all Hyundai cars for which the model name starts with an 'a'.

Another possibility is supplying multiple values to search on the same parameter, for example if you wish to search for both
Audi's and Hyundai's. You can do that by adding the same search parameter to the url multiple times, like this:

Request:

`GET /cars?make=audi&make=hyundai`

Response:

```json
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
    }
  ],
  "number": 0,
  "size": 5,
  "first": true,
  "last": false,
  "totalElements": 5,
  "totalPages": 2
}
```

In a Spring MVC application you could achieve this by creating the following controller method:

```java
@RestController
@RequestMapping("/cars")
public class CarController {

  private final CarService carService;

  @Autowired
  public CarController(CarService carService) {
    this.carService = carService;
  }

  @RequestMapping
  public Page<Car> search(@RequestParam(name = "make", defaultValue = "") Set<String> makes) {
      return carService.findByMakes(makes);
  }
}
```

When a get to /cars is performed without specifying a 'make' parameter the set makes is empty. If you do put one or more 'make'
parameter in your request url the set is filled with those.


Specifying parameters on the controller method is useful for search operations with a small amount of parameters.
For search operations where you have a lot of parameters, you can specify them in a class instead, like this:

```java
public class CarSearchParameters {

    public Set<String> make = new HashSet<>();

    public CarController.Color color;

    public Integer buildMonth;
    public Integer buildYear;

    public Integer minimalCost;
    public Integer maximalCost;
}
```

You can then use this class as the parameter of the search method like this:

```java
@RestController
@RequestMapping("/cars")
public class CarController {

  private final CarService carService;

  @Autowired
  public CarController(CarService carService) {
    this.carService = carService;
  }

  @RequestMapping
  public Page<Car> search(CarSearchParameters carSearchParameters) {
    return carService.find(carSearchParameters);
  }
}
```

This sets the appropriate properties on a new CarSearchParameters instance.

## Combining pagination, filtering and sorting ##
The parameters to apply pagination, filtering and sorting mentioned in the previous sections of this chapter
can be combined.

For example using the following request:

`GET /cars?page=1&make=hyundai&sort=model,desc`

Would return the second page of Hyundai cars and sort them by model in descending order.

In a Spring MVC application you can easily combine these concepts in a controller method as well, for example:

```java
@RestController
@RequestMapping("/cars")
public class CarController {

  private final CarService carService;

  @Autowired
  public CarController(CarService carService) {
    this.carService = carService;
  }

  @RequestMapping
  public Page<Car> search(@RequestParam(name = "make", defaultValue = "") Set<String> makes, @SortDefault("make") Pageable pageable) {
      return carService.findByMakes(makes, pageable);
  }
}
```