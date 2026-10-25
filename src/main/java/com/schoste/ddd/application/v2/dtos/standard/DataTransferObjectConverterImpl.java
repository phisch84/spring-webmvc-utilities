package com.schoste.ddd.application.v2.dtos.standard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.schoste.ddd.application.v2.dtos.DataTransferObject;
import com.schoste.ddd.application.v2.dtos.DataTransferObjectConverter;
import com.schoste.ddd.application.v2.exceptions.ApplicationException;
import com.schoste.ddd.application.v2.exceptions.EntityNotFoundException;
import com.schoste.ddd.application.v2.exceptions.NoRepositoryException;
import com.schoste.ddd.domain.v3.entities.Entity;
import com.schoste.ddd.domain.v3.exceptions.DomainException;
import com.schoste.ddd.domain.v3.services.GenericRepository;
import com.schoste.ddd.domain.v3.services.standard.AutoObjectConverterImpl;

/**
 * Standard implementation of the {@link DataTransferObjectConverter}: the base class of converters.
 * <p>
 * Properties are converted by the {@link AutoObjectConverterImpl} of domain-repository: every setter of the destination
 * (the DTO when converting an entity, the entity when converting a DTO) which is annotated with
 * {@link com.schoste.ddd.domain.v3.annotations.AutoSet} is set from the corresponding getter of the source.
 * Instances the annotation asks for (by class or class name) are beans of the Spring application context.
 * Everything which can't be converted automatically is up to the hooks
 * {@link DataTransferObjectConverterImpl#afterAutoConversion(Entity, DataTransferObject)} and
 * {@link DataTransferObjectConverterImpl#afterAutoConversion(DataTransferObject, Entity)}.
 * <p>
 * An implementing class must be a bean of a Spring application context (it gets the context injected) and implement
 * {@link DataTransferObjectConverterImpl#getRepository()}, {@link DataTransferObjectConverter#createDataTransferObject()},
 * {@link DataTransferObjectConverter#toDataTransferObject(Object...)},
 * {@link DataTransferObjectConverter#fromDataTransferObject(DataTransferObject)} and both hooks.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 *
 * @param <ID> the class of the ids of the entities and DTOs
 * @param <E> the class of the entities
 * @param <DTO> the class of the data transfer objects
 */
abstract public class DataTransferObjectConverterImpl<ID extends Serializable, E extends Entity<ID>, DTO extends DataTransferObject<ID>> extends AutoObjectConverterImpl implements DataTransferObjectConverter<ID, E, DTO>
{
	/**
	 * The application context instances for {@link com.schoste.ddd.domain.v3.annotations.AutoSet} are taken from
	 */
	@Autowired
	protected ApplicationContext applicationContext;

	/**
	 * Creates the converter; the application context is injected afterwards
	 */
	protected DataTransferObjectConverterImpl()
	{
		super();
	}

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
	 * To be implemented by the extending class: gets the repository entities are taken from and created by.
	 * If it returns null, {@link DataTransferObjectConverterImpl#createEntity(DataTransferObject)} throws
	 * {@link NoRepositoryException} unless it is overridden.
	 *
	 * @return the repository of the entity
	 */
	abstract protected GenericRepository<ID, E, ?> getRepository();

	/**
	 * Called after a data transfer object (DTO) was automatically created from an entity, e.g. to set properties
	 * which can't be converted automatically.
	 * The DTO's id and entity are already set.
	 *
	 * @param sourceEntity the entity from which the DTO was created
	 * @param destinationDataTransferObject the DTO which was created from the entity
	 */
	abstract protected void afterAutoConversion(E sourceEntity, DTO destinationDataTransferObject);

	/**
	 * Called after an entity was automatically created or obtained for a data transfer object (DTO) and its properties
	 * were set from the DTO, e.g. to set properties which can't be converted automatically.
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
	 * Gets or creates the entity of a DTO by the repository of {@link DataTransferObjectConverterImpl#getRepository()}.
	 * If the repository returns null (i.e. because it was not implemented)
	 * this method needs to be overwritten to create an entity.
	 *
	 * {@inheritDoc}
	 */
	public E createEntity(DTO dataTransferObject) throws NoRepositoryException, EntityNotFoundException, ApplicationException
	{
		try
		{
			GenericRepository<ID, E, ?> repo = this.getRepository();

			if (repo == null) throw new NoRepositoryException();

			ID id = dataTransferObject.getId();

			if ((id == null) || repo.isVirtualId(id)) return repo.createEntity();

			E entity = repo.get(id);

			if (entity == null) throw new EntityNotFoundException(id);

			return entity;
		}
		catch (ApplicationException e)
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

			if (dto.getEntity() == null) dto.setEntity(entity);

			super.convert(dto, entity);
			this.afterAutoConversion(dto, entity);

			return entity;
		}
		catch (DomainException | ApplicationException e)
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
