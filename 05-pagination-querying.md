# Pagination & Querying

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
