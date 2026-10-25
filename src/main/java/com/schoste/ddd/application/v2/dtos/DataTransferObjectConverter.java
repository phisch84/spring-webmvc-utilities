package com.schoste.ddd.application.v2.dtos;

import java.io.Serializable;
import java.util.Collection;

import com.schoste.ddd.application.v2.exceptions.ApplicationException;
import com.schoste.ddd.application.v2.exceptions.EntityNotFoundException;
import com.schoste.ddd.application.v2.exceptions.NoRepositoryException;
import com.schoste.ddd.domain.v3.entities.Entity;
import com.schoste.ddd.domain.v3.exceptions.DomainException;

/**
 * Converter between entities and data transfer objects (DTOs) of one class each.
 * Extend this interface with one for a concrete entity and DTO, which may add methods to create DTOs from
 * typed parameters, and implement it by extending
 * {@link com.schoste.ddd.application.v2.dtos.standard.DataTransferObjectConverterImpl}.
 * <p>
 * Converting a DTO to an entity never adds the entity to its repository or saves it: the caller decides what
 * happens to the entity (e.g. by adding it to the repository and committing a unit of work).
 * <p>
 * Exceptions: every method passes on {@link DomainException}s (where it declares them) and {@link ApplicationException}s,
 * including their subclasses, unchanged. Every other exception is wrapped in an {@link ApplicationException} with the
 * exception as its cause. The methods for collections stop at the first object which fails.
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
	 * Creates an instance of a new data transfer object without id and entity
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
	 * @throws DomainException re-throws every exception from the domain layer, e.g. of the repository
	 * @throws NoRepositoryException thrown if there is no repository to get or create entities from
	 * @throws EntityNotFoundException thrown if the id of the DTO is actual, but the repository returns no entity for it:
	 * it doesn't exist, its data object is flagged deleted, or its removal is pending
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	E createEntity(DTO dataTransferObject) throws DomainException, NoRepositoryException, EntityNotFoundException, ApplicationException;

	/**
	 * Creates a data transfer object (DTO) from a given entity:
	 * creates a new DTO (see {@link DataTransferObjectConverter#createDataTransferObject()}), sets its properties from
	 * the entity, its id to the id of the entity, and its entity to the given entity.
	 *
	 * @param entity the entity to convert to the data transfer object; must not be null
	 * @return the DTO
	 * @throws DomainException re-throws every exception from the domain layer
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	DTO toDataTransferObject(E entity) throws DomainException, ApplicationException;

	/**
	 * Creates data transfer objects (DTOs) from given entities, each as by
	 * {@link DataTransferObjectConverter#toDataTransferObject(Entity)}
	 *
	 * @param entities the entities to convert; none of them may be null
	 * @return the DTOs in the order of the entities, or null if entities is null
	 * @throws DomainException re-throws every exception from the domain layer
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	Collection<DTO> toDataTransferObjectsFromEntities(Collection<E> entities) throws DomainException, ApplicationException;

	/**
	 * Creates a data transfer object (DTO) from given parameters, without entity.
	 * The meaning of the parameters is defined by the implementing class; it usually offers a typed method as well.
	 *
	 * @param param the parameters used to create the DTO
	 * @return the DTO, or null if param is null
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	DTO toDataTransferObject(Object...param) throws ApplicationException;

	/**
	 * Creates data transfer objects (DTOs) from lists of given parameters, each as by
	 * {@link DataTransferObjectConverter#toDataTransferObject(Object...)}
	 *
	 * @param params the parameters of each DTO
	 * @return the DTOs in the order of the parameters, or null if params is null
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	Collection<DTO> toDataTransferObjects(Collection<Object[]> params) throws ApplicationException;

	/**
	 * Converts a given data transfer object (DTO) to a result defined by the implementing class,
	 * e.g. the parameters {@link DataTransferObjectConverter#toDataTransferObject(Object...)} would create it from
	 *
	 * @param dto the data transfer object to process
	 * @return the result of the DTO
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	Object fromDataTransferObject(DTO dto) throws ApplicationException;

	/**
	 * Converts given data transfer objects (DTOs), each as by {@link DataTransferObjectConverter#fromDataTransferObject(DataTransferObject)}
	 *
	 * @param dtos the data transfer objects to process
	 * @return the results in the order of the DTOs, or null if dtos is null
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	Collection<Object> fromDataTransferObjects(Collection<DTO> dtos) throws ApplicationException;

	/**
	 * Converts a given data transfer object (DTO) to an entity:
	 * gets or creates the entity of the DTO (see {@link DataTransferObjectConverter#createEntity(DataTransferObject)})
	 * and sets its properties from the DTO.
	 * If {@link DataTransferObject#getEntity()} returns null for the provided DTO
	 * then {@link DataTransferObject#setEntity(Entity)} will be called with
	 * the entity that was created or obtained for the DTO.
	 * The id of the DTO isn't changed, and the entity isn't added to the repository.
	 *
	 * @param dto the DTO to create the entity from
	 * @return the entity which was created from the DTO, or null if dto is null
	 * @throws DomainException re-throws every exception from the domain layer
	 * @throws EntityNotFoundException thrown if the DTO refers to an entity which the repository doesn't provide
	 * (see {@link DataTransferObjectConverter#createEntity(DataTransferObject)})
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	E toEntity(DTO dto) throws DomainException, EntityNotFoundException, ApplicationException;

	/**
	 * Converts given data transfer objects (DTOs) to entities, each as by
	 * {@link DataTransferObjectConverter#toEntity(DataTransferObject)}.
	 * The conversion stops at the first DTO which fails; the entities of the DTOs before stay as they were converted.
	 *
	 * @param dtos the DTOs to create the entities from
	 * @return the entities in the order of the DTOs, or null if dtos is null
	 * @throws DomainException re-throws every exception from the domain layer
	 * @throws EntityNotFoundException thrown if a DTO refers to an entity which the repository doesn't provide
	 * @throws ApplicationException re-throws other exceptions as application exceptions
	 */
	Collection<E> toEntities(Collection<DTO> dtos) throws DomainException, EntityNotFoundException, ApplicationException;
}
