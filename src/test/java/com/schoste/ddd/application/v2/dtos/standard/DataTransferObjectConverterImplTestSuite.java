package com.schoste.ddd.application.v2.dtos.standard;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.schoste.ddd.application.v2.dtos.MockedDataTransferObjectA;
import com.schoste.ddd.application.v2.dtos.MockedDataTransferObjectAConverter;
import com.schoste.ddd.application.v2.dtos.mocked.MockedDataTransferObjectAConverterImpl;
import com.schoste.ddd.application.v2.exceptions.EntityNotFoundException;
import com.schoste.ddd.application.v2.exceptions.NoRepositoryException;
import com.schoste.ddd.application.v2.testfixtures.ExampleDAOImpl;
import com.schoste.ddd.application.v2.testfixtures.ExampleDO;
import com.schoste.ddd.application.v2.testfixtures.ExampleEntity;
import com.schoste.ddd.application.v2.testfixtures.ExampleRepository;
import com.schoste.ddd.domain.v3.services.GenericRepository;
import com.schoste.ddd.domain.v3.services.UnitOfWork;

/**
 * Test suite for the version 1 implementation of <@see DataTransferObjectConverter>
 * 
 * @author Philipp Schosteritsch <s.philipp@schoste.com>
 */
abstract public class DataTransferObjectConverterImplTestSuite
{
	@Autowired
	protected ExampleRepository repository;
	
	@Autowired
	protected MockedDataTransferObjectAConverter dtoConverter;

	@Autowired
	protected ExampleDAOImpl dao;

	protected abstract UnitOfWork getUnitOfWork() throws Exception;

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
		this.dao.save(dataObject);

		Assert.assertFalse(this.dao.isVirtualId(dataObject.getId()));

		return dataObject.getId();
	}

	protected MockedDataTransferObjectA createDTO(Long id, String exampleProperty1)
	{
		MockedDataTransferObjectA dto = new MockedDataTransferObjectA();

		dto.setId(id);
		dto.setExampleProperty1(exampleProperty1);

		return dto;
	}

	/**
	 * Asserts that a DTO without id is converted to a new entity with a virtual id from the repository,
	 * and that the DTO keeps its id
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToEntityWithNullIdCreatesEntity() throws Exception
	{
		MockedDataTransferObjectA dto = this.createDTO(null, "new");

		ExampleEntity entity = this.dtoConverter.toEntity(dto);

		Assert.assertNotNull(entity);
		Assert.assertNotNull(entity.getId());
		Assert.assertTrue(this.repository.isVirtualId(entity.getId()));
		Assert.assertEquals("new", entity.getExampleProperty1());
		Assert.assertNull(dto.getId());
		Assert.assertSame(entity, dto.getEntity());
	}

	/**
	 * Asserts that a DTO with a virtual id (e.g. one the client made up) is converted to a new entity with a virtual id
	 * from the repository, and that the DTO keeps its id
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToEntityWithVirtualIdCreatesEntity() throws Exception
	{
		MockedDataTransferObjectA dto = this.createDTO(-4711L, "new");

		ExampleEntity entity = this.dtoConverter.toEntity(dto);

		Assert.assertNotNull(entity);
		Assert.assertTrue(this.repository.isVirtualId(entity.getId()));
		Assert.assertNotEquals(Long.valueOf(-4711L), entity.getId());
		Assert.assertEquals(Long.valueOf(-4711L), dto.getId());
		Assert.assertSame(entity, dto.getEntity());
	}

	/**
	 * Asserts that the client's virtual ids never become the ids of entities, even if they equal virtual ids of the repository
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToEntitiesWithVirtualIdsCreateDistinctEntities() throws Exception
	{
		Collection<MockedDataTransferObjectA> dtos = new ArrayList<>(2);

		dtos.add(this.createDTO(-1L, "new 1"));
		dtos.add(this.createDTO(-1L, "new 2"));

		Collection<ExampleEntity> entities = this.dtoConverter.toEntities(dtos);
		ExampleEntity[] entityArray = entities.toArray(new ExampleEntity[0]);

		Assert.assertEquals(2, entityArray.length);
		Assert.assertNotEquals(entityArray[0].getId(), entityArray[1].getId());
		Assert.assertEquals("new 1", entityArray[0].getExampleProperty1());
		Assert.assertEquals("new 2", entityArray[1].getExampleProperty1());
	}

	/**
	 * Asserts that a DTO with an actual id is converted to the repository's entity with this id
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToEntityWithActualIdGetsEntity() throws Exception
	{
		Long id = this.persist("stored");
		MockedDataTransferObjectA dto = this.createDTO(id, "changed");

		ExampleEntity entity = this.dtoConverter.toEntity(dto);

		Assert.assertSame(this.repository.get(id), entity);
		Assert.assertEquals(id, entity.getId());
		Assert.assertEquals("changed", entity.getExampleProperty1());
		Assert.assertSame(entity, dto.getEntity());
	}

	/**
	 * Asserts that a DTO with an actual id which the DAO doesn't know causes an {@link EntityNotFoundException}
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToEntityWithUnknownIdThrows() throws Exception
	{
		MockedDataTransferObjectA dto = this.createDTO(4711L, "unknown");

		EntityNotFoundException e = Assert.assertThrows(EntityNotFoundException.class, () -> this.dtoConverter.toEntity(dto));

		Assert.assertEquals(Long.valueOf(4711L), e.getId());
		Assert.assertNull(dto.getEntity());
	}

	/**
	 * Asserts that a DTO with the id of a deleted data object causes an {@link EntityNotFoundException}
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToEntityWithDeletedIdThrows() throws Exception
	{
		Long id = this.persist("deleted");

		this.dao.deleteById(id);

		MockedDataTransferObjectA dto = this.createDTO(id, "deleted");

		EntityNotFoundException e = Assert.assertThrows(EntityNotFoundException.class, () -> this.dtoConverter.toEntity(dto));

		Assert.assertEquals(id, e.getId());
	}

	/**
	 * Asserts that a DTO with the id of an entity whose removal is pending causes an {@link EntityNotFoundException}
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToEntityWithPendingRemovalThrows() throws Exception
	{
		Long id = this.persist("removed");

		try (UnitOfWork uow = this.getUnitOfWork())
		{
			this.repository.remove(this.repository.get(id));

			MockedDataTransferObjectA dto = this.createDTO(id, "removed");

			EntityNotFoundException e = Assert.assertThrows(EntityNotFoundException.class, () -> this.dtoConverter.toEntity(dto));

			Assert.assertEquals(id, e.getId());
		}
	}

	/**
	 * Asserts that converting a DTO to an entity doesn't add the entity to the repository: committing doesn't store it
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testToEntityDoesNotAddEntity() throws Exception
	{
		try (UnitOfWork uow = this.getUnitOfWork())
		{
			this.dtoConverter.toEntity(this.createDTO(null, "not added"));

			uow.commit();
		}

		Assert.assertTrue(this.dao.getDataObjectIds().isEmpty());
	}

	/**
	 * Asserts that {@link NoRepositoryException} is thrown as it is (not wrapped) if the converter has no repository
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testCreateEntityWithoutRepositoryThrows() throws Exception
	{
		MockedDataTransferObjectAConverterImpl converter = new MockedDataTransferObjectAConverterImpl()
		{
			@Override
			public GenericRepository<Long, ExampleEntity, ?> getRepository()
			{
				return null;
			}
		};

		Assert.assertThrows(NoRepositoryException.class, () -> converter.createEntity(this.createDTO(null, "no repository")));
	}

	@Test
	public void testToDataTransferObjectEmpty() throws Exception
	{
		Assert.assertNotNull(this.dtoConverter.toDataTransferObject());
	}

	@Test
	public void testToDataTransferObjectNull() throws Exception
	{
		Assert.assertNull(this.dtoConverter.toDataTransferObject((Object[])null));
	}

	@Test
	public void testToDataTransferObject() throws Exception
	{
		MockedDataTransferObjectA dto = this.dtoConverter.toDataTransferObject(new Object[] { "Hello world", 2 });

		Assert.assertNotNull(dto);
		Assert.assertEquals("Hello world", dto.getExampleProperty1());
		Assert.assertEquals(2, dto.getExampleProperty2());
	}

	@Test
	public void testToDataTransferObjectInterfaced() throws Exception
	{
		MockedDataTransferObjectA dto = this.dtoConverter.toDataTransferObject("Hello world", 2);

		Assert.assertNotNull(dto);
		Assert.assertEquals("Hello world", dto.getExampleProperty1());
		Assert.assertEquals(2, dto.getExampleProperty2());
	}

	@Test
	public void testToDataTransferObjects() throws Exception
	{
		Object[] param1 = new Object[2];
		Object[] param2 = new Object[2];
		Collection<Object[]> params = new ArrayList<>(2);
		
		param1[0] = "Hello world";
		param1[1] = 2;
		param2[0] = "Hello world";
		param2[1] = 2;
		
		params.add(param1);
		params.add(param2);
		
		Collection<MockedDataTransferObjectA> dtos = this.dtoConverter.toDataTransferObjects(params);
		
		Assert.assertNotNull(dtos);
		Assert.assertEquals(2, dtos.size());
		
		for (MockedDataTransferObjectA dto : dtos)
		{
			Assert.assertEquals("Hello world", dto.getExampleProperty1());
			Assert.assertEquals(2, dto.getExampleProperty2());
		}
	}

	@Test
	public void testToDataTransferObjectsFromEntities() throws Exception
	{
		ExampleEntity do1 = this.repository.createEntity();
		ExampleEntity do2 = this.repository.createEntity();
		Collection<ExampleEntity> entities = new ArrayList<>(2);
		
		do1.setExampleProperty1("Hello world");
		do1.setExampleProperty2(2);
		do2.setExampleProperty1("Hello world");
		do2.setExampleProperty2(2);
		
		entities.add(do1);
		entities.add(do2);
		
		Collection<MockedDataTransferObjectA> dtos = this.dtoConverter.toDataTransferObjectsFromEntities(entities);
		
		Assert.assertNotNull(dtos);
		Assert.assertEquals(2, dtos.size());
		
		for (MockedDataTransferObjectA dto : dtos)
		{
			Assert.assertEquals("Hello world", dto.getExampleProperty1());
			Assert.assertEquals(2, dto.getExampleProperty2());
			Assert.assertNotNull(dto.getEntity());
			Assert.assertEquals(dto.getEntity().getId(), dto.getId());
		}
	}

	@Test
	public void testFromDataTransferObjectNull() throws Exception
	{
		Assert.assertNull(this.dtoConverter.fromDataTransferObject((MockedDataTransferObjectA)null));
	}

	@Test
	public void testFromDataTransferObject() throws Exception
	{
		MockedDataTransferObjectA dto = new MockedDataTransferObjectA();

		dto.setExampleProperty1("Hello world");
		dto.setExampleProperty2(2);

		Object[] result = (Object[])this.dtoConverter.fromDataTransferObject(dto);

		Assert.assertNotNull(result);
		Assert.assertEquals(2, result.length);
		Assert.assertEquals("Hello world", result[0]);
		Assert.assertEquals(2, result[1]);
	}

	@Test
	public void testFromDataTransferObjects() throws Exception
	{
		MockedDataTransferObjectA dto1 = new MockedDataTransferObjectA();
		MockedDataTransferObjectA dto2 = new MockedDataTransferObjectA();
		Collection<MockedDataTransferObjectA> dtos = new ArrayList<>(2);
		
		dto1.setExampleProperty1("Hello world");
		dto1.setExampleProperty2(2);
		dtos.add(dto1);
		dto2.setExampleProperty1("Hello world");
		dto2.setExampleProperty2(2);
		dtos.add(dto2);

		Collection<Object> results = this.dtoConverter.fromDataTransferObjects(dtos);

		Assert.assertNotNull(results);
		Assert.assertEquals(2, results.size());
		
		for (Object result : results)
		{
			Object[] result_ = (Object[]) result;

			Assert.assertEquals(2, result_.length);
			Assert.assertEquals("Hello world", result_[0]);
			Assert.assertEquals(2, result_[1]);
		}
	}

	@Test
	public void testToEntity() throws Exception
	{
		MockedDataTransferObjectA dto = new MockedDataTransferObjectA();
		dto.setExampleProperty1("Hello world");
		dto.setExampleProperty2(2);

		ExampleEntity dObj = this.dtoConverter.toEntity(dto);

		Assert.assertNotNull(dObj);
		Assert.assertEquals("Hello world", dObj.getExampleProperty1());
		Assert.assertEquals(2, dObj.getExampleProperty2());
		Assert.assertSame(dObj, dto.getEntity());
		Assert.assertNull(dto.getId());
	}

	@Test
	public void testToEntities() throws Exception
	{
		MockedDataTransferObjectA dto1 = new MockedDataTransferObjectA();
		MockedDataTransferObjectA dto2 = new MockedDataTransferObjectA();
		Collection<MockedDataTransferObjectA> dtos = new ArrayList<>(2);

		dto1.setExampleProperty1("Hello world");
		dto1.setExampleProperty2(2);
		dtos.add(dto1);
		dto2.setExampleProperty1("Hello world");
		dto2.setExampleProperty2(2);
		dtos.add(dto2);

		Collection<ExampleEntity> dObjs = this.dtoConverter.toEntities(dtos);

		Assert.assertNotNull(dObjs);
		Assert.assertEquals(2, dObjs.size());
		
		for (ExampleEntity dObj : dObjs)
		{
			Assert.assertEquals("Hello world", dObj.getExampleProperty1());
			Assert.assertEquals(2, dObj.getExampleProperty2());
		}

		for (MockedDataTransferObjectA dto : dtos)
		{
			Assert.assertNotNull(dto.getEntity());
			Assert.assertNull(dto.getId());
		}
	}

	@Test
	public void testToEntitiesCommit() throws Exception
	{
		MockedDataTransferObjectA dto1 = new MockedDataTransferObjectA();
		MockedDataTransferObjectA dto2 = new MockedDataTransferObjectA();
		Collection<MockedDataTransferObjectA> dtos = new ArrayList<>(2);

		try(UnitOfWork uow = this.getUnitOfWork())
		{
			dto1.setId(-1L);
			dto1.setExampleProperty1("Hello world 1");
			dto1.setExampleProperty2(1);
			dtos.add(dto1);
			dto2.setId(-2L);
			dto2.setExampleProperty1("Hello world 2");
			dto2.setExampleProperty2(2);
			dtos.add(dto2);

			Collection<ExampleEntity> dObjs = this.dtoConverter.toEntities(dtos);

			for (ExampleEntity dObj : dObjs) this.repository.add(dObj);

			uow.commit();

			Assert.assertTrue(true);
		}

		for (MockedDataTransferObjectA dto : dtos)
		{
			Assert.assertNotNull(dto.getEntity());
			Assert.assertNotNull(dto.getEntity().getId());
			Assert.assertNotEquals(dto.getEntity().getId(), dto.getId());
		}
	}
}
