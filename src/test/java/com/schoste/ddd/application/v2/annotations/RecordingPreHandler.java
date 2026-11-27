package com.schoste.ddd.application.v2.annotations;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.schoste.ddd.application.v2.interceptor.GenericAnnotationPreHandler;

/**
 * Pre handler which records its invocations, for tests. By default it lets the request through; tests can make it reject
 * the request ({@link #setResult(boolean)}) or fail ({@link #setFailure(Exception)}).
 * It is an intermediate generic base class: the annotation is bound by the extending classes (also anonymous ones, e.g.
 * {@code new RecordingPreHandler<ExampleMethodAnnotation>() { }}).
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 *
 * @param <A> the annotation the handler is defined for
 */
public abstract class RecordingPreHandler<A extends Annotation> extends GenericAnnotationPreHandler<A>
{
	/**
	 * Counts the invocations of all recording handlers (pre and post), to determine their order
	 */
	public static final AtomicLong SEQUENCE = new AtomicLong();

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
	 * @param result true to let the request through, false to reject it
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
	protected boolean handle(A what, HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		this.lastAnnotation = what;
		this.lastSequence = SEQUENCE.incrementAndGet();
		this.invocations.incrementAndGet();

		if (this.failure != null) throw this.failure;

		return this.result;
	}
}
