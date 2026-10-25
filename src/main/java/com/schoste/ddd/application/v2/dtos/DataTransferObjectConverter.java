package com.schoste.ddd.application.v2.dtos;

import java.io.Serializable;
import java.util.Collection;

import com.schoste.ddd.application.v2.exceptions.ApplicationException;
import com.schoste.ddd.application.v2.exceptions.EntityNotFoundException;
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
	 * Gets or creates the entity a given data transfer object (DTO) refers to by its id:
	 * <ul>
	 * <li>if the id of the DTO is null or virtual, a new entity is created by the repository
	 * (see {@link com.schoste.ddd.domain.v3.services.GenericRepository#createEntity()}). It has a virtual id of the repository
	 * and isn't added to the repository: adding it is up to the caller;</li>
	 * <li>otherwise (the id is actual), it is the repository's entity with this id
	 * (see {@link com.schoste.ddd.domain.v3.services.GenericRepository#get(Serializable)}).</li>
	 * </ul>
	 * The id of the DTO is never assigned to the entity, since ids of entities are managed by the repository; a virtual id
	 * the client made up therefore only identifies the DTO.
	 * The properties of the entity are not changed (see {@link DataTransferObjectConverter#toEntity(DataTransferObject)}).
	 *
	 * @param dataTransferObject the DTO to get or create the entity for
	 * @return the entity of the DTO
	 * @throws NoRepositoryException thrown if there is no repository to get or create entities from
	 * @throws EntityNotFoundException thrown if the id of the DTO is actual, but the repository returns no entity for it:
	 * it doesn't exist, its data object is flagged deleted, or its removal is pending
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	E createEntity(DTO dataTransferObject) throws NoRepositoryException, EntityNotFoundException, ApplicationException;

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
	 * Converts a given data transfer object (DTO) to an entity:
	 * gets or creates the entity of the DTO (see {@link DataTransferObjectConverter#createEntity(DataTransferObject)})
	 * and sets its properties from the DTO.
	 * If {@see DataTransferObject#getEntity()} returns null for the provided DTO
	 * then {@see DataTransferObject#setEntity(Entity)} will be called with
	 * the entity as parameter that was created or obtained for the DTO.
	 * The id of the DTO isn't changed, and the entity isn't added to the repository.
	 *
	 * @param dto the DTO to create the entity from
	 * @return the entity which was created from the DTO, or null if dto is null
	 * @throws Exception re-throws every exception
	 * @throws DomainException re-throws every exception from the domain layer
	 * @throws EntityNotFoundException thrown if the DTO refers to an entity which the repository doesn't provide
	 * (see {@link DataTransferObjectConverter#createEntity(DataTransferObject)})
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	E toEntity(DTO dto) throws DomainException, EntityNotFoundException, ApplicationException;

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
