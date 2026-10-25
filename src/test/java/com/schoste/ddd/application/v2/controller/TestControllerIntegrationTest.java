package com.schoste.ddd.application.v2.controller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.schoste.ddd.application.v2.annotations.ExampleClassAnnotationPreHandler;
import com.schoste.ddd.application.v2.annotations.ExampleMethodAnnotationPreHandler;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Integration tests for the {@link com.schoste.ddd.application.v2.interceptor.AnnotationInterceptor}
 * working on the {@link TestController}
 * 
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
@ContextConfiguration(locations = {
		"file:src/test/resources/spring-mvc.xml",
		"file:src/test/resources/unittest-beans.xml",
		})
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class})
@WebAppConfiguration
public class TestControllerIntegrationTest 
{
	@Autowired
	protected WebApplicationContext context;

	@Autowired
	protected ExampleClassAnnotationPreHandler classAnnotationHandler;

	@Autowired
	protected ExampleMethodAnnotationPreHandler methodAnnotationHandler;

	protected MockMvc mvc;

	@Before
	public void setUpWebAppContextSetup() throws Exception
	{
		mvc = webAppContextSetup(context).alwaysDo(print()).build();
	}

	/**
	 * Asserts that requests of a controller method succeed, and that the handlers of its annotations are executed
	 * for every request
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testIndex() throws Exception
	{
		int classInvocations = this.classAnnotationHandler.getInvocations();
		int methodInvocations = this.methodAnnotationHandler.getInvocations();

		mvc.perform(get("/")).andExpect(status().is(200)).andReturn();
		mvc.perform(get("/")).andExpect(status().is(200)).andReturn();
		mvc.perform(get("/")).andExpect(status().is(200)).andReturn();

		Assert.assertEquals(classInvocations + 3, this.classAnnotationHandler.getInvocations());
		Assert.assertEquals(methodInvocations + 3, this.methodAnnotationHandler.getInvocations());
	}

	/**
	 * Asserts that the interceptor lets requests of static resources through
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testStaticResource() throws Exception
	{
		mvc.perform(get("/resources/example.txt")).andExpect(status().is(200)).andExpect(content().string("Hello resource\n"));
	}
}
