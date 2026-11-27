package com.schoste.ddd.application.v2.exceptions;

import java.util.ResourceBundle;

/**
 * This exception indicates that a module could not get a domain repository for a certain action.
 * This exception is usually thrown if a method that should return the expected repository returns null,
 * e.g. {@link com.schoste.ddd.application.v2.dtos.standard.DataTransferObjectConverterImpl#getRepository()}.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class NoRepositoryException extends ApplicationException
{
	private static final long serialVersionUID = 4931489405991378135L;

	/**
	 * Creates the exception with its standard message
	 */
	public NoRepositoryException()
	{
		super(String.format(ResourceBundle.getBundle("com.schoste.ddd.application.v2.exceptions.messages").getString(NoRepositoryException.class.getSimpleName())));
	}
}
