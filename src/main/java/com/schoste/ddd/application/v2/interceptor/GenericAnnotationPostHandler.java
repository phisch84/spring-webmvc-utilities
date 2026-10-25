package com.schoste.ddd.application.v2.interceptor;

import java.lang.annotation.Annotation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

/**
 * Base class for annotation handlers which are executed by the {@link AnnotationInterceptor} after a request was processed
 * (and before the view is rendered).
 * <p>
 * To handle an annotation of a project, extend this class with the annotation as type argument, implement
 * {@link GenericAnnotationPostHandler#handle(Annotation, HttpServletRequest, HttpServletResponse, ModelAndView)}, and register
 * the handler as a singleton bean in the application context of the interceptor (or a parent context); its bean id doesn't
 * matter. The type argument tells the interceptor which annotation the handler belongs to (see
 * {@link GenericAnnotationPostHandler#getAnnotationType()}); an intermediate generic base class is fine, as long as the
 * class of the handler binds the type argument to a concrete annotation.
 * An annotation may have several post handlers, and pre handlers as well (see {@link GenericAnnotationPreHandler}).
 * Handlers are executed by their priority, starting with the lowest number.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 *
 * @param <T> the annotation the handler is defined for
 */
abstract public class GenericAnnotationPostHandler<T extends Annotation> implements Comparable<GenericAnnotationPostHandler<?>>
{
	/**
	 * The execution priority of the handler; handlers with lower numbers are executed first
	 */
	protected int priority=0;

	/**
	 * Default constructor
	 */
	public GenericAnnotationPostHandler()
	{
	}

	/**
	 * Gets the annotation the handler is defined for: the type argument of this class, as bound by the class of the handler.
	 * Override it only if the type argument can't be determined from the class (e.g. because the handler class is generic).
	 *
	 * @return the annotation type
	 * @throws IllegalStateException thrown if the class of the handler doesn't bind the type argument to a concrete annotation
	 */
	public Class<? extends Annotation> getAnnotationType() throws IllegalStateException
	{
		return AnnotationTypes.of(this, GenericAnnotationPostHandler.class);
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
