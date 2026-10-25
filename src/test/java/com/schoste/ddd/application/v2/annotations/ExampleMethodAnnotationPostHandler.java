package com.schoste.ddd.application.v2.annotations;

import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.schoste.ddd.application.v2.interceptor.GenericAnnotationPostHandler;

/**
 * Example post handler of the {@link ExampleMethodAnnotation}, which also has pre handlers; it records its invocations.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class ExampleMethodAnnotationPostHandler extends GenericAnnotationPostHandler<ExampleMethodAnnotation>
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
	 * {@inheritDoc}
	 */
	@Override
	protected boolean handle(ExampleMethodAnnotation what, HttpServletRequest request, HttpServletResponse response, ModelAndView modelAndView) throws Exception
	{
		this.invocations.incrementAndGet();

		return true;
	}
}
