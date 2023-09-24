package com.schoste.ddd.domain.v1.models;

import com.schoste.ddd.domain.v1.annotations.AutoSet;

/**
 * Dummy domain object used for testing
 * 
 * @author Philipp Schosteritsch <s.philipp@schoste.com>
 */
public class MockedDomainObjectA extends BasicDomainObject implements DomainObject
{
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
