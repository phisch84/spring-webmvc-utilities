# Working on Spring MVC Web Application Utilities 2

Instructions for AI agents and contributors. What the library does: [README.md](README.md). Why it is the way it is:
[docs/decisions.md](docs/decisions.md) – read it before changing behavior. What changed: [CHANGELOG.md](CHANGELOG.md).
The layers below are Domain Repository 3 ([phisch84/domain-repository](https://github.com/phisch84/domain-repository)) and
DAO 4 ([phisch84/dao](https://github.com/phisch84/dao)), which have their own `AGENTS.md`.

## The documentation is part of the product

Users get the artifact from Maven and usually never see the source code. What they read: the **Javadoc jar** (in their IDE),
the **POM** (name, description) and, at most, the README on GitHub. Therefore:

- Every public or protected element has Javadoc; the package documentation (`package-info.java`) and
  `src/main/javadoc/overview.html` are the entry points. The build fails on any Javadoc problem (doclint), also in the test
  jar (except missing comments).
- A change of behavior updates, in the same step: the Javadoc where users read the rule, the README if it mentions it,
  `CHANGELOG.md` (under the version in development), and `docs/decisions.md` if a decision changes.
- The examples in the package documentation and the README are copied as they are, so compile them against the built jars
  (e.g. with `javac`) whenever they change; doclint doesn't check code in `<pre>` blocks.

## Layout

One Maven module, artifact `spring-webmvc-utilities2`, package `com.schoste.ddd.application.v2`, compiled for Java 8.

| Package | Content |
|---|---|
| `dtos` | `DataTransferObject`, `DataTransferObjectConverter` |
| `dtos.standard` | `DataTransferObjectConverterImpl`, the base class of all converters |
| `interceptor` | `AnnotationInterceptor`, the handler base classes `GenericAnnotationPreHandler` / `GenericAnnotationPostHandler` |
| `exceptions` | `ApplicationException` and its subclasses; messages in `src/main/resources/…/exceptions/messages.properties` |

Tests (`src/test/java`, same package root):

- `dtos.standard.DataTransferObjectConverterImplUnitTest` and `…ExceptionTest` check the converter rules on the in-memory
  repository of `testfixtures` (built on `AbstractInMemoryDAO` from the `dao4` test jar).
- `interceptor.AnnotationInterceptorTest` checks how handlers are found and run; `controller.TestControllerIntegrationTest`
  runs the interceptor in Spring MVC (`MockMvc`, configuration in `src/test/resources/spring-mvc.xml`).
- The example annotations and handlers are in `annotations`.

## Build and test

- JDK 21, Maven 3.9. **Always build with `clean`**: IDEs (e.g. VS Code) write into `target/classes`, so an incremental build can
  report success for code which doesn't compile.
- `mvn clean install` runs all tests and builds the jar, test jar and Javadoc jars.
- `domain-repository3` and `dao4` come from GitHub Packages (repository ids `github-domain-repository` and `github-dao`): your
  `~/.m2/settings.xml` needs a `<server>` for each of these ids with a token with `read:packages`.
- Tests go through the public interface. Test names: `test` + method + situation/expectation, e.g.
  `testToDataTransferObjectsWrapsOtherExceptions`; a new rule gets its test first.

## Conventions

- **Line endings are kept per file**: most files are LF; `.gitignore` is CRLF. New files take the endings of their neighbors.
  Git doesn't convert them; `sed -i` in Git Bash turns CRLF into LF, so check endings after shell edits.
- Java style: tabs and braces on their own line, `this.` for members; keep the style of the file.
- Dependencies: the artifact depends on nothing but `domain-repository3` (which brings `dao4`). Spring and the Servlet API are
  `provided`; everything else is a test dependency.
- Versions: change the version only with `mvn versions:set -DnewVersion=X.Y.Z.RELEASE -DgenerateBackupPoms=false`.
  Release versions end with `.RELEASE`, development versions with `-SNAPSHOT` (see decision 16). The versions of the libraries
  below are the properties `ddd.domain-repository.version` and `ddd.dao.version` (the `dao4` test jar); keep `ddd.dao.version`
  at the `dao4` version `domain-repository3` uses. Only released versions may be used for a release.

## Releasing

1. On the branch `X.Y.Z`: set the release version (see above), date the version in `CHANGELOG.md`, update the README's
   status line. Build with `mvn clean install`; push; CI must be green.
2. Fast-forward `master` to the branch (`git merge --ff-only X.Y.Z`), so the release is exactly the commit CI tested.
3. On GitHub, create the release with the tag `X.Y.Z.RELEASE` on `master` and the version's changelog section as notes, and
   publish it (not as draft): the workflow `maven-publish.yml` builds, tests and deploys the artifact to GitHub Packages.
4. GitHub Packages refuses to overwrite a version: if the deploy fails after publishing, delete the package version before
   publishing again. The version stays at the release afterwards (no new `-SNAPSHOT`).

## Working with the owner

- Don't stage or commit; the owner reviews the diff and commits. Use plain `mv`/`rm`, not `git mv`/`git rm`.
- Work in small steps and summarize each one for review.
- Other projects depend on `spring-webmvc-utilities2`: breaking changes need a new major version (decision 15).
