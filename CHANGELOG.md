# Changelog

What changed between the versions of Spring MVC Web Application Utilities 2, for users of the artifact. Versions without a
release date aren't released yet. Release versions end with `.RELEASE`.

## 2.0.0 (2026-11-27)

The first version of Spring MVC Web Application Utilities 2: a new artifact (`spring-webmvc-utilities2`) and package
(`com.schoste.ddd.application.v2`), which can be used side by side with `spring-webmvc-utilities` 0.x (package
`….application.v1`). It is built on Domain Repository 3 (`domain-repository3`) and DAO 4 (`dao4`), and uses the same stack
as they do: Java 8, Spring 5.3 and Servlet 4.0 (`javax.servlet`). Version 0.3 had moved to Spring 7 and Jakarta.

**Changed compared to 0.x** (details and a mapping of the names:
[Migrating from Spring MVC Web Application Utilities 0.x](README.md#migrating-from-spring-mvc-web-application-utilities-0x))

- DTOs and converters have a type parameter for the id (`DataTransferObject<ID>`, `DataTransferObjectConverter<ID, E, DTO>`);
  "domain object" is called "entity" in all names.
- A DTO's id is never assigned to its entity: a `null` or virtual id means a new entity with a virtual id of the repository,
  an actual id means the repository's entity with this id.
- An unknown id throws `EntityNotFoundException` instead of failing with a `NullPointerException` inside an
  `ApplicationException`.
- `NoRepositoryException`, `EntityNotFoundException` and exceptions of the domain layer (`DomainException`) are passed on
  unchanged instead of being wrapped in an `ApplicationException`.
- Annotation handlers are found by their type argument: a handler no longer implements its annotation, its bean id is free,
  an annotation may have several pre and post handlers, and handlers of parent contexts are found. Configuration errors make
  requests fail with an `IllegalStateException`.
- Requests of other handlers (e.g. static resources) pass the interceptor instead of being blocked.
- The messages of the exceptions come from the library's own resource bundle
  `com.schoste.ddd.application.v2.exceptions.messages` instead of `exceptions`.

**Tested with** `domain-repository3` 3.0.0.RELEASE, `dao4` 4.1.0.RELEASE and Spring 5.3.39; compiled for Java 8, built with
JDK 21.
