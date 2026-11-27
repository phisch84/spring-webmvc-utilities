package com.schoste.ddd.application.v2.examples;

import com.schoste.ddd.infrastructure.dao.v4.annotations.AutoSet;
import com.schoste.ddd.infrastructure.dao.v4.models.DataObject;

/**
 * Data object of {@link Customer}
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class CustomerDO extends DataObject<Long>
{
	private static final long serialVersionUID = 1L;

	private String name;

	public String getName()
	{
		return this.name;
	}

	@AutoSet
	public void setName(String name)
	{
		this.name = name;
	}
}
