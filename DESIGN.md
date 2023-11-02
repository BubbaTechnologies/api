Design Patterns 
---
    Written by Matthew Groholski
    Last Updated on November 1st, 2023
---
### Table of Contents
- [Introduction](#introduction-)
- [Unique Sections](#unique-sections)
- [Data Transfer Objects](#data-transfer-object-dto)
- [Controllers](#controller)
- [Service](#service)
- [Repository](#repository)


### Introduction 
The repository is designed with two orientations in mind: Service and Class.
The top layers of the application are divided into the different services (admin, app, and scraper)
and the middle and bottom layers are divided on class (clothing, like, store, user, etc.). This decision was made to
reduce confusion within security and data processing. With this design, minimal mistakes will be made
based on user permissions and what data is being processed.

### Unique Sections
#### Application
This folder contains code pertaining to the configurations of the application itself.
#### Security
This folder contains code that configures the generic Spring Security. Code that pertains to
user authorities, authentication, and rate limiting belong within this folder.
#### Cache
Configurations to caching should be placed within this folder.
    
### Data Transfer Object (DTO)
Hibernate and Springboot continues an attachment between an object and its database representation
once queried. To avoid accidentally manipulating data, queried objects are **immediately** mapped into
their DTO equivalent. **All mappings take place within the service layer**.

### Deserializer
To create generic DTOs and classes, a decision was made to create a custom deserializer for each DTO.

### Controller
Controllers are the first layer that requests pass through. There are four main controllers based on distinct services:
- Scraper
- Admin
- App
- Generic

Each of the top three previously mentioned have protected routes. 
The latter has been used as a catch-all for unprotected routes. Each controller utilizes services to request data in DTO format. The controllers are responsible for formatting the data into a response.

**All data within the layer must be DTOs**. Every request containing data must use [EntityModel](https://docs.spring.io/spring-hateoas/docs/current/api/org/springframework/hateoas/EntityModel.html) or 
[CollectionModel](https://docs.spring.io/spring-hateoas/docs/current/api/org/springframework/hateoas/CollectionModel.html). We will begin moving towards a [HATEOAS](https://htmx.org/essays/hateoas/#:~:text=Hypermedia%20as%20the%20Engine%20of,provide%20information%20dynamically%20through%20hypermedia) standard to provide
a better experience between frontend and backend updates.
### Service
The service layer is the middle layer divided by the class represented. Services will query raw data from repositories and convert them into the developer safe
DTO. A majority of the logic will reside in this layer that pertains the manipulating data to satisfy the function.

### Repository
The repository is the bottom layer divided by the class represented. Each repository class is an implementation of a JPARepostiory. The syntax used is a combination of 
native [SQL and JPQL](https://www.baeldung.com/spring-data-jpa-query). While writing queries consider the potential return size and time complexity. A common workaround is [paging](https://www.baeldung.com/spring-data-jpa-pagination-sorting). 

#### Note
Querying repositories **is the slowest** part of every request. Always consider the tradeoff of cross-server communication 
(network speeds, serialization, etc.) versus a larger workload within the codebase.
