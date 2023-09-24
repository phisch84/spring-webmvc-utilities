package com.schoste.ddd.infrastructure.dal.v2.services.mocked;

import com.schoste.ddd.infrastructure.dal.v2.exceptions.DALException;
import com.schoste.ddd.infrastructure.dal.v2.models.MockedDataObjectA;
import com.schoste.ddd.infrastructure.dal.v2.services.MockedDataObjectADAO;

public class MockedDataObjectADAOImpl extends GenericMockedDAO<MockedDataObjectA> implements MockedDataObjectADAO
{
	@Override
	public MockedDataObjectA createDataObject() throws DALException
	{
		return new MockedDataObjectA();
	}

}
