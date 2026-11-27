package com.schoste.ddd.application.v2.testfixtures;

/**
 * Spring bean whose getter {@link ExampleDTO} names in its {@code @AutoSet} annotations. The bean gets a different greeting
 * than a new instance, so tests can tell whether the converter took the bean.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class ExampleGreetingService
{
	/**
	 * The greeting of an instance which wasn't configured by Spring
	 */
	public static final String DEFAULT_GREETING = "Hello from a new instance";

	private String greeting = DEFAULT_GREETING;

	public String getGreeting()
	{
		return this.greeting;
	}

	public void setGreeting(String greeting)
	{
		this.greeting = greeting;
	}
}
