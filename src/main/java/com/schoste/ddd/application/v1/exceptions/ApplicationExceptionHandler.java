package com.schoste.ddd.application.v1.exceptions;

/**
 * Version 1 interface to a handler of {@see ApplicationException}
 * If there is an implementation and the instance is set at {@see ApplicationException#handler}, this handler is
 * called whenever a {@see ApplicationException} is created.
 * 
 * @author Philipp Schosteritsch <s.philipp@schoste.com>
 *
 */
public interface ApplicationExceptionHandler
{
	/**
	 * Called by constructors of {@see ApplicationException}
	 * 
	 * @param exception the exception that was created
	 */
	void onExceptionCreated(ApplicationException exception);
}
