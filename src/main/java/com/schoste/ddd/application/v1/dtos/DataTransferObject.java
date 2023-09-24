package com.schoste.ddd.application.v1.dtos;

import java.beans.Transient;
import java.io.Serializable;

import com.schoste.ddd.domain.v1.models.DomainObject;

/**
 * Version 1 of the basic properties of a DTO
 * 
 * @author Philipp Schosteritsch <s.philipp@schoste.com>
 */
abstract public class DataTransferObject implements Serializable
{
	private static final long serialVersionUID = 5290146275262596247L;

	private transient DomainObject domainObject;
	private int id;
	private Boolean deleted;
	private Boolean addedOrModified;

	/**
	 * Gets the domain object that belongs to this DTO or null if none is related or set
	 * 
	 * @return the domain object that belongs to this DTO
	 */
	@Transient
	public DomainObject getDomainObject()
	{
		return this.domainObject;
	}

	/**
	 * Sets the domain object that belongs to this DTO
	 * 
	 * @param domainObject the domain object that belongs to this DTO
	 */
	public void setDomainObject(DomainObject domainObject)
	{
		this.domainObject = domainObject;
	}

	/**
	 * Gets the id of the object.
	 * Usually, if this id is positive, it relates to a domain object with the same id.
	 * If the id is negative, it usually means there is no domain object yet.
	 * 
	 * @return the id of the domain object
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * Sets the id of the object.
	 * Usually, if this id is positive, it relates to a domain object with the same id.
	 * If the id is negative, it usually means there is no domain object yet.
	 * 
	 * @param id the id of the domain object
	 */
	public void setId(int id)
	{
		this.id = id;
	}

	/**
	 * If sent from the server to the client, it usually means the corresponding domain
	 * object has been deleted on server side.
	 * If sent from the client to the server, it usually means the client (user) requests
	 * the deletion of the corresponding domain object.
	 * 
	 * @return true if deleted, false otherwise. If null it isn't defined/set.
	 */
	public Boolean isDeleted()
	{
		return deleted;
	}

	/**
	 * If sent from the server to the client, it usually means the corresponding domain
	 * object has been deleted on server side.
	 * If sent from the client to the server, it usually means the client (user) requests
	 * the deletion of the corresponding domain object.
	 * 
	 * @param deleted true if deleted, false otherwise
	 */
	public void setDeleted(Boolean deleted)
	{
		this.deleted = deleted;
	}

	/**
	 * If sent from the server to the client, it usually means the corresponding domain
	 * object has been added or was modified on server side. The client should update.
	 * If sent from the client to the server, it usually means the client (user) requests
	 * the creation of a new or the update of the corresponding domain object.
	 * 
	 * @return true if added or modified, false otherwise. If null it isn't defined/set.
	 */
	public Boolean isAddedOrModified()
	{
		return addedOrModified;
	}

	/**
	 * If sent from the server to the client, it usually means the corresponding domain
	 * object has been added or was modified on server side. The client should update.
	 * If sent from the client to the server, it usually means the client (user) requests
	 * the creation of a new or the update of the corresponding domain object.
	 * 
	 * @param addedOrModified true if added or modified, false otherwise
	 */
	public void setAddedOrModified(Boolean addedOrModified)
	{
		this.addedOrModified = addedOrModified;
	}
}
