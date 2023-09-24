package com.schoste.ddd.application.v1.interceptor;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import com.schoste.ddd.application.v1.controller.TestController;
import com.schoste.ddd.application.v1.interceptor.AnnotationInterceptor;

/**
 * Test class for {@link AnnotationInterceptor}
 * 
 * @author Philipp Schosteritsch <s.philipp@schoste.com>
 */
@ContextConfiguration(locations = {
		"file:src/test/resources/spring-mvc.xml",
		"file:src/test/resources/unittest-beans-v1.xml",
		})
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class AnnotationInterceptorTest
{
	@Autowired
	protected TestController testController;

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
