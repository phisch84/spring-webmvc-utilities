package com.schoste.ddd.application.v2.dtos;

import com.schoste.ddd.application.v2.dtos.DataTransferObjectConverter;
import com.schoste.ddd.application.v2.testfixtures.ExampleEntity;

public interface MockedDataTransferObjectAConverter extends DataTransferObjectConverter<Long, ExampleEntity, MockedDataTransferObjectA>
{
	MockedDataTransferObjectA toDataTransferObject(String prop1, int prop2) throws Exception;
}
