package com.schoste.ddd.application.v2.interceptor;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import com.schoste.ddd.application.v2.annotations.ExampleClassAnnotationPreHandler;
import com.schoste.ddd.application.v2.annotations.ExampleMethodAnnotationPreHandler;
import com.schoste.ddd.application.v2.controller.TestController;

/**
 * Test class for {@link AnnotationInterceptor}
 * 
 * @author Philipp Schosteritsch <s.philipp@schoste.com>
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
	 * Asserts that the handlers of the class and method annotations of a controller method are executed
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testPreHandleExecutesHandlers() throws Exception
	{
		AnnotationInterceptor annotationInterceptor = this.createInterceptor(this.applicationContext);
		int classInvocations = this.classAnnotationHandler.getInvocations();
		int methodInvocations = this.methodAnnotationHandler.getInvocations();

		Assert.assertTrue(annotationInterceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), this.getIndexHandlerMethod()));

		Assert.assertEquals(classInvocations + 1, this.classAnnotationHandler.getInvocations());
		Assert.assertEquals(methodInvocations + 1, this.methodAnnotationHandler.getInvocations());
	}

	/**
	 * Asserts that interceptors don't share what they found, in both directions: an interceptor which can't find handlers
	 * (here: it has no application context) executes none, even if another interceptor found handlers before, and it doesn't
	 * keep another interceptor from executing its handlers afterwards
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testInterceptorsDontShareHandlers() throws Exception
	{
		AnnotationInterceptor interceptorWithContext = this.createInterceptor(this.applicationContext);
		AnnotationInterceptor interceptorWithoutContext = this.createInterceptor(null);
		AnnotationInterceptor laterInterceptorWithContext = this.createInterceptor(this.applicationContext);

		Assert.assertTrue(interceptorWithContext.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), this.getIndexHandlerMethod()));

		int classInvocations = this.classAnnotationHandler.getInvocations();
		int methodInvocations = this.methodAnnotationHandler.getInvocations();

		Assert.assertTrue(interceptorWithoutContext.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), this.getIndexHandlerMethod()));

		Assert.assertEquals(classInvocations, this.classAnnotationHandler.getInvocations());
		Assert.assertEquals(methodInvocations, this.methodAnnotationHandler.getInvocations());

		Assert.assertTrue(laterInterceptorWithContext.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), this.getIndexHandlerMethod()));

		Assert.assertEquals(classInvocations + 1, this.classAnnotationHandler.getInvocations());
		Assert.assertEquals(methodInvocations + 1, this.methodAnnotationHandler.getInvocations());
	}

	/**
	 * Asserts that {@link AnnotationInterceptor#preHandle(HttpServletRequest, javax.servlet.http.HttpServletResponse, Object)}
	 * throws no exception.
	 * 
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testPreHandle() throws Exception
	{
		AnnotationInterceptor annotationInterceptor = new AnnotationInterceptor();
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		HandlerMethod handler = new HandlerMethod(this.testController, "index", HttpServletRequest.class);

		Assert.assertTrue(annotationInterceptor.preHandle(request, response, handler));
	}

	/**
	 * Asserts that {@link AnnotationInterceptor#postHandle(HttpServletRequest, javax.servlet.http.HttpServletResponse, Object, org.springframework.web.servlet.ModelAndView)}
	 * throws no exception.
	 * 
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testPostHandle() throws Exception
	{
		AnnotationInterceptor annotationInterceptor = new AnnotationInterceptor();

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		ModelAndView modelAndView = new ModelAndView();

		HandlerMethod handler = new HandlerMethod(this.testController, "index", HttpServletRequest.class);

		annotationInterceptor.postHandle(request, response, handler, modelAndView);

		Assert.assertTrue(true);
	}
}
