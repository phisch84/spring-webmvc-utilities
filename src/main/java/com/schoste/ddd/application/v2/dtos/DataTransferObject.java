package com.schoste.ddd.application.v2.dtos;

import java.beans.Transient;
import java.io.Serializable;

import com.schoste.ddd.domain.v3.entities.Entity;

/**
 * Base class of data transfer objects (DTOs): the objects a web application exchanges with its clients instead of entities.
 * A DTO refers to its entity by the entity's id (see {@link DataTransferObject#getId()}), and it can hold the entity itself
 * while it is processed on the server (see {@link DataTransferObject#getEntity()}), which is never serialized.
 * <p>
 * A subclass adds the properties of the DTO. Setters annotated with {@link com.schoste.ddd.domain.v3.annotations.AutoSet}
 * are set automatically by a {@link DataTransferObjectConverter} from the corresponding getters of the entity,
 * and vice versa for the annotated setters of the entity.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 *
 * @param <ID> the class of the ids of the DTO and its entity
 */
abstract public class DataTransferObject<ID extends Serializable> implements Serializable
{
	private static final long serialVersionUID = 5290146275262596247L;

	private transient Entity<ID> entity;

	/**
	 * The id of the entity the DTO refers to
	 */
	private ID id;

	/**
	 * Whether the entity is (to be) deleted; null if not set
	 */
	private Boolean deleted;

	/**
	 * Whether the entity is (to be) added or modified; null if not set
	 */
	private Boolean addedOrModified;

	/**
	 * Creates a DTO without id and entity
	 */
	protected DataTransferObject()
	{
	}

	/**
	 * Gets the entity that belongs to this DTO or null if none is related or set
	 *
	 * @return the entity that belongs to this DTO
	 */
	@Transient
	public Entity<ID> getEntity()
	{
		return this.entity;
	}

	/**
	 * Sets the entity that belongs to this DTO
	 *
	 * @param entity the entity that belongs to this DTO
	 */
	public void setEntity(Entity<ID> entity)
	{
		this.entity = entity;
	}

	/**
	 * Gets the id of the object.
	 * If this id is an actual id, it relates to the entity with the same id.
	 * If the id is null or virtual, it means there is no persisted entity yet: a virtual id the client made up
	 * only identifies the DTO, and the entity created for it gets its own virtual id from the repository.
	 *
	 * @return the id of the entity
	 */
	public ID getId()
	{
		return id;
	}

	/**
	 * Sets the id of the object.
	 * If this id is an actual id, it relates to the entity with the same id.
	 * If the id is null or virtual, it means there is no persisted entity yet: a virtual id the client made up
	 * only identifies the DTO, and the entity created for it gets its own virtual id from the repository.
	 *
	 * @param id the id of the entity
	 */
	public void setId(ID id)
	{
		this.id = id;
	}

	/**
	 * If sent from the server to the client, it usually means the corresponding entity
	 * has been deleted on server side.
	 * If sent from the client to the server, it usually means the client (user) requests
	 * the deletion of the corresponding entity.
	 *
	 * @return true if deleted, false otherwise. If null it isn't defined/set.
	 */
	public Boolean isDeleted()
	{
		return deleted;
	}

	/**
	 * If sent from the server to the client, it usually means the corresponding entity
	 * has been deleted on server side.
	 * If sent from the client to the server, it usually means the client (user) requests
	 * the deletion of the corresponding entity.
	 *
	 * @param deleted true if deleted, false otherwise
	 */
	public void setDeleted(Boolean deleted)
	{
		this.deleted = deleted;
	}

	/**
	 * If sent from the server to the client, it usually means the corresponding entity
	 * has been added or was modified on server side. The client should update.
	 * If sent from the client to the server, it usually means the client (user) requests
	 * the creation of a new or the update of the corresponding entity.
	 *
	 * @return true if added or modified, false otherwise. If null it isn't defined/set.
	 */
	public Boolean isAddedOrModified()
	{
		return addedOrModified;
	}

	/**
	 * If sent from the server to the client, it usually means the corresponding entity
	 * has been added or was modified on server side. The client should update.
	 * If sent from the client to the server, it usually means the client (user) requests
	 * the creation of a new or the update of the corresponding entity.
	 *
	 * @param addedOrModified true if added or modified, false otherwise
	 */
	public void setAddedOrModified(Boolean addedOrModified)
	{
		this.addedOrModified = addedOrModified;
	}
}
