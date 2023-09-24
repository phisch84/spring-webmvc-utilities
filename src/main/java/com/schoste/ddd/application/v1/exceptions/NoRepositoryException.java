package com.schoste.ddd.application.v1.exceptions;

import java.util.ResourceBundle;

/**
 * This exception indicates that a module could not get a domain repository for a certain action.
 * This exception is usually thrown if a method that should return the expected repository returns null
 * 
 * @author Philipp Schosteritsch <s.philipp@schoste.com>
 */
public class NoRepositoryException extends ApplicationException
{
	private static final long serialVersionUID = 4931489405991378135L;

	public NoRepositoryException()
	{
		super(String.format(ResourceBundle.getBundle("exceptions").getString(NoRepositoryException.class.getSimpleName())));
	}
}
