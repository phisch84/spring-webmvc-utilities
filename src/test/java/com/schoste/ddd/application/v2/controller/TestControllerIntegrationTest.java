package com.schoste.ddd.application.v2.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.schoste.ddd.application.v2.annotations.ExampleClassAnnotationPreHandler;
import com.schoste.ddd.application.v2.annotations.ExampleMethodAnnotationPostHandler;
import com.schoste.ddd.application.v2.annotations.ExampleMethodAnnotationPreHandler;

/**
 * Integration tests of the {@link com.schoste.ddd.application.v2.interceptor.AnnotationInterceptor} in Spring MVC, configured
 * as the README shows ({@code <mvc:interceptors>} in {@code spring-mvc.xml}), on the {@link TestController}.
 * Rejected requests are tested with the README's example in {@code ExamplesTest}.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
@ContextConfiguration(locations = { "classpath:spring-mvc.xml", "classpath:unittest-beans.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class TestControllerIntegrationTest
{
	@Autowired
	protected WebApplicationContext context;

	@Autowired
	protected ExampleClassAnnotationPreHandler classAnnotationHandler;

	@Autowired
	protected ExampleMethodAnnotationPreHandler methodAnnotationHandler;

	@Autowired
	protected ExampleMethodAnnotationPostHandler methodAnnotationPostHandler;

	protected MockMvc mvc;

	@Before
	public void setUp() throws Exception
	{
		this.mvc = webAppContextSetup(this.context).build();
	}

	/**
	 * Requests of a controller method succeed, and the pre and post handlers of its annotations are executed for every request
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testHandlersRunForEveryRequest() throws Exception
	{
		int classInvocations = this.classAnnotationHandler.getInvocations();
		int methodInvocations = this.methodAnnotationHandler.getInvocations();
		int postInvocations = this.methodAnnotationPostHandler.getInvocations();

		for (int i = 0; i < 3; i++) this.mvc.perform(get("/")).andExpect(status().isOk());

		Assert.assertEquals(classInvocations + 3, this.classAnnotationHandler.getInvocations());
		Assert.assertEquals(methodInvocations + 3, this.methodAnnotationHandler.getInvocations());
		Assert.assertEquals(postInvocations + 3, this.methodAnnotationPostHandler.getInvocations());
	}

	/**
	 * Decision 10: the interceptor lets requests of static resources through
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testStaticResourcePasses() throws Exception
	{
		this.mvc.perform(get("/resources/example.txt")).andExpect(status().isOk()).andExpect(content().string("Hello resource\n"));
	}
}
