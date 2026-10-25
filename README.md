# Spring MVC Web Application Utilities 2

A small library for the application layer of Spring MVC web applications built on
[Domain Repository 3](https://github.com/phisch84/domain-repository):

- **Data transfer objects (DTOs)** and **converters** between entities and DTOs, so that controllers exchange DTOs with
  their clients instead of entities.
- An **annotation interceptor**, which executes handlers for custom annotations of controllers and their methods
  (e.g. to check a login before a request is processed).

> **Status:** version 2.0.0 is under development and not released yet. This page describes the contract of 2.0.0.
> The Javadoc of each type is the complete reference; this page is the entry point.

## Dependencies

The artifact is published to GitHub Packages (`https://maven.pkg.github.com/phisch84/spring-webmvc-utilities`), together
with a Javadoc jar (`javadoc` classifier). It brings `domain-repository3` and `dao4` along; Spring and the Servlet API are
provided by the web application:

```xml
<dependency>
    <groupId>com.schoste.ddd.application</groupId>
    <artifactId>spring-webmvc-utilities2</artifactId>
    <version>2.0.0</version>
</dependency>

<!-- provided by the web application -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
    <version>5.3.39</version>
</dependency>
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>4.0.1</version>
    <scope>provided</scope>
</dependency>
```

The library is compiled for Java 8 and uses the same stack as DAO 4 and Domain Repository 3: Spring 5.3 and Servlet 4.0
(`javax.servlet`). The major version is part of both the artifact id and the package name
(`com.schoste.ddd.application.v2`), so version 2 can be used side by side with version 1 (`spring-webmvc-utilities`,
package `….application.v1`) while a project migrates.

## Data transfer objects

A DTO extends `DataTransferObject<ID>` and adds the properties the client sees. It refers to its entity by the entity's id:

| Id of the DTO | Entity of the DTO (`converter.toEntity(dto)`) |
|---|---|
| `null` | a new entity of the repository, with a virtual id of the repository |
| virtual (e.g. a negative id the client made up) | a new entity of the repository, with a virtual id of the repository |
| actual | the repository's entity with this id; `EntityNotFoundException` if there is none (missing, deleted, or removal pending) |

The rules:

- Converting never changes the id of a DTO and never assigns it to an entity, since the repository manages entity ids.
  A client-side virtual id therefore only identifies the DTO, e.g. to match the server's answer to the client's object.
- Converting never adds an entity to its repository or saves it. The caller decides, usually within a Unit of Work:

  ```java
  try (UnitOfWork uow = applicationContext.getBean(UnitOfWork.class, customerRepository))
  {
      Customer customer = customerDTOConverter.toEntity(customerDTO);

      if (customerRepository.isVirtualId(customer.getId())) customerRepository.add(customer);
      else customerRepository.setModified(customer);

      uow.commit();
  }
  ```

- Converting an entity to a DTO sets the DTO's properties, its id (the entity's id) and its entity reference
  (`getEntity()`, which is never serialized).
- Properties are converted automatically: every setter of the destination which is annotated with Domain Repository's
  `@AutoSet` is set from the corresponding getter of the source. Annotate the setters of the DTO for entity → DTO, and the
  setters of the entity for DTO → entity. Everything else goes into the two `afterAutoConversion` hooks.

A converter extends `DataTransferObjectConverterImpl<ID, E, DTO>`, is a Spring bean (it gets the application context
injected) and implements `getRepository()`, `createDataTransferObject()`, `toDataTransferObject(Object...)`,
`fromDataTransferObject(DTO)` and the two hooks. The Javadoc of the package `com.schoste.ddd.application.v2.dtos.standard`
contains a complete example.

## Annotation interceptor

`AnnotationInterceptor` is a Spring MVC `HandlerInterceptor`. For each request of a controller method it collects the
annotations of the controller class and the method and executes their handlers:

- `GenericAnnotationPreHandler<A>` before the request is processed. If a handler returns `false`, the request is discarded
  and that handler is responsible for the response.
- `GenericAnnotationPostHandler<A>` after the request was processed, before the view is rendered.

A project adds its own annotations without changing the library: it defines the annotation, writes a handler which extends
one of the base classes **with the annotation as type argument**, and registers the handler as a singleton bean (any bean id).
The handler gets the annotation of the controller (method), so it can read the annotation's attributes:

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface RequiresRole
{
    String value();
}

public class RequiresRoleHandler extends GenericAnnotationPreHandler<RequiresRole>
{
    @Override
    protected boolean handle(RequiresRole what, HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        if (request.isUserInRole(what.value())) return true;

        response.sendError(HttpServletResponse.SC_FORBIDDEN);

        return false;
    }
}
```

The rules:

- The interceptor collects all handler beans of its application context and its parent contexts on the first request.
- An annotation may have any number of pre and post handlers; they run by their `priority`, lowest number first.
- Annotations without handlers are ignored. Requests of other handlers (e.g. static resources) pass without handling.
- The handler's class must bind the type argument to a concrete annotation (an intermediate generic base class is fine).
  Otherwise, and if the interceptor wasn't created by Spring, requests fail with an `IllegalStateException` naming the cause.

```xml
<bean id="RequiresRoleHandler" class="com.example.RequiresRoleHandler" scope="singleton">
    <property name="priority" value="0" />
</bean>

<mvc:interceptors>
    <mvc:interceptor>
        <mvc:mapping path="/**" />
        <bean class="com.schoste.ddd.application.v2.interceptor.AnnotationInterceptor" />
    </mvc:interceptor>
</mvc:interceptors>
```

The Javadoc of the package `com.schoste.ddd.application.v2.interceptor` contains the complete example.

## Exceptions

All exceptions extend `ApplicationException`. If `ApplicationException.handler` is set, it is notified whenever one is created
(e.g. to log them at one place). `NoRepositoryException` means a converter has no repository, `EntityNotFoundException`
(with `getId()`) means a DTO refers to an entity the repository doesn't provide; a web application usually answers it with
404. Exceptions of the domain layer (`DomainException`) and application exceptions are passed on unchanged; every other
exception is wrapped in an `ApplicationException`. The messages come from the library's own resource bundle
`com.schoste.ddd.application.v2.exceptions.messages`, so a project's own `exceptions.properties` doesn't interfere.

The DTO flags `isDeleted()` / `isAddedOrModified()` are a convention between a server and its clients; the library never
reads or sets them.

## Building

```
mvn clean install
```

builds the jar, a test jar and both Javadoc jars; Javadoc warnings fail the build. Building needs DAO 4 and Domain Repository 3
in the local Maven repository or GitHub Packages.
