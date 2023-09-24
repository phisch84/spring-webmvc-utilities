package com.schoste.ddd.application.v1.annotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.schoste.ddd.application.v1.interceptor.GenericAnnotationPreHandler;

/**
 * Example implementation of a handler for the {@link ExampleClassAnnotation} annotation.
 * 
 * @author Philipp Schosteritsch <s.philipp@schoste.com>
 */
public class ExampleClassAnnotationPreHandler extends GenericAnnotationPreHandler<ExampleClassAnnotation> implements ExampleClassAnnotation
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean handle(ExampleClassAnnotation what, HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		System.out.println(String.format("Class handler (%s): %s%s", System.identityHashCode(this), System.identityHashCode(what), what));

		return true;
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
}
