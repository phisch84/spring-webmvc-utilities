package com.schoste.ddd.application.v2.examples;

import com.schoste.ddd.domain.v3.annotations.AutoSet;
import com.schoste.ddd.domain.v3.entities.BasicEntity;

/**
 * Entity of the converter example
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class Customer extends BasicEntity<Long>
{
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
