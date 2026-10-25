package com.schoste.ddd.application.v2.interceptor;

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
import com.schoste.ddd.application.v2.annotations.ExampleMethodAnnotationPostHandler;
import com.schoste.ddd.application.v2.annotations.ExampleMethodAnnotationPreHandler;
import com.schoste.ddd.application.v2.annotations.ExampleMethodAnnotationSecondPreHandler;
import com.schoste.ddd.application.v2.controller.TestController;

/**
 * Test class for {@link AnnotationInterceptor}.
 * The application context of the tests has two pre handlers of the method annotation (priority 1 and 2), one pre handler
 * of the class annotation (priority 0) and one post handler of the method annotation. None of them implements its annotation.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
@ContextConfiguration(locations = {
		"file:src/test/resources/spring-mvc.xml",
		"file:src/test/resources/unittest-beans.xml",
		})
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

	protected AnnotationInterceptor createInterceptor(ApplicationContext context)
	{
		AnnotationInterceptor annotationInterceptor = new AnnotationInterceptor();

		annotationInterceptor.context = context;

		return annotationInterceptor;
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

	/**
	 * Asserts that requests for handlers which aren't controller methods (here: static resources) are processed further
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
	 * Asserts that all pre handlers of the class and method annotations of a controller method are executed, also several
	 * handlers of one annotation, and that the handlers are found by their type argument (they don't implement their annotation)
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
	 * Asserts that the pre handlers are executed by their priority, starting with the lowest number
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
	 * Asserts that a handler gets the annotation of the controller (method), so it can read the annotation's attributes
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
	 * Asserts that an annotation can have both pre and post handlers, and that each runs in its phase only
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
	 * Asserts that handlers of a parent context are found (e.g. handlers in the root context, interceptor in the servlet context)
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
	 * Asserts that interceptors don't share what they found, in both directions: an interceptor whose context has no
	 * handlers executes none, even if another interceptor found handlers before, and it doesn't keep another interceptor
	 * from executing its handlers afterwards
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testInterceptorsDontShareHandlers() throws Exception
	{
		try (GenericApplicationContext emptyContext = new GenericApplicationContext())
		{
			emptyContext.refresh();

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

	/**
	 * Asserts that a handler whose annotation can't be determined (its type argument is left open) is reported
	 * by an {@link IllegalStateException} which names the handler, instead of being ignored
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
	 * Asserts that an interceptor without application context (i.e. it wasn't created by Spring) is reported
	 * by an {@link IllegalStateException}, instead of silently executing no handlers
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
