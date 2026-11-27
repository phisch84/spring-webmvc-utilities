package com.schoste.ddd.application.v2.exceptions;

/**
 * Handler of {@link ApplicationException}s.
 * If an implementation is set at {@link ApplicationException#handler}, it is
 * called whenever an {@link ApplicationException} is created.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public interface ApplicationExceptionHandler
{
	/**
	 * Called by the constructors of {@link ApplicationException}, i.e. before the constructor of a subclass has finished.
	 * Exceptions thrown by this method are printed to {@link System#err} and don't propagate.
	 *
	 * @param exception the exception that was created
	 */
	void onExceptionCreated(ApplicationException exception);
}
