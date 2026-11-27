package com.schoste.ddd.application.v2.testfixtures;

import com.schoste.ddd.application.v2.dtos.DataTransferObjectConverter;

/**
 * Converter between {@link ExampleEntity} and {@link ExampleDTO}, with a typed factory method as projects usually add one
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public interface ExampleDTOConverter extends DataTransferObjectConverter<Long, ExampleEntity, ExampleDTO>
{
	/**
	 * Creates a DTO from its properties
	 *
	 * @param exampleProperty1 the first property
	 * @param exampleProperty2 the second property
	 * @return the DTO
	 * @throws Exception re-throws every exception
	 */
	ExampleDTO toDataTransferObject(String exampleProperty1, int exampleProperty2) throws Exception;
}
