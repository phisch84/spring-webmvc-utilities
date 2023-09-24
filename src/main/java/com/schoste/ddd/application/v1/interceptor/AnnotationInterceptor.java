package com.schoste.ddd.application.v1.interceptor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Version 1 implementation of the handler interceptor to handle
 * custom annotations with {@link GenericAnnotationPreHandler}
 *  
 * @author Philipp Schosteritsch <s.philipp@schoste.com>
 *
 */
public class AnnotationInterceptor extends HandlerInterceptorAdapter
{
	@Autowired
	protected ApplicationContext context;
	
	static protected Map<HandlerMethod, Collection<Annotation>> annotationsForMethods = new HashMap<>();
	static protected Map<Class<?>, GenericAnnotationPreHandler<?>> preHandlersForAnnotations = new HashMap<>();
	static protected Map<Class<?>, GenericAnnotationPostHandler<?>> postHandlersForAnnotations = new HashMap<>();
	
	/**
	 * Gets class and method annotations for a given handler method.
	 * All annotations for a method are cached to avoid redundant look ups
	 * 
	 * @param handlerMethod the handler method to get the annotations for
	 * @return a collection of annotations of the method and its declaring class
	 */
	static protected Collection<Annotation> getCachedAnnotations(HandlerMethod handlerMethod)
	{
		if (!annotationsForMethods.containsKey(handlerMethod))
		{
			Annotation[] classAnnotations = handlerMethod.getBeanType().getAnnotations();
			Annotation[] methodAnnotations = handlerMethod.getMethod().getAnnotations();

			Collection<Annotation> allAnnotations = new ArrayList<>(classAnnotations.length + methodAnnotations.length);

			allAnnotations.addAll(Arrays.asList(classAnnotations));
			allAnnotations.addAll(Arrays.asList(methodAnnotations));

			annotationsForMethods.put(handlerMethod, allAnnotations);
		}

		return annotationsForMethods.get(handlerMethod);
	}

	/**
	 * Loads and caches pre handlers for given annotations. If a handler is found for an annotation
	 * it is cached. The instances of the handlers are then mapped to the corresponding annotations.
	 * 
	 * @param annotations a collection of annotations to find handlers for
	 * @return a map of handler instances for each annotation
	 */
	protected Map<GenericAnnotationPreHandler<?>, Annotation> getCachedPreHandlers(Collection<Annotation> annotations)
	{
		Map<GenericAnnotationPreHandler<?>, Annotation> handlersToParameters = new HashMap<>();
		
		for (Annotation annotation : annotations)
		{
			Class<?> annotationType = annotation.annotationType();

			try
			{
				if (!preHandlersForAnnotations.containsKey(annotationType))
				{
					preHandlersForAnnotations.put(annotationType, (GenericAnnotationPreHandler<?>)this.context.getBean(annotation.annotationType()));
				}

				GenericAnnotationPreHandler<?> handler = preHandlersForAnnotations.get(annotationType);

				if (handler != null) handlersToParameters.put(handler, annotation);
			}
			catch (Exception e)
			{
				preHandlersForAnnotations.put(annotationType, null);
			}
		}

		return handlersToParameters;
	}

	/**
	 * Loads and caches post handlers for given annotations. If a handler is found for an annotation
	 * it is cached. The instances of the handlers are then mapped to the corresponding annotations.
	 * 
	 * @param annotations a collection of annotations to find handlers for
	 * @return a map of handler instances for each annotation
	 */
	protected Map<GenericAnnotationPostHandler<?>, Annotation> getCachedPostHandlers(Collection<Annotation> annotations)
	{
		Map<GenericAnnotationPostHandler<?>, Annotation> handlersToParameters = new HashMap<>();
		
		for (Annotation annotation : annotations)
		{
			Class<?> annotationType = annotation.annotationType();

			try
			{
				if (!postHandlersForAnnotations.containsKey(annotationType))
				{
					postHandlersForAnnotations.put(annotationType, (GenericAnnotationPostHandler<?>)this.context.getBean(annotation.annotationType()));
				}

				GenericAnnotationPostHandler<?> handler = postHandlersForAnnotations.get(annotationType);

				if (handler != null) handlersToParameters.put(handler, annotation);
			}
			catch (Exception e)
			{
				postHandlersForAnnotations.put(annotationType, null);
			}
		}

		return handlersToParameters;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Dispatches for actual handling if the handler is a {@link HandlerMethod}
	 */
	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
		if (!(handler instanceof HandlerMethod)) return super.preHandle(request, response, handler);
		if (!this.preHandle(request, response, (HandlerMethod)handler)) return false;
		
		return super.preHandle(request, response, handler);
    }

	/**
	 * {@inheritDoc}
	 * 
	 * Dispatches for actual handling if the handler is a {@link HandlerMethod}
	 */
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception
	{
		if (handler instanceof HandlerMethod) this.postHandle(request, response, (HandlerMethod)handler, modelAndView);
	}

	/**
	 * Executes registered handlers ({@link GenericAnnotationPreHandler})
	 * for each annotation of a handler method by their priority
	 * and before the request is processed, starting with the lowest priority number.
	 * 
	 * @param request the server's request object
	 * @param response the server's response object
	 * @param requestHandlerMethod the handler method with annotations to be handled
	 * @return true if the request should be processed further or false if it should be discarded
	 * @throws Exception re-throws every exception
	 */
	protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod requestHandlerMethod) throws Exception
	{
		Collection<Annotation> annotations = getCachedAnnotations(requestHandlerMethod);
		Map<GenericAnnotationPreHandler<?>, Annotation> handlerToAnnotations = this.getCachedPreHandlers(annotations);
		List<GenericAnnotationPreHandler<?>> handlers = Arrays.asList(handlerToAnnotations.keySet().toArray(new GenericAnnotationPreHandler<?>[0]));

		Collections.sort(handlers);

		for (int i=0; i<handlers.size(); i++)
		{
			GenericAnnotationPreHandler<?> handler = handlers.get(i);
			Annotation what = handlerToAnnotations.get(handler);

			if (!handler.preHandle(what, request, response)) return false;
		}

		return true;
	}

	/**
	 * Executes registered handlers ({@link GenericAnnotationPreHandler})
	 * for each annotation of a handler method by their priority, after
	 * the request was processed, starting with the lowest priority number.
	 * 
	 * @param request the server's request object
	 * @param response the server's response object
	 * @param requestHandlerMethod the handler method with annotations to be handled
	 * @param modelAndView the model and view that was loaded for this request
	 * @throws Exception re-throws every exception
	 */
	protected void postHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod requestHandlerMethod, ModelAndView modelAndView) throws Exception
	{
		Collection<Annotation> annotations = getCachedAnnotations(requestHandlerMethod);
		Map<GenericAnnotationPostHandler<?>, Annotation> handlerToAnnotations = this.getCachedPostHandlers(annotations);
		List<GenericAnnotationPostHandler<?>> handlers = Arrays.asList(handlerToAnnotations.keySet().toArray(new GenericAnnotationPostHandler<?>[0]));

		Collections.sort(handlers);

		for (int i=0; i<handlers.size(); i++)
		{
			GenericAnnotationPostHandler<?> handler = handlers.get(i);
			Annotation what = handlerToAnnotations.get(handler);

			if (!handler.postHandle(what, request, response, modelAndView)) return;
		}
		
	}
}
