package com.schoste.ddd.application.v1.dtos.standard;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.schoste.ddd.application.v1.dtos.DataTransferObject;
import com.schoste.ddd.application.v1.dtos.DataTransferObjectConverter;
import com.schoste.ddd.application.v1.exceptions.ApplicationException;
import com.schoste.ddd.application.v1.exceptions.NoRepositoryException;
import com.schoste.ddd.domain.v1.exceptions.DomainException;
import com.schoste.ddd.domain.v1.models.DomainObject;
import com.schoste.ddd.domain.v1.services.GenericRepository;
import com.schoste.ddd.domain.v1.services.standard.AutoObjectConverterImpl;

abstract public class DataTransferObjectConverterImpl<DO extends DomainObject, DTO extends DataTransferObject> extends AutoObjectConverterImpl implements DataTransferObjectConverter<DO, DTO>
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
	 * @return the repository of the domain object
	 */
	abstract protected GenericRepository<DO, ?> getDomainObjectRepository();

	/**
	 * Called after a data transfer object (DTO) was automatically created from a domain object (DO).
	 * 
	 * @param sourceDomainObject the DO from which the DTO was created
	 * @param destinationDataTransferObject the DTO which was created from the DO
	 */
	abstract protected void afterAutoConversion(DO sourceDomainObject, DTO destinationDataTransferObject);

	/**
	 * Called after a domain object (DO) was automatically created from a data transfer object (DTO).
	 * 
	 * @param sourceDataTransferObject the DTO from which the DO was created
	 * @param destinationDomainObject the DO which was created from the DTO
	 */
	abstract protected void afterAutoConversion(DTO sourceDataTransferObject, DO destinationDomainObject);

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
	 * Tries to create a domain object (DO) from a repository.
	 * If {@link DataTransferObjectConverterImpl#getDomainObjectRepository() returns null (i.e. because it was not implemented)
	 * this method needs to be overwritten to create a domain object.
	 */
	public DO createDomainObject(DTO dataTransferObject) throws NoRepositoryException, ApplicationException
	{
		try
		{
			GenericRepository<DO, ?> repo = this.getDomainObjectRepository();

			if (repo == null) throw new NoRepositoryException();
			
			DO domainObject =(dataTransferObject.getId() > 0) ? 
					repo.get(dataTransferObject.getId()) :
					repo.createObject();

			return domainObject;
		}
		catch (Exception e)
		{
			throw new ApplicationException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public DTO toDataTransferObject(DO domainObject) throws DomainException, ApplicationException
	{
		try
		{
			DTO dataTransferObject = this.createDataTransferObject();
	
			super.convert(domainObject, dataTransferObject);

			dataTransferObject.setId(domainObject.getId());
			dataTransferObject.setDomainObject(domainObject);

			this.afterAutoConversion(domainObject, dataTransferObject);
	
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
	public Collection<DTO> toDataTransferObjectsFromDomainObjects(Collection<DO> domainObjects) throws ApplicationException
	{
		try
		{
			if (domainObjects == null) return null;
	
			Collection<DTO> dtos = new ArrayList<DTO>(domainObjects.size());
			
			for (DO domainObject : domainObjects) dtos.add(this.toDataTransferObject(domainObject));
	
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
	public DO toDomainObject(DTO dto) throws DomainException, ApplicationException
	{
		try
		{
			if (dto == null) return null;
	
			DO domainObject = this.createDomainObject(dto);
	
			domainObject.setId(dto.getId());

			if (dto.getDomainObject() == null) dto.setDomainObject(domainObject);

			super.convert(dto, domainObject);
			this.afterAutoConversion(dto, domainObject);
	
			return domainObject;
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
	public Collection<DO> toDomainObjects(Collection<DTO> dtos) throws DomainException, ApplicationException
	{
		if (dtos == null) return null;

		Collection<DO> domainObjects = new ArrayList<DO>(dtos.size());
		
		for (DTO dto : dtos) domainObjects.add(this.toDomainObject(dto));

		return domainObjects;
	}
}
