package com.schoste.ddd.application.v2.dtos.standard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.schoste.ddd.application.v2.exceptions.EntityNotFoundException;
import com.schoste.ddd.application.v2.exceptions.NoRepositoryException;
import com.schoste.ddd.application.v2.testfixtures.ExampleDAOImpl;
import com.schoste.ddd.application.v2.testfixtures.ExampleDO;
import com.schoste.ddd.application.v2.testfixtures.ExampleDTO;
import com.schoste.ddd.application.v2.testfixtures.ExampleDTOConverterImpl;
import com.schoste.ddd.application.v2.testfixtures.ExampleEntity;
import com.schoste.ddd.application.v2.testfixtures.ExampleRepository;
import com.schoste.ddd.domain.v3.services.GenericRepository;
import com.schoste.ddd.domain.v3.services.UnitOfWork;

/**
 * Tests the rules of {@link DataTransferObjectConverterImpl} (README "Data transfer objects"). Each test names the decision
 * of {@code docs/decisions.md} it checks, if any. Which exceptions are passed on or wrapped is tested by
 * {@link DataTransferObjectConverterImplExceptionTest}.
 * <p>
 * The converter works on a repository whose DAO keeps its data objects in memory; {@link #persist(String)} stores data objects
 * directly in the DAO, as if another application had stored them. Virtual ids are negative, actual ids positive.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
@ContextConfiguration(locations = { "classpath:unittest-beans.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class DataTransferObjectConverterImplTest
{
	@Autowired
	protected ApplicationContext applicationContext;

	@Autowired
	protected ExampleRepository repository;

	/**
	 * A new converter for each test (prototype bean), so the records of its hooks belong to the test
	 */
	@Autowired
	protected ExampleDTOConverterImpl converter;

	@Autowired
	protected ExampleDAOImpl dao;

	protected void clearAndReset() throws Exception
	{
		this.dao.clear();
		this.repository.reset();
	}

	@Before
	public void setUp() throws Exception
	{
		this.clearAndReset();
	}

	@After
	public void tearDown() throws Exception
	{
		this.clearAndReset();
	}

	protected UnitOfWork createUnitOfWork()
	{
		return this.applicationContext.getBean(UnitOfWork.class, this.repository);
	}

	/**
	 * Stores a data object in the DAO, bypassing the repository
	 *
	 * @param exampleProperty1 the value of the data object's first property
	 * @return the actual id of the stored data object
	 * @throws Exception re-throws every exception
	 */
	protected Long persist(String exampleProperty1) throws Exception
	{
		ExampleDO dataObject = this.dao.createDataObject();

		dataObject.setExampleProperty1(exampleProperty1);
		dataObject.setExampleProperty2(42);
		this.dao.save(dataObject);

		Assert.assertFalse(this.dao.isVirtualId(dataObject.getId()));

		return dataObject.getId();
	}

	protected ExampleDTO createDTO(Long id, String exampleProperty1, int exampleProperty2)
	{
		ExampleDTO dto = new ExampleDTO();

		dto.setId(id);
		dto.setExampleProperty1(exampleProperty1);
		dto.setExampleProperty2(exampleProperty2);

		return dto;
	}

	// ---------------------------------------------------------------------------------------------------------------------
	// DTO to entity
	// ---------------------------------------------------------------------------------------------------------------------

	/**
	 * Decision 1: a DTO without id is converted to a new entity with a virtual id from the repository, with the DTO's
	 * properties; decision 2: the DTO keeps its id; the DTO refers to the entity afterwards
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToEntityWithNullIdCreatesEntity() throws Exception
	{
		ExampleDTO dto = this.createDTO(null, "new", 2);

		ExampleEntity entity = this.converter.toEntity(dto);

		Assert.assertNotNull(entity);
		Assert.assertNotNull(entity.getId());
		Assert.assertTrue(this.repository.isVirtualId(entity.getId()));
		Assert.assertEquals("new", entity.getExampleProperty1());
		Assert.assertEquals(2, entity.getExampleProperty2());
		Assert.assertNull(dto.getId());
		Assert.assertSame(entity, dto.getEntity());
	}

	/**
	 * Decision 1: a DTO with a virtual id (e.g. one the client made up) is converted to a new entity with a virtual id from
	 * the repository; decision 2: the DTO keeps its id
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToEntityWithVirtualIdCreatesEntity() throws Exception
	{
		ExampleDTO dto = this.createDTO(-4711L, "new", 2);

		ExampleEntity entity = this.converter.toEntity(dto);

		Assert.assertNotNull(entity);
		Assert.assertTrue(this.repository.isVirtualId(entity.getId()));
		Assert.assertNotEquals(Long.valueOf(-4711L), entity.getId());
		Assert.assertEquals(Long.valueOf(-4711L), dto.getId());
		Assert.assertSame(entity, dto.getEntity());
	}

	/**
	 * Decision 1: the client's virtual ids never become the ids of entities, even if they equal virtual ids of the repository
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToEntitiesWithEqualVirtualIdsCreateDistinctEntities() throws Exception
	{
		Collection<ExampleDTO> dtos = Arrays.asList(this.createDTO(-1L, "new 1", 1), this.createDTO(-1L, "new 2", 2));

		ExampleEntity[] entities = this.converter.toEntities(dtos).toArray(new ExampleEntity[0]);

		Assert.assertEquals(2, entities.length);
		Assert.assertNotEquals(entities[0].getId(), entities[1].getId());
		Assert.assertEquals("new 1", entities[0].getExampleProperty1());
		Assert.assertEquals("new 2", entities[1].getExampleProperty1());
	}

	/**
	 * Decision 1: a DTO with an actual id is converted to the repository's entity with this id, which gets the DTO's properties
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToEntityWithActualIdGetsEntity() throws Exception
	{
		Long id = this.persist("stored");
		ExampleDTO dto = this.createDTO(id, "changed", 7);

		ExampleEntity entity = this.converter.toEntity(dto);

		Assert.assertSame(this.repository.get(id), entity);
		Assert.assertEquals(id, entity.getId());
		Assert.assertEquals("changed", entity.getExampleProperty1());
		Assert.assertEquals(7, entity.getExampleProperty2());
		Assert.assertSame(entity, dto.getEntity());
	}

	/**
	 * Decision 4: a DTO with an actual id which the DAO doesn't know causes an {@link EntityNotFoundException} with the id;
	 * the DTO gets no entity
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToEntityWithUnknownIdThrows() throws Exception
	{
		ExampleDTO dto = this.createDTO(4711L, "unknown", 0);

		EntityNotFoundException e = Assert.assertThrows(EntityNotFoundException.class, () -> this.converter.toEntity(dto));

		Assert.assertEquals(Long.valueOf(4711L), e.getId());
		Assert.assertNull(dto.getEntity());
	}

	/**
	 * Decision 4: a DTO with the id of a deleted data object causes an {@link EntityNotFoundException}
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToEntityWithDeletedIdThrows() throws Exception
	{
		Long id = this.persist("deleted");

		this.dao.deleteById(id);

		ExampleDTO dto = this.createDTO(id, "deleted", 0);

		EntityNotFoundException e = Assert.assertThrows(EntityNotFoundException.class, () -> this.converter.toEntity(dto));

		Assert.assertEquals(id, e.getId());
	}

	/**
	 * Decision 4: a DTO with the id of an entity whose removal is pending causes an {@link EntityNotFoundException}
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToEntityWithPendingRemovalThrows() throws Exception
	{
		Long id = this.persist("removed");

		try (UnitOfWork uow = this.createUnitOfWork())
		{
			this.repository.remove(this.repository.get(id));

			ExampleDTO dto = this.createDTO(id, "removed", 0);

			EntityNotFoundException e = Assert.assertThrows(EntityNotFoundException.class, () -> this.converter.toEntity(dto));

			Assert.assertEquals(id, e.getId());
		}
	}

	/**
	 * Decision 3: converting a DTO to an entity doesn't add the entity to the repository, so committing doesn't store it
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToEntityDoesNotAddEntity() throws Exception
	{
		try (UnitOfWork uow = this.createUnitOfWork())
		{
			this.converter.toEntity(this.createDTO(null, "not added", 0));

			uow.commit();
		}

		Assert.assertTrue(this.dao.getDataObjectIds().isEmpty());
	}

	/**
	 * Decision 3: converting a DTO with an actual id doesn't mark the entity as modified, so committing doesn't store the
	 * DTO's properties
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToEntityDoesNotSaveChanges() throws Exception
	{
		Long id = this.persist("stored");

		try (UnitOfWork uow = this.createUnitOfWork())
		{
			this.converter.toEntity(this.createDTO(id, "changed", 0));

			uow.commit();
		}

		Assert.assertEquals("stored", this.dao.get(id).getExampleProperty1());
	}

	/**
	 * The documented way to save the entity of a DTO (README "Data transfer objects"): add a new entity, mark an existing one
	 * as modified, commit. New entities get actual ids; decision 2: the DTOs keep the client's ids, so the client can match
	 * the answer to its objects.
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToEntitiesThenAddAndCommitStoresEntities() throws Exception
	{
		Long storedId = this.persist("stored");
		Collection<ExampleDTO> dtos = Arrays.asList(this.createDTO(-1L, "new", 1), this.createDTO(storedId, "changed", 2));

		try (UnitOfWork uow = this.createUnitOfWork())
		{
			for (ExampleEntity entity : this.converter.toEntities(dtos))
			{
				if (this.repository.isVirtualId(entity.getId())) this.repository.add(entity);
				else this.repository.setModified(entity);
			}

			uow.commit();
		}

		ExampleDTO[] dtoArray = dtos.toArray(new ExampleDTO[0]);
		Long newId = dtoArray[0].getEntity().getId();

		Assert.assertFalse(this.repository.isVirtualId(newId));
		Assert.assertEquals(Long.valueOf(-1L), dtoArray[0].getId());
		Assert.assertEquals("new", this.dao.get(newId).getExampleProperty1());
		Assert.assertEquals(storedId, dtoArray[1].getId());
		Assert.assertEquals("changed", this.dao.get(storedId).getExampleProperty1());
	}

	/**
	 * A DTO which already refers to an entity keeps that reference: the converted entity is returned, but not assigned to
	 * the DTO (see {@code DataTransferObjectConverter.toEntity})
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToEntityKeepsEntityOfDTO() throws Exception
	{
		ExampleEntity previousEntity = new ExampleEntity();
		ExampleDTO dto = this.createDTO(null, "new", 0);

		dto.setEntity(previousEntity);

		ExampleEntity entity = this.converter.toEntity(dto);

		Assert.assertNotSame(previousEntity, entity);
		Assert.assertSame(previousEntity, dto.getEntity());
	}

	/**
	 * Decision 5: the hook gets the DTO and the entity, after the {@code @AutoSet} properties were set
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToEntityCallsHookAfterAutoConversion() throws Exception
	{
		ExampleDTO dto = this.createDTO(null, "converted", 0);

		ExampleEntity entity = this.converter.toEntity(dto);

		Assert.assertSame(dto, this.converter.lastHookSource);
		Assert.assertSame(entity, this.converter.lastHookDestination);
		Assert.assertEquals("converted", this.converter.lastHookDestinationProperty1);
	}

	/**
	 * Converting no DTO results in no entity
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToEntityOfNullIsNull() throws Exception
	{
		Assert.assertNull(this.converter.toEntity(null));
	}

	/**
	 * Every DTO is converted, in the order of the collection
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToEntitiesConvertsEachDTO() throws Exception
	{
		Collection<ExampleDTO> dtos = Arrays.asList(this.createDTO(null, "first", 1), this.createDTO(null, "second", 2));

		ExampleEntity[] entities = this.converter.toEntities(dtos).toArray(new ExampleEntity[0]);

		Assert.assertEquals(2, entities.length);
		Assert.assertEquals("first", entities[0].getExampleProperty1());
		Assert.assertEquals(2, entities[1].getExampleProperty2());

		for (ExampleDTO dto : dtos)
		{
			Assert.assertNotNull(dto.getEntity());
			Assert.assertNull(dto.getId());
		}
	}

	/**
	 * {@link NoRepositoryException} is thrown as it is (not wrapped) if the converter has no repository
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testCreateEntityWithoutRepositoryThrows() throws Exception
	{
		ExampleDTOConverterImpl converterWithoutRepository = new ExampleDTOConverterImpl()
		{
			@Override
			public GenericRepository<Long, ExampleEntity, ?> getRepository()
			{
				return null;
			}
		};

		Assert.assertThrows(NoRepositoryException.class, () -> converterWithoutRepository.createEntity(this.createDTO(null, "no repository", 0)));
	}

	// ---------------------------------------------------------------------------------------------------------------------
	// entity to DTO
	// ---------------------------------------------------------------------------------------------------------------------

	/**
	 * Converting an entity sets the DTO's {@code @AutoSet} properties, its id (the entity's id) and its entity reference
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToDataTransferObjectOfEntitySetsPropertiesIdAndEntity() throws Exception
	{
		Long id = this.persist("stored");
		ExampleEntity entity = this.repository.get(id);

		ExampleDTO dto = this.converter.toDataTransferObject(entity);

		Assert.assertEquals("stored", dto.getExampleProperty1());
		Assert.assertEquals(42, dto.getExampleProperty2());
		Assert.assertEquals(id, dto.getId());
		Assert.assertSame(entity, dto.getEntity());
	}

	/**
	 * Every entity is converted, in the order of the collection
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToDataTransferObjectsFromEntitiesConvertsEachEntity() throws Exception
	{
		ExampleEntity first = this.repository.createEntity();
		ExampleEntity second = this.repository.createEntity();

		first.setExampleProperty1("first");
		second.setExampleProperty1("second");

		ExampleDTO[] dtos = this.converter.toDataTransferObjectsFromEntities(Arrays.asList(first, second)).toArray(new ExampleDTO[0]);

		Assert.assertEquals(2, dtos.length);
		Assert.assertEquals("first", dtos[0].getExampleProperty1());
		Assert.assertEquals(first.getId(), dtos[0].getId());
		Assert.assertSame(first, dtos[0].getEntity());
		Assert.assertEquals("second", dtos[1].getExampleProperty1());
		Assert.assertSame(second, dtos[1].getEntity());
	}

	/**
	 * Decision 5: the hook gets the entity and the DTO, after the {@code @AutoSet} properties were set
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToDataTransferObjectCallsHookAfterAutoConversion() throws Exception
	{
		ExampleEntity entity = this.repository.createEntity();

		entity.setExampleProperty1("converted");

		ExampleDTO dto = this.converter.toDataTransferObject(entity);

		Assert.assertSame(entity, this.converter.lastHookSource);
		Assert.assertSame(dto, this.converter.lastHookDestination);
		Assert.assertEquals("converted", this.converter.lastHookDestinationProperty1);
	}

	/**
	 * The instances an {@code @AutoSet} annotation names, by class or by class name, are the beans of the Spring application
	 * context, not new instances. By class name means the bean's name: a bean defined without id has its class name as alias.
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testAutoSetInstancesAreSpringBeans() throws Exception
	{
		ExampleDTO dto = this.converter.toDataTransferObject(this.repository.createEntity());

		Assert.assertEquals("Hello from Spring", dto.getGreeting());
		Assert.assertEquals("Hello from Spring", dto.getGreetingByClassName());
	}

	/**
	 * Decision 6: the flags of a DTO are a convention between server and client; converting never reads or sets them
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testConversionLeavesFlagsAlone() throws Exception
	{
		ExampleDTO dtoFromEntity = this.converter.toDataTransferObject(this.repository.createEntity());

		Assert.assertNull(dtoFromEntity.isDeleted());
		Assert.assertNull(dtoFromEntity.isAddedOrModified());

		ExampleDTO dto = this.createDTO(null, "flagged", 0);

		dto.setDeleted(true);
		dto.setAddedOrModified(false);

		ExampleEntity entity = this.converter.toEntity(dto);

		Assert.assertTrue(this.repository.isVirtualId(entity.getId()));
		Assert.assertEquals(Boolean.TRUE, dto.isDeleted());
		Assert.assertEquals(Boolean.FALSE, dto.isAddedOrModified());
	}

	// ---------------------------------------------------------------------------------------------------------------------
	// parameters to DTO and back (implemented by the converter)
	// ---------------------------------------------------------------------------------------------------------------------

	/**
	 * The converter's own implementation creates DTOs from parameters, also through a typed method of its interface
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToDataTransferObjectOfParameters() throws Exception
	{
		Assert.assertNotNull(this.converter.toDataTransferObject());
		Assert.assertNull(this.converter.toDataTransferObject((Object[]) null));

		ExampleDTO dto = this.converter.toDataTransferObject(new Object[] { "Hello world", 2 });

		Assert.assertEquals("Hello world", dto.getExampleProperty1());
		Assert.assertEquals(2, dto.getExampleProperty2());

		ExampleDTO typedDTO = this.converter.toDataTransferObject("Hello typed", 3);

		Assert.assertEquals("Hello typed", typedDTO.getExampleProperty1());
		Assert.assertEquals(3, typedDTO.getExampleProperty2());
	}

	/**
	 * Every parameter array is converted, in the order of the collection
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToDataTransferObjectsConvertsEachParameterArray() throws Exception
	{
		Collection<Object[]> params = new ArrayList<>(2);

		params.add(new Object[] { "first", 1 });
		params.add(new Object[] { "second", 2 });

		ExampleDTO[] dtos = this.converter.toDataTransferObjects(params).toArray(new ExampleDTO[0]);

		Assert.assertEquals(2, dtos.length);
		Assert.assertEquals("first", dtos[0].getExampleProperty1());
		Assert.assertEquals(2, dtos[1].getExampleProperty2());
	}

	/**
	 * Every DTO is converted to parameters, in the order of the collection
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testFromDataTransferObjectsConvertsEachDTO() throws Exception
	{
		Assert.assertNull(this.converter.fromDataTransferObject(null));

		Object[] results = this.converter.fromDataTransferObjects(Arrays.asList(this.createDTO(null, "first", 1), this.createDTO(null, "second", 2))).toArray();

		Assert.assertEquals(2, results.length);
		Assert.assertArrayEquals(new Object[] { "first", 1 }, (Object[]) results[0]);
		Assert.assertArrayEquals(new Object[] { "second", 2 }, (Object[]) results[1]);
	}

	/**
	 * The collection methods return null for no collection
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testCollectionMethodsReturnNullForNull() throws Exception
	{
		Assert.assertNull(this.converter.toEntities(null));
		Assert.assertNull(this.converter.toDataTransferObjectsFromEntities(null));
		Assert.assertNull(this.converter.toDataTransferObjects(null));
		Assert.assertNull(this.converter.fromDataTransferObjects(null));
	}
}
