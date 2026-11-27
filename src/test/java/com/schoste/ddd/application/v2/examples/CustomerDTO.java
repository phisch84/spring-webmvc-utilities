package com.schoste.ddd.application.v2.examples;

import com.schoste.ddd.application.v2.dtos.DataTransferObject;
import com.schoste.ddd.domain.v3.annotations.AutoSet;

/**
 * DTO of {@link Customer}
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class CustomerDTO extends DataTransferObject<Long>
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
