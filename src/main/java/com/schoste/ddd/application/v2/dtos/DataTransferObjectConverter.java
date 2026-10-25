package com.schoste.ddd.application.v2.dtos;

import java.io.Serializable;
import java.util.Collection;

import com.schoste.ddd.application.v2.exceptions.ApplicationException;
import com.schoste.ddd.application.v2.exceptions.NoRepositoryException;
import com.schoste.ddd.domain.v3.entities.Entity;
import com.schoste.ddd.domain.v3.exceptions.DomainException;

/**
 * Interface to the version 2 Entity <-> Data Transfer Object converter.
 * Extend interfaces of implementing classes with this one and define concrete parameters
 * and results.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 *
 * @param <ID> the class of the ids of the entities and DTOs
 * @param <E> the class of the entities
 * @param <DTO> the class of the data transfer objects
 */
public interface DataTransferObjectConverter<ID extends Serializable, E extends Entity<ID>, DTO extends DataTransferObject<ID>>
{
	/**
	 * Creates an instance of a new data transfer object
	 *
	 * @return an instance of a new data transfer object
	 */
	DTO createDataTransferObject();

	/**
	 * Creates an instance of a new entity for a given data transfer object (DTO)
	 *
	 * @param dataTransferObject the DTO to create the entity from
	 * @return an instance of a new entity
	 * @throws NoRepositoryException thrown if there is no repository to create entities from
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	E createEntity(DTO dataTransferObject) throws NoRepositoryException, ApplicationException;

	/**
	 * Creates a data transfer object from a given entity
	 *
	 * @param entity the entity to convert to the data transfer object
	 * @return the DTO
	 * @throws DomainException re-throws every exception from the domain layer
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	DTO toDataTransferObject(E entity) throws DomainException, ApplicationException;

	/**
	 * Creates data transfer objects from a list of given entities
	 *
	 * @param param the list of given entities
	 * @return the DTOs. The output is in the order of the input.
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	Collection<DTO> toDataTransferObjectsFromEntities(Collection<E> entities) throws ApplicationException;

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
	 * Converts a given data transfer object (DTO) to an entity
	 * If {@see DataTransferObject#getEntity()} returns null for the provided DTO
	 * then {@see DataTransferObject#setEntity(Entity)} will be called with
	 * the entity as parameter that was created or obtained for the DTO.
	 *
	 * @param dto the DTO to create the entity from
	 * @return the entity which was created from the DTO
	 * @throws Exception re-throws every exception
	 * @throws DomainException re-throws every exception from the domain layer
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	E toEntity(DTO dto) throws DomainException, ApplicationException;

	/**
	 * Converts a given list of data transfer objects (DTOs) to a list of entities
	 *
	 * @param dtos the DTOs to create the entities from
	 * @return the entities which were created from the DTOs
	 * @throws Exception re-throws every exception
	 * @throws DomainException re-throws every exception from the domain layer
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	Collection<E> toEntities(Collection<DTO> dtos) throws DomainException, ApplicationException;
}
