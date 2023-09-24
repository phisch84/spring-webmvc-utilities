package com.schoste.ddd.application.v1.dtos.standard;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.schoste.ddd.application.v1.dtos.MockedDataTransferObjectA;
import com.schoste.ddd.application.v1.dtos.MockedDataTransferObjectAConverter;
import com.schoste.ddd.domain.v1.models.MockedDomainObjectA;
import com.schoste.ddd.domain.v1.services.MockedDomainObjectARepository;
import com.schoste.ddd.domain.v1.services.UnitOfWork;

/**
 * Test suite for the version 1 implementation of <@see DataTransferObjectConverter>
 * 
 * @author Philipp Schosteritsch <s.philipp@schoste.com>
 */
abstract public class DataTransferObjectConverterImplTestSuite
{
	@Autowired
	protected MockedDomainObjectARepository repository;
	
	@Autowired
	protected MockedDataTransferObjectAConverter dtoConverter;

	protected abstract UnitOfWork getUnitOfWork() throws Exception;

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
	public void testToDataTransferObjectsFromDomainObjects() throws Exception
	{
		MockedDomainObjectA do1 = this.repository.createObject();
		MockedDomainObjectA do2 = this.repository.createObject();
		Collection<MockedDomainObjectA> domainObjects = new ArrayList<>(2);
		
		do1.setExampleProperty1("Hello world");
		do1.setExampleProperty2(2);
		do2.setExampleProperty1("Hello world");
		do2.setExampleProperty2(2);
		
		domainObjects.add(do1);
		domainObjects.add(do2);
		
		Collection<MockedDataTransferObjectA> dtos = this.dtoConverter.toDataTransferObjectsFromDomainObjects(domainObjects);
		
		Assert.assertNotNull(dtos);
		Assert.assertEquals(2, dtos.size());
		
		for (MockedDataTransferObjectA dto : dtos)
		{
			Assert.assertEquals("Hello world", dto.getExampleProperty1());
			Assert.assertEquals(2, dto.getExampleProperty2());
			Assert.assertNotNull(dto.getDomainObject());
			Assert.assertEquals(dto.getDomainObject().getId(), dto.getId());
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
	public void testToDomainObject() throws Exception
	{
		MockedDataTransferObjectA dto = new MockedDataTransferObjectA();
		dto.setExampleProperty1("Hello world");
		dto.setExampleProperty2(2);

		MockedDomainObjectA dObj = this.dtoConverter.toDomainObject(dto);

		Assert.assertNotNull(dObj);
		Assert.assertEquals("Hello world", dObj.getExampleProperty1());
		Assert.assertEquals(2, dObj.getExampleProperty2());
		Assert.assertNotNull(dto.getDomainObject());
		Assert.assertEquals(dto.getDomainObject().getId(), dto.getId());
	}

	@Test
	public void testToDomainObjects() throws Exception
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

		Collection<MockedDomainObjectA> dObjs = this.dtoConverter.toDomainObjects(dtos);

		Assert.assertNotNull(dObjs);
		Assert.assertEquals(2, dObjs.size());
		
		for (MockedDomainObjectA dObj : dObjs)
		{
			Assert.assertEquals("Hello world", dObj.getExampleProperty1());
			Assert.assertEquals(2, dObj.getExampleProperty2());
		}

		for (MockedDataTransferObjectA dto : dtos)
		{
			Assert.assertNotNull(dto.getDomainObject());
			Assert.assertEquals(dto.getDomainObject().getId(), dto.getId());
		}
	}

	@Test
	public void testToDomainObjectsCommit() throws Exception
	{
		MockedDataTransferObjectA dto1 = new MockedDataTransferObjectA();
		MockedDataTransferObjectA dto2 = new MockedDataTransferObjectA();
		Collection<MockedDataTransferObjectA> dtos = new ArrayList<>(2);

		try(UnitOfWork uow = this.getUnitOfWork())
		{
			dto1.setId(-1);
			dto1.setExampleProperty1("Hello world 1");
			dto1.setExampleProperty2(1);
			dtos.add(dto1);
			dto2.setId(-2);
			dto2.setExampleProperty1("Hello world 2");
			dto2.setExampleProperty2(2);
			dtos.add(dto2);

			Collection<MockedDomainObjectA> dObjs = this.dtoConverter.toDomainObjects(dtos);

			for (MockedDomainObjectA dObj : dObjs) this.repository.add(dObj);

			uow.commit();

			Assert.assertTrue(true);
		}

		for (MockedDataTransferObjectA dto : dtos)
		{
			Assert.assertNotNull(dto.getDomainObject());
			Assert.assertNotEquals(0, dto.getDomainObject().getId());
			Assert.assertNotEquals(dto.getDomainObject().getId(), dto.getId());
		}
	}
}
