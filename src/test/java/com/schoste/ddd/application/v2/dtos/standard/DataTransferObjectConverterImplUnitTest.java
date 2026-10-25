package com.schoste.ddd.application.v2.dtos.standard;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.schoste.ddd.application.v2.testfixtures.ExampleRepository;
import com.schoste.ddd.domain.v3.services.UnitOfWork;

/**
 * Unit tests for the version 1 implementation of <@see DataTransferObjectConverter>
 * 
 * @author Philipp Schosteritsch <s.philipp@schoste.com>
 */
@ContextConfiguration(locations = {
		"file:src/test/resources/unittest-beans.xml",
		})
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class})
public class DataTransferObjectConverterImplUnitTest extends DataTransferObjectConverterImplTestSuite
{
	@Autowired
	protected ApplicationContext applicationContext;

	@Autowired
	protected ExampleRepository exampleRepository;

	@Override
	protected UnitOfWork getUnitOfWork() throws Exception
	{
		
		UnitOfWork uow = this.applicationContext.getBean(UnitOfWork.class, this.exampleRepository);

		return uow;
	}

}
