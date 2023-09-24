package com.schoste.ddd.application.v1.annotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.schoste.ddd.application.v1.interceptor.GenericAnnotationPreHandler;

/**
 * Example implementation of a handler for the {@link ExampleMethodAnnotation} annotation.
 * 
 * @author Philipp Schosteritsch <s.philipp@schoste.com>
 */
public class ExampleMethodAnnotationPreHandler  extends GenericAnnotationPreHandler<ExampleMethodAnnotation> implements ExampleMethodAnnotation
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String exampleProperty()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Unused interface implementation
	 */
	@Override
	protected boolean handle(ExampleMethodAnnotation what, HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		System.out.println(String.format("Method handler (%s): %s%s", System.identityHashCode(this), System.identityHashCode(what), what));

		return true;
	}

}
