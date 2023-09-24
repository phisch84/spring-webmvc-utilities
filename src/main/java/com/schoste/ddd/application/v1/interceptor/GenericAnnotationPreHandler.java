package com.schoste.ddd.application.v1.interceptor;

import java.lang.annotation.Annotation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Base class for annotation handlers which are fired before a request is processed.
 * There should be one handler for each annotation that implements this class.
 * Annotation handlers should be singletons.
 * Annotation handlers can be sorted by priority, starting with the lowest.
 * 
 * @author Philipp Schosteritsch <s.philipp@schoste.com>
 *
 * @param <T> the annotation/interface the handler is defined for
 */
abstract public class GenericAnnotationPreHandler<T extends Annotation> extends java.text.Annotation implements Comparable<GenericAnnotationPreHandler<?>>
{
	protected int priority=0;

	/**
	 * Default constructor
	 */
	public GenericAnnotationPreHandler()
	{
		super(null);
	}

	/**
	 * Unused implementation of {@link java.lang.annotation.Annotation#annotationType()}
	 * 
	 * @return null
	 */
	public Class<? extends java.lang.annotation.Annotation> annotationType()
	{
		return null;
	}

	/**
	 * Sets the execution priority of this handler before other handlers
	 * 
	 * @param priority the priority of this handler
	 */
	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	/**
	 * Gets the execution priority of this handler before other handlers
	 * 
	 * @return the priority of this handler
	 */
	public int getPriority()
	{
		return this.priority;
	}

	/**
	 * Called by the {@link AnnotationInterceptor} to handle a given method with its annotation.
	 * 
	 * @param what the annotation of the method
	 * @param request the server's request object
	 * @param response the server's response object
	 * @return true if the request should be processed further or false if it should be discarded
	 * @throws Exception re-throws every exception
	 */
	@SuppressWarnings("unchecked")
	public boolean preHandle(Annotation what, HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		return this.handle((T) what, request, response);
	}

	/**
	 * Compares two handlers by their priority.
	 * 
	 * {@inheritDoc}
	 * @return 0 if their priorities are equal, less than 0 of the priority of this handler is lower and more than 0 if it is higher
	 */
	@Override
	public int compareTo(GenericAnnotationPreHandler<?> o)
	{
		if (this.priority < o.priority) return -1;
		if (this.priority > o.priority) return  1;
		
		return 0;
	}

	/**
	 * The actual method to handle a given method with its annotation.
	 * 
	 * @param what the annotation of the method
	 * @param request the server's request object
	 * @param response the server's response object
	 * @return true if the request should be processed further or false if it should be discarded
	 * @throws Exception re-throws every exception
	 */
	abstract protected boolean handle(T what, HttpServletRequest request, HttpServletResponse response)  throws Exception;
}
