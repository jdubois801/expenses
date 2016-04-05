Overall Design

The overall design of the application is a conventional Spring controller-service-repository architecture.
The controller and repository classes are kept a simple as possible with the bulk of the implementation
occurring in the service class driven by several Test Driven Design concerns.  First, Separation of Concerns
allows the service implementation to refer to fewer Java classes which makes the code easier to read and simpler
to unit test with the added benefit that validation of unit tests by engineers and automated tools is improved.
Second, by focusing on unit testing the service, test occurs more quickly with the advantage that positive
and negative testing, input validation, and business rules can all be covered in an automated testing environment
which improves the reliability of the application.  Third, by concentrating test in the service implementation
which relies heavily on mock objects, many more cases of positive and negative test and edge case behaviors can
be directly tested than if other techniques are used.  And finally, by concentrating test in a single location
in the architectural stack, engineers are much more likely to keep the test suite up to date.

In general, it is assumed that the service implementation is part of an early stage green field micro-services
oriented application that is not run as part of a larger scale production environment.


The remaining sections of this document touch on assumptions and design decisions made in the implementation, as
well as operational considerations and possible improvements in no particular order.


Data Binding on Update

The data validation and binding in the update service can appear slightly inelegant as implemented.  In a real
production system with a much more complicated data model (for example, when refactoring a legacy application), 
this would be instead implemented with Dozer or the Spring Data Binding utilities, or another similar utility.
For the purposes of this exercise, Dozer was rejected because it would introduce an external jar dependency.  
Spring Data Binding was investigated since it does provide validation after data binding which is useful for
detecting complicated and multi-property validation dependencies, but Spring Data Binding was rejected because the validation error messages are not at all user friendly.  The validation error messages returned from the RESTful service should be presentable to the end user.  (Localization of error messages is left out of scope but should be
considered for a production application.)  So validation is implemented in a small class.  Data binding is also 
implemented in a small class which is clearly inelegant, but this solution avoids the more desirable dependency 
on the Apache Commons BeanUtils.


Validation Error Messages

The data validation implementation returns only one validation error message at a time.  In practice, it is more
desirable to developers to receive a collection of validation error messages rather than a single one in order to
reduce the number of edit-build-test iterations when building an application against the service.


Transaction Management

While the requirements specify the creation of a RESTful service implementation, they do not specify if the
implementation exists in a micro-services architecture or a larger application context.  This has an impact on
the design of transaction management.  For example, if the service is being created as part of a step-by-step
migration of a server side legacy application to RESTful services, the new service would likely need to interact
with other parts of the application in addition to the Spring Data repository.  The new service may also be called 
from other parts of the application as part of a separation of concerns refactor.  In this case, the new service may
need to participate in transactions that are initiated in other parts of the application to maintain consistency
in a RDBMS.  For a convenient way to accomplish this, Spring provides the @Transactional annotation which would be applied to the saveExpense() and updateExpense() methods of the ExpenseService class.

In this implementation, a green field service is assumed to be built in a micro-services architecture with a non-JPA
Spring Data repository and therefore the @Transactional annotation is not present.


MongoDB Service Location

The implementation assumes a local installation of MongoDB.  In a highly scalable production environment, it is much
more desirable to use a service location service such as etcd, Apache Zookeper, Consul, or a similar utility to
find the location of the NoSQL db.  The implementation does not include any db connection management in the case of
failure.  In a highly scalable and highly available production environment, it would probably be preferable to adopt
the nodejs model of application shutdown on db error, but that is assumed to be out of scope of the exercise.


Saved Expense Id Uniqueness

The Ids returned from saved expenses are the Ids assigned by MongoDB.  These Ids are unique to a running MongoDB instance (or cluster) but not necessarily across multiple MongoDB instances.  Experimentation also indicates that
the Ids may be reused when objects are deleted from a collection.  This makes them not necessarily unique at all 
times, and together these two effects can lead to data consistency issues in a large enterprise data warehouse.


Saved Expense Response Location Header

Some implementations of RESTful services include a HTTP location header in the service response when a new entity is
created.  This implementation does not for the sake of clarity of the controller class source code.


Ordering

Ordering of listed expenses is highly desireable.  While not implemented as part of this exercise,
the ordering of expenses can probably be implemented in not many lines of Java code.


Requirements in Conflict

The requirements for the exercise are in conflict for the spelling of the "comments" property of the expense entity.
It is twice referred to as "comments", and once as "comment".  The implementation assumes "comments" as the name of
the property.


Size, Precision and Currency of Expense Total Property

No requirement was given for the precision of the "total" property of the expense entity.  While two decimal places is common for most currencies, three is needed in some locales.  The implementation does not enforce a maximum value, nor
a precision, but stored the submitted value directly into MongoDB.  In a real application, size, precision, and
currency would be required to be validated.


Logging

The requirements only mention logging when expenses are deleted.  Accordingly, the implementation logs successful
deletions, but no other events or errors.  In a real application, it is very desireable to log not only errors, but
also successful uses of the service.  Logging of errors is usually done to the local file system where they are
picked up by a log forwarding system.  Tracking successful uses can easily be added to a metrics gathering system
for use in operations, billing, and most importantly product management for purposes such as A/B testing and
validation of the business model that the application supports.


