/**
 * Exceptions of the application layer.
 * <p>
 * Every exception is an {@link com.schoste.ddd.application.v2.exceptions.ApplicationException}, which notifies a global
 * {@link com.schoste.ddd.application.v2.exceptions.ApplicationExceptionHandler} when it is created, if one is set.
 * Methods of this library pass on exceptions of the domain layer ({@link com.schoste.ddd.domain.v3.exceptions.DomainException})
 * and application exceptions unchanged; every other exception is wrapped in an {@code ApplicationException}.
 * The messages are read from the resource bundle {@code com.schoste.ddd.application.v2.exceptions.messages}.
 */
package com.schoste.ddd.application.v2.exceptions;
