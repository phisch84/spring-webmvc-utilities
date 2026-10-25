package com.schoste.ddd.application.v2.testfixtures;

import com.schoste.ddd.infrastructure.dao.v4.annotations.AutoSet;
import com.schoste.ddd.infrastructure.dao.v4.models.DataObject;

/**
 * Data object of the {@link ExampleEntity} used for testing
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class ExampleDO extends DataObject<Long>
{
	private static final long serialVersionUID = 1L;

	private String exampleProperty1;
	private int exampleProperty2;

	public String getExampleProperty1()
	{
		return exampleProperty1;
	}

	@AutoSet
	public void setExampleProperty1(String exampleProperty1)
	{
		this.exampleProperty1 = exampleProperty1;
	}

	public int getExampleProperty2()
	{
		return exampleProperty2;
	}

	@AutoSet
	public void setExampleProperty2(int exampleProperty2)
	{
		this.exampleProperty2 = exampleProperty2;
	}
}
