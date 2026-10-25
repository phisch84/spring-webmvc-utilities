package com.schoste.ddd.application.v2.interceptor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Spring MVC handler interceptor which executes handlers for the custom annotations of controllers and their methods.
 * <p>
 * For every request of a controller method, the interceptor collects the annotations of the controller class and of the
 * method, and executes the handler of each annotation which has one:
 * {@link GenericAnnotationPreHandler}s before the request is processed and {@link GenericAnnotationPostHandler}s after it
 * was processed, each ordered by their priority, starting with the lowest number. Handlers with equal priority are executed
 * in undefined order. If an annotation is present on both the class and the method, its handler is executed once,
 * with the method's annotation. Requests of other handlers (e.g. static resources) are processed without executing any
 * handlers.
 * <p>
 * The handler of an annotation is the bean of the interceptor's application context which is found by the annotation
 * type, i.e. a singleton bean which extends one of the handler base classes and implements the annotation's interface.
 * If there is no such bean, or it can't be determined unambiguously, the annotation is ignored.
 * Since a bean can't extend both base classes, an annotation has either a pre or a post handler.
 * The handlers found are cached by the interceptor; it is safe to use with concurrent requests.
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
	 * They are accessed by concurrent requests. An empty Optional caches that no handler was found.
	 */

	/**
	 * Cache of the class and method annotations of each handler method
	 */
	protected final Map<HandlerMethod, Collection<Annotation>> annotationsForMethods = new ConcurrentHashMap<>();

	/**
	 * Cache of the pre handler of each annotation type; empty if it has none
	 */
	protected final Map<Class<?>, Optional<GenericAnnotationPreHandler<?>>> preHandlersForAnnotations = new ConcurrentHashMap<>();

	/**
	 * Cache of the post handler of each annotation type; empty if it has none
	 */
	protected final Map<Class<?>, Optional<GenericAnnotationPostHandler<?>>> postHandlersForAnnotations = new ConcurrentHashMap<>();

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
	 * Looks up the pre handler of an annotation in the application context
	 *
	 * @param annotationType the annotation to find the handler for
	 * @return the handler, or an empty Optional if there is none
	 */
	protected Optional<GenericAnnotationPreHandler<?>> findPreHandler(Class<?> annotationType)
	{
		try
		{
			return Optional.of((GenericAnnotationPreHandler<?>) this.context.getBean(annotationType));
		}
		catch (Exception e)
		{
			return Optional.empty();
		}
	}

	/**
	 * Looks up the post handler of an annotation in the application context
	 *
	 * @param annotationType the annotation to find the handler for
	 * @return the handler, or an empty Optional if there is none
	 */
	protected Optional<GenericAnnotationPostHandler<?>> findPostHandler(Class<?> annotationType)
	{
		try
		{
			return Optional.of((GenericAnnotationPostHandler<?>) this.context.getBean(annotationType));
		}
		catch (Exception e)
		{
			return Optional.empty();
		}
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
			Optional<GenericAnnotationPreHandler<?>> handler = this.preHandlersForAnnotations.computeIfAbsent(annotation.annotationType(), this::findPreHandler);

			if (handler.isPresent()) handlersToParameters.put(handler.get(), annotation);
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
			Optional<GenericAnnotationPostHandler<?>> handler = this.postHandlersForAnnotations.computeIfAbsent(annotation.annotationType(), this::findPostHandler);

			if (handler.isPresent()) handlersToParameters.put(handler.get(), annotation);
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
