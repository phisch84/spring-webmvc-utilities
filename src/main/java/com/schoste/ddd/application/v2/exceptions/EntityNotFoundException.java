package com.schoste.ddd.application.v2.exceptions;

import java.io.Serializable;
import java.util.ResourceBundle;

/**
 * This exception indicates that a data transfer object refers to an entity by an actual id, but the repository doesn't
 * provide an entity with this id: it doesn't exist, its data object is flagged deleted, or its removal is pending.
 * A web application usually answers such a request with "404 Not Found".
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class EntityNotFoundException extends ApplicationException
{
	private static final long serialVersionUID = -2350834419517930468L;

	private final Serializable id;

	/**
	 * Creates the exception for the id of an entity which wasn't found
	 *
	 * @param id the id of the entity which wasn't found
	 */
	public EntityNotFoundException(Serializable id)
	{
		super(String.format(ResourceBundle.getBundle("exceptions").getString(EntityNotFoundException.class.getSimpleName()), id));

		this.id = id;
	}

	/**
	 * Gets the id of the entity which wasn't found
	 *
	 * @return the id of the entity which wasn't found
	 */
	public Serializable getId()
	{
		return this.id;
	}
}
