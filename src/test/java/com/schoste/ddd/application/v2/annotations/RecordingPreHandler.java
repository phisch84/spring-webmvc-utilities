package com.schoste.ddd.application.v2.annotations;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.schoste.ddd.application.v2.interceptor.GenericAnnotationPreHandler;

/**
 * Pre handler which records its invocations, for tests.
 * It is an intermediate generic base class: the annotation is bound by the extending classes.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 *
 * @param <A> the annotation the handler is defined for
 */
public abstract class RecordingPreHandler<A extends Annotation> extends GenericAnnotationPreHandler<A>
{
	/**
	 * Counts the invocations of all recording handlers, to determine their order
	 */
	public static final AtomicLong SEQUENCE = new AtomicLong();

	protected final AtomicInteger invocations = new AtomicInteger();
	protected volatile A lastAnnotation = null;
	protected volatile long lastSequence = 0;

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
	 * Gets the annotation the handler has handled last
	 *
	 * @return the annotation, or null if the handler hasn't handled one yet
	 */
	public A getLastAnnotation()
	{
		return this.lastAnnotation;
	}

	/**
	 * Gets the position of the last invocation in the sequence of the invocations of all recording handlers
	 *
	 * @return the position, or 0 if the handler hasn't been invoked yet
	 */
	public long getLastSequence()
	{
		return this.lastSequence;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean handle(A what, HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		this.lastAnnotation = what;
		this.lastSequence = SEQUENCE.incrementAndGet();
		this.invocations.incrementAndGet();

		return true;
	}
}
