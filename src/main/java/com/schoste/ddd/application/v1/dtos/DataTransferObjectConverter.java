package com.schoste.ddd.application.v1.dtos;

import java.util.Collection;

import com.schoste.ddd.application.v1.exceptions.ApplicationException;
import com.schoste.ddd.application.v1.exceptions.NoRepositoryException;
import com.schoste.ddd.domain.v1.exceptions.DomainException;
import com.schoste.ddd.domain.v1.models.DomainObject;

/**
 * Interface to the version 1 Domain Object <-> Data Transfer Object converter.
 * Extend interfaces of implementing classes with this one and define concrete parameters
 * and results.
 * 
 * @author Philipp Schosteritsch <s.philipp@schoste.com>
 */
public interface DataTransferObjectConverter<DO extends DomainObject, DTO extends DataTransferObject>
{
	/**
	 * Creates an instance of a new data transfer object
	 * 
	 * @return an instance of a new data transfer object
	 */
	DTO createDataTransferObject();

	/**
	 * Creates an instance of a new (DO) domain object for a given data transfer object (DTO)
	 * 
	 * @param dataTransferObject the DTO to create the DO from
	 * @return an instance of a new DO
	 * @throws NoRepositoryException thrown if there is no repository to create DOs from
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	DO createDomainObject(DTO dataTransferObject) throws NoRepositoryException, ApplicationException;

	/**
	 * Creates a data transfer object from a given domain object
	 * 
	 * @param domainObject the domain object to convert to the data transfer object
	 * @return the DTO
	 * @throws DomainException re-throws every exception from the domain layer
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	DTO toDataTransferObject(DO domainObject) throws DomainException, ApplicationException;

	/**
	 * Creates data transfer objects from a list of given domain objects
	 * 
	 * @param param the list of given domain objects
	 * @return the DTOs. The output is in the order of the input.
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	Collection<DTO> toDataTransferObjectsFromDomainObjects(Collection<DO> domainObjects) throws ApplicationException;

	/**
	 * Creates a data transfer object from given parameters
	 * 
	 * @param param the parameters used to create the DTO
	 * @return the DTO
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	DTO toDataTransferObject(Object...param) throws ApplicationException;

	/**
	 * Creates data transfer objects from a list of given parameters
	 * 
	 * @param param the list of parameters to create the DTOs
	 * @return the DTOs. The output is in the order of the input.
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	Collection<DTO> toDataTransferObjects(Collection<Object[]> params) throws ApplicationException;

	/**
	 * Processes a given data transfer object
	 * 
	 * @param dto the data transfer object to process
	 * @return a result of the process
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	Object fromDataTransferObject(DTO dto) throws ApplicationException;

	/**
	 * Processes given data transfer objects
	 * 
	 * @param dtos the data transfer objects to process
	 * @return a result of the process of each DTO. The output is in the order of the input.
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	Collection<Object> fromDataTransferObjects(Collection<DTO> dtos) throws ApplicationException;

	/**
	 * Converts a given data transfer object (DTO) to a domain object (DO)
	 * If {@see DataTransferObject#getDomainObject()} returns null for the provided DTO
	 * then {@see DataTransferObject#setDomainObject(DomainObject)} will be called with
	 * the DO as parameter that was created or obtained for the DTO.
	 * 
	 * @param dto the DTO to create the DO from
	 * @return the DO which was created from the DTO
	 * @throws Exception re-throws every exception
	 * @throws DomainException re-throws every exception from the domain layer
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	DO toDomainObject(DTO dto) throws DomainException, ApplicationException;

	/**
	 * Converts a given list of data transfer objects (DTOs) to a list of domain objects (DOs)
	 * 
	 * @param dtos the DTOs to create the DOs from
	 * @return the DOs which were created from the DTOs
	 * @throws Exception re-throws every exception
	 * @throws DomainException re-throws every exception from the domain layer
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	Collection<DO> toDomainObjects(Collection<DTO> dtos) throws DomainException, ApplicationException;
}
