package com.schoste.ddd.application.v2.interceptor;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import com.schoste.ddd.application.v2.annotations.ExampleClassAnnotationPreHandler;
import com.schoste.ddd.application.v2.annotations.ExampleMethodAnnotation;
import com.schoste.ddd.application.v2.annotations.ExampleMethodAnnotationPostHandler;
import com.schoste.ddd.application.v2.annotations.ExampleMethodAnnotationPreHandler;
import com.schoste.ddd.application.v2.annotations.ExampleMethodAnnotationSecondPreHandler;
import com.schoste.ddd.application.v2.annotations.RecordingPostHandler;
import com.schoste.ddd.application.v2.annotations.RecordingPreHandler;
import com.schoste.ddd.application.v2.controller.TestController;
import com.schoste.ddd.application.v2.examples.RequiresRole;
import com.schoste.ddd.application.v2.examples.SecuredController;

/**
 * Tests the rules of {@link AnnotationInterceptor} (README "Annotation interceptor"). Each test names the decision of
 * {@code docs/decisions.md} it checks, if any. The interceptor is called directly here; {@code TestControllerIntegrationTest}
 * and {@code ExamplesTest} run it in Spring MVC.
 * <p>
 * Two kinds of tests:
 * <ul>
 * <li>On the shared application context of the tests, which has two pre handlers of the method annotation (priority 1 and 2),
 * one pre handler of the class annotation (priority 0) and one post handler of the method annotation. Its handlers are
 * shared by the tests, so these tests compare invocation counts before and after.</li>
 * <li>On an own context with handlers made for the test ({@link #createContext(Object...)}), e.g. handlers which reject the
 * request or fail.</li>
 * </ul>
 * Both work on the controller method {@link TestController#index(HttpServletRequest)}, which has the class annotation
 * {@code @ExampleClassAnnotation("Hello class")} and the method annotation {@code @ExampleMethodAnnotation("Hello method")}.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
@ContextConfiguration(locations = { "classpath:spring-mvc.xml", "classpath:unittest-beans.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class AnnotationInterceptorTest
{
	@Autowired
	protected TestController testController;

	@Autowired
	protected ApplicationContext applicationContext;

	@Autowired
	protected ExampleClassAnnotationPreHandler classAnnotationHandler;

	@Autowired
	protected ExampleMethodAnnotationPreHandler methodAnnotationHandler;

	@Autowired
	protected ExampleMethodAnnotationSecondPreHandler secondMethodAnnotationHandler;

	@Autowired
	protected ExampleMethodAnnotationPostHandler methodAnnotationPostHandler;

	/**
	 * Creates an interceptor as Spring would, with a given application context
	 *
	 * @param context the context, or null for an interceptor which wasn't created by Spring
	 * @return the interceptor
	 */
	protected AnnotationInterceptor createInterceptor(ApplicationContext context)
	{
		AnnotationInterceptor annotationInterceptor = new AnnotationInterceptor();

		annotationInterceptor.context = context;

		return annotationInterceptor;
	}

	/**
	 * Creates an application context whose only beans are given handlers. Close it after the test.
	 *
	 * @param handlers the handlers
	 * @return the refreshed context
	 */
	protected GenericApplicationContext createContext(Object... handlers)
	{
		GenericApplicationContext context = new GenericApplicationContext();

		for (int i = 0; i < handlers.length; i++) context.getBeanFactory().registerSingleton("handler" + i, handlers[i]);

		context.refresh();

		return context;
	}

	/**
	 * Creates a pre handler of {@link ExampleMethodAnnotation}
	 *
	 * @param priority the priority of the handler
	 * @return the handler
	 */
	protected RecordingPreHandler<ExampleMethodAnnotation> createPreHandler(int priority)
	{
		RecordingPreHandler<ExampleMethodAnnotation> handler = new RecordingPreHandler<ExampleMethodAnnotation>() { };

		handler.setPriority(priority);

		return handler;
	}

	/**
	 * Creates a post handler of {@link ExampleMethodAnnotation}
	 *
	 * @param priority the priority of the handler
	 * @return the handler
	 */
	protected RecordingPostHandler<ExampleMethodAnnotation> createPostHandler(int priority)
	{
		RecordingPostHandler<ExampleMethodAnnotation> handler = new RecordingPostHandler<ExampleMethodAnnotation>() { };

		handler.setPriority(priority);

		return handler;
	}

	protected HandlerMethod getIndexHandlerMethod() throws Exception
	{
		return new HandlerMethod(this.testController, "index", HttpServletRequest.class);
	}

	protected boolean preHandleIndex(AnnotationInterceptor annotationInterceptor) throws Exception
	{
		return annotationInterceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), this.getIndexHandlerMethod());
	}

	protected void postHandleIndex(AnnotationInterceptor annotationInterceptor) throws Exception
	{
		annotationInterceptor.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), this.getIndexHandlerMethod(), new ModelAndView());
	}

	// ---------------------------------------------------------------------------------------------------------------------
	// finding and running the handlers
	// ---------------------------------------------------------------------------------------------------------------------

	/**
	 * Decision 10: requests for handlers which aren't controller methods (here: static resources) are processed further
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testPreHandleProceedsForOtherHandlers() throws Exception
	{
		AnnotationInterceptor annotationInterceptor = this.createInterceptor(this.applicationContext);

		Assert.assertTrue(annotationInterceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new ResourceHttpRequestHandler()));
	}

	/**
	 * Decisions 7 and 8: all pre handlers of the class and method annotations of a controller method are executed, also
	 * several handlers of one annotation; the handlers are found by their type argument (they don't implement their
	 * annotation)
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testPreHandleExecutesHandlers() throws Exception
	{
		AnnotationInterceptor annotationInterceptor = this.createInterceptor(this.applicationContext);
		int classInvocations = this.classAnnotationHandler.getInvocations();
		int methodInvocations = this.methodAnnotationHandler.getInvocations();
		int secondMethodInvocations = this.secondMethodAnnotationHandler.getInvocations();

		Assert.assertTrue(this.preHandleIndex(annotationInterceptor));

		Assert.assertEquals(classInvocations + 1, this.classAnnotationHandler.getInvocations());
		Assert.assertEquals(methodInvocations + 1, this.methodAnnotationHandler.getInvocations());
		Assert.assertEquals(secondMethodInvocations + 1, this.secondMethodAnnotationHandler.getInvocations());
	}

	/**
	 * Decision 8: the pre handlers are executed by their priority, starting with the lowest number, across annotations
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testPreHandleExecutesHandlersByPriority() throws Exception
	{
		AnnotationInterceptor annotationInterceptor = this.createInterceptor(this.applicationContext);

		Assert.assertTrue(this.preHandleIndex(annotationInterceptor));

		Assert.assertTrue(this.classAnnotationHandler.getLastSequence() < this.methodAnnotationHandler.getLastSequence());
		Assert.assertTrue(this.methodAnnotationHandler.getLastSequence() < this.secondMethodAnnotationHandler.getLastSequence());
	}

	/**
	 * Decision 8: the post handlers are executed by their priority, starting with the lowest number, independent of the
	 * order in which they were registered
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testPostHandleExecutesHandlersByPriority() throws Exception
	{
		RecordingPostHandler<ExampleMethodAnnotation> last = this.createPostHandler(5);
		RecordingPostHandler<ExampleMethodAnnotation> first = this.createPostHandler(-1);
		RecordingPostHandler<ExampleMethodAnnotation> middle = this.createPostHandler(2);

		try (GenericApplicationContext context = this.createContext(last, first, middle))
		{
			this.postHandleIndex(this.createInterceptor(context));

			Assert.assertTrue(first.getLastSequence() > 0);
			Assert.assertTrue(first.getLastSequence() < middle.getLastSequence());
			Assert.assertTrue(middle.getLastSequence() < last.getLastSequence());
		}
	}

	/**
	 * A handler gets the annotation of the controller (method), so it can read the annotation's attributes
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testHandlersGetAnnotations() throws Exception
	{
		AnnotationInterceptor annotationInterceptor = this.createInterceptor(this.applicationContext);

		Assert.assertTrue(this.preHandleIndex(annotationInterceptor));

		Assert.assertEquals("Hello class", this.classAnnotationHandler.getLastAnnotation().exampleProperty());
		Assert.assertEquals("Hello method", this.methodAnnotationHandler.getLastAnnotation().exampleProperty());
	}

	/**
	 * An annotation on both the controller class and the method: each of its handlers is executed once, with the method's
	 * annotation
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testAnnotationOnClassAndMethodHandledOnceWithMethodAnnotation() throws Exception
	{
		RecordingPreHandler<RequiresRole> preHandler = new RecordingPreHandler<RequiresRole>() { };
		RecordingPostHandler<RequiresRole> postHandler = new RecordingPostHandler<RequiresRole>() { };
		HandlerMethod adminMethod = new HandlerMethod(new SecuredController(), "admin");

		try (GenericApplicationContext context = this.createContext(preHandler, postHandler))
		{
			AnnotationInterceptor annotationInterceptor = this.createInterceptor(context);

			Assert.assertTrue(annotationInterceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), adminMethod));
			annotationInterceptor.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), adminMethod, new ModelAndView());

			Assert.assertEquals(1, preHandler.getInvocations());
			Assert.assertEquals("admin", preHandler.getLastAnnotation().value());
			Assert.assertEquals(1, postHandler.getInvocations());
			Assert.assertEquals("admin", postHandler.getLastAnnotation().value());
		}
	}

	/**
	 * An annotation can have both pre and post handlers, and each runs in its phase only
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testPreAndPostHandlersOfOneAnnotation() throws Exception
	{
		AnnotationInterceptor annotationInterceptor = this.createInterceptor(this.applicationContext);
		int preInvocations = this.methodAnnotationHandler.getInvocations();
		int postInvocations = this.methodAnnotationPostHandler.getInvocations();

		Assert.assertTrue(this.preHandleIndex(annotationInterceptor));

		Assert.assertEquals(preInvocations + 1, this.methodAnnotationHandler.getInvocations());
		Assert.assertEquals(postInvocations, this.methodAnnotationPostHandler.getInvocations());

		this.postHandleIndex(annotationInterceptor);

		Assert.assertEquals(preInvocations + 1, this.methodAnnotationHandler.getInvocations());
		Assert.assertEquals(postInvocations + 1, this.methodAnnotationPostHandler.getInvocations());
	}

	/**
	 * Decision 7: handlers of a parent context are found (e.g. handlers in the root context, interceptor in the servlet context)
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testHandlersOfParentContext() throws Exception
	{
		try (GenericApplicationContext childContext = new GenericApplicationContext(this.applicationContext))
		{
			childContext.refresh();

			AnnotationInterceptor annotationInterceptor = this.createInterceptor(childContext);
			int methodInvocations = this.methodAnnotationHandler.getInvocations();
			int postInvocations = this.methodAnnotationPostHandler.getInvocations();

			Assert.assertTrue(this.preHandleIndex(annotationInterceptor));
			this.postHandleIndex(annotationInterceptor);

			Assert.assertEquals(methodInvocations + 1, this.methodAnnotationHandler.getInvocations());
			Assert.assertEquals(postInvocations + 1, this.methodAnnotationPostHandler.getInvocations());
		}
	}

	/**
	 * The handlers are collected on the first request: a handler added to the context later isn't executed by that interceptor
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testHandlersAreCollectedOnFirstRequest() throws Exception
	{
		RecordingPreHandler<ExampleMethodAnnotation> early = this.createPreHandler(0);
		RecordingPreHandler<ExampleMethodAnnotation> late = this.createPreHandler(0);

		try (GenericApplicationContext context = this.createContext(early))
		{
			AnnotationInterceptor annotationInterceptor = this.createInterceptor(context);

			Assert.assertTrue(this.preHandleIndex(annotationInterceptor));

			context.getBeanFactory().registerSingleton("late", late);

			Assert.assertTrue(this.preHandleIndex(annotationInterceptor));

			Assert.assertEquals(2, early.getInvocations());
			Assert.assertEquals(0, late.getInvocations());
		}
	}

	/**
	 * Interceptors don't share what they found, in both directions: an interceptor whose context has no handlers executes
	 * none, even if another interceptor found handlers before, and it doesn't keep another interceptor from executing its
	 * handlers afterwards. Decision 9: an annotation without handlers is ignored.
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testInterceptorsDontShareHandlers() throws Exception
	{
		try (GenericApplicationContext emptyContext = this.createContext())
		{
			AnnotationInterceptor interceptorWithHandlers = this.createInterceptor(this.applicationContext);
			AnnotationInterceptor interceptorWithoutHandlers = this.createInterceptor(emptyContext);
			AnnotationInterceptor laterInterceptorWithHandlers = this.createInterceptor(this.applicationContext);

			Assert.assertTrue(this.preHandleIndex(interceptorWithHandlers));

			int classInvocations = this.classAnnotationHandler.getInvocations();
			int methodInvocations = this.methodAnnotationHandler.getInvocations();

			Assert.assertTrue(this.preHandleIndex(interceptorWithoutHandlers));
			this.postHandleIndex(interceptorWithoutHandlers);

			Assert.assertEquals(classInvocations, this.classAnnotationHandler.getInvocations());
			Assert.assertEquals(methodInvocations, this.methodAnnotationHandler.getInvocations());

			Assert.assertTrue(this.preHandleIndex(laterInterceptorWithHandlers));

			Assert.assertEquals(classInvocations + 1, this.classAnnotationHandler.getInvocations());
			Assert.assertEquals(methodInvocations + 1, this.methodAnnotationHandler.getInvocations());
		}
	}

	// ---------------------------------------------------------------------------------------------------------------------
	// rejecting, stopping and failing
	// ---------------------------------------------------------------------------------------------------------------------

	/**
	 * A pre handler which returns false discards the request: the interceptor returns false, so Spring doesn't call the
	 * controller, and no further pre handler is executed
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testRejectingPreHandlerDiscardsRequest() throws Exception
	{
		RecordingPreHandler<ExampleMethodAnnotation> before = this.createPreHandler(0);
		RecordingPreHandler<ExampleMethodAnnotation> rejecting = this.createPreHandler(1);
		RecordingPreHandler<ExampleMethodAnnotation> after = this.createPreHandler(2);

		rejecting.setResult(false);

		try (GenericApplicationContext context = this.createContext(after, rejecting, before))
		{
			Assert.assertFalse(this.preHandleIndex(this.createInterceptor(context)));

			Assert.assertEquals(1, before.getInvocations());
			Assert.assertEquals(1, rejecting.getInvocations());
			Assert.assertEquals(0, after.getInvocations());
		}
	}

	/**
	 * A post handler which returns false stops the post handlers with higher priority numbers
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testStoppingPostHandlerStopsFurtherPostHandlers() throws Exception
	{
		RecordingPostHandler<ExampleMethodAnnotation> stopping = this.createPostHandler(0);
		RecordingPostHandler<ExampleMethodAnnotation> after = this.createPostHandler(1);

		stopping.setResult(false);

		try (GenericApplicationContext context = this.createContext(after, stopping))
		{
			this.postHandleIndex(this.createInterceptor(context));

			Assert.assertEquals(1, stopping.getInvocations());
			Assert.assertEquals(0, after.getInvocations());
		}
	}

	/**
	 * An exception of a pre handler reaches Spring unchanged (which then answers the request with an error); no further pre
	 * handler is executed
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testExceptionOfPreHandlerIsPassedOn() throws Exception
	{
		IOException failure = new IOException("pre handler failed");
		RecordingPreHandler<ExampleMethodAnnotation> failing = this.createPreHandler(0);
		RecordingPreHandler<ExampleMethodAnnotation> after = this.createPreHandler(1);

		failing.setFailure(failure);

		try (GenericApplicationContext context = this.createContext(failing, after))
		{
			AnnotationInterceptor annotationInterceptor = this.createInterceptor(context);

			Assert.assertSame(failure, Assert.assertThrows(IOException.class, () -> this.preHandleIndex(annotationInterceptor)));
			Assert.assertEquals(0, after.getInvocations());
		}
	}

	/**
	 * An exception of a post handler reaches Spring unchanged; no further post handler is executed
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testExceptionOfPostHandlerIsPassedOn() throws Exception
	{
		IOException failure = new IOException("post handler failed");
		RecordingPostHandler<ExampleMethodAnnotation> failing = this.createPostHandler(0);
		RecordingPostHandler<ExampleMethodAnnotation> after = this.createPostHandler(1);

		failing.setFailure(failure);

		try (GenericApplicationContext context = this.createContext(failing, after))
		{
			AnnotationInterceptor annotationInterceptor = this.createInterceptor(context);

			Assert.assertSame(failure, Assert.assertThrows(IOException.class, () -> this.postHandleIndex(annotationInterceptor)));
			Assert.assertEquals(0, after.getInvocations());
		}
	}

	/**
	 * Decision 9: a handler whose annotation can't be determined (its type argument is left open) is reported by an
	 * {@link IllegalStateException} which names the handler, instead of being ignored
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testUnresolvableHandlerThrows() throws Exception
	{
		try (GenericApplicationContext context = new GenericApplicationContext())
		{
			context.registerBean(OpenPreHandler.class);
			context.refresh();

			AnnotationInterceptor annotationInterceptor = this.createInterceptor(context);

			IllegalStateException e = Assert.assertThrows(IllegalStateException.class, () -> this.preHandleIndex(annotationInterceptor));

			Assert.assertTrue(e.getMessage(), e.getMessage().contains(OpenPreHandler.class.getName()));
		}
	}

	/**
	 * Decision 9: an interceptor without application context (i.e. it wasn't created by Spring) is reported by an
	 * {@link IllegalStateException}, instead of silently executing no handlers
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testMissingContextThrows() throws Exception
	{
		AnnotationInterceptor annotationInterceptor = this.createInterceptor(null);

		Assert.assertThrows(IllegalStateException.class, () -> this.preHandleIndex(annotationInterceptor));
		Assert.assertThrows(IllegalStateException.class, () -> this.postHandleIndex(annotationInterceptor));
	}
}
