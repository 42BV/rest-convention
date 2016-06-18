# JSON object design

Sometimes you don't only want to have the data you asked, but also the related data. So not only a bookcase, but also all the books in it. That can be a lot. How do we handle this?
There are a few options, also depending what on is common in your domain. 


We have three ways to cope with relations:

* ID
* Link
* Nested


## ID


if we request:
> /employees/42
We get back:

> ids[] 
>     {
>     23,
>     24,
>     25
>     }

if we request:
> /employees/42/ids
> 
We get back:
> ids[] 
>     {
>     23,
>     24,
>     25
>     }

if we request:
> /employees/42/ids/23

We get back:
>    { "name" : "Michel Greve",
>      "programminglanguages: "ids[]{1,2,3,4,5,6 }" 
>    }

We can request again:
> employees/42/ids/23/programminglanguages/1

We get back:
>    {
>        "id":"1",
>        "name": "6809 assembly"
>    } 


## Link

We request:
>/employees/42

and we get back:

>    {
>        [
>            "href" : "/employees/42/ids/23",
>            "href" : "/employees/42/ids/24",
>            "href" : "/employees/42/ids/25",
>            "href" : "/employees/42/ids/26"
>        ]
>    } 

We can request again:
> employees/42/ids/23/programminglanguages

And we get back:

>    { 
>        [
>            "href" : "/employees/42/ids/23/programminglanguages/1",
>            "href" : "/employees/42/ids/23/programminglanguages/2",
>            "href" : "/employees/42/ids/23/programminglanguages/3",
>            "href" : "/employees/42/ids/23/programminglanguages/4"
>            "href" : "/employees/42/ids/23/programminglanguages/5",
>            "href" : "/employees/42/ids/23/programminglanguages/6"
>        ]
>    }


## Embedded

if we request:
> /employees/42

We get back:
>    { 
>        [
>            {
>                "name" : "Michel Greve",
>                "programminglanguages": 
>                    [
>                        Java, 
>                        Python, 
>                        C++, 
>                        Delphi
>                    ]
>            },
>            {
>                "name" : "Someone Else",
>                "programminglanguages": 
>                    [
>                        Java, 
>                        Python
>                    ]
>            }
>            
>      ] 
>    }

You can see the problem with embedding, when do we stop with embedding and how can we prevent that the data that is returned isn't huge.


You don't always need all the embedded data. Remember your a system delivering a service. You don't know if you're servicing a front-end or another system.
So we have to ask ourselves, do we want the embedded data now or can the system also do an extra request for the needed data.


Single relation: Embed the relation directly in JSON

Few relations: Embed the relation or use id's or links

For all relations: implement an "expand" like keyword
---------------------
You can give the "expand" keyword to expand a part of the relation:
> /employees/42/ids/23?expand=address

We get back:

>    { 
>        "name" : "Michel Greve",
>        "address": 
>            {
>                "street": "Burgemeester Nederveenlaan",
>                "number": "23",
>                "postcode": "2761 VJ",
>                "city": "Zevenhuizen"
>            }
>    
>        "programminglanguages: "ids[]{1,2,3,4,5,6 }" 
>    }

You see that only the address is expanded. This gives a better control on the data you request.


# The silver bullet

We don't have any. You have to think for yourself. 

* Give the minumum amount of data back that in most cases don't give an extra call for the extra data.
* Only give data back instead of ID's/links, if people/systems more often than not need that data.
* Use "expand" to let the server know that you really want that data now.



# Links

1. http://blog.stateless.co/post/13296666138/json-linking-with-hal/

2. https://stormpath.com/blog/linking-and-resource-expansion-rest-api-tips/
3. http://jsonapi.org/format/
4. https://phlyrestfully.readthedocs.org/en/latest/ref/embedding-resources.html
5. http://martinfowler.com/articles/richardsonMaturityModel.html
6. https://stackoverflow.com/questions/21306047/how-to-expose-rest-api-hal-format-pagination
7. http://venkat.io/posts/expanding-your-rest-api/
8. http://blog.mwaysolutions.com/2014/06/05/10-best-practices-for-better-restful-api/
9. [GOOGLE]:https://google.github.io/styleguide/jsoncstyleguide.xml

