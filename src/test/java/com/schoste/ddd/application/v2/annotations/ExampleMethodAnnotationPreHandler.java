package com.schoste.ddd.application.v2.annotations;

import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.schoste.ddd.application.v2.interceptor.GenericAnnotationPreHandler;

/**
 * Example implementation of a handler for the {@link ExampleMethodAnnotation} annotation.
 * 
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class ExampleMethodAnnotationPreHandler  extends GenericAnnotationPreHandler<ExampleMethodAnnotation> implements ExampleMethodAnnotation
{
	protected final AtomicInteger invocations = new AtomicInteger();

	/**
	 * Gets how often the handler has handled a request
	 *
	 * @return the number of handled requests
	 */
	public int getInvocations()
	{
		return this.invocations.get();
	}

	/**
	 * Unused interface implementation
	 */
	@Override
	public String exampleProperty()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean handle(ExampleMethodAnnotation what, HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		this.invocations.incrementAndGet();

		System.out.println(String.format("Method handler (%s): %s%s", System.identityHashCode(this), System.identityHashCode(what), what));

		return true;
	}

}
