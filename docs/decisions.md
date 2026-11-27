# Design decisions

Why Spring MVC Web Application Utilities 2 is the way it is. The rules themselves are documented in the Javadoc (start with
the overview and the package documentation); this page keeps the reasons, so that nobody "fixes" a deliberate choice. Change
a decision only on purpose: update this page, the Javadoc, the README and the tests together. The decisions of the
underlying libraries are in the `docs/decisions.md` of [Domain Repository 3](https://github.com/phisch84/domain-repository/blob/master/docs/decisions.md)
and [DAO 4](https://github.com/phisch84/dao/blob/master/docs/decisions.md).

## Data transfer objects and converters

| # | Decision | Why | So don't … |
|---|---|---|---|
| 1 | **Converting never assigns a DTO's id to an entity**; a `null` or virtual id means a new entity with a virtual id of the repository, an actual id means the repository's entity with this id. | Entity ids belong to the repository (virtual until the commit, then actual). A client-made id would collide with the repository's virtual ids or claim an id the storage never gave out. | … copy the DTO's id into the entity. |
| 2 | **Converting never changes the id of a DTO.** | A client-side virtual id identifies the client's object, e.g. to match the server's answer to it. | … replace the DTO's id while converting to an entity. |
| 3 | **Converting never adds an entity to its repository or saves it.** | Whether an entity is new, changed or ignored, and which Unit of Work commits it, is the caller's decision; a converter which records changes would surprise every caller who only wants to read. | … call `add` or `setModified` in a converter. |
| 4 | **An unknown actual id throws `EntityNotFoundException`** (with the id), also for an entity that is deleted or whose removal is pending. | The client refers to something that doesn't exist; a web application usually answers 404. A `null` entity would fail later, far from the cause. | … return `null` or create a new entity for an unknown id. |
| 5 | **Properties are converted by `@AutoSet` setters** of Domain Repository 3 (DTO setters for entity → DTO, entity setters for DTO → entity); everything else goes into the `afterAutoConversion` hooks. | One annotation for the domain and application layer; the converter needs no mapping code for plain properties. | … add a second mapping mechanism. |
| 6 | **The DTO's entity reference is transient** (`getEntity()` is never serialized); the flags `isDeleted()` / `isAddedOrModified()` are a convention between server and client which the library never reads or sets. | Entities must not leak to clients; the flags' meaning depends on the client protocol of each application. | … serialize the entity or act on the flags in the library. |

## Annotation interceptor

| # | Decision | Why | So don't … |
|---|---|---|---|
| 7 | **Handlers are found by their type argument**, as beans of the application context and its parents; any bean id. | A project adds annotations without changing the library, and a handler doesn't have to implement (and stub) its annotation as in 0.x. | … find handlers by bean id or by implemented interfaces. |
| 8 | **An annotation may have several pre and post handlers**, run by `priority`, lowest number first. | Independent concerns (e.g. login and logging) on the same annotation stay in separate handlers. | … allow only one handler per annotation. |
| 9 | **Configuration errors fail requests with `IllegalStateException`** (a handler whose annotation can't be determined, an interceptor without application context). Annotations without handlers are ignored. | A misconfigured security handler that is silently skipped would let requests through; an annotation without a handler is a normal case (e.g. Spring's own). | … skip handlers which can't be resolved. |
| 10 | **Requests of other handlers (e.g. static resources) pass** the interceptor unhandled. | Only controller methods carry the annotations; 0.x blocked these requests. | … block or handle non-controller requests. |

## Exceptions

| # | Decision | Why | So don't … |
|---|---|---|---|
| 11 | **`DomainException` and `ApplicationException` (with its subclasses) are passed on unchanged**; every other exception is wrapped in an `ApplicationException`. | Callers can catch exactly what happened (e.g. `EntityNotFoundException` → 404) instead of unwrapping causes. | … wrap exceptions which already belong to a layer. |
| 12 | **The messages come from the library's own resource bundle** `com.schoste.ddd.application.v2.exceptions.messages`. | A project's own `exceptions.properties` (the bundle name 0.x used) must not interfere with the library's messages. | … use a bundle name without the package. |

## Dependencies and versions

| # | Decision | Why | So don't … |
|---|---|---|---|
| 13 | **Spring 5.3 and Servlet 4.0 (`javax.servlet`)**, both `provided`; the artifact brings only `domain-repository3` (and with it `dao4`). | The same stack as Domain Repository 3 and DAO 4 (the latest Spring for Java 8); the web application decides the exact Spring version. 0.3 had moved to Spring 7 and Jakarta, which split the stack. | … add Spring or the Servlet API as compile dependencies, or move to Jakarta in this major version. |
| 14 | **Java 8**, compiled with `release 8`. | Same baseline as Domain Repository 3 and DAO 4. | … use Java 9+ APIs. |
| 15 | **The major version is part of the artifact id and the package** (`spring-webmvc-utilities2`, `….application.v2`). | Versions 0.x and 2 can be used side by side while a project migrates (as in Domain Repository 3 and DAO 4). | … publish a new major version under the old artifact id. |
| 16 | **Release versions end with `.RELEASE`** and never change; development versions are `-SNAPSHOT`. A release is tagged on `master`. Only released versions of `domain-repository3` and `dao4` are used for a release. | A `.RELEASE` version exists exactly once; Maven sorts it like the plain version, after the snapshot. | … rebuild or re-publish a release version, or release against snapshots. |
