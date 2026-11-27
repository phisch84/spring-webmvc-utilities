package com.schoste.ddd.application.v2.examples;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import com.schoste.ddd.domain.v3.services.UnitOfWork;

/**
 * Runs the examples of the README and the package documentation (see the package documentation of this package):
 * the {@link RequiresRole} example in Spring MVC, and the converter example with the code which saves a customer.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
@ContextConfiguration(locations = { "classpath:spring-mvc.xml", "classpath:unittest-beans.xml", "classpath:examples-beans.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class ExamplesTest
{
	@Autowired
	protected WebApplicationContext context;

	@Autowired
	protected SecuredController securedController;

	@Autowired
	protected RequiresRoleRecordingHandler recordingHandler;

	protected MockMvc mvc;

	// the names of the converter example
	@Autowired
	protected ApplicationContext applicationContext;

	@Autowired
	protected CustomerRepository customerRepository;

	@Autowired
	protected CustomerDTOConverter customerDTOConverter;

	@Autowired
	protected CustomerDAO customerDAO;

	@Before
	public void setUp() throws Exception
	{
		this.mvc = webAppContextSetup(this.context).build();
		this.customerDAO.clear();
		this.customerRepository.reset();
	}

	/**
	 * Creates a GET request of a user with given roles
	 *
	 * @param url the URL
	 * @param roles the roles of the user
	 * @return the request
	 */
	protected MockHttpServletRequestBuilder getAs(String url, String... roles)
	{
		return get(url).with(request ->
		{
			request.addUserRole("guest");
			for (String role : roles) request.addUserRole(role);

			return request;
		});
	}

	/**
	 * The handler of the README rejects a user without the role with 403; the controller isn't called and no further handler
	 * of the annotation is executed
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testRequiresRoleRejectsUserWithoutRole() throws Exception
	{
		int calls = this.securedController.getCalls();
		int recorded = this.recordingHandler.getInvocations();

		this.mvc.perform(this.getAs("/secured/admin", "user")).andExpect(status().isForbidden());

		Assert.assertEquals(calls, this.securedController.getCalls());
		Assert.assertEquals(recorded, this.recordingHandler.getInvocations());
	}

	/**
	 * A user with the role gets through; the further handler of the annotation is executed with the method's annotation
	 * (the class has {@code @RequiresRole("user")}, the method {@code @RequiresRole("admin")})
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testRequiresRoleLetsUserWithRoleThrough() throws Exception
	{
		int calls = this.securedController.getCalls();
		int recorded = this.recordingHandler.getInvocations();

		this.mvc.perform(this.getAs("/secured/admin", "admin")).andExpect(status().isOk()).andExpect(content().string("admin"));

		Assert.assertEquals(calls + 1, this.securedController.getCalls());
		Assert.assertEquals(recorded + 1, this.recordingHandler.getInvocations());
		Assert.assertEquals("admin", this.recordingHandler.getLastAnnotation().value());
	}

	/**
	 * The class annotation applies to methods without an own annotation
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testClassAnnotationAppliesToMethodsWithoutAnnotation() throws Exception
	{
		this.mvc.perform(this.getAs("/secured/home")).andExpect(status().isForbidden());
		this.mvc.perform(this.getAs("/secured/home", "user")).andExpect(status().isOk()).andExpect(content().string("home"));
	}

	/**
	 * The converter example converts a customer DTO of the client, and the code of the package documentation saves it; the
	 * customer gets an actual id, which the server sends back in a DTO of the saved entity
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testConverterExampleSavesCustomer() throws Exception
	{
		CustomerDTO customerDTO = new CustomerDTO();

		customerDTO.setName("Jane");

		// as in the package documentation of com.schoste.ddd.application.v2.dtos.standard
		try (UnitOfWork uow = this.applicationContext.getBean(UnitOfWork.class, this.customerRepository))
		{
			Customer customer = this.customerDTOConverter.toEntity(customerDTO);

			if (this.customerRepository.isVirtualId(customer.getId())) this.customerRepository.add(customer);
			else this.customerRepository.setModified(customer);

			uow.commit();
		}

		Customer saved = (Customer) customerDTO.getEntity();
		CustomerDTO answer = this.customerDTOConverter.toDataTransferObject(saved);

		Assert.assertFalse(this.customerRepository.isVirtualId(saved.getId()));
		Assert.assertEquals("Jane", this.customerDAO.get(saved.getId()).getName());
		Assert.assertEquals(saved.getId(), answer.getId());
		Assert.assertEquals("Jane", answer.getName());
	}
}
