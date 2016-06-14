# REST documentation

With the separation of frontend and backend it has become of critical importance that the REST interface is clearly documented. It is possible that frontend and backend tasks are performed by different developers, which simply means that the developers should know which requests to call and what kind of output to expect.

Besides an overview of resources the documentation should also discuss cross cutting concerns, such as status codes, error handling and security. The REST document serves as contract, helping consumers of the interface and ensuring that the interface stays consistent and clear.

Writing REST documention takes time, but should be worth it in the process of a project. REST documentation minimizes communication issues between developers and helps new developers become productive in a shorter time span. To really benefit from a REST document it has to stay accurate. Outdated documentation can bring more problems than solutions.

# Tooling

We use tooling to write and maintain the REST documentation in a minimal amount of time. In the sections below two tools are described and compared. Use the tool that best fits your situation.

## Swagger

[Swagger](http://swagger.io/) provides a Spring MVC addon that inspects all REST endpoints and generates an interface description. In combination with [Swagger UI](http://swagger.io/swagger-ui/) a visual test suite can be rendered, showing each controller and request. The requests can be invoked on demand, with parameter and request body support.

Actual documentation can be added by enhancing the controller methods with annotations:

	@ApiResponses(value = {@ApiResponse(code = 405, message = "Invalid input")})
	public .... createSomething(..)

Swagger is easy to integrate in projects but also has various drawbacks:

- *No cross cutting concerns.* Swagger only shows the requests, there is no way to define cross cutting concerns. One clear example is the usage of error codes. Each request shows a list of all possible error codes, most are clearly irrelevant for that request, but could confuse a frontend developer. The only way to overcome this problem is by heavily annotating your controller with Swagger annotations.
- *Grouping per controller not resource.* Sometimes a resource is split over various controllers for design reasons. Each controller is shown seperately, while you would rather see them as one resource.

## Spring REST docs

[Spring REST docs](http://docs.spring.io/spring-restdocs/docs/1.0.x/reference/html5/) helps to generate an acurate and readable REST document. Rather than a testsuite we will produce an actual document, using the text processor [Ascii doctor](http://asciidoctor.org/). In this document we can manually describe the cross cutting concerns and resources, allowing us to manage our own markup and chapters.

Frequently changing information, such as HTTP request details, are generated. Spring REST docs generates various snippets from unit tests written in [Spring MVC Test](http://docs.spring.io/spring/docs/current/spring-framework-reference/htmlsingle/#spring-mvc-test-framework). By using unit tests you ensure that the documentation is actual, otherwise the test will fail. Another nice side effect is that each request will be tested.

Ascii doctor allows us to include the generated snippets in our document, with synthax highlighting. This way the document is always acurate, while maintaining control over the document.

## Practice

Both tools are actively used in projects, but Spring REST docs is preferred. There is a clear difference between both tools. Swagger guesses how your controller should be used, while Spring REST docs allows you to demonstrate the usage in a unit test. Spring REST docs will result in a much clearer and relevant document, staying acurate during code changes.

# Document

When writing a REST document ensure the following subjects are discussed:
- Cross cutting concerns
- Resources: Introduction, Requests, Relation with other resources

We also recommend referencing to this REST convention, allowing various trivial subjects to be skipped.

## Example resource

To make this clearer we will provide an example resource description below.

### User

Represents a person that uses our system. Each user has a role, which is also a seperate resource.

**Get a user**

	GET /api/users/1

	{
		id: 1,
		email: developer@42.nl,
		role: {
			name: "ADMIN"
		}
	}

**Create a user**

	POST /api/users

	{
		email: developer@42.nl
	}

Name          | Description
 ------------ | -----------
Email         | Email adress of user
Phone         | Phone number of user *(optional)*

# Other example

Another good example of REST documentation is [this](https://developer.github.com/v3/git/commits/) where GitHub describes their *commit* resource of their REST api.

# Further reading

* [Spring REST docs](http://docs.spring.io/spring-restdocs/docs/1.0.x/reference/html5/)
* [Swagger](http://swagger.io/)
* [Ascii doctor](http://asciidoctor.org/)
* [Github](https://developer.github.com/v3/)
