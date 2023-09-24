package com.schoste.ddd.application.v1.interceptor;

import java.lang.annotation.Annotation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

/**
 * Base class for annotation handlers which are fired after a request was processed.
 * There should be one handler for each annotation that implements this class.
 * Annotation handlers should be singletons.
 * Annotation handlers can be sorted by priority, starting with the lowest.
 * 
 * @author Philipp Schosteritsch <s.philipp@schoste.com>
 *
 * @param <T> the annotation/interface the handler is defined for
 */
abstract public class GenericAnnotationPostHandler<T extends Annotation> extends java.text.Annotation implements Comparable<GenericAnnotationPostHandler<?>>
{
	protected int priority=0;

	/**
	 * Default constructor
	 */
	public GenericAnnotationPostHandler()
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
	 * @param modelAndView the model and view that was loaded for this request
	 * @return true if further handlers should be executed or false if no other handlers should execute
	 * @throws Exception re-throws every exception
	 */
	@SuppressWarnings("unchecked")
	public boolean postHandle(Annotation what, HttpServletRequest request, HttpServletResponse response, ModelAndView modelAndView) throws Exception
	{
		return this.handle((T) what, request, response, modelAndView);
	}

	/**
	 * Compares two handlers by their priority.
	 * 
	 * {@inheritDoc}
	 * @return 0 if their priorities are equal, less than 0 of the priority of this handler is lower and more than 0 if it is higher
	 */
	@Override
	public int compareTo(GenericAnnotationPostHandler<?> o)
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
	 * @param modelAndView the model and view that was loaded for this request
	 * @return true if further handlers should be executed or false if no other handlers should execute
	 * @throws Exception re-throws every exception
	 */
	abstract protected boolean handle(T what, HttpServletRequest request, HttpServletResponse response, ModelAndView modelAndView)  throws Exception;
}
