package com.schoste.ddd.application.v2.dtos.mocked;

import org.springframework.beans.factory.annotation.Autowired;

import com.schoste.ddd.application.v2.dtos.MockedDataTransferObjectA;
import com.schoste.ddd.application.v2.dtos.MockedDataTransferObjectAConverter;
import com.schoste.ddd.application.v2.dtos.standard.DataTransferObjectConverterImpl;
import com.schoste.ddd.application.v2.exceptions.ApplicationException;
import com.schoste.ddd.application.v2.testfixtures.ExampleEntity;
import com.schoste.ddd.application.v2.testfixtures.ExampleRepository;
import com.schoste.ddd.domain.v3.services.GenericRepository;

public class MockedDataTransferObjectAConverterImpl extends DataTransferObjectConverterImpl<Long, ExampleEntity, MockedDataTransferObjectA> implements MockedDataTransferObjectAConverter
{
	@Autowired
	protected ExampleRepository repository;

	@Override
	public GenericRepository<Long, ExampleEntity, ?> getRepository()
	{
		return this.repository;
	}

	@Override
	public MockedDataTransferObjectA createDataTransferObject()
	{
		return new MockedDataTransferObjectA();
	}

	@Override
	public MockedDataTransferObjectA toDataTransferObject(Object... param) throws ApplicationException
	{
		if (param == null) return null;
		
		MockedDataTransferObjectA dto = this.createDataTransferObject();
		
		if ((param.length > 0) && (param[0] instanceof String)) dto.setExampleProperty1((String) param[0]);
		if ((param.length > 1) && (param[1] instanceof Integer)) dto.setExampleProperty2((Integer) param[1]);

		return dto;
	}

	@Override
	public Object fromDataTransferObject(MockedDataTransferObjectA dto) throws ApplicationException
	{
		if (dto == null) return null;

		return new Object[] { dto.getExampleProperty1(), dto.getExampleProperty2() };
	}

	@Override
	public MockedDataTransferObjectA toDataTransferObject(String prop1, int prop2) throws Exception
	{
		return this.toDataTransferObject(new Object[] { prop1, prop2 });
	}

	@Override
	protected void afterAutoConversion(ExampleEntity sourceEntity,
			MockedDataTransferObjectA destinationDataTransferObject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void afterAutoConversion(MockedDataTransferObjectA sourceDataTransferObject,
			ExampleEntity destinationEntity) {
		// TODO Auto-generated method stub
		
	}
}
