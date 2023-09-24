package com.schoste.ddd.application.v1.dtos;

import com.schoste.ddd.application.v1.dtos.DataTransferObjectConverter;
import com.schoste.ddd.domain.v1.models.MockedDomainObjectA;

public interface MockedDataTransferObjectAConverter extends DataTransferObjectConverter<MockedDomainObjectA, MockedDataTransferObjectA>
{
	MockedDataTransferObjectA toDataTransferObject(String prop1, int prop2) throws Exception;
}
