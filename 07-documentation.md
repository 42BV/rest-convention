# Rest documentation

A work in progress.

# Motivation

With the separation of frontend and backend it has become of critical importance that the REST interface is clearly documented. It is possible that frontend and backend tasks are performed by different developers, which practically means that the frontender should know what requests to call and what output to expect.

Besides an overview of resources the documentation should also discuss cross cutting concerns, such as status codes, error handling and security. The REST document serves as contract, helping consumers of the interface and ensuring that the interface stays consistent and clear.

Writing REST documention takes time, but should be worth it in the process of a project. The document minimizes communication problems between developers and helps new developers become productive in a shorter time span.

In order to really benefit from a REST document it has to stay up to date. Outdated documentation can bring more problems than solutions. To keep the document actual we have selected certain tooling.

# Tooling

To write and maintain REST documentation in a minimal amount of time we use tooling.

## Swagger

[Swagger](http://swagger.io/) provides a Spring MVC addon that inspects all REST endpoints and generates an interface description. In combination with [Swagger UI](http://swagger.io/swagger-ui/) a visual test suite can be rendered, showing each controller and request. The requests can be invoked on demand, with parameter and request body support.

Additional documentation can be added by enhancing the controller methods with annotations:

	@ApiResponses(value = {@ApiResponse(code = 405, message = "Invalid input")})
	public .... createSomething(..)

## Spring REST docs

[Spring REST docs](http://docs.spring.io/spring-restdocs/docs/1.0.x/reference/html5/)

## Practice



# Further reading

* [Spring REST docs](http://docs.spring.io/spring-restdocs/docs/1.0.x/reference/html5/)
* [Swagger](http://swagger.io/)
* [Ascii doctor](http://asciidoctor.org/)
