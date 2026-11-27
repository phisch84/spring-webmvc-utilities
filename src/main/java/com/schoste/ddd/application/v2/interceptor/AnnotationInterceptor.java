package com.schoste.ddd.application.v2.interceptor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Spring MVC handler interceptor which executes handlers for the custom annotations of controllers and their methods.
 * <p>
 * For every request of a controller method, the interceptor collects the annotations of the controller class and of the
 * method, and executes the handlers of each annotation which has some:
 * {@link GenericAnnotationPreHandler}s before the request is processed and {@link GenericAnnotationPostHandler}s after it
 * was processed, each ordered by their priority, starting with the lowest number. Handlers with equal priority are executed
 * in undefined order. If an annotation is present on both the class and the method, each of its handlers is executed once,
 * with the method's annotation. Requests of other handlers (e.g. static resources) are processed without executing any
 * handlers.
 * <p>
 * The handlers are the beans of the interceptor's application context and its ancestors which extend one of the handler
 * base classes; the type argument of the base class is the annotation the handler belongs to. They are collected on the
 * first request. An annotation may have any number of pre and post handlers; annotations without handlers are ignored.
 * If the annotation of a handler can't be determined, or the interceptor has no application context, every request fails
 * with an {@link IllegalStateException} which names the cause.
 * The interceptor is safe to use with concurrent requests.
 * <p>
 * Register the interceptor e.g. by {@code <mvc:interceptors>} in the Spring MVC configuration, so that it gets the
 * application context injected.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class AnnotationInterceptor implements HandlerInterceptor
{
	/**
	 * The application context the handlers are looked up in
	 */
	@Autowired
	protected ApplicationContext context;

	/*
	 * The caches belong to the interceptor, since the handlers are beans of its application context.
	 * They are accessed by concurrent requests: the handler maps are never changed once they are set.
	 */

	/**
	 * Cache of the class and method annotations of each handler method
	 */
	protected final Map<HandlerMethod, Collection<Annotation>> annotationsForMethods = new ConcurrentHashMap<>();

	/**
	 * The pre handlers of each annotation type, collected on the first request; null before
	 */
	protected volatile Map<Class<? extends Annotation>, List<GenericAnnotationPreHandler<?>>> preHandlersForAnnotations = null;

	/**
	 * The post handlers of each annotation type, collected on the first request; null before
	 */
	protected volatile Map<Class<? extends Annotation>, List<GenericAnnotationPostHandler<?>>> postHandlersForAnnotations = null;

	/**
	 * Creates the interceptor; the application context is injected afterwards
	 */
	public AnnotationInterceptor()
	{
	}

	/**
	 * Gets class and method annotations for a given handler method.
	 * All annotations for a method are cached to avoid redundant look ups
	 * 
	 * @param handlerMethod the handler method to get the annotations for
	 * @return a collection of annotations of the method and its declaring class
	 */
	protected Collection<Annotation> getCachedAnnotations(HandlerMethod handlerMethod)
	{
		return this.annotationsForMethods.computeIfAbsent(handlerMethod, method ->
		{
			Annotation[] classAnnotations = method.getBeanType().getAnnotations();
			Annotation[] methodAnnotations = method.getMethod().getAnnotations();

			Collection<Annotation> allAnnotations = new ArrayList<>(classAnnotations.length + methodAnnotations.length);

			allAnnotations.addAll(Arrays.asList(classAnnotations));
			allAnnotations.addAll(Arrays.asList(methodAnnotations));

			return allAnnotations;
		});
	}

	/**
	 * Gets the application context the handlers are collected from
	 *
	 * @return the application context
	 * @throws IllegalStateException thrown if the interceptor has no application context, i.e. it wasn't created by Spring
	 */
	protected ApplicationContext getContext() throws IllegalStateException
	{
		if (this.context == null) throw new IllegalStateException("The AnnotationInterceptor has no application context: it must be created by Spring, e.g. by <mvc:interceptors>");

		return this.context;
	}

	/**
	 * Gets the pre handlers of each annotation type. They are collected once from the application context and its
	 * ancestors: every bean which extends {@link GenericAnnotationPreHandler}, assigned to its annotation type.
	 *
	 * @return the pre handlers of each annotation type which has some
	 * @throws IllegalStateException thrown if the interceptor has no application context, or if the annotation of a handler
	 * can't be determined (see {@link GenericAnnotationPreHandler#getAnnotationType()})
	 */
	protected Map<Class<? extends Annotation>, List<GenericAnnotationPreHandler<?>>> getPreHandlers() throws IllegalStateException
	{
		Map<Class<? extends Annotation>, List<GenericAnnotationPreHandler<?>>> handlers = this.preHandlersForAnnotations;

		if (handlers == null)
		{
			// concurrent first requests may collect the handlers more than once, with the same result
			handlers = new HashMap<>();

			for (GenericAnnotationPreHandler<?> handler : BeanFactoryUtils.beansOfTypeIncludingAncestors(this.getContext(), GenericAnnotationPreHandler.class).values())
			{
				handlers.computeIfAbsent(handler.getAnnotationType(), annotationType -> new ArrayList<>()).add(handler);
			}

			this.preHandlersForAnnotations = handlers;
		}

		return handlers;
	}

	/**
	 * Gets the post handlers of each annotation type. They are collected once from the application context and its
	 * ancestors: every bean which extends {@link GenericAnnotationPostHandler}, assigned to its annotation type.
	 *
	 * @return the post handlers of each annotation type which has some
	 * @throws IllegalStateException thrown if the interceptor has no application context, or if the annotation of a handler
	 * can't be determined (see {@link GenericAnnotationPostHandler#getAnnotationType()})
	 */
	protected Map<Class<? extends Annotation>, List<GenericAnnotationPostHandler<?>>> getPostHandlers() throws IllegalStateException
	{
		Map<Class<? extends Annotation>, List<GenericAnnotationPostHandler<?>>> handlers = this.postHandlersForAnnotations;

		if (handlers == null)
		{
			// concurrent first requests may collect the handlers more than once, with the same result
			handlers = new HashMap<>();

			for (GenericAnnotationPostHandler<?> handler : BeanFactoryUtils.beansOfTypeIncludingAncestors(this.getContext(), GenericAnnotationPostHandler.class).values())
			{
				handlers.computeIfAbsent(handler.getAnnotationType(), annotationType -> new ArrayList<>()).add(handler);
			}

			this.postHandlersForAnnotations = handlers;
		}

		return handlers;
	}

	/**
	 * Gets the pre handlers of given annotations, mapped to the annotation each handler is executed with.
	 * A handler of an annotation which is given twice (on the class and the method) is mapped to the later one.
	 *
	 * @param annotations a collection of annotations to find handlers for
	 * @return the handlers of the annotations, each with its annotation
	 * @throws IllegalStateException see {@link AnnotationInterceptor#getPreHandlers()}
	 */
	protected Map<GenericAnnotationPreHandler<?>, Annotation> getCachedPreHandlers(Collection<Annotation> annotations) throws IllegalStateException
	{
		Map<Class<? extends Annotation>, List<GenericAnnotationPreHandler<?>>> handlers = this.getPreHandlers();
		Map<GenericAnnotationPreHandler<?>, Annotation> handlersToParameters = new HashMap<>();

		for (Annotation annotation : annotations)
		{
			for (GenericAnnotationPreHandler<?> handler : handlers.getOrDefault(annotation.annotationType(), Collections.emptyList())) handlersToParameters.put(handler, annotation);
		}

		return handlersToParameters;
	}

	/**
	 * Gets the post handlers of given annotations, mapped to the annotation each handler is executed with.
	 * A handler of an annotation which is given twice (on the class and the method) is mapped to the later one.
	 *
	 * @param annotations a collection of annotations to find handlers for
	 * @return the handlers of the annotations, each with its annotation
	 * @throws IllegalStateException see {@link AnnotationInterceptor#getPostHandlers()}
	 */
	protected Map<GenericAnnotationPostHandler<?>, Annotation> getCachedPostHandlers(Collection<Annotation> annotations) throws IllegalStateException
	{
		Map<Class<? extends Annotation>, List<GenericAnnotationPostHandler<?>>> handlers = this.getPostHandlers();
		Map<GenericAnnotationPostHandler<?>, Annotation> handlersToParameters = new HashMap<>();

		for (Annotation annotation : annotations)
		{
			for (GenericAnnotationPostHandler<?> handler : handlers.getOrDefault(annotation.annotationType(), Collections.emptyList())) handlersToParameters.put(handler, annotation);
		}

		return handlersToParameters;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Dispatches for actual handling if the handler is a {@link HandlerMethod}.
	 * Requests of other handlers (e.g. static resources) are processed further without handling.
	 */
	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
		if (!(handler instanceof HandlerMethod)) return true;
		if (!this.preHandle(request, response, (HandlerMethod)handler)) return false;
		
		return true;
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
	 * As soon as a handler returns false, no further handlers are executed and the request is discarded;
	 * that handler is responsible for the response.
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
	 * Executes registered handlers ({@link GenericAnnotationPostHandler})
	 * for each annotation of a handler method by their priority, after
	 * the request was processed, starting with the lowest priority number.
	 * As soon as a handler returns false, no further handlers are executed.
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
