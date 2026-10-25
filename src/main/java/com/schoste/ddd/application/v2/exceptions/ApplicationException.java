package com.schoste.ddd.application.v2.exceptions;

/**
 * Basic class for all exceptions thrown in the application layer.
 * Every constructor notifies the global {@link ApplicationException#handler}, if one is set,
 * e.g. to log every application exception at one place.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class ApplicationException extends Exception
{
	private static final long serialVersionUID = -5137067546868974900L;

	/**
	 * Global handler of application exceptions.
	 * If not null, {@link ApplicationExceptionHandler#onExceptionCreated(ApplicationException)} is
	 * called whenever an {@link ApplicationException} (or one of its subclasses) is created.
	 */
	public static ApplicationExceptionHandler handler = null;

	/**
	 * Notifies the global {@link ApplicationException#handler}, if one is set.
	 * Exceptions thrown by the handler are printed to {@link System#err} and don't propagate,
	 * so a failing handler never replaces the exception that is being created.
	 *
	 * @param exception the exception that was created
	 */
	protected static void invokeHandler(ApplicationException exception)
	{
		try
		{
			if (handler != null) handler.onExceptionCreated(exception);
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Creates the exception with a message and notifies the global handler
	 *
	 * @param message the message of the exception
	 */
	public ApplicationException(String message)
	{
		super(message);

		invokeHandler(this);
	}

	/**
	 * Creates the exception for a cause and notifies the global handler
	 *
	 * @param inner the cause of the exception
	 */
	public ApplicationException(Throwable inner)
	{
		super(inner);

		invokeHandler(this);
	}

	/**
	 * Creates the exception with a message for a cause and notifies the global handler
	 *
	 * @param message the message of the exception
	 * @param inner the cause of the exception
	 */
	public ApplicationException(String message, Throwable inner)
	{
		super(message, inner);

		invokeHandler(this);
	}
}
