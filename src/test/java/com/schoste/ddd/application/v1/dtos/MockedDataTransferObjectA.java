package com.schoste.ddd.application.v1.dtos;

import com.schoste.ddd.application.v1.dtos.DataTransferObject;
import com.schoste.ddd.domain.v1.annotations.AutoSet;

public class MockedDataTransferObjectA extends DataTransferObject
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
	public void setExampleProperty2(int exampleProperty2) {
		this.exampleProperty2 = exampleProperty2;
	}
}
