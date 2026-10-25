package com.schoste.ddd.application.v2.interceptor;

import java.lang.annotation.Annotation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Misconfigured pre handler which leaves its annotation open, so the interceptor can't assign it to an annotation.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 *
 * @param <A> the annotation, never bound
 */
public class OpenPreHandler<A extends Annotation> extends GenericAnnotationPreHandler<A>
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean handle(A what, HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		return true;
	}
}
