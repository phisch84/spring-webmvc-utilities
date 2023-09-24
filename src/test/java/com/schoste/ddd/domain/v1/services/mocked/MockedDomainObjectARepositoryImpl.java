package com.schoste.ddd.domain.v1.services.mocked;

import org.springframework.beans.factory.annotation.Autowired;

import com.schoste.ddd.domain.v1.models.MockedDomainObjectA;
import com.schoste.ddd.domain.v1.services.MockedDomainObjectARepository;
import com.schoste.ddd.domain.v1.services.standard.GenericRepositoryImpl;
import com.schoste.ddd.infrastructure.dal.v2.models.MockedDataObjectA;
import com.schoste.ddd.infrastructure.dal.v2.services.MockedDataObjectADAO;

public class MockedDomainObjectARepositoryImpl extends GenericRepositoryImpl<MockedDomainObjectA, MockedDataObjectA> implements MockedDomainObjectARepository
{
	@Autowired
	private MockedDataObjectADAO dao;

	@Override
	protected MockedDataObjectADAO getDataAccessObject()
	{
		return this.dao;
	}

	@Override
	protected void afterAutoConversation(MockedDataObjectA dataObject, MockedDomainObjectA domainObject) throws Exception
	{
	}

	@Override
	protected void afterAutoConversation(MockedDomainObjectA domainObject, MockedDataObjectA dataObject) throws Exception
	{
	}
}
