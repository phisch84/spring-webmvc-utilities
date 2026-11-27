package com.schoste.ddd.application.v2.testfixtures;

import com.schoste.ddd.infrastructure.dao.v4.inmemory.AbstractInMemoryDAO;

/**
 * In-memory DAO of {@link ExampleDO} used for testing.
 * Virtual ids are negative, actual ids are positive.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class ExampleDAOImpl extends AbstractInMemoryDAO<ExampleDO>
{
	protected long lastVirtualId = 0;

	@Override
	protected ExampleDO doCreateDataObject() throws Exception
	{
		return new ExampleDO();
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
