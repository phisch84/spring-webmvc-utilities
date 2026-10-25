package com.schoste.ddd.application.v2.testfixtures;

import org.springframework.beans.factory.annotation.Autowired;

import com.schoste.ddd.domain.v3.services.standard.GenericRepositoryImpl;

/**
 * Repository of {@link ExampleEntity} on the {@link ExampleDAOImpl} used for testing
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class ExampleRepositoryImpl extends GenericRepositoryImpl<Long, ExampleEntity, ExampleDO> implements ExampleRepository
{
	@Autowired
	private ExampleDAOImpl dao;

	@Override
	protected ExampleDAOImpl getDataAccessObject()
	{
		return this.dao;
	}
}
