package com.schoste.ddd.application.v2.annotations;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.schoste.ddd.application.v2.interceptor.GenericAnnotationPostHandler;

/**
 * Post handler which records its invocations, for tests; the counterpart of {@link RecordingPreHandler}, whose
 * {@link RecordingPreHandler#SEQUENCE} it shares. By default it lets the following handlers run; tests can make it stop them
 * ({@link #setResult(boolean)}) or fail ({@link #setFailure(Exception)}).
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 *
 * @param <A> the annotation the handler is defined for
 */
public abstract class RecordingPostHandler<A extends Annotation> extends GenericAnnotationPostHandler<A>
{
	protected final AtomicInteger invocations = new AtomicInteger();
	protected volatile A lastAnnotation = null;
	protected volatile long lastSequence = 0;
	protected volatile boolean result = true;
	protected volatile Exception failure = null;

	public int getInvocations()
	{
		return this.invocations.get();
	}

	public A getLastAnnotation()
	{
		return this.lastAnnotation;
	}

	/**
	 * Gets the position of the last invocation among the invocations of all recording handlers
	 *
	 * @return the position; 0 if the handler wasn't invoked yet
	 */
	public long getLastSequence()
	{
		return this.lastSequence;
	}

	/**
	 * Sets what the handler returns
	 *
	 * @param result true to let the following handlers run, false to stop them
	 */
	public void setResult(boolean result)
	{
		this.result = result;
	}

	/**
	 * Sets an exception the handler throws (after recording the invocation)
	 *
	 * @param failure the exception, or null to return normally
	 */
	public void setFailure(Exception failure)
	{
		this.failure = failure;
	}

	@Override
	protected boolean handle(A what, HttpServletRequest request, HttpServletResponse response, ModelAndView modelAndView) throws Exception
	{
		this.lastAnnotation = what;
		this.lastSequence = RecordingPreHandler.SEQUENCE.incrementAndGet();
		this.invocations.incrementAndGet();

		if (this.failure != null) throw this.failure;

		return this.result;
	}
}
