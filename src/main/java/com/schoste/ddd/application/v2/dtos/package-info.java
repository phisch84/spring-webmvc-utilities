/**
 * Data transfer objects (DTOs) and their converters.
 * <p>
 * A {@link com.schoste.ddd.application.v2.dtos.DataTransferObject} is what a web application exchanges with its clients
 * instead of an entity. It refers to its entity by the entity's id. A
 * {@link com.schoste.ddd.application.v2.dtos.DataTransferObjectConverter} converts between the entities of one repository
 * and their DTOs:
 * <ul>
 * <li>entity to DTO: the DTO gets the entity's properties, its id and a reference to the entity;</li>
 * <li>DTO to entity: a DTO with a null or virtual id gets a new entity of the repository, a DTO with an actual id gets the
 * repository's entity with this id (or an {@link com.schoste.ddd.application.v2.exceptions.EntityNotFoundException}).
 * The entity gets the DTO's properties, but never its id, and it isn't added to the repository.</li>
 * </ul>
 * Converters are implemented by extending {@link com.schoste.ddd.application.v2.dtos.standard.DataTransferObjectConverterImpl}.
 */
package com.schoste.ddd.application.v2.dtos;
