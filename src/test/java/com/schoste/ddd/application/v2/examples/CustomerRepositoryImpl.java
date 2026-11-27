package com.schoste.ddd.application.v2.examples;

import org.springframework.beans.factory.annotation.Autowired;

import com.schoste.ddd.domain.v3.services.standard.GenericRepositoryImpl;

/**
 * Repository of {@link Customer}, on the {@link CustomerDAO}
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class CustomerRepositoryImpl extends GenericRepositoryImpl<Long, Customer, CustomerDO> implements CustomerRepository
{
	@Autowired
	private CustomerDAO dao;

	@Override
	protected CustomerDAO getDataAccessObject()
	{
		return this.dao;
	}
}
