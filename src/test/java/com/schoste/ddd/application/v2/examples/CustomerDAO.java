package com.schoste.ddd.application.v2.examples;

import com.schoste.ddd.infrastructure.dao.v4.inmemory.AbstractInMemoryDAO;

/**
 * In-memory DAO of {@link CustomerDO}, with negative virtual ids
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class CustomerDAO extends AbstractInMemoryDAO<CustomerDO>
{
	protected long lastVirtualId = 0;

	@Override
	protected CustomerDO doCreateDataObject() throws Exception
	{
		return new CustomerDO();
	}

	@Override
	protected boolean doIsVirtualId(Long id)
	{
		return (id < 0);
	}

	@Override
	protected synchronized Long doCreateVirtualId() throws Exception
	{
		return --this.lastVirtualId;
	}
}
