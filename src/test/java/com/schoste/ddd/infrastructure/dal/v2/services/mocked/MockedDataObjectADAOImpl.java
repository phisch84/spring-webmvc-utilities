package com.schoste.ddd.infrastructure.dal.v2.services.mocked;

import com.schoste.ddd.infrastructure.dal.v2.exceptions.DALException;
import com.schoste.ddd.infrastructure.dal.v2.models.MockedDataObjectA;
import com.schoste.ddd.infrastructure.dal.v2.services.LazyLoader;
import com.schoste.ddd.infrastructure.dal.v2.services.MockedDataObjectADAO;

public class MockedDataObjectADAOImpl extends GenericMockedDAO<MockedDataObjectA> implements MockedDataObjectADAO
{
	@Override
	public MockedDataObjectA createDataObject() throws DALException
	{
		return new MockedDataObjectA();
	}

	@Override
	protected LazyLoader<Integer, MockedDataObjectA> createLazyLoader() throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'createLazyLoader'");
	}

}
