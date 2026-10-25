/**
 * Exceptions of the application layer.
 * <p>
 * Every exception is an {@link com.schoste.ddd.application.v2.exceptions.ApplicationException}, which notifies a global
 * {@link com.schoste.ddd.application.v2.exceptions.ApplicationExceptionHandler} when it is created, if one is set.
 * Exceptions of the domain layer ({@link com.schoste.ddd.domain.v3.exceptions.DomainException}) are passed on unchanged
 * where a method declares them, and wrapped in an {@code ApplicationException} otherwise.
 * The messages are read from the resource bundle {@code exceptions}.
 */
package com.schoste.ddd.application.v2.exceptions;
