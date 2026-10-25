package com.schoste.ddd.application.v2.dtos.standard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.schoste.ddd.application.v2.dtos.DataTransferObject;
import com.schoste.ddd.application.v2.dtos.DataTransferObjectConverter;
import com.schoste.ddd.application.v2.exceptions.ApplicationException;
import com.schoste.ddd.application.v2.exceptions.NoRepositoryException;
import com.schoste.ddd.domain.v3.entities.Entity;
import com.schoste.ddd.domain.v3.exceptions.DomainException;
import com.schoste.ddd.domain.v3.services.GenericRepository;
import com.schoste.ddd.domain.v3.services.standard.AutoObjectConverterImpl;

abstract public class DataTransferObjectConverterImpl<ID extends Serializable, E extends Entity<ID>, DTO extends DataTransferObject<ID>> extends AutoObjectConverterImpl implements DataTransferObjectConverter<ID, E, DTO>
{
	@Autowired
	protected ApplicationContext applicationContext;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Object getInstance(Class<?> clazz)
	{
		return this.applicationContext.getBean(clazz);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Object getInstance(String className)
	{
		return this.applicationContext.getBean(className);
	}

	/**
	 * To be implemented by the extending class:
	 *
	 * @return the repository of the entity
	 */
	abstract protected GenericRepository<ID, E, ?> getRepository();

	/**
	 * Called after a data transfer object (DTO) was automatically created from an entity.
	 *
	 * @param sourceEntity the entity from which the DTO was created
	 * @param destinationDataTransferObject the DTO which was created from the entity
	 */
	abstract protected void afterAutoConversion(E sourceEntity, DTO destinationDataTransferObject);

	/**
	 * Called after an entity was automatically created from a data transfer object (DTO).
	 *
	 * @param sourceDataTransferObject the DTO from which the entity was created
	 * @param destinationEntity the entity which was created from the DTO
	 */
	abstract protected void afterAutoConversion(DTO sourceDataTransferObject, E destinationEntity);

	/**
	 * To be implemented by the extending class:
	 *
	 * {@inheritDoc}
	 */
	abstract public DTO toDataTransferObject(Object... param) throws ApplicationException;

	/**
	 * To be implemented by the extending class:
	 *
	 * {@inheritDoc}
	 */
	abstract public DTO createDataTransferObject();

	/**
	 * Tries to create an entity from a repository.
	 * If {@link DataTransferObjectConverterImpl#getRepository() returns null (i.e. because it was not implemented)
	 * this method needs to be overwritten to create an entity.
	 */
	public E createEntity(DTO dataTransferObject) throws NoRepositoryException, ApplicationException
	{
		try
		{
			GenericRepository<ID, E, ?> repo = this.getRepository();

			if (repo == null) throw new NoRepositoryException();

			ID id = dataTransferObject.getId();
			E entity = ((id != null) && !repo.isVirtualId(id)) ?
					repo.get(id) :
					repo.createEntity();

			return entity;
		}
		catch (Exception e)
		{
			throw new ApplicationException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public DTO toDataTransferObject(E entity) throws DomainException, ApplicationException
	{
		try
		{
			DTO dataTransferObject = this.createDataTransferObject();

			super.convert(entity, dataTransferObject);

			dataTransferObject.setId(entity.getId());
			dataTransferObject.setEntity(entity);

			this.afterAutoConversion(entity, dataTransferObject);

			return dataTransferObject;
		}
		catch (DomainException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new ApplicationException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<DTO> toDataTransferObjectsFromEntities(Collection<E> entities) throws ApplicationException
	{
		try
		{
			if (entities == null) return null;

			Collection<DTO> dtos = new ArrayList<DTO>(entities.size());

			for (E entity : entities) dtos.add(this.toDataTransferObject(entity));

			return dtos;
		}
		catch (Exception e)
		{
			throw new ApplicationException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<DTO> toDataTransferObjects(Collection<Object[]> params) throws ApplicationException
	{
		try
		{
			if (params == null) return null;

			Collection<DTO> dtos = new ArrayList<DTO>(params.size());

			for (Object[] param : params) dtos.add(this.toDataTransferObject(param));

			return dtos;
		}
		catch (Exception e)
		{
			throw new ApplicationException(e);
		}
	}

	/**
	 * To be implemented by the extending class:
	 *
	 * {@inheritDoc}
	 */
	abstract public Object fromDataTransferObject(DTO dto) throws ApplicationException;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Object> fromDataTransferObjects(Collection<DTO> dtos) throws ApplicationException
	{
		try
		{
			if (dtos == null) return null;

			Collection<Object> results = new ArrayList<Object>(dtos.size());

			for (DTO dto : dtos) results.add(this.fromDataTransferObject(dto));

			return results;
		}
		catch (Exception e)
		{
			throw new ApplicationException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public E toEntity(DTO dto) throws DomainException, ApplicationException
	{
		try
		{
			if (dto == null) return null;

			E entity = this.createEntity(dto);

			entity.setId(dto.getId());

			if (dto.getEntity() == null) dto.setEntity(entity);

			super.convert(dto, entity);
			this.afterAutoConversion(dto, entity);

			return entity;
		}
		catch (DomainException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new ApplicationException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<E> toEntities(Collection<DTO> dtos) throws DomainException, ApplicationException
	{
		if (dtos == null) return null;

		Collection<E> entities = new ArrayList<E>(dtos.size());

		for (DTO dto : dtos) entities.add(this.toEntity(dto));

		return entities;
	}
}
