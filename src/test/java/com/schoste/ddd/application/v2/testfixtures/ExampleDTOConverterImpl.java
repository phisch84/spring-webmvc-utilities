package com.schoste.ddd.application.v2.testfixtures;

import org.springframework.beans.factory.annotation.Autowired;

import com.schoste.ddd.application.v2.dtos.standard.DataTransferObjectConverterImpl;
import com.schoste.ddd.application.v2.exceptions.ApplicationException;
import com.schoste.ddd.domain.v3.services.GenericRepository;

/**
 * Converter of the tests. Its hooks record what they get, and the value of the first property of the destination at the
 * time they are called, so tests can check that the hooks run after the automatic conversion.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class ExampleDTOConverterImpl extends DataTransferObjectConverterImpl<Long, ExampleEntity, ExampleDTO> implements ExampleDTOConverter
{
	@Autowired
	protected ExampleRepository repository;

	/**
	 * The source the last hook was called with (an entity or a DTO)
	 */
	public Object lastHookSource = null;

	/**
	 * The destination the last hook was called with (a DTO or an entity)
	 */
	public Object lastHookDestination = null;

	/**
	 * The first property of the destination when the last hook was called
	 */
	public String lastHookDestinationProperty1 = null;

	@Override
	public GenericRepository<Long, ExampleEntity, ?> getRepository()
	{
		return this.repository;
	}

	@Override
	public ExampleDTO createDataTransferObject()
	{
		return new ExampleDTO();
	}

	@Override
	public ExampleDTO toDataTransferObject(Object... param) throws ApplicationException
	{
		if (param == null) return null;

		ExampleDTO dto = this.createDataTransferObject();

		if ((param.length > 0) && (param[0] instanceof String)) dto.setExampleProperty1((String) param[0]);
		if ((param.length > 1) && (param[1] instanceof Integer)) dto.setExampleProperty2((Integer) param[1]);

		return dto;
	}

	@Override
	public Object fromDataTransferObject(ExampleDTO dto) throws ApplicationException
	{
		if (dto == null) return null;

		return new Object[] { dto.getExampleProperty1(), dto.getExampleProperty2() };
	}

	@Override
	public ExampleDTO toDataTransferObject(String exampleProperty1, int exampleProperty2) throws Exception
	{
		return this.toDataTransferObject(new Object[] { exampleProperty1, exampleProperty2 });
	}

	@Override
	protected void afterAutoConversion(ExampleEntity sourceEntity, ExampleDTO destinationDataTransferObject)
	{
		this.lastHookSource = sourceEntity;
		this.lastHookDestination = destinationDataTransferObject;
		this.lastHookDestinationProperty1 = destinationDataTransferObject.getExampleProperty1();
	}

	@Override
	protected void afterAutoConversion(ExampleDTO sourceDataTransferObject, ExampleEntity destinationEntity)
	{
		this.lastHookSource = sourceDataTransferObject;
		this.lastHookDestination = destinationEntity;
		this.lastHookDestinationProperty1 = destinationEntity.getExampleProperty1();
	}
}
