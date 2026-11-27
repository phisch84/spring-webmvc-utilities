package com.schoste.ddd.application.v2.testfixtures;

import com.schoste.ddd.application.v2.dtos.DataTransferObject;
import com.schoste.ddd.domain.v3.annotations.AutoSet;

/**
 * DTO of an {@link ExampleEntity}. Its first two properties are converted from the entity's getters; the greetings are taken
 * from the Spring bean {@link ExampleGreetingService}, once named by class and once by class name.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class ExampleDTO extends DataTransferObject<Long>
{
	private static final long serialVersionUID = 1L;

	private String exampleProperty1;
	private int exampleProperty2;
	private String greeting;
	private String greetingByClassName;

	public String getExampleProperty1()
	{
		return this.exampleProperty1;
	}

	@AutoSet
	public void setExampleProperty1(String exampleProperty1)
	{
		this.exampleProperty1 = exampleProperty1;
	}

	public int getExampleProperty2()
	{
		return this.exampleProperty2;
	}

	@AutoSet
	public void setExampleProperty2(int exampleProperty2)
	{
		this.exampleProperty2 = exampleProperty2;
	}

	public String getGreeting()
	{
		return this.greeting;
	}

	/**
	 * Set from the bean of the class {@link ExampleGreetingService}
	 *
	 * @param greeting the greeting
	 */
	@AutoSet(clazz = ExampleGreetingService.class, methodName = "getGreeting")
	public void setGreeting(String greeting)
	{
		this.greeting = greeting;
	}

	public String getGreetingByClassName()
	{
		return this.greetingByClassName;
	}

	/**
	 * Set from the bean named by the full class name of {@link ExampleGreetingService}
	 *
	 * @param greetingByClassName the greeting
	 */
	@AutoSet(className = "com.schoste.ddd.application.v2.testfixtures.ExampleGreetingService", methodName = "getGreeting")
	public void setGreetingByClassName(String greetingByClassName)
	{
		this.greetingByClassName = greetingByClassName;
	}
}
