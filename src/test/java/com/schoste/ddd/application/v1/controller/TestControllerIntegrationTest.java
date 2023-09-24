package com.schoste.ddd.application.v1.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Integration tests for the {@link com.schoste.ddd.application.v1.interceptor.AnnotationInterceptor}
 * working on the {@link TestController}
 * 
 * @author Philipp Schosteritsch <s.philipp@schoste.com>
 */
@ContextConfiguration(locations = {
		"file:src/test/resources/spring-mvc.xml",
		"file:src/test/resources/unittest-beans-v1.xml",
		})
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class})
@WebAppConfiguration
public class TestControllerIntegrationTest 
{
	@Autowired
	protected WebApplicationContext context;

	protected MockMvc mvc;

	@Before
	public void setUpWebAppContextSetup() throws Exception 
	{
		mvc = webAppContextSetup(context).alwaysDo(print()).build();
	}

	/**
	 * Asserts that
	 * 
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testIndex() throws Exception
	{
		mvc.perform(get("/")).andExpect(status().is(200)).andReturn();
		mvc.perform(get("/")).andExpect(status().is(200)).andReturn();
		mvc.perform(get("/")).andExpect(status().is(200)).andReturn();

		Assert.assertTrue(true);
	}
}
