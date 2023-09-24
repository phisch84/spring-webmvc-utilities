package com.schoste.ddd.application.v1.exceptions;

/**
 * Basic class for all exceptions thrown in the application layer
 * 
 * @author Philipp Schosteritsch <s.philipp@schoste.com>
 */
public class ApplicationException extends Exception
{
	private static final long serialVersionUID = -5137067546868974900L;

	/**
	 * Instance to a handler for {@see ApplicationException}.
	 * If not null {@ ApplicationExceptionHandler#onExceptionCreated(ApplicationException)} will be
	 * called whenever a {@see ApplicationException} is created.
	 */
	public static ApplicationExceptionHandler handler = null;

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

	public ApplicationException(String message)
	{
		super(message);
		
		invokeHandler(this);
	}

	public ApplicationException(Throwable inner)
	{
		super(inner);

		invokeHandler(this);
	}

	public ApplicationException(String message, Throwable inner)
	{
		super(message, inner);

		invokeHandler(this);
	}
}
